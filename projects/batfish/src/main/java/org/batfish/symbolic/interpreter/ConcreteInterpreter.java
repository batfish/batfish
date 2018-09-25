package org.batfish.symbolic.interpreter;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
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

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation. However this operates entirely
 * over concrete messages rather than a symbolic approach (e.g., bdds)
 */
public class ConcreteInterpreter {

  private Graph _graph;

  /*
   * Construct an abstract interpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public ConcreteInterpreter(Graph graph) {
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
          Map<String, Set<Prefix>> map =
              originatedStatic.computeIfAbsent(router, k -> new HashMap<>());
          Set<Prefix> pfxs = map.computeIfAbsent(sr.getNextHop(), k -> new HashSet<>());
          pfxs.add(sr.getNetwork());
        }
      }

      // connected
      Set<Tuple<Prefix, String>> conn = new HashSet<>();
      for (Interface iface : conf.getAllInterfaces().values()) {
        InterfaceAddress address = iface.getAddress();
        if (address != null) {
          conn.add(new Tuple<>(address.getPrefix(), iface.getName()));
        }
      }

      originatedConnected.put(router, conn);
    }
  }

  public <T extends AbstractRoute> void computeFixedPoint(
      IConcreteDomain<T> domain, ConcreteState<T> state, Queue<T> pq) {

    long totalTimeSelectBest = 0;
    long totalTimeTransfer = 0;
    long tempTime;
    long t = System.currentTimeMillis();

    int iterations = 0;

    while (!pq.isEmpty()) {
      iterations++;

      T route = pq.poll();
      String router = route.getNode();
      Configuration conf = _graph.getConfigurations().get(router);

      System.out.println("Looking at: " + route + " at " + router);

      ConcreteRib<T> r = state.getPerRouterRoutes().get(router);

      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        System.out.println("  Edge: " + ge);
        GraphEdge rev = _graph.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {
          String neighbor = ge.getPeer();
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);

          ConcreteRib<T> nr = state.getPerRouterRoutes().get(neighbor);
          Map<Prefix, T> neighborConn = nr.getConnectedRib();
          Map<Prefix, T> neighborStat = nr.getStaticRib();
          Map<Prefix, T> neighborBgp = nr.getBgpRib();
          Map<Prefix, T> neighborOspf = nr.getOspfRib();
          Map<Prefix, T> neighborMainRib = nr.getMainRib();

          // Update OSPF
          if (route.getProtocol().equals(RoutingProtocol.OSPF)
              && _graph.isEdgeUsed(conf, Protocol.OSPF, ge)
              && _graph.isEdgeUsed(neighborConf, Protocol.OSPF, rev)) {

            RoutingProtocol ospf = RoutingProtocol.OSPF;
            Integer cost = rev.getStart().getOspfCost();
            Ip nextHop = ge.getStart().getAddress().getIp();

            T transformedRoute;

            EdgeTransformer exp =
                new EdgeTransformer(
                    ge, EdgeType.EXPORT, ospf, null, ge.getStart().getAddress().getIp());
            tempTime = System.currentTimeMillis();
            transformedRoute = domain.transform(route, exp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            if (transformedRoute == null) {
              continue;
            }

            EdgeTransformer imp = new EdgeTransformer(rev, EdgeType.IMPORT, ospf, cost, nextHop);
            tempTime = System.currentTimeMillis();
            transformedRoute = domain.transform(transformedRoute, imp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            if (transformedRoute == null) {
              continue;
            }

            T bestRoute = null;

            Prefix network = transformedRoute.getNetwork();
            T existingRoute = neighborOspf.get(network);
            if (existingRoute == null) {
              neighborOspf.put(network, transformedRoute);
              System.out.println("  added: " + transformedRoute + " at " + neighbor);
              pq.add(transformedRoute);
              bestRoute = transformedRoute;

            } else {
              T merged = domain.merge(existingRoute, transformedRoute);
              if (!merged.equals(existingRoute)) {
                neighborOspf.put(network, merged);
                System.out.println("  added: " + merged + " at " + neighbor);
                pq.add(merged);
                bestRoute = merged;
              }
            }

            // update main rib
            if (bestRoute != null) {
              existingRoute = neighborMainRib.get(network);
              if (existingRoute == null) {
                neighborMainRib.put(network, transformedRoute);
                //System.out.println("  added: " + transformedRoute + " at " + neighbor);
                //pq.add(transformedRoute);
              } else {
                T merged = domain.merge(existingRoute, transformedRoute);
                if (!merged.equals(existingRoute)) {
                  neighborMainRib.put(network, merged);
                  //System.out.println("  added: " + merged + " at " + neighbor);
                  //pq.add(merged);
                }
              }
            }

            totalTimeSelectBest += (System.currentTimeMillis() - tempTime);
          }

          // Update BGP
          /* if (_graph.isEdgeUsed(conf, Protocol.BGP, ge)) {
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

              // TODO: if withdraw, recompute rib from all neighbors rather than just this one
              newNeighborBgp = domain.merge(neighborBgp, tmpBgp);

              // tempTime = System.currentTimeMillis();
              // newNeighborBgp = domain.selectBest(newNeighborBgp);
              // totalTimeSelectBest += (System.currentTimeMillis() - tempTime);
            }
          } */

