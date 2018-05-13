package org.batfish.symbolic.interpreter;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDInteger;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRouteConfig;
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDRouteFactory.BDDRoute;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.collections.Table2;
import org.batfish.symbolic.smt.EdgeType;

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class AbstractInterpreter {

  private IBatfish _batfish;

  private HeaderLocationQuestion _question;

  private BDDPairing _pairing;

  private BDDRoute _variables;

  private Set<BDD> _communityAndProtocolBits;

  private Map<Integer, BDD> _lengthCache;

  private Table2<Boolean, Integer, Set<BDD>> _dstBitsCache;

  private Set<BDD> _lenBits;

  public AbstractInterpreter(IBatfish batfish, HeaderLocationQuestion q) {
    _batfish = batfish;
    _question = q;
    _lengthCache = new HashMap<>();
    _dstBitsCache = new Table2<>();
    _lenBits = new HashSet<>();
  }

  /*
   * Initialize what prefixes are 'originated' at each router
   * and for each routing protocol.
   */
  private void initializeOriginatedPrefixes(
      Graph g,
      Map<String, Set<Prefix>> originatedOSPF,
      Map<String, Set<Prefix>> originatedConnected,
      Map<String, Map<String, Set<Prefix>>> originatedStatic) {
    // create the initial route message sets
    for (String router : g.getRouters()) {
      Configuration conf = g.getConfigurations().get(router);
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

  private BDD makeLength(int i) {
    BDD len = _lengthCache.get(i);
    if (len == null) {
      BDDInteger pfxLen = _variables.getPrefixLength();
      BDDInteger newVal = new BDDInteger(pfxLen);
      newVal.setValue(i);
      len = BDDRouteFactory.factory.zero();
      for (BDD x : newVal.getBitvec()) {
        len = len.and(x);
      }
      _lengthCache.put(i, len);
    }
    return len;
  }

  private Set<BDD> dstBits(boolean b, int i) {
    Set<BDD> dstBits = _dstBitsCache.get(b, i);
    if (dstBits == null) {
      dstBits = new HashSet<>(Arrays.asList(_variables.getPrefix().getBitvec()).subList(i, 32));
      _dstBitsCache.put(b, i, dstBits);
    }
    return dstBits;
  }

  private BDD toHeaderspace(BDD rib, boolean removeRouters) {
    BDD pfxOnly = BDDUtils.exists(rib, _communityAndProtocolBits);
    if (pfxOnly.isZero()) {
      return pfxOnly;
    }

    BDD acc = BDDRouteFactory.factory.zero();
    for (int i = 0; i <= 32; i++) {
      // _pairing.reset();
      BDD len = makeLength(i);
      /* restrict prefix length
      for (int j = 0; j < pfxLen.getBitvec().length; j++) {
        BDD var = pfxLen.getBitvec()[j];
        BDD val = newVal.getBitvec()[j];
        _pairing.set(var.var(), val);
      } */
      // quantify out prefix bits
      Set<BDD> dstBits = dstBits(removeRouters, i);
      BDD withLen = pfxOnly.and(len);

      if (withLen.isZero()) {
        continue;
      }

      withLen = BDDUtils.exists(withLen, dstBits);
      acc = acc.or(withLen);
    }

    if (_lenBits.isEmpty()) {
      Collections.addAll(_lenBits, _variables.getPrefixLength().getBitvec());
    }
    return BDDUtils.exists(acc, _lenBits);
  }

  /*
   * Iteratively compute reachable sets until a fixed point is reached
   */
  private <T> Map<String, BDD> computeFixedPoint(
      Graph g, BDDNetwork network, BDDRouteFactory factory, IAbstractDomain<T> domain) {

    Map<String, Set<Prefix>> originatedOSPF = new HashMap<>();
    Map<String, Set<Prefix>> originatedConnected = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> originatedStatic = new HashMap<>();
    initializeOriginatedPrefixes(g, originatedOSPF, originatedConnected, originatedStatic);

    Map<String, AbstractRib<T>> reachable = new HashMap<>();
    Set<String> initialRouters = new HashSet<>();

    long t = System.currentTimeMillis();
    // initialize for BGP by converting prefixes to BDDs

    for (String router : g.getRouters()) {
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
          neighbor = (neighbor == null ? "null" : neighbor);
          T statForNeighbor = domain.value(neighbor, Protocol.STATIC, prefixes);
          stat = domain.merge(stat, statForNeighbor);
        }
      }
      T rib = domain.merge(domain.merge(domain.merge(bgp, ospf), stat), conn);
      BDD headerspace = toHeaderspace(domain.toBdd(rib), false);

      AbstractRib<T> abstractRib = new AbstractRib<>(bgp, ospf, stat, conn, rib, headerspace);
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
      Configuration conf = g.getConfigurations().get(router);

      AbstractRib<T> r = reachable.get(router);
      T routerOspf = r.getOspfRib();
      T routerRib = r.getRibEntry();
      BDD routerHeaderspace = r.getHeaderspace();

      // System.out.println("Looking at router: " + router);
      // System.out.println("RIB is " + "\n" + factory.variables().dot(domain.toBdd(routerRib)));

      for (GraphEdge ge : g.getEdgeMap().get(router)) {
        GraphEdge rev = g.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {

          String neighbor = ge.getPeer();

          AbstractRib<T> nr = reachable.get(neighbor);
          T neighborConn = nr.getConnectedRib();
          T neighborStat = nr.getStaticRib();
          T neighborBgp = nr.getBgpRib();
          T neighborOspf = nr.getOspfRib();
          T neighborRib = nr.getRibEntry();
          BDD neighborHeaderspace = nr.getHeaderspace();

          // System.out.println("  Got neighbor: " + neighbor);

          T newNeighborOspf = neighborOspf;
          T newNeighborBgp = neighborBgp;

          BDD transferedHeaderspace = BDDRouteFactory.factory.zero();

          // Update static
          List<StaticRoute> srs = g.getStaticRoutes().get(neighbor, rev.getStart().getName());
          if (srs != null) {
            Set<Prefix> pfxs = new HashSet<>();
            for (StaticRoute sr : srs) {
              pfxs.add(sr.getNetwork());
            }
            T stat = domain.value(neighbor, Protocol.STATIC, pfxs);
            BDD h = toHeaderspace(domain.toBdd(stat), true);
            transferedHeaderspace = transferedHeaderspace.or(h);
          }

          // Update OSPF
          if (g.isEdgeUsed(conf, Protocol.OSPF, ge)) {
            newNeighborOspf = domain.merge(neighborOspf, routerOspf);
            BDD h = toHeaderspace(domain.toBdd(routerOspf), false);
            transferedHeaderspace = transferedHeaderspace.or(h);
          }

          // Update BGP
          if (g.isEdgeUsed(conf, Protocol.BGP, ge)) {
            BDDTransferFunction exportFilter = network.getExportBgpPolicies().get(ge);
            BDDTransferFunction importFilter = network.getImportBgpPolicies().get(rev);

            T tmpBgp = routerRib;
            if (exportFilter != null) {
              /* System.out.println(
              "  Export filter for "
                  + "\n"
                  + factory.variables().dot(exportFilter.getFilter())); */
              EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, exportFilter);
              tmpBgp = domain.transform(tmpBgp, exp);
            }

            if (importFilter != null) {
              EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, importFilter);
              tmpBgp = domain.transform(tmpBgp, imp);
            }

            // System.out.println(
            //    "  After processing: " + "\n" + factory.variables().dot(domain.toBdd(tmpBgp)));

            newNeighborBgp = domain.merge(neighborBgp, tmpBgp);

            BDD h = toHeaderspace(domain.toBdd(tmpBgp), false);
            transferedHeaderspace = transferedHeaderspace.or(h);
          }

          // TODO: conjoin with acls
          // TODO: need to replace destination IP with prefix bits in ACLs
          // TODO: only restrict to prefix lengths that appear in the configuration?
          /* BDDAcl out = network.getOutAcls().get(rev);
          BDDAcl in = network.getInAcls().get(ge);
          if (out != null) {
            out.
          } */

          transferedHeaderspace = transferedHeaderspace.and(routerHeaderspace);
          BDD newHeaderspace = neighborHeaderspace.or(transferedHeaderspace);

          // System.out.println("  New Headerspace: \n" + factory.variables().dot(newHeaderspace));

          // Update RIB
          T newNeighborRib =
              domain.merge(
                  domain.merge(domain.merge(newNeighborBgp, newNeighborOspf), neighborStat),
                  neighborConn);

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborRib)
              || !newNeighborOspf.equals(neighborOspf)
              || !newHeaderspace.equals(neighborHeaderspace)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(
                    newNeighborBgp,
                    newNeighborOspf,
                    neighborStat,
                    neighborConn,
                    newNeighborRib,
                    newHeaderspace);
            reachable.put(neighbor, newAbstractRib);
            if (!updateSet.contains(neighbor)) {
              updateSet.add(neighbor);
              update.add(neighbor);
            }
          }
        }
      }

      // System.out.println("Now for router: " + router);
      // System.out.println(_variables.dot(rprime));
      // System.out.println("\n\n");
    }
    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));

    Map<String, BDD> reach = new HashMap<>();
    for (Entry<String, AbstractRib<T>> e : reachable.entrySet()) {
      BDD val = domain.toBdd(e.getValue().getRibEntry());
      reach.put(e.getKey(), val);
      // System.out.println("Final router: " + e.getKey());
      // System.out.println("" + factory.variables().dot(val));
    }
    return reach;
  }

  /*
   * Variables that will be existentially quantified away
   */
  private void initializeQuantificationVariables() {
    _communityAndProtocolBits = new HashSet<>();
    for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
      BDD c = e.getValue();
      _communityAndProtocolBits.add(c);
    }
    BDD[] protoHistory = _variables.getProtocolHistory().getInteger().getBitvec();
    _communityAndProtocolBits.addAll(Arrays.asList(protoHistory));
  }

  /*
   * Compute an underapproximation of all-pairs reachability
   */
  public AnswerElement interpret() {
    Graph g = new Graph(_batfish);
    NodesSpecifier ns = new NodesSpecifier(_question.getIngressNodeRegex());
    BDDRouteConfig config = new BDDRouteConfig(true);

    BDDRouteFactory routeFactory = new BDDRouteFactory(g, config);
    _variables = routeFactory.variables();
    _pairing = BDDRouteFactory.factory.makePair();

    long t = System.currentTimeMillis();
    BDDNetwork network = BDDNetwork.create(g, ns, config, false);
    System.out.println("Time to build BDDs: " + (System.currentTimeMillis() - t));

    initializeQuantificationVariables();

    ReachabilityDomain domain = new ReachabilityDomain(_variables, _communityAndProtocolBits);

    // t = System.currentTimeMillis();
    // ReachabilityDomainAP domain2 =
    //    new ReachabilityDomainAP(domain, g.getRouters(), network);
    // System.out.println("Time for atomic preds: " + (System.currentTimeMillis() - t));

    Map<String, BDD> reachable = computeFixedPoint(g, network, routeFactory, domain);
    return new StringAnswerElement("Foo Bar");
  }
}
