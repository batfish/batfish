package org.batfish.symbolic.interpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.CommunityVar;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDRouteConfig;
import org.batfish.symbolic.bdd.TransferReturn;

public class ReachabilityInterpreter {

  private IBatfish _batfish;

  private HeaderLocationQuestion _question;

  private BDDRoute _variables;

  private BDDNetwork _network;

  private BDDPairing _pairing;

  public ReachabilityInterpreter(IBatfish batfish, HeaderLocationQuestion q) {
    _batfish = batfish;
    _question = q;
    _pairing = BDDRoute.factory.makePair();
  }

  // TODO: this code is copied from TransferBDD

  private BDD firstBitsEqual(BDD[] bits, Prefix p, int length) {
    long b = p.getStartIp().asLong();
    BDD acc = BDDRoute.factory.one();
    for (int i = 0; i < length; i++) {
      boolean res = Ip.getBitAtPosition(b, i);
      if (res) {
        acc = acc.and(bits[i]);
      } else {
        acc = acc.and(bits[i].not());
      }
    }
    return acc;
  }

  // TODO: this code is copied from TransferBDD

  /*
   * Check if a prefix range match is applicable for the packet destination
   * Ip address, given the prefix length variable.
   *
   * Since aggregation is modelled separately, we assume that prefixLen
   * is not modified, and thus will contain only the underlying variables:
   * [var(0), ..., var(n)]
   */
  private BDD isRelevantFor(BDDRoute record, PrefixRange range) {
    Prefix p = range.getPrefix();
    SubRange r = range.getLengthRange();
    int len = p.getPrefixLength();
    int lower = r.getStart();
    int upper = r.getEnd();

    BDD lowerBitsMatch = firstBitsEqual(record.getPrefix().getBitvec(), p, len);
    BDD acc = BDDRoute.factory.zero();
    if (lower == 0 && upper == 32) {
      acc = BDDRoute.factory.one();
    } else {
      for (int i = lower; i <= upper; i++) {
        BDD equalLen = record.getPrefixLength().value(i);
        acc = acc.or(equalLen);
      }
    }
    return acc.and(lowerBitsMatch);
  }

  private BDD applyTransformer(BDD r, TransferReturn t) {
    BDD passThrough = t.getSecond();
    BDD acc = r.and(passThrough);
    BDDRoute mods = t.getFirst();

    _pairing.reset();

    if (mods.getConfig().getKeepCommunities()) {
      for (Entry<CommunityVar, BDD> e : _variables.getCommunities().entrySet()) {
        CommunityVar cvar = e.getKey();
        BDD x = e.getValue();
        BDD temp = _variables.getTemporary(x);
        BDD expr = mods.getCommunities().get(cvar);
        BDD equal = temp.biimp(expr);
        acc = acc.and(equal);

        _pairing.set(x.var(), temp.var());
      }
    }

    // TODO for other fields

    acc = acc.replace(_pairing);
    return acc;
  }