          // System.out.println("");
          // System.out.println("  neighbor RIB now: " + domain.debug(newNeighborRib));

          // If changed, then add it to the workset

        }
      }
    }

    System.out.println("Number of iterations: " + iterations);
    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));
    System.out.println("Time in select best: " + totalTimeSelectBest);
    System.out.println("Time in transfer: " + totalTimeTransfer);
  }

  private <T extends AbstractRoute> void mergeAll(
      IConcreteDomain<T> domain, Map<Prefix, T> merged, Map<Prefix, T> table) {
    for (Entry<Prefix, T> entry : table.entrySet()) {
      Prefix pfx = entry.getKey();
      T value = entry.getValue();
      T current = merged.get(pfx);
      if (current == null) {
        merged.put(pfx, value);
      } else {
        merged.put(pfx, domain.merge(current, value));
      }
    }
  }

  /*
   * Iteratively computes a fixed point over an abstract domain.
   * Starts with some initial advertisements that are 'originated'
   * by different protocols and maintains an underapproximation of
   * reachable sets at each router for every iteration.
   */
  public <T extends AbstractRoute> ConcreteState<T> computeFixedPoint(IConcreteDomain<T> domain) {
    Map<String, Set<Prefix>> origBgp = new HashMap<>();
    Map<String, Set<Prefix>> origOspf = new HashMap<>();
    Map<String, Set<Tuple<Prefix, String>>> origConn = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> origStatic = new HashMap<>();
    initializeOriginatedPrefixes(origBgp, origOspf, origConn, origStatic);

    // The up-to-date collection of messages from each protocol
    Map<String, ConcreteRib<T>> reachable = new HashMap<>();

    // Per-neighbor rib information
    Table3<String, String, GraphEdge, Map<Prefix, T>> perNeighbor = new Table3<>();

    Queue<T> initialRoutes = new ArrayDeque<>();

    long t = System.currentTimeMillis();
    for (String router : _graph.getRouters()) {
      Configuration conf = _graph.getConfigurations().get(router);
      Set<Prefix> bgpPrefixes = origBgp.get(router);
      Set<Prefix> ospfPrefixes = origOspf.get(router);
      Set<Tuple<Prefix, String>> connPrefixes = origConn.get(router);
      Map<String, Set<Prefix>> staticPrefixes = origStatic.get(router);
      Set<Prefix> localPrefixes = new HashSet<>();

      for (Interface iface : _graph.getConfigurations().get(router).getAllInterfaces().values()) {
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
        for (Prefix pfx : bgpPrefixes) {
          initialRoutes.add(domain.create(router, pfx, RoutingProtocol.BGP, null, 20, 0));
        }
      }
      if (staticPrefixes != null && !staticPrefixes.isEmpty()) {
        for (Set<Prefix> pfxs : staticPrefixes.values()) {
          for (Prefix pfx : pfxs) {
            initialRoutes.add(domain.create(router, pfx, RoutingProtocol.STATIC, null, 1, 0));
          }
        }
      }
      if (ospfPrefixes != null && !ospfPrefixes.isEmpty()) {
        for (Prefix pfx : ospfPrefixes) {
          initialRoutes.add(domain.create(router, pfx, RoutingProtocol.OSPF, null, 110, 0));
        }
      }

      Map<Prefix, T> bgp = new HashMap<>();
      Map<Prefix, T> ospf = domain.value(conf, RoutingProtocol.OSPF, ospfPrefixes);
      Map<Prefix, T> conn = domain.value(conf, RoutingProtocol.CONNECTED, connectedPrefixes);
      Map<Prefix, T> local = domain.value(conf, RoutingProtocol.LOCAL, localPrefixes);
      Map<Prefix, T> stat = new HashMap<>();
      if (staticPrefixes != null) {
        for (Entry<String, Set<Prefix>> e : staticPrefixes.entrySet()) {
          String neighbor = e.getKey();
          Set<Prefix> prefixes = e.getValue();
          neighbor = (neighbor == null || neighbor.equals("(none)") ? router : neighbor);
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);
          Map<Prefix, T> statForNeighbor =
              domain.value(neighborConf, RoutingProtocol.STATIC, prefixes);
          mergeAll(domain, stat, statForNeighbor);
        }
      }

      Map<Prefix, T> mainRib = new HashMap<>();
      mergeAll(domain, mainRib, bgp);
      mergeAll(domain, mainRib, ospf);
      mergeAll(domain, mainRib, stat);
      mergeAll(domain, mainRib, conn);
      ConcreteRib<T> concreteRib = new ConcreteRib<T>(bgp, ospf, stat, conn, mainRib);
      reachable.put(router, concreteRib);
    }

    ConcreteState<T> state = new ConcreteState<T>(reachable, perNeighbor);

    for (T route : initialRoutes) {
      System.out.println("Initial route: " + route);
    }

    computeFixedPoint(domain, state, initialRoutes);
    return state;
  }

  /* private <T> boolean reachable(
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
  } */
}
