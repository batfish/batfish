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
import net.sf.javabdd.BDDPairing;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.answers.AiReachabilityAnswerElement;
import org.batfish.symbolic.answers.AiRoutesAnswerElement;
import org.batfish.symbolic.bdd.BDDAcl;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDNetConfig;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDNetFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDNetFactory.SatAssigment;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.smt.EdgeType;

// TODO: Take ACLs into account. Will likely require using a single Factory object

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class ReachabilityInterpreter {

  private static BDDNetFactory netFactory;

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

  // The set of community and protocol BDD routeVariables that we will quantify away
  private BDD _communityAndProtocolBits;

  // A cache of BDDs representing a prefix length exact match
  private Map<Integer, BDD> _lengthCache;

  // A cache of sets of BDDs for a given prefix length to quantify away
  private Map<Integer, BDD> _dstBitsCache;

  // The prefix length bits that we will quantify away
  private BDD _lenBits;

  // The source router bits that we will quantify away
  private BDD _tempRouterBits;

  // Substitution of dst router bits for src router bits used to compute transitive closure
  private BDDPairing _dstToTempRouterSubstitution;

  // Substitution of dst router bits for src router bits used to compute transitive closure
  private BDDPairing _srcToTempRouterSubstitution;

  /*
   * Construct an abstract ainterpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public ReachabilityInterpreter(IBatfish batfish) {

    _graph = new Graph(batfish);
    _lengthCache = new HashMap<>();
    _dstBitsCache = new HashMap<>();
    NodesSpecifier ns = new NodesSpecifier(".*");
    BDDNetConfig config = new BDDNetConfig(true);
    _network = BDDNetwork.create(_graph, ns, config, false);
    _netFactory = _network.getNetFactory();
    _lenBits = _netFactory.one();
    _variables = _netFactory.routeVariables();

    Set<BDDAcl> acls = new HashSet<>();
    acls.addAll(_network.getInAcls().values());
    acls.addAll(_network.getOutAcls().values());
    _aclIndexes = new FiniteIndexMap<>(acls);

    BDD[] srcRouterBits = _variables.getSrcRouter().getInteger().getBitvec();
    BDD[] tempRouterBits = _variables.getRouterTemp().getInteger().getBitvec();
    BDD[] dstRouterBits = _variables.getDstRouter().getInteger().getBitvec();

    _dstToTempRouterSubstitution = _netFactory.makePair();
    _srcToTempRouterSubstitution = _netFactory.makePair();

    _tempRouterBits = _netFactory.one();
    for (int i = 0; i < srcRouterBits.length; i++) {
      _tempRouterBits = _tempRouterBits.and(tempRouterBits[i]);
      _dstToTempRouterSubstitution.set(dstRouterBits[i].var(), tempRouterBits[i].var());
      _srcToTempRouterSubstitution.set(srcRouterBits[i].var(), tempRouterBits[i].var());
    }

    _communityAndProtocolBits = _netFactory.one();
    BDD[] protoHistory = _variables.getProtocolHistory().getInteger().getBitvec();
    for (BDD x : protoHistory) {
      _communityAndProtocolBits = _communityAndProtocolBits.and(x);
    }
    for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
      BDD c = e.getValue();
      _communityAndProtocolBits = _communityAndProtocolBits.and(c);
    }
  }

  /*
   * Initialize what prefixes are 'originated' at each router
   * and for each routing protocol. These are used as the
   * starting values for the fixed point computation.
   */
  private void initializeOriginatedPrefixes(
      Map<String, Set<Prefix>> originatedOSPF,
      Map<String, Set<Prefix>> originatedConnected,
      Map<String, Map<String, Set<Prefix>>> originatedStatic) {

    for (String router : _graph.getRouters()) {
      Configuration conf = _graph.getConfigurations().get(router);
      Vrf vrf = conf.getDefaultVrf();
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
   * Make a BDD representing an exact prefix length match.
   * For efficiency, these results are cached for each prefix length.
   */
  private BDD makeLength(int i) {
    BDD len = _lengthCache.get(i);
    if (len == null) {
      BDDInteger pfxLen = _variables.getPrefixLength();
      BDDInteger newVal = new BDDInteger(pfxLen);
      newVal.setValue(i);
      len = _netFactory.one();
      for (int j = 0; j < pfxLen.getBitvec().length; j++) {
        BDD var = pfxLen.getBitvec()[j];
        BDD val = newVal.getBitvec()[j];
        if (val.isOne()) {
          len = len.and(var);
        } else {
          len = len.andWith(var.not());
        }
      }
      _lengthCache.put(i, len);
    }
    return len;
  }

  /*
   * Compute the set of BDD routeVariables to quantify away for a given prefix length.
   * The boolean indicates whether the router bits should be removed
   * For efficiency, these values are cached.
   */
  private BDD removeBits(int len) {
    BDD removeBits = _dstBitsCache.get(len);
    if (removeBits == null) {
      removeBits = _netFactory.one();
      for (int i = len; i < 32; i++) {
        BDD x = _variables.getPrefix().getBitvec()[i];
        removeBits = removeBits.and(x);
      }
      _dstBitsCache.put(len, removeBits);
    }
    return removeBits;
  }

  /*
   * Iteratively computes a fixed point over an abstract domain.
   * Starts with some initial advertisements that are 'originated'
   * by different protocols and maintains an underapproximation of
   * reachable sets at each router for every iteration.
   */
  private <T> Map<String, AbstractFib<BDD>> computeFixedPoint(IAbstractDomain<T> domain) {

    Map<String, Set<Prefix>> originatedOSPF = new HashMap<>();
    Map<String, Set<Prefix>> originatedConnected = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> originatedStatic = new HashMap<>();
    initializeOriginatedPrefixes(originatedOSPF, originatedConnected, originatedStatic);

    Map<String, AbstractRib<T>> reachable = new HashMap<>();
    Set<String> initialRouters = new HashSet<>();

    long t = System.currentTimeMillis();
    for (String router : _graph.getRouters()) {
      Set<Prefix> ospfPrefixes = originatedOSPF.get(router);
      Set<Prefix> connPrefixes = originatedConnected.get(router);
      Map<String, Set<Prefix>> staticPrefixes = originatedStatic.get(router);

      if (staticPrefixes != null && !staticPrefixes.isEmpty()) {
        initialRouters.add(router);
      }
      if (ospfPrefixes != null && !ospfPrefixes.isEmpty()) {
        initialRouters.add(router);
      }

      T bgp = domain.bot();
      T ospf = domain.value(router, Protocol.OSPF, ospfPrefixes);
      T conn = domain.value(router, Protocol.CONNECTED, connPrefixes);
      T stat = domain.bot();
      if (staticPrefixes != null) {
        for (Entry<String, Set<Prefix>> e : staticPrefixes.entrySet()) {
          String neighbor = e.getKey();
          Set<Prefix> prefixes = e.getValue();
          neighbor = (neighbor == null ? router : neighbor);
          T statForNeighbor = domain.value(neighbor, Protocol.STATIC, prefixes);
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
      updateSet.remove(router);
      Configuration conf = _graph.getConfigurations().get(router);

      AbstractRib<T> r = reachable.get(router);
      T routerOspf = r.getOspfRib();
      T routerRib = r.getMainRib();
      BitSet routerAclsSoFar = r.getAclIds();

      // System.out.println("Looking at router: " + router);
      // System.out.println("RIB is " + "\n" + _variables.dot(domain.toBdd(routerRib)));

      for (GraphEdge ge : _graph.getEdgeMap().get(router)) {
        GraphEdge rev = _graph.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {

          String neighbor = ge.getPeer();

          // System.out.println("  Got neighbor: " + neighbor);

          AbstractRib<T> nr = reachable.get(neighbor);
          T neighborConn = nr.getConnectedRib();
          T neighborStat = nr.getStaticRib();
          T neighborBgp = nr.getBgpRib();
          T neighborOspf = nr.getOspfRib();
          T neighborRib = nr.getMainRib();
          BitSet neighborAclsSoFar = nr.getAclIds();

          T newNeighborOspf = neighborOspf;
          T newNeighborBgp = neighborBgp;

          // Update OSPF
          if (_graph.isEdgeUsed(conf, Protocol.OSPF, ge)) {
            newNeighborOspf = domain.merge(neighborOspf, routerOspf);
          }

          // Update BGP
          if (_graph.isEdgeUsed(conf, Protocol.BGP, ge)) {
            BDDTransferFunction exportFilter = _network.getExportBgpPolicies().get(ge);
            BDDTransferFunction importFilter = _network.getImportBgpPolicies().get(rev);

            T tmpBgp = routerRib;
            if (exportFilter != null) {
              // System.out.println(
              // "  Export filter \n"
              //    + _variables.dot(exportFilter.getFilter()));
              EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, exportFilter);
              tmpBgp = domain.transform(tmpBgp, exp);
            }

            if (importFilter != null) {
              EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, importFilter);
              tmpBgp = domain.transform(tmpBgp, imp);
            }

            // System.out.println(
            //    "  After processing: " + "\n" + _variables.dot(domain.toBdd(tmpBgp)));

            newNeighborBgp = domain.merge(neighborBgp, tmpBgp);
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

          // System.out.println("  New Headerspace: \n" + _variables.dot(newHeaderspace));

          // Update RIB
          T newNeighborRib =
              domain.merge(
                  domain.merge(domain.merge(newNeighborBgp, newNeighborOspf), neighborStat),
                  neighborConn);

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborRib) || !newNeighborOspf.equals(neighborOspf)) {
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

    Map<String, AbstractFib<BDD>> reach = new HashMap<>();
    for (Entry<String, AbstractRib<T>> e : reachable.entrySet()) {
      AbstractRib<T> val = e.getValue();
      BDD bgp = domain.toBdd(val.getMainRib());
      BDD ospf = domain.toBdd(val.getOspfRib());
      BDD conn = domain.toBdd(val.getConnectedRib());
      BDD stat = domain.toBdd(val.getStaticRib());
      BDD rib = domain.toBdd(val.getMainRib());
      AbstractRib<BDD> bddRib = new AbstractRib<>(bgp, ospf, stat, conn, rib, val.getAclIds());
      AbstractFib<BDD> bddFib = new AbstractFib<>(bddRib, toHeaderspace(rib));
      reach.put(e.getKey(), bddFib);
    }

    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));
    return reach;
  }

  /*
   * Convert a RIB represented as a BDD to the actual headerspace that
   * it matches. Normally, the final destination router is preserved
   * in this operation. The removeRouters flag allows the routers to
   * be projected away.
   */
  private BDD toHeaderspace(BDD rib) {
    BDD pfxOnly = rib.exist(_communityAndProtocolBits);
    if (pfxOnly.isZero()) {
      return pfxOnly;
    }
    BDD acc = _netFactory.zero();
    for (int i = 32; i >= 0; i--) {
      // pick out the routes with prefix length i
      BDD len = makeLength(i);
      BDD withLen = pfxOnly.and(len);
      if (withLen.isZero()) {
        continue;
      }
      // quantify out bits i+1 to 32
      BDD removeBits = removeBits(i);
      if (!removeBits.isOne()) {
        withLen = withLen.exist(removeBits);
      }
      // accumulate resulting headers
      acc = acc.orWith(withLen);
    }

    if (_lenBits.isOne()) {
      BDD[] pfxLen = _variables.getPrefixLength().getBitvec();
      for (BDD x : pfxLen) {
        _lenBits = _lenBits.and(x);
      }
    }
    return acc.exist(_lenBits);
  }

  /*
   * Evaluate a particular packet to check reachability to a router
   */
  /* private <T> boolean reachable(
      IAbstractDomain<T> domain, Map<String, AbstractRib<T>> ribs, String router, Ip dstIp) {
    BDD ip =
        BDDUtils.firstBitsEqual(
            BDDNetFactory._factory, _variables.getPrefix().getBitvec(), dstIp, 32);

    AbstractRib<T> rib = ribs.get(router);
    BDD headerspace = toHeaderspace(domain.toBdd(rib.getMainRib()));

    headerspace = headerspace.and(ip);
  } */

  /*
   * Computes the transitive closure of a fib
   */
  private BDD transitiveClosure(BDD fibs) {
    BDD fibsTemp = fibs.replace(_srcToTempRouterSubstitution);
    Set<BDD> seenFibs = new HashSet<>();
    BDD newFib = fibs;
    do {
      seenFibs.add(newFib);
      newFib = newFib.replace(_dstToTempRouterSubstitution);
      newFib = newFib.and(fibsTemp);
      newFib = newFib.exist(_tempRouterBits);
    } while (!seenFibs.contains(newFib));
    return newFib;
  }

  private <T> BDD transitiveClosure(Map<String, AbstractFib<T>> fibs) {
    BDD allFibs = _netFactory.zero();
    for (Entry<String, AbstractFib<T>> e : fibs.entrySet()) {
      String router = e.getKey();
      AbstractFib<T> fib = e.getValue();
      BDD routerBdd = _variables.getSrcRouter().value(router);
      BDD annotatedFib = fib.getHeaderspace().and(routerBdd);
      allFibs = allFibs.orWith(annotatedFib);
    }
    return transitiveClosure(allFibs);
  }

  /*
   * Compute an underapproximation of all-pairs reachability
   */
  public AnswerElement routes(NodesSpecifier ns) {
    ReachabilityDomain domain = new ReachabilityDomain(_netFactory, _communityAndProtocolBits);
    Map<String, AbstractFib<BDD>> reachable = computeFixedPoint(domain);
    SortedSet<RibEntry> routes = new TreeSet<>();
    SortedMap<String, SortedSet<RibEntry>> routesByHostname = new TreeMap<>();
    Set<String> routers = ns.getMatchingNodesByName(_graph.getRouters());
    for (String router : routers) {
      AbstractFib<BDD> fib = reachable.get(router);
      BDD rib = fib.getRib().getMainRib();
      SortedSet<RibEntry> ribEntries = new TreeSet<>();
      List<SatAssigment> entries = _netFactory.allSat(rib);
      for (SatAssigment entry : entries) {
        Prefix p = new Prefix(entry.getDstIp(), entry.getPrefixLen());
        RibEntry r = new RibEntry(router, p, entry.getRoutingProtocol(), entry.getDstRouter());
        ribEntries.add(r);
      }
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

  // TODO: ensure unique reachability (i.e., no other destinations)
  public AnswerElement reachability(HeaderLocationQuestion question) {
    ReachabilityDomain domain = new ReachabilityDomain(_netFactory, _communityAndProtocolBits);
    Map<String, AbstractFib<BDD>> reachable = computeFixedPoint(domain);
    long t = System.currentTimeMillis();
    BDD fibs = transitiveClosure(reachable);
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
      SatAssigment assignment = _netFactory.satOne(subset).get(0);
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
