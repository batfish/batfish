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

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class ReachabilityInterpreter {

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

  // The destination router bits that we will quantify away
  private BDD _dstRouterBits;

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

    _dstRouterBits = _netFactory.one();
    for (BDD x : dstRouterBits) {
      _dstRouterBits = _dstRouterBits.and(x);
    }

    _lenBits = _netFactory.one();
    BDD[] pfxLen = _variables.getPrefixLength().getBitvec();
    for (BDD x : pfxLen) {
      _lenBits = _lenBits.and(x);
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
      AbstractRib<T> abstractRib =
          new AbstractRib<>(bgp, bgp, ospf, ospf, stat, conn, rib, rib, new BitSet());
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
      T underRouterOspf = r.getUnderOspfRib();
      T overRouterOspf = r.getOverOspfRib();
      T underRouterRib = r.getUnderMainRib();
      T overRouterRib = r.getOverMainRib();
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
          T underNeighborBgp = nr.getUnderBgpRib();
          T overNeighborBgp = nr.getOverBgpRib();
          T underNeighborOspf = nr.getUnderOspfRib();
          T overNeighborOspf = nr.getOverOspfRib();
          T underNeighborRib = nr.getUnderMainRib();
          T overNeighborRib = nr.getOverMainRib();
          BitSet neighborAclsSoFar = nr.getAclIds();

          T newUnderNeighborOspf = underNeighborOspf;
          T newOverNeighborOspf = overNeighborOspf;
          T newUnderNeighborBgp = underNeighborBgp;
          T newOverNeighborBgp = overNeighborBgp;

          // Update OSPF
          if (_graph.isEdgeUsed(conf, Protocol.OSPF, ge)) {
            newUnderNeighborOspf = domain.merge(underNeighborOspf, underRouterOspf);
            newOverNeighborOspf = domain.merge(overNeighborOspf, overRouterOspf);
          }

          // Update BGP
          if (_graph.isEdgeUsed(conf, Protocol.BGP, ge)) {
            BDDTransferFunction exportFilter = _network.getExportBgpPolicies().get(ge);
            BDDTransferFunction importFilter = _network.getImportBgpPolicies().get(rev);

            T underTmpBgp = underRouterRib;
            T overTmpBgp = overRouterRib;

            if (exportFilter != null) {
              EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, exportFilter);
              underTmpBgp = domain.transform(underTmpBgp, exp, Transformation.UNDER_APPROXIMATION);
              overTmpBgp = domain.transform(overTmpBgp, exp, Transformation.OVER_APPROXIMATION);

              if (!underTmpBgp.equals(overTmpBgp)) {
                System.out.println("FAILED!!!");
              }
            }

            if (importFilter != null) {
              EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, importFilter);
              underTmpBgp = domain.transform(underTmpBgp, imp, Transformation.UNDER_APPROXIMATION);
              overTmpBgp = domain.transform(overTmpBgp, imp, Transformation.OVER_APPROXIMATION);
            }

            // System.out.println(
            //    "  After processing: " + "\n" + _variables.dot(domain.toBdd(tmpBgp)));

            newUnderNeighborBgp = domain.merge(underNeighborBgp, underTmpBgp);
            newOverNeighborBgp = domain.merge(overNeighborBgp, overTmpBgp);
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
          T newUnderNeighborRib =
              domain.merge(
                  domain.merge(
                      domain.merge(newUnderNeighborBgp, newUnderNeighborOspf), neighborStat),
                  neighborConn);

          T newOverNeighborRib =
              domain.merge(
                  domain.merge(domain.merge(newOverNeighborBgp, newOverNeighborOspf), neighborStat),
                  neighborConn);

          // If changed, then add it to the workset
          if (!newUnderNeighborRib.equals(underNeighborRib)
              || !newOverNeighborRib.equals(overNeighborRib)
              || !newUnderNeighborOspf.equals(underNeighborOspf)
              || !newOverNeighborOspf.equals(overNeighborOspf)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(
                    newUnderNeighborBgp,
                    newOverNeighborBgp,
                    newUnderNeighborOspf,
                    newOverNeighborOspf,
                    neighborStat,
                    neighborConn,
                    newUnderNeighborRib,
                    newOverNeighborRib,
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
      BDD underBgp = domain.toBdd(val.getUnderBgpRib());
      BDD overBgp = domain.toBdd(val.getOverBgpRib());
      BDD underOspf = domain.toBdd(val.getUnderOspfRib());
      BDD overOspf = domain.toBdd(val.getOverOspfRib());
      BDD conn = domain.toBdd(val.getConnectedRib());
      BDD stat = domain.toBdd(val.getStaticRib());
      BDD underRib = domain.toBdd(val.getUnderMainRib());
      BDD overRib = domain.toBdd(val.getOverMainRib());
      AbstractRib<BDD> bddRib =
          new AbstractRib<>(
              underBgp,
              overBgp,
              underOspf,
              overOspf,
              stat,
              conn,
              underRib,
              overRib,
              val.getAclIds());
      BDD headerspace = toHeaderspace(underRib, overRib);
      BDD notBlockedByAcl = notBlockedByAcl(val.getAclIds());
      headerspace = headerspace.andWith(notBlockedByAcl);

      AbstractFib<BDD> bddFib = new AbstractFib<>(bddRib, headerspace);
      reach.put(e.getKey(), bddFib);
    }

    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));
    return reach;
  }

  /*
   * BDD representing those packets not blocked by an ACL
   */
  private BDD notBlockedByAcl(BitSet aclIds) {
    BDD blocked = _netFactory.zero();
    for (int i = aclIds.nextSetBit(0); i != -1; i = aclIds.nextSetBit(i + 1)) {
      BDDAcl acl = _aclIndexes.value(i);
      blocked = blocked.or(acl.getBdd());
    }
    return blocked.not();
  }

  /*
   * Convert a pair of an underapproximation of the RIB and an overapproximation
   * of the RIB represented as a BDDs to an actual headerspace of packets that can
   * reach a destination. Normally, the final destination router is preserved
   * in this operation.
   *
   * As an example, if we have 1.2.3.0/24 learned from router X in the RIB, and
   * there can not be any more specific routes (in the overapproximation of routes)
   * that can take traffic away from this prefix, then the resulting packets matched
   * are going to be 1.2.3.*
   *
   * In general, we want to remove destinations in the overapproximated set that
   * are not in the underapproximated set. For instance, if we have prefix
   * 1.2.3.4/32 --> A in the underapproximation, and prefix
   * 1.2.3.4/32 --> {A,B} in the overapproximation, then we want to remove
   * this prefix since we can't be guaranteed that it will forward to A.
   *
   * As another example, if we have the prefix
   * 1.2.3.0/24 --> A in the underapproximated set, and another prefix
   * 1.2.3.4/32 --> B in the overapproximated set, then we only want the
   * destinations matched by the /24 but not the /32.
   *
   */
  private BDD toHeaderspace(BDD underRib, BDD overRib) {
    // Get the prefixes only by removing communities, protocol tag etc.
    BDD underPfxOnly = underRib.exist(_communityAndProtocolBits);
    BDD overPfxOnly = overRib.exist(_communityAndProtocolBits);

    // Early return if there is nothing in the rib
    if (underPfxOnly.isZero()) {
      return underPfxOnly;
    }

    // Prefixes that might take traffic away
    BDD allOverPrefixes = overPfxOnly.andWith(underPfxOnly.not()).exist(_dstRouterBits);

    // Accumulate the might hijack destination addresses by prefix length
    BDD mightHijack = _netFactory.zero();

    // Accumulate the reachable packets (prefix length is removed at the end)
    BDD reachablePackets = _netFactory.zero();

    // Walk starting from most specific prefix length
    for (int i = 32; i >= 0; i--) {
      BDD len = makeLength(i);

      // Add new prefixes that might hijack traffic
      BDD withLenOver = allOverPrefixes.and(len).exist(_lenBits);
      mightHijack = mightHijack.orWith(withLenOver);

      // Get the must reach prefixes for this length
      BDD withLen = underPfxOnly.and(len);
      if (withLen.isZero()) {
        continue;
      }

      // quantify out bits i+1 to 32
      BDD removeBits = removeBits(i);
      if (!removeBits.isOne()) {
        withLen = withLen.exist(removeBits);
      }

      // Remove the may hijack addresses
      withLen = withLen.andWith(mightHijack.not());

      // accumulate resulting headers
      reachablePackets = reachablePackets.orWith(withLen);
    }

    return reachablePackets.exist(_lenBits);
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
    BDD headerspace = toHeaderspace(domain.toBdd(rib.getUnderMainRib()));

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
      BDD rib = fib.getRib().getUnderMainRib();
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
      SatAssigment assignment = _netFactory.satOne(matchingFromSrc).get(0);
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
