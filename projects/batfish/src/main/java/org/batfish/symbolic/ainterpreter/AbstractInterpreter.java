package org.batfish.symbolic.ainterpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.NamedPort;
import org.batfish.datamodel.Prefix;
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
import org.batfish.symbolic.smt.EdgeType;
import org.batfish.symbolic.utils.Tuple;

// TODO: only recompute reachability for iBGP if something has changed
// For example, when processing multiple edges on the same router

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class AbstractInterpreter {

  // The network topology graph
  private Graph _graph;

  // BDDs representing every transfer function in the network
  private BDDNetwork _network;

  // BDD _factory
  private BDDNetFactory _netFactory;

  // Convert acls to a unique identifier
  private FiniteIndexMap<BDDAcl> _aclIndexes;

  // A collection of single node BDDs representing the individual routeVariables
  private BDDRoute _variables;

  /*
   * Construct an abstract ainterpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public AbstractInterpreter(IBatfish batfish) {
    _graph = new Graph(batfish);
    NodesSpecifier ns = new NodesSpecifier(".*");
    BDDNetConfig config = new BDDNetConfig(true);
    _network = BDDNetwork.create(_graph, ns, config, false);
    _netFactory = _network.getNetFactory();
    _variables = _netFactory.routeVariables();
    Set<BDDAcl> acls = new HashSet<>();
    acls.addAll(_network.getInAcls().values());
    acls.addAll(_network.getOutAcls().values());
    _aclIndexes = new FiniteIndexMap<>(acls);
  }

  /*
   * Initialize what prefixes are 'originated' at each router
   * and for each routing protocol. These are used as the
   * starting values for the fixed point computation.
   */
  private void initializeOriginatedPrefixes(
      Map<String, Set<Prefix>> originatedBGP,
      Map<String, Set<Prefix>> originatedOSPF,
      Map<String, Set<Prefix>> originatedConnected,
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
      originatedConnected.put(router, Graph.getOriginatedNetworks(conf, Protocol.CONNECTED));
    }
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
    Map<String, Set<Prefix>> origConn = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> origStatic = new HashMap<>();
    initializeOriginatedPrefixes(origBgp, origOspf, origConn, origStatic);

    Map<String, AbstractRib<T>> reachable = new HashMap<>();
    Set<String> initialRouters = new HashSet<>();

    long t = System.currentTimeMillis();
    for (String router : _graph.getRouters()) {
      Set<Prefix> bgpPrefixes = origBgp.get(router);
      Set<Prefix> ospfPrefixes = origOspf.get(router);
      Set<Prefix> connPrefixes = origConn.get(router);
      Map<String, Set<Prefix>> staticPrefixes = origStatic.get(router);

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
      T ospf = domain.value(router, RoutingProtocol.OSPF, ospfPrefixes);
      T conn = domain.value(router, RoutingProtocol.CONNECTED, connPrefixes);
      T stat = domain.bot();
      if (staticPrefixes != null) {
        for (Entry<String, Set<Prefix>> e : staticPrefixes.entrySet()) {
          String neighbor = e.getKey();
          Set<Prefix> prefixes = e.getValue();
          neighbor = (neighbor == null || neighbor.equals("(none)") ? router : neighbor);
          T statForNeighbor = domain.value(neighbor, RoutingProtocol.STATIC, prefixes);
          stat = domain.merge(stat, statForNeighbor);
        }
      }

      T rib = domain.merge(domain.merge(domain.merge(bgp, ospf), stat), conn);
      AbstractRib<T> abstractRib = new AbstractRib<>(bgp, ospf, stat, conn, rib, new BitSet());
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

    t = System.currentTimeMillis();
    while (!update.isEmpty()) {
      String router = update.poll();
      assert (router != null);
      updateSet.remove(router);
      Configuration conf = _graph.getConfigurations().get(router);

      AbstractRib<T> r = reachable.get(router);
      T routerOspf = r.getOspfRib();
      T routerMainRib = r.getMainRib();
      BitSet routerAclsSoFar = r.getAclIds();

      // System.out.println("");
      // System.out.println("At: " + router);
      // System.out.println("Current RIB: " + domain.debug(routerMainRib));

      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        GraphEdge rev = _graph.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {
          String neighbor = ge.getPeer();
          Configuration neighborConf = _graph.getConfigurations().get(neighbor);

          AbstractRib<T> nr = reachable.get(neighbor);
          T neighborConn = nr.getConnectedRib();
          T neighborStat = nr.getStaticRib();
          T neighborBgp = nr.getBgpRib();
          T neighborOspf = nr.getOspfRib();
          T neighborMainRib = nr.getMainRib();
          BitSet neighborAclsSoFar = nr.getAclIds();

          T newNeighborOspf = neighborOspf;
          T newNeighborBgp = neighborBgp;

          // System.out.println("");
          // System.out.println("  neighbor: " + neighbor);
          // System.out.println("  neighbor RIB: " + domain.debug(neighborMainRib));

          // Update OSPF
          if (_graph.isEdgeUsed(conf, Protocol.OSPF, ge)
              && _graph.isEdgeUsed(neighborConf, Protocol.OSPF, rev)) {
            T tmpOspf = routerMainRib;
            BDDTransferFunction exportFilter = _network.getExportOspfPolicies().get(ge);
            BDDTransferFunction importFilter = _network.getImportOspfPolicies().get(rev);

            if (exportFilter != null) {
              EdgeTransformer exp =
                  new EdgeTransformer(ge, EdgeType.EXPORT, RoutingProtocol.OSPF, exportFilter);
              tmpOspf = domain.transform(tmpOspf, exp);
            }
            if (importFilter != null) {
              EdgeTransformer imp =
                  new EdgeTransformer(ge, EdgeType.IMPORT, RoutingProtocol.OSPF, importFilter);
              tmpOspf = domain.transform(tmpOspf, imp);
            }

            newNeighborOspf = domain.merge(routerOspf, tmpOspf);
            newNeighborOspf = domain.merge(neighborOspf, newNeighborOspf);
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
                  domain.reachable(reachable, _aclIndexes, router, neighbor, flow1)
                      && domain.reachable(reachable, _aclIndexes, neighbor, router, flow2);

              // System.out.println("    Both are reachable? " + doUpdate);

            } else {
              proto = RoutingProtocol.BGP;
              BgpNeighbor n = _graph.getEbgpNeighbors().get(ge);
              doUpdate = (n != null && !n.getLocalAs().equals(n.getRemoteAs()));
            }

            if (doUpdate) {
              if (exportFilter != null) {
                EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, proto, exportFilter);
                tmpBgp = domain.transform(tmpBgp, exp);

                // System.out.println("  tmpBgp after export: " + domain.debug(tmpBgp));
                // System.out.println("  export filter: ");
                // System.out.println(BDDUtils.dot(_netFactory, exp.getBddTransfer().getFilter()));
              }
              if (importFilter != null) {
                EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, proto, importFilter);
                tmpBgp = domain.transform(tmpBgp, imp);

                // System.out.println("  tmpBgp after import: " + domain.debug(tmpBgp));
              }

              newNeighborBgp = domain.merge(neighborBgp, tmpBgp);
            }

            // System.out.println("  now neighbor is: " + domain.debug(newNeighborBgp));
          }

          // Update set of relevant ACLs so far
          BitSet newNeighborAclsSoFar = (BitSet) neighborAclsSoFar.clone();
          newNeighborAclsSoFar.or(routerAclsSoFar);
          BDDAcl out = _network.getOutAcls().get(rev);
          if (out != null) {
            int idx = _aclIndexes.index(out);
            newNeighborAclsSoFar.set(idx);
          }
          BDDAcl in = _network.getInAcls().get(ge);
          if (in != null) {
            int idx = _aclIndexes.index(in);
            newNeighborAclsSoFar.set(idx);
          }

          // Update RIB and apply decision process (if any)
          T newNeighborRib = domain.merge(newNeighborBgp, newNeighborOspf);
          newNeighborRib = domain.merge(newNeighborRib, neighborStat);
          newNeighborRib = domain.merge(newNeighborRib, neighborConn);
          newNeighborRib = domain.selectBest(newNeighborRib);

          // System.out.println("  neighbor RIB now: " + domain.debug(newNeighborRib));

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborMainRib) || !newNeighborOspf.equals(neighborOspf)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(
                    newNeighborBgp,
                    newNeighborOspf,
                    neighborStat,
                    neighborConn,
                    newNeighborRib,
                    newNeighborAclsSoFar);
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
    return reachable;
  }

  /*
   * Compute an underapproximation of all-pairs reachability
   */
  public AnswerElement routes(NodesSpecifier ns) {
    ReachabilityDomain domain = new ReachabilityDomain(_netFactory);
    Map<String, AbstractRib<Tuple<BDD, BDD>>> reachable = computeFixedPoint(domain);
    SortedSet<RibEntry> routes = new TreeSet<>();
    SortedMap<String, SortedSet<RibEntry>> routesByHostname = new TreeMap<>();
    Set<String> routers = ns.getMatchingNodesByName(_graph.getRouters());
    for (String router : routers) {
      AbstractRib<Tuple<BDD, BDD>> rib = reachable.get(router);
      SortedSet<RibEntry> ribEntries = new TreeSet<>(domain.toRoutes(rib));
      routesByHostname.put(router, ribEntries);
      routes.addAll(ribEntries);
    }
    AiRoutesAnswerElement answer = new AiRoutesAnswerElement();
    answer.setRoutes(routes);
    answer.setRoutesByHostname(routesByHostname);
    return answer;
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
    Map<String, AbstractRib<Tuple<BDD, BDD>>> reachable = computeFixedPoint(domain);
    long t = System.currentTimeMillis();
    BDD fibs = domain.toFib(reachable, _aclIndexes);
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
