package org.batfish.symbolic.interpreter;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
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
import org.batfish.symbolic.smt.EdgeType;

// TODO: Take ACLs into account. Will likely require using a single Factory object
// TODO: Compute end-to-end reachability

/*
 * Computes an overapproximation of some concrete set of states in the
 * network using abstract interpretation
 */
public class AbstractInterpreter {

  // Handle the the batfish object
  private IBatfish _batfish;

  // The question asked
  private HeaderLocationQuestion _question;

  // A collection of single node BDDs representing the individual variables
  private BDDRoute _variables;

  // The set of community and protocol BDD variables that we will quantify away
  private BDD _communityAndProtocolBits;

  // A cache of BDDs representing a prefix length exact match
  private Map<Integer, BDD> _lengthCache;

  // A cache of sets of BDDs for a given prefix length to quantify away
  private Map<Integer, BDD> _dstBitsCache;

  // The prefix length bits that we will quantify away
  private BDD _lenBits;

  private long _time;

  /*
   * Construct an abstract interpreter the answer a particular question.
   * This could be done more in a fashion like Batfish, where we run
   * the computation once and then answer many questions.
   */
  public AbstractInterpreter(IBatfish batfish, HeaderLocationQuestion q) {
    _batfish = batfish;
    _question = q;
    _lengthCache = new HashMap<>();
    _dstBitsCache = new HashMap<>();
    _lenBits = BDDRouteFactory.factory.one();
    _time = 0;
  }

