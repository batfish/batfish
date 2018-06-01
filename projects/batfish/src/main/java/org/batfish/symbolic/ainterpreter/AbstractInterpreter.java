package org.batfish.symbolic.ainterpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
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
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.answers.AiReachabilityAnswerElement;
import org.batfish.symbolic.answers.AiRoutesAnswerElement;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDNetConfig;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.collections.Table2;
import org.batfish.symbolic.smt.EdgeType;
import org.batfish.symbolic.utils.Tuple;

// TODO: only recompute reachability for iBGP if something has changed?
// TODO: have separate update and withdraw advertisements?

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class AbstractInterpreter {

  public enum DomainType {
    EXACT,
    REACHABILITY
  }

  private static final DomainType domainType = DomainType.EXACT;

  // The network topology graph
  private Graph _graph;

  // BDDs representing every transfer function in the network
  private BDDNetwork _network;

  // BDD network factory
  private BDDNetFactory _netFactory;

  // A collection of single node BDDs representing the individual routeVariables
  private BDDRoute _variables;

  private DomainType _domainType;

  /*
   * Construct an abstract ainterpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public AbstractInterpreter(IBatfish batfish) {
    _graph = new Graph(batfish);
    NodesSpecifier ns = new NodesSpecifier(".*");
    BDDNetConfig config;
    switch (domainType) {
      case EXACT:
        config = new BDDNetConfig(false);
        break;
      case REACHABILITY:
        config = new BDDNetConfig(true);
        break;
      default:
        throw new BatfishException("Invalid domain type: " + domainType);
    }
    _network = BDDNetwork.create(_graph, ns, config, false);
    _netFactory = _network.getNetFactory();
    _variables = _netFactory.routeVariables();
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
      Table2<String, GraphEdge, T> perNeighbor,
      String router,
      GraphEdge edge,
      T newValue) {

    T existing = perNeighbor.get(router, edge);
    perNeighbor.put(router, edge, newValue);
    if (existing == null) {
      return false;
    }
    T both = domain.merge(existing, newValue);
    return (!both.equals(newValue));
  }

  /*
   * Iteratively computes a fixed point over an abstract domain.
   * Starts with some initial advertisements that are 'originated'
   * by different protocols and maintains an underapproximation of
   * reachable sets at each router for every iteration.
   */
  private <T> Map<String, AbstractRib<T>> computeFixedPoint(IAbstractDomain<T> domain) {
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
    Table2<String, GraphEdge, T> perNeighbor = new Table2<>();

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

    System.out.println("Time for network to BDD conversion: " + (System.currentTimeMillis() - t));

    // Initialize the workset
    Set<String> updateSet = new HashSet<>();
    Queue<String> update = new ArrayDeque<>();
    for (String router : initialRouters) {
      updateSet.add(router);
      update.add(router);
    }

    long totalTimeSelectBest = 0;
    long totalTimeTransfer = 0;
    long tempTime;

    t = System.currentTimeMillis();

    // int i = 0;
    while (!update.isEmpty()) {
      // i++;
      String router = update.poll();
      assert (router != null);
      updateSet.remove(router);
      Configuration conf = _graph.getConfigurations().get(router);

      AbstractRib<T> r = reachable.get(router);
      T routerOspf = r.getOspfRib();
      T routerMainRib = r.getMainRib();

      // System.out.println("");
      // System.out.println("At: " + router);
      // System.out.println("Current RIB: " + domain.debug(routerMainRib));
      // System.out.println("Current OSPF: " + domain.debug(routerOspf));

      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        GraphEdge rev = _graph.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {
          String neighbor = ge.getPeer();
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);

          BDDAcl aclOut = _network.getOutAcls().get(ge);
          BDDAcl aclIn = _network.getOutAcls().get(rev);

          AbstractRib<T> nr = reachable.get(neighbor);
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
          // System.out.println("  neighbor ospf old: " + domain.debug(neighborOspf));

          // Update OSPF
          if (_graph.isEdgeUsed(conf, Protocol.OSPF, ge)
              && _graph.isEdgeUsed(neighborConf, Protocol.OSPF, rev)) {
            T tmpOspf = routerMainRib;
            BDDTransferFunction exportFilter = _network.getExportOspfPolicies().get(ge);
            BDDTransferFunction importFilter = _network.getImportOspfPolicies().get(rev);

            RoutingProtocol ospf = RoutingProtocol.OSPF;
            Integer cost = rev.getStart().getOspfCost();
            Ip nextHop = ge.getStart().getAddress().getIp();

            EdgeTransformer exp =
                new EdgeTransformer(ge, EdgeType.EXPORT, ospf, exportFilter, null, null, aclOut);
            tempTime = System.currentTimeMillis();
            tmpOspf = domain.transform(tmpOspf, exp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            EdgeTransformer imp =
                new EdgeTransformer(rev, EdgeType.IMPORT, ospf, importFilter, cost, nextHop, aclIn);
            tempTime = System.currentTimeMillis();
            tmpOspf = domain.transform(tmpOspf, imp);
            totalTimeTransfer += (System.currentTimeMillis() - tempTime);

            // Update the cost if we keep that around. This is not part of the transfer function
            T fromRouter = routerOspf;
            EdgeTransformer et =
                new EdgeTransformer(rev, EdgeType.IMPORT, ospf, null, cost, nextHop, aclIn);
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
            BDDTransferFunction exportFilter = _network.getExportBgpPolicies().get(ge);
            BDDTransferFunction importFilter = _network.getImportBgpPolicies().get(rev);
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

              doUpdate =
                  domain.reachable(reachable, router, neighbor, flow1)
                      && domain.reachable(reachable, neighbor, router, flow2);

              // System.out.println("    Both are reachable? " + doUpdate);

            } else {
              proto = RoutingProtocol.BGP;
              BgpNeighbor n = _graph.getEbgpNeighbors().get(ge);
              doUpdate = (n != null && !n.getLocalAs().equals(n.getRemoteAs()));
            }

            if (doUpdate) {

              Ip nextHop;
              if (ge.isAbstract()) {
                BgpNeighbor nRouter = _graph.getIbgpNeighbors().get(ge);
                nextHop = nRouter.getLocalIp();
              } else {
                nextHop = ge.getStart().getAddress().getIp();
              }

              EdgeTransformer exp =
                  new EdgeTransformer(ge, EdgeType.EXPORT, proto, exportFilter, 1, nextHop, aclOut);
              tempTime = System.currentTimeMillis();
              tmpBgp = domain.transform(tmpBgp, exp);
              totalTimeTransfer += (System.currentTimeMillis() - tempTime);
              // System.out.println("  tmpBgp after export: " + domain.debug(tmpBgp));

              EdgeTransformer imp =
                  new EdgeTransformer(rev, EdgeType.IMPORT, proto, importFilter, null, null, aclIn);
              tempTime = System.currentTimeMillis();
              tmpBgp = domain.transform(tmpBgp, imp);
              totalTimeTransfer += (System.currentTimeMillis() - tempTime);
              // System.out.println("  tmpBgp after import: " + domain.debug(tmpBgp));

              // Apply BGP aggregates if needed
              List<AggregateTransformer> transformers = new ArrayList<>();
              for (GeneratedRoute gr : neighborConf.getDefaultVrf().getGeneratedRoutes()) {
                String policyName = gr.getGenerationPolicy();
                BDDTransferFunction f = _network.getGeneratedRoutes().get(neighbor, policyName);
                assert (f != null);
                AggregateTransformer at = new AggregateTransformer(f, gr);
                transformers.add(at);
              }
              tmpBgp = domain.aggregate(neighborConf, transformers, tmpBgp);

              boolean anyWithdraw = withdrawn(domain, perNeighbor, neighbor, rev, tmpBgp);
              assert (!anyWithdraw);
              // TODO: if withdraw, recompute rib from all neighbors rather than just this one
              newNeighborBgp = domain.merge(neighborBgp, tmpBgp);

              // tempTime = System.currentTimeMillis();
              // newNeighborBgp = domain.selectBest(newNeighborBgp);
              // totalTimeSelectBest += (System.currentTimeMillis() - tempTime);
            }
          }

          // Update RIB and apply decision process (if any)
          T ndyn = nonDynamic.get(neighbor);
          T newNeighborRib = domain.merge(newNeighborBgp, newNeighborOspf);
          newNeighborRib = domain.merge(newNeighborRib, ndyn);

          tempTime = System.currentTimeMillis();
          newNeighborRib = domain.selectBest(newNeighborRib);
          totalTimeSelectBest += (System.currentTimeMillis() - tempTime);

          // System.out.println("  neighbor RIB now: " + domain.debug(newNeighborRib));

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborMainRib) || !newNeighborOspf.equals(neighborOspf)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(
                    newNeighborBgp, newNeighborOspf, neighborStat, neighborConn, newNeighborRib);
            reachable.put(neighbor, newAbstractRib);
            if (!updateSet.contains(neighbor)) {
              updateSet.add(neighbor);
              update.add(neighbor);
            }
          }
        }
      }
    }

    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));
    System.out.println("Time in select best: " + totalTimeSelectBest);
    System.out.println("Time in transfer: " + totalTimeTransfer);
    return reachable;
  }

  private <T> AnswerElement routes(NodesSpecifier ns, IAbstractDomain<T> domain) {
    Map<String, AbstractRib<T>> reachable = computeFixedPoint(domain);
    SortedSet<Route> routes = new TreeSet<>();
    Set<String> routers = ns.getMatchingNodesByName(_graph.getRouters());

    for (String router : routers) {

      Configuration conf = _graph.getConfigurations().get(router);
      Map<Prefix, String> connPrefixesMap = new HashMap<>();
      Map<Prefix, String> localPrefixesMap = new HashMap<>();

      for (Interface iface : conf.getInterfaces().values()) {
        InterfaceAddress address = iface.getAddress();
        if (address != null) {
          connPrefixesMap.put(address.getPrefix(), iface.getName());
        }
      }
      for (Interface iface : conf.getInterfaces().values()) {
        Ip ip = iface.getAddress().getIp();
        Prefix pfx = new Prefix(ip, 32);
        String x = connPrefixesMap.get(pfx);
        if (x == null || !x.equals(iface.getName())) {
          localPrefixesMap.put(pfx, iface.getName());
        }
      }

      // create interface prefix map
      Map<Ip, String> nhipMap = new HashMap<>();
      Map<Ip, String> nhopMap = new HashMap<>();
      for (GraphEdge edge : _graph.getEdgeMap().get(router)) {
        if (!edge.isAbstract()) {
          Interface iface = edge.getStart();
          Prefix p = iface.getAddress().getPrefix();
          nhipMap.put(p.getStartIp(), iface.getName());
          Interface peerIface = edge.getEnd();
          if (peerIface != null) {
            Ip peerIp = peerIface.getAddress().getIp();
            nhopMap.put(peerIp, edge.getPeer());
          }
        }
      }

      Map<Prefix, String> staticNhintMap = new HashMap<>();
      Map<Prefix, String> staticNhop = new HashMap<>();

      _graph
          .getStaticRoutes()
          .forEach(
              (r, iface, srs) -> {
                for (StaticRoute sr : srs) {
                  staticNhintMap.put(sr.getNetwork(), sr.getNextHopInterface());
                  staticNhop.put(sr.getNetwork(), sr.getNextHop());
                }
              });

      // build new rib to match Batfish output
      AbstractRib<T> rib = reachable.get(router);
      SortedSet<Route> entries = new TreeSet<>(domain.toRoutes(rib));
      for (Route r : entries) {
        Ip nhopIp = (r.getNextHopIp().asLong() == 0 ? new Ip(-1) : r.getNextHopIp());
        String nhint = "dynamic";
        if (r.getProtocol() == RoutingProtocol.LOCAL) {
          nhint = nhipMap.get(r.getNetwork().getStartIp());
        }

        if (r.getProtocol() == RoutingProtocol.CONNECTED) {
          nhint = connPrefixesMap.get(r.getNetwork());
        }
        if (r.getProtocol() == RoutingProtocol.LOCAL) {
          nhint = localPrefixesMap.get(r.getNetwork());
        }
        if (r.getProtocol() == RoutingProtocol.STATIC) {
          nhint = staticNhintMap.get(r.getNetwork());
        }

        String nhop;
        if (r.getProtocol() == RoutingProtocol.CONNECTED
            || r.getProtocol() == RoutingProtocol.LOCAL) {
          nhop = "N/A";
        } else {
          nhop = nhopMap.get(r.getNextHopIp());
        }
        if (r.getProtocol() == RoutingProtocol.STATIC) {
          nhop = staticNhop.get(r.getNetwork());
          nhop = (nhop == null || nhop.equals("(none)") ? "N/A" : nhop);
        }
        long cost = 0;
        if (r.getProtocol() == RoutingProtocol.OSPF) {
          cost = r.getMetric();
        }

        Route route =
            new Route(
                router,
                r.getVrf(),
                r.getNetwork(),
                nhopIp,
                nhop,
                nhint,
                r.getAdministrativeCost(),
                cost,
                r.getProtocol(),
                r.getTag());
        routes.add(route);
      }
    }
    AiRoutesAnswerElement answer = new AiRoutesAnswerElement();
    answer.setRoutes(routes);
    return answer;
  }

  /*
   * Compute an underapproximation of all-pairs reachability
   */
  public AnswerElement routes(NodesSpecifier ns) {
    switch (domainType) {
      case EXACT:
        return routes(ns, new ConcreteDomain(_netFactory));
      case REACHABILITY:
        return routes(ns, new ReachabilityDomain(_netFactory));
      default:
        throw new BatfishException("Invalid domain type: " + domainType);
    }
  }

  private BDD isRouter(BDDFiniteDomain<String> fdom, NodesSpecifier ns) {
    BDD isRouter = _netFactory.zero();
    for (String router : ns.getMatchingNodesByName(_graph.getRouters())) {
      BDD r = fdom.value(router);
      isRouter.orWith(r);
    }
    return isRouter;
  }

  private BDD query(HeaderLocationQuestion question) {
    NodesSpecifier ingress = new NodesSpecifier(question.getIngressNodeRegex());
    NodesSpecifier egress = new NodesSpecifier(question.getFinalNodeRegex());
    BDD headerspace = BDDUtils.headerspaceToBdd(_netFactory, question.getHeaderSpace());
    BDD startRouter = isRouter(_variables.getSrcRouter(), ingress);
    BDD finalRouter = isRouter(_variables.getDstRouter(), egress);
    return startRouter.and(finalRouter).and(headerspace);
  }

  public AnswerElement reachability(HeaderLocationQuestion question) {
    ReachabilityDomain domain = new ReachabilityDomain(_netFactory);
    Map<String, AbstractRib<ReachabilityDomainElement>> reachable = computeFixedPoint(domain);
    long t = System.currentTimeMillis();
    BDD fibs = domain.toFib(reachable);
    System.out.println("Transitive closure: " + (System.currentTimeMillis() - t));
    BDD query = query(question);
    BDD subset = query.and(fibs);
    List<AbstractFlowTrace> traces = new ArrayList<>();
    NodesSpecifier ns = new NodesSpecifier(question.getIngressNodeRegex());
    for (String srcRouter : ns.getMatchingNodesByName(_graph.getRouters())) {
      BDD src = _variables.getSrcRouter().value(srcRouter);
      BDD matchingFromSrc = subset.and(src);
      if (matchingFromSrc.isZero()) {
        continue;
      }
      SatAssignment assignment = BDDUtils.satOne(_netFactory, matchingFromSrc);
      assert (assignment != null);
      Flow flow = assignment.toFlow();
      AbstractFlowTrace trace = new AbstractFlowTrace();
      trace.setIngressRouter(srcRouter);
      trace.setFinalRouter(assignment.getDstRouter());
      trace.setFlow(flow);
      traces.add(trace);
    }
    AiReachabilityAnswerElement answer = new AiReachabilityAnswerElement();
    answer.setAbstractFlowTraces(traces);
    return answer;
  }
}
