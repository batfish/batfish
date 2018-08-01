package org.batfish.symbolic.ainterpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.GeneratedRoute;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.collections.Table3;
import org.batfish.symbolic.smt.EdgeType;
import org.batfish.symbolic.utils.Tuple;

// TODO: only recompute reachability for iBGP if something has changed?
// TODO: have separate update and withdraw advertisements?

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class AbstractInterpreter {

  private Graph _graph;

  /*
   * Construct an abstract ainterpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public AbstractInterpreter(Graph graph) {
    _graph = graph;
  }

  /*
   * Initialize what prefixes are 'originated' at each router
   * and for each routing protocol. These are used as the
   * starting values for the fixed point computation.
   */
  private void initializeOriginatedPrefixes(
      Map<String, Set<Prefix>> originatedBGP,
      Map<String, Set<Prefix>> originatedOSPF,
      Map<String, Set<Tuple<Prefix, String>>> originatedConnected,
      Map<String, Map<String, Set<Prefix>>> originatedStatic) {

    for (String router : _graph.getRouters()) {
      Configuration conf = _graph.getConfigurations().get(router);
      Vrf vrf = conf.getDefaultVrf();
      if (vrf.getBgpProcess() != null) {
        originatedBGP.put(router, Graph.getOriginatedNetworks(conf, Protocol.BGP));
      }
      if (vrf.getOspfProcess() != null) {
        originatedOSPF.put(router, Graph.getOriginatedNetworks(conf, Protocol.OSPF));
      }
      if (vrf.getStaticRoutes() != null) {

        for (StaticRoute sr : conf.getDefaultVrf().getStaticRoutes()) {
          if (sr.getNetwork() != null) {
            Map<String, Set<Prefix>> map =
                originatedStatic.computeIfAbsent(router, k -> new HashMap<>());
            Set<Prefix> pfxs = map.computeIfAbsent(sr.getNextHop(), k -> new HashSet<>());
            pfxs.add(sr.getNetwork());
          }
        }
      }

      // connected
      Set<Tuple<Prefix, String>> conn = new HashSet<>();
      for (Interface iface : conf.getInterfaces().values()) {
        InterfaceAddress address = iface.getAddress();
        if (address != null) {
          conn.add(new Tuple<>(address.getPrefix(), iface.getName()));
        }
      }

      originatedConnected.put(router, conn);
    }
  }

  private <T> boolean withdrawn(
      IAbstractDomain<T> domain,
      Table3<String, String, GraphEdge, T> perNeighbor,
      String router,
      RoutingProtocol proto,
      GraphEdge edge,
      T newValue) {
    String protoName = proto.protocolName();
    T existing = perNeighbor.get(router, protoName, edge);
    perNeighbor.put(router, protoName, edge, newValue);
    if (existing == null) {
      return false;
    }
    T both = domain.merge(existing, newValue);
    return (!both.equals(newValue));
  }

  public <T> void computeFixedPoint(
      IAbstractDomain<T> domain, AbstractState<T> state, Set<String> initialRouters) {

    Set<String> updateSet = new HashSet<>();
    Queue<String> update = new ArrayDeque<>();
    for (String router : initialRouters) {
      updateSet.add(router);
      update.add(router);
    }

    long totalTimeSelectBest = 0;
    long totalTimeTransfer = 0;
    long tempTime;
    long t = System.currentTimeMillis();

    int iterations = 0;

    while (!update.isEmpty()) {
      iterations++;

      String router = update.poll();
      assert (router != null);
      updateSet.remove(router);
      Configuration conf = _graph.getConfigurations().get(router);

      AbstractRib<T> r = state.getPerRouterRoutes().get(router);
      T routerOspf = r.getOspfRib();
      T routerMainRib = r.getMainRib();

      // System.out.println("");
      // System.out.println("At: " + router);
      // System.out.println("Current RIB: " + domain.debug(routerMainRib));

      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        GraphEdge rev = _graph.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {
          String neighbor = ge.getPeer();
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);

          AbstractRib<T> nr = state.getPerRouterRoutes().get(neighbor);
          T neighborConn = nr.getConnectedRib();
          T neighborStat = nr.getStaticRib();
          T neighborBgp = nr.getBgpRib();
          T neighborOspf = nr.getOspfRib();
          T neighborMainRib = nr.getMainRib();

          T newNeighborOspf = neighborOspf;
          T newNeighborBgp = neighborBgp;

          // System.out.println("");
          // System.out.println("  neighbor: " + neighbor);
          // System.out.println("  neighbor RIB old: " + domain.debug(neighborMainRib));

          // Update OSPF
          if (_graph.isEdgeUsed(conf, Protocol.OSPF, ge)
              && _graph.isEdgeUsed(neighborConf, Protocol.OSPF, rev)) {
            T tmpOspf = routerMainRib;

            RoutingProtocol ospf = RoutingProtocol.OSPF;
            Integer cost = rev.getStart().getOspfCost();
            Ip nextHop = ge.getStart().getAddress().getIp();

            EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, ospf, null, null);
            tempTime = System.currentTimeMillis();
            tmpOspf = domain.transform(tmpOspf, exp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            EdgeTransformer imp = new EdgeTransformer(rev, EdgeType.IMPORT, ospf, cost, nextHop);
            tempTime = System.currentTimeMillis();
            tmpOspf = domain.transform(tmpOspf, imp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            // Update the cost if we keep that around. This is not part of the transfer function
            T fromRouter = routerOspf;
            EdgeTransformer et = new EdgeTransformer(rev, EdgeType.IMPORT, ospf, cost, nextHop);
            tempTime = System.currentTimeMillis();
            tmpOspf = domain.transform(tmpOspf, et);
            fromRouter = domain.transform(fromRouter, et);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            newNeighborOspf = domain.merge(fromRouter, tmpOspf);
            newNeighborOspf = domain.merge(neighborOspf, newNeighborOspf);

            tempTime = System.currentTimeMillis();
            newNeighborOspf = domain.selectBest(newNeighborOspf);
            totalTimeSelectBest += (System.currentTimeMillis() - tempTime);
          }

          // Update BGP
          if (_graph.isEdgeUsed(conf, Protocol.BGP, ge)) {
            T tmpBgp = routerMainRib;
            RoutingProtocol proto;
            boolean doUpdate;

            // Update iBGP
            if (ge.isAbstract()) {
              proto = RoutingProtocol.IBGP;
              BgpNeighbor nRouter = _graph.getIbgpNeighbors().get(ge);
              BgpNeighbor nNeighbor = _graph.getIbgpNeighbors().get(rev);
              Ip loopbackRouter = nRouter.getLocalIp();
              Ip loopbackNeighbor = nNeighbor.getLocalIp();

              // System.out.println("    loopback for router: " + loopbackRouter);
              // System.out.println("    loopback for neighbor: " + loopbackNeighbor);

              // Make the first flow
              Flow.Builder fb = new Flow.Builder();
              fb.setIpProtocol(IpProtocol.TCP);
              fb.setTag("neighbor-resolution");
              fb.setIngressNode(router);
              fb.setDstIp(loopbackNeighbor);
              fb.setSrcIp(loopbackRouter);
              fb.setSrcPort(NamedPort.EPHEMERAL_LOWEST.number());
              fb.setDstPort(NamedPort.BGP.number());
              Flow flow1 = fb.build();

              // Make the second flow
              fb = new Flow.Builder();
              fb.setIpProtocol(IpProtocol.TCP);
              fb.setTag("neighbor-resolution");
              fb.setIngressNode(neighbor);
              fb.setDstIp(loopbackRouter);
              fb.setSrcIp(loopbackNeighbor);
              fb.setSrcPort(NamedPort.BGP.number());
              fb.setDstPort(NamedPort.EPHEMERAL_LOWEST.number());
              Flow flow2 = fb.build();

              // TODO: a bit inefficient
              Map<String, T> reach = new HashMap<>();
              for (Entry<String, AbstractRib<T>> e : state.getPerRouterRoutes().entrySet()) {
                reach.put(e.getKey(), e.getValue().getMainRib());
              }

              doUpdate =
                  reachable(domain, reach, router, neighbor, flow1)
                      && reachable(domain, reach, neighbor, router, flow2);

              // System.out.println("    Both are reachable? " + doUpdate);

            } else {
              proto = RoutingProtocol.BGP;
              BgpNeighbor n = _graph.getEbgpNeighbors().get(ge);
              doUpdate = (n != null && !Objects.equals(n.getLocalAs(), n.getRemoteAs()));
            }

            if (doUpdate) {

              Ip nextHop;
              if (ge.isAbstract()) {
                BgpNeighbor nRouter = _graph.getIbgpNeighbors().get(ge);
                nextHop = nRouter.getLocalIp();
              } else {
                nextHop = ge.getStart().getAddress().getIp();
              }

              EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, proto, 1, nextHop);
              tempTime = System.currentTimeMillis();
              tmpBgp = domain.transform(tmpBgp, exp);
              totalTimeTransfer += (System.currentTimeMillis() - tempTime);
              // System.out.println("  tmpBgp after export: " + domain.debug(tmpBgp));

              EdgeTransformer imp = new EdgeTransformer(rev, EdgeType.IMPORT, proto, null, null);
              tempTime = System.currentTimeMillis();
              tmpBgp = domain.transform(tmpBgp, imp);
              totalTimeTransfer += (System.currentTimeMillis() - tempTime);
              // System.out.println("  tmpBgp after import: " + domain.debug(tmpBgp));

              // Apply BGP aggregates if needed
              List<AggregateTransformer> transformers = new ArrayList<>();
              for (GeneratedRoute gr : neighborConf.getDefaultVrf().getGeneratedRoutes()) {
                String policyName = gr.getGenerationPolicy();
                AggregateTransformer at = new AggregateTransformer(neighbor, policyName, gr);
                transformers.add(at);
              }
              tmpBgp = domain.aggregate(neighborConf, transformers, tmpBgp);

              /* boolean anyWithdraw =
                  withdrawn(
                      domain,
                      state.getPerNeighborRoutes(),
                      neighbor,
                      RoutingProtocol.BGP,
                      rev,
                      tmpBgp);
              assert (!anyWithdraw); */
              // TODO: if withdraw, recompute rib from all neighbors rather than just this one
              newNeighborBgp = domain.merge(neighborBgp, tmpBgp);

              // tempTime = System.currentTimeMillis();
              // newNeighborBgp = domain.selectBest(newNeighborBgp);
              // totalTimeSelectBest += (System.currentTimeMillis() - tempTime);
            }
          }

          // Update RIB and apply decision process (if any)
          T ndyn = state.getNonDynamicRoutes().get(neighbor);
          T newNeighborRib = domain.merge(newNeighborBgp, newNeighborOspf);
          newNeighborRib = domain.merge(newNeighborRib, ndyn);

          tempTime = System.currentTimeMillis();
          newNeighborRib = domain.selectBest(newNeighborRib);
          totalTimeSelectBest += (System.currentTimeMillis() - tempTime);

          // System.out.println("");
          // System.out.println("  neighbor RIB now: " + domain.debug(newNeighborRib));

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborMainRib) || !newNeighborOspf.equals(neighborOspf)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(
                    newNeighborBgp, newNeighborOspf, neighborStat, neighborConn, newNeighborRib);
            state.getPerRouterRoutes().put(neighbor, newAbstractRib);
            if (!updateSet.contains(neighbor)) {
              updateSet.add(neighbor);
              update.add(neighbor);
            }
          }
        }
      }
    }

    System.out.println("Number of iterations: " + iterations);
    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));
    System.out.println("Time in select best: " + totalTimeSelectBest);
    System.out.println("Time in transfer: " + totalTimeTransfer);
  }

  /*
   * Iteratively computes a fixed point over an abstract domain.
   * Starts with some initial advertisements that are 'originated'
   * by different protocols and maintains an underapproximation of
   * reachable sets at each router for every iteration.
   */
  public <T> AbstractState<T> computeFixedPoint(IAbstractDomain<T> domain) {
    Map<String, Set<Prefix>> origBgp = new HashMap<>();
    Map<String, Set<Prefix>> origOspf = new HashMap<>();
    Map<String, Set<Tuple<Prefix, String>>> origConn = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> origStatic = new HashMap<>();
    initializeOriginatedPrefixes(origBgp, origOspf, origConn, origStatic);

    // The up-to-date collection of messages from each protocol
    Map<String, AbstractRib<T>> reachable = new HashMap<>();

    // Cache of all messages that can't change, so we don't have to recompute
    Map<String, T> nonDynamic = new HashMap<>();

    // Per-neighbor rib information
    Table3<String, String, GraphEdge, T> perNeighbor = new Table3<>();

    Set<String> initialRouters = new HashSet<>();
    long t = System.currentTimeMillis();
    for (String router : _graph.getRouters()) {
      Configuration conf = _graph.getConfigurations().get(router);
      Set<Prefix> bgpPrefixes = origBgp.get(router);
      Set<Prefix> ospfPrefixes = origOspf.get(router);
      Set<Tuple<Prefix, String>> connPrefixes = origConn.get(router);
      Map<String, Set<Prefix>> staticPrefixes = origStatic.get(router);
      Set<Prefix> localPrefixes = new HashSet<>();

      for (Interface iface : _graph.getConfigurations().get(router).getInterfaces().values()) {
        Ip ip = iface.getAddress().getIp();
        Prefix pfx = new Prefix(ip, 32);
        Tuple<Prefix, String> tup = new Tuple<>(pfx, iface.getName());
        if (!connPrefixes.contains(tup)) {
          localPrefixes.add(pfx);
        }
      }

      Set<Prefix> connectedPrefixes = new HashSet<>();
      for (Tuple<Prefix, String> tup : connPrefixes) {
        connectedPrefixes.add(tup.getFirst());
      }

      if (bgpPrefixes != null && !bgpPrefixes.isEmpty()) {
        initialRouters.add(router);
      }
      if (staticPrefixes != null && !staticPrefixes.isEmpty()) {
        initialRouters.add(router);
      }
      if (ospfPrefixes != null && !ospfPrefixes.isEmpty()) {
        initialRouters.add(router);
      }

      T bgp = domain.bot();
      T ospf = domain.selectBest(domain.value(conf, RoutingProtocol.OSPF, ospfPrefixes));
      T conn = domain.selectBest(domain.value(conf, RoutingProtocol.CONNECTED, connectedPrefixes));
      T local = domain.selectBest(domain.value(conf, RoutingProtocol.LOCAL, localPrefixes));
      T stat = domain.bot();
      if (staticPrefixes != null) {
        for (Entry<String, Set<Prefix>> e : staticPrefixes.entrySet()) {
          String neighbor = e.getKey();
          Set<Prefix> prefixes = e.getValue();
          neighbor = (neighbor == null || neighbor.equals("(none)") ? router : neighbor);
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);
          T statForNeighbor = domain.value(neighborConf, RoutingProtocol.STATIC, prefixes);
          stat = domain.merge(stat, statForNeighbor);
        }
      }

      T ndyn = domain.merge(domain.merge(stat, conn), local);
      nonDynamic.put(router, ndyn);

      T rib = domain.selectBest(domain.merge(domain.merge(bgp, ospf), ndyn));
      AbstractRib<T> abstractRib = new AbstractRib<>(bgp, ospf, stat, conn, rib);
      reachable.put(router, abstractRib);
    }

    AbstractState<T> state = new AbstractState<>(reachable, perNeighbor, nonDynamic);

    System.out.println("Time for network to BDD conversion: " + (System.currentTimeMillis() - t));
    computeFixedPoint(domain, state, initialRouters);
    return state;
  }

  private <T> boolean reachable(
      IAbstractDomain<T> domain, Map<String, T> ribs, String src, String dst, Flow flow) {
    String current = src;
    while (true) {
      if (current == null) {
        return false;
      }
      if (current.equals(dst)) {
        return true;
      }
      T rib = ribs.get(current);
      current = domain.nextHop(rib, current, flow);
    }
  }
}