  /*
   * Initialize what prefixes are 'originated' at each router
   * and for each routing protocol. These are used as the
   * starting values for the fixed point computation.
   */
  private void initializeOriginatedPrefixes(
      Graph g,
      Map<String, Set<Prefix>> originatedOSPF,
      Map<String, Set<Prefix>> originatedConnected,
      Map<String, Map<String, Set<Prefix>>> originatedStatic) {

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
      len = BDDRouteFactory.factory.one();
      for (int j = 0; j < pfxLen.getBitvec().length; j++) {
        BDD var = pfxLen.getBitvec()[j];
        BDD val = newVal.getBitvec()[j];
        if (val.isOne()) {
          len = len.and(var);
        } else {
          len = len.and(var.not());
        }
      }
      _lengthCache.put(i, len);
    }
    return len;
  }

  /*
   * Compute the set of BDD variables to quantify away for a given prefix length.
   * The boolean indicates whether the router bits should be removed
   * For efficiency, these values are cached.
   */
  private BDD removeBits(int len) {
    BDD removeBits = _dstBitsCache.get(len);
    if (removeBits == null) {
      removeBits = BDDRouteFactory.factory.one();
      for (int i = len; i < 32; i++) {
        BDD x = _variables.getPrefix().getBitvec()[i];
        removeBits = removeBits.and(x);
      }
      _dstBitsCache.put(len, removeBits);
    }
    return removeBits;
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
    BDD acc = BDDRouteFactory.factory.zero();
    for (int i = 32; i >= 0; i--) {
      BDD len = makeLength(i);
      BDD withLen = pfxOnly.and(len);
      if (withLen.isZero()) {
        continue;
      }

      /* System.out.println("Remove bits:");
      for (BDD x : removeBits) {
        System.out.println(" " + _variables.name(x.var()) + ", " + x.var());
      } */

      // long t = System.currentTimeMillis();

      BDD removeBits = removeBits(i);
      if (!removeBits.isOne()) {
        withLen = withLen.exist(removeBits);
      }

      acc = acc.or(withLen);
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
   * Iteratively computes a fixed point over an abstract domain.
   * Starts with some initial advertisements that are 'originated'
   * by different protocols and maintains an underapproximation of
   * reachable sets at each router for every iteration.
   */
  private <T> Map<String, AbstractRib<BDD>> computeFixedPoint(
      Graph g, BDDNetwork network, IAbstractDomain<T> domain) {

    Map<String, Set<Prefix>> originatedOSPF = new HashMap<>();
    Map<String, Set<Prefix>> originatedConnected = new HashMap<>();
    Map<String, Map<String, Set<Prefix>>> originatedStatic = new HashMap<>();
    initializeOriginatedPrefixes(g, originatedOSPF, originatedConnected, originatedStatic);

    Map<String, AbstractRib<T>> reachable = new HashMap<>();
    Set<String> initialRouters = new HashSet<>();

    long t = System.currentTimeMillis();
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
          neighbor = (neighbor == null ? router : neighbor);
          T statForNeighbor = domain.value(neighbor, Protocol.STATIC, prefixes);
          stat = domain.merge(stat, statForNeighbor);
        }
      }
      T rib = domain.merge(domain.merge(domain.merge(bgp, ospf), stat), conn);
      BDD headerspace = toHeaderspace(domain.toBdd(rib));

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
      // System.out.println("RIB is " + "\n" + _variables.dot(domain.toBdd(routerRib)));

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
            BDD h = toHeaderspace(domain.toBdd(stat));
            transferedHeaderspace = transferedHeaderspace.or(h);
          }

          // Update OSPF
          if (g.isEdgeUsed(conf, Protocol.OSPF, ge)) {
            newNeighborOspf = domain.merge(neighborOspf, routerOspf);
            BDD h = toHeaderspace(domain.toBdd(routerOspf));
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
            //    "  After processing: " + "\n" + _variables.dot(domain.toBdd(tmpBgp)));

            newNeighborBgp = domain.merge(neighborBgp, tmpBgp);
            BDD h = toHeaderspace(domain.toBdd(tmpBgp));
            transferedHeaderspace = transferedHeaderspace.or(h);
          }

          // TODO: conjoin with acls
          // TODO: need to replace destination IP with prefix bits in ACLs
          /* BDDAcl out = network.getOutAcls().get(rev);
          BDDAcl in = network.getInAcls().get(ge);
          if (out != null) {
            out.
          } */

          transferedHeaderspace = transferedHeaderspace.and(routerHeaderspace);
          BDD newHeaderspace = neighborHeaderspace.or(transferedHeaderspace);

          // System.out.println("  New Headerspace: \n" + _variables.dot(newHeaderspace));

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
    }
    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));

    Map<String, AbstractRib<BDD>> reach = new HashMap<>();
    for (Entry<String, AbstractRib<T>> e : reachable.entrySet()) {
      AbstractRib<T> val = e.getValue();
      BDD bgp = domain.toBdd(val.getRibEntry());
      BDD ospf = domain.toBdd(val.getOspfRib());
      BDD conn = domain.toBdd(val.getConnectedRib());
      BDD stat = domain.toBdd(val.getStaticRib());
      BDD rib = domain.toBdd(val.getRibEntry());
      BDD headerspace = val.getHeaderspace();
      AbstractRib<BDD> bddRib = new AbstractRib<>(bgp, ospf, stat, conn, rib, headerspace);
      reach.put(e.getKey(), bddRib);
    }
    return reach;
  }

  /*
   * Print a collection of routes in a BDD as representative examples
   * that can be understood by a human.
   */
  private void debug(BDDRouteFactory factory, BDD routes, boolean isFib) {
    List assignments = routes.allsat();
    for (Object o : assignments) {
      long pfx = 0;
      int proto = 0;
      int len = 0;
      int router = 0;
      byte[] variables = (byte[]) o;
      for (int i = 0; i < variables.length; i++) {
        byte var = variables[i];
        String name = _variables.name(i);
        // avoid temporary variables
        if (name != null) {
          boolean isTrue = (var == 1);
          if (isTrue) {
            if (name.startsWith("proto") && !name.contains("'")) {
              int num = Integer.parseInt(name.substring(5));
              proto = proto + (1 << (2 - num));
            } else if (name.startsWith("pfxLen") && !name.contains("'")) {
              int num = Integer.parseInt(name.substring(6));
              len = len + (1 << (6 - num));
            } else if (name.startsWith("pfx") && !name.contains("'")) {
              int num = Integer.parseInt(name.substring(3));
              pfx = pfx + (1 << (32 - num));
            } else if (name.startsWith("router") && !name.contains("'")) {
              int num = Integer.parseInt(name.substring(6));
              router =
                  router + (1 << (_variables.getDstRouter().getInteger().getBitvec().length - num));
            }
          }
        }
      }

      if (isFib) {
        Ip ip = new Ip(pfx);
        System.out.println("  " + ip);
      } else {
        Protocol prot = BDDRouteFactory.allProtos.get(proto);
        String r = factory.getRouter(router);
        Ip ip = new Ip(pfx);
        Prefix p = new Prefix(ip, len);
        System.out.println("  " + prot.name() + ", " + p + " --> " + r);
      }
    }
  }

  /*
   * Variables that will be existentially quantified away
   */
  private void initializeQuantificationVariables() {
    _communityAndProtocolBits = BDDRouteFactory.factory.one();
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
   * Compute an underapproximation of all-pairs reachability
   */
  public AnswerElement interpret() {
    Graph g = new Graph(_batfish);
    NodesSpecifier ns = new NodesSpecifier(_question.getIngressNodeRegex());
    BDDRouteConfig config = new BDDRouteConfig(true);

    BDDRouteFactory routeFactory = new BDDRouteFactory(g, config);
    _variables = routeFactory.variables();

    long t = System.currentTimeMillis();
    BDDNetwork network = BDDNetwork.create(g, ns, config, false);
    System.out.println("Time to build BDDs: " + (System.currentTimeMillis() - t));

    initializeQuantificationVariables();

    ReachabilityDomain domain = new ReachabilityDomain(_variables, _communityAndProtocolBits);
    Map<String, AbstractRib<BDD>> reachable = computeFixedPoint(g, network, domain);

    System.out.println("To headerspace time: " + _time);

    /* for (Entry<String, AbstractRib<BDD>> e : reachable.entrySet()) {
      AbstractRib<BDD> rib = e.getValue();
      System.out.println("Router " + e.getKey() + " RIB:");
      debug(routeFactory, rib.getRibEntry(), false);
      System.out.println("Router " + e.getKey() + " FIB:");
      // System.out.println(_variables.dot(rib.getHeaderspace()));
      debug(routeFactory, rib.getHeaderspace(), true);
    } */

    return new StringAnswerElement("Done");
  }
}