  public AnswerElement computeStuff() {
    Graph g = new Graph(_batfish);
    BDDRouteConfig config = new BDDRouteConfig(true);
    NodesSpecifier ns = new NodesSpecifier(_question.getIngressNodeRegex());

    _variables = new BDDRoute(config, g.getAllCommunities());

    long t;

    t = System.currentTimeMillis();
    _network = BDDNetwork.create(g, ns, config);
    System.out.println("Time to build BDDs: " + (System.currentTimeMillis() - t));

    Map<String, Set<Prefix>> originatedBGP = new HashMap<>();
    Map<String, Set<Prefix>> originatedOSPF = new HashMap<>();
    Map<String, Set<Prefix>> originatedStatic = new HashMap<>();
    Map<String, Set<Prefix>> originatedConnected = new HashMap<>();

    // create the initial route message sets
    for (String router : g.getRouters()) {
      Configuration conf = g.getConfigurations().get(router);
      Vrf vrf = conf.getDefaultVrf();
      if (vrf.getOspfProcess() != null) {
        originatedOSPF.put(router, Graph.getOriginatedNetworks(conf, Protocol.OSPF));
      }
      if (vrf.getBgpProcess() != null) {
        originatedBGP.put(router, Graph.getOriginatedNetworks(conf, Protocol.BGP));
      }
      if (vrf.getStaticRoutes() != null) {
        originatedStatic.put(router, Graph.getOriginatedNetworks(conf, Protocol.STATIC));
      }
      originatedConnected.put(router, Graph.getOriginatedNetworks(conf, Protocol.CONNECTED));
    }

    Map<String, BDD> reachable = new HashMap<>();

    t = System.currentTimeMillis();
    // initialize for BGP by converting prefixes to BDDs
    for (Entry<String, Set<Prefix>> e : originatedBGP.entrySet()) {
      String router = e.getKey();
      Set<Prefix> prefixes = e.getValue();
      BDD acc = BDDRoute.factory.zero();
      for (Prefix prefix : prefixes) {
        System.out.println("Got prefix: " + prefix + " for router " + router);
        SubRange r = new SubRange(32, 32);
        PrefixRange range = new PrefixRange(prefix, r);
        BDD pfx = isRelevantFor(_variables, range);
        acc = acc.or(pfx);
      }
      reachable.put(router, acc);
    }
    System.out.println("Time for network to BDD conversion: " + (System.currentTimeMillis() - t));

    // Initialize the workset
    Set<String> updateSet = new HashSet<>();
    Queue<String> update = new ArrayDeque<>();

    for (Entry<String, BDD> e : reachable.entrySet()) {
      String router = e.getKey();
      BDD bdd = e.getValue();
      if (!bdd.isZero()) {
        for (String neighbor : g.getNeighbors().get(router)) {
          updateSet.add(neighbor);
          update.add(neighbor);
          // System.out.println("Must update: " + neighbor);
        }
      }
    }

    t = System.currentTimeMillis();
    while (!update.isEmpty()) {

      String router = update.poll();
      updateSet.remove(router);

      BDD r = reachable.get(router);
      BDD rprime = r;

      // System.out.println("Looking at router: " + router);
      // System.out.println(_variables.dot(r));
      // System.out.println("\n\n");

      Set<String> neighbors = new HashSet<>();

      for (GraphEdge ge : g.getEdgeMap().get(router)) {
        GraphEdge rev = g.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {

          String neighbor = ge.getPeer();
          neighbors.add(neighbor);
          BDD nr = reachable.get(neighbor);

          // System.out.println("  Got neighbor: " + neighbor);
          // System.out.println(_variables.dot(nr));
          // System.out.println("\n\n");

          TransferReturn exportFilter = _network.getExportBgpPolicies().get(rev);
          TransferReturn importFilter = _network.getImportBgpPolicies().get(ge);

          if (exportFilter != null) {
            nr = applyTransformer(nr, exportFilter);
          }

          if (importFilter != null) {
            nr = applyTransformer(nr, importFilter);
          }

          rprime = rprime.or(nr);
        }
      }

      // System.out.println("Now for router: " + router);
      // System.out.println(_variables.dot(rprime));
      // System.out.println("\n\n");

      if (!r.equals(rprime)) {
        // for (String neighbor : neighbors) {
        //  System.out.println("  change at " + router + ", adding " + neighbor);
        // }
        for (String neighbor : neighbors) {
          if (!updateSet.contains(neighbor)) {
            updateSet.add(neighbor);
            update.add(neighbor);
          }
        }
        reachable.put(router, rprime);
      }
    }
    System.out.println("Time to compute fixedpoint: " + (System.currentTimeMillis() - t));

    // atomic predicates
    Set<BDD> allFilters = new HashSet<>();
    Set<TransferReturn> allTransforms = new HashSet<>();
    for (TransferReturn r : _network.getExportBgpPolicies().values()) {
      allFilters.add(r.getSecond());
      allTransforms.add(r);
    }
    List<BDD> filters = new ArrayList<>(allFilters);
    List<TransferReturn> trans = new ArrayList<>(allTransforms);
    List<Transformer> transforms = new ArrayList<>();
    for (TransferReturn tr : trans) {
      Function<BDD, BDD> f = (bdd) -> applyTransformer(bdd, tr);
      Transformer x = new Transformer(tr, f);
      transforms.add(x);
    }
    AtomicPredicates ap = AtomOps.computeAtomicPredicates(filters, transforms);
    System.out.println("Disjoint predicates: " + ap.getDisjoint().size());
    System.out.println("Atom map: " + ap.getAtoms().size());

    return new StringAnswerElement("Foo");
  }
}
