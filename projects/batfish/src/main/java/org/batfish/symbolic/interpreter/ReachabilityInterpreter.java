package org.batfish.symbolic.interpreter;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.bdd.BDDNetwork;
import org.batfish.symbolic.bdd.BDDRouteConfig;
import org.batfish.symbolic.bdd.BDDRouteFactory;
import org.batfish.symbolic.bdd.BDDTransferFunction;
import org.batfish.symbolic.smt.EdgeType;

public class ReachabilityInterpreter {

  private IBatfish _batfish;

  private HeaderLocationQuestion _question;

  public ReachabilityInterpreter(IBatfish batfish, HeaderLocationQuestion q) {
    _batfish = batfish;
    _question = q;
  }

  private <T> Map<String, BDD> computeFixedPoint(
      Graph g, BDDNetwork network, BDDRouteFactory factory, IAbstractDomain<T> domain) {

    long t;

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

    Map<String, T> reachable = new HashMap<>();

    t = System.currentTimeMillis();
    // initialize for BGP by converting prefixes to BDDs

    for (Entry<String, Set<Prefix>> e : originatedBGP.entrySet()) {
      String router = e.getKey();
      Set<Prefix> prefixes = e.getValue();
      T r = domain.init(router, prefixes);
      reachable.put(router, r);
    }
    System.out.println("Time for network to BDD conversion: " + (System.currentTimeMillis() - t));

    // Initialize the workset
    Set<String> updateSet = new HashSet<>();
    Queue<String> update = new ArrayDeque<>();

    for (Entry<String, T> e : reachable.entrySet()) {
      String router = e.getKey();
      for (String neighbor : g.getNeighbors().get(router)) {
        updateSet.add(neighbor);
        update.add(neighbor);
      }
    }

    t = System.currentTimeMillis();
    while (!update.isEmpty()) {

      String router = update.poll();
      updateSet.remove(router);

      T r = reachable.get(router);
      T rprime = r;

      // System.out.println("Looking at router: " + router);
      // System.out.println(_variables.dot(r));
      // System.out.println("\n\n");

      Set<String> neighbors = new HashSet<>();

      for (GraphEdge ge : g.getEdgeMap().get(router)) {
        GraphEdge rev = g.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {

          String neighbor = ge.getPeer();
          neighbors.add(neighbor);
          T nr = reachable.get(neighbor);

          // System.out.println("  Got neighbor: " + neighbor);
          // System.out.println(_variables.dot(nr));
          // System.out.println("\n\n");

          BDDTransferFunction exportFilter = network.getExportBgpPolicies().get(ge);
          BDDTransferFunction importFilter = network.getImportBgpPolicies().get(rev);

          if (exportFilter != null) {
            EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, exportFilter);
            nr = domain.transform(nr, exp);
          }

          if (importFilter != null) {
            EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, importFilter);
            nr = domain.transform(nr, imp);
          }

          rprime = domain.merge(rprime, nr);
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

    Map<String, BDD> reach = new HashMap<>();
    for (Entry<String, T> e : reachable.entrySet()) {
      BDD val = domain.finalize(e.getValue());
      reach.put(e.getKey(), val);
      // System.out.println("Final router: " + e.getKey());
      // System.out.println("" + factory.variables().dot(val));
    }
    return reach;
  }

  public AnswerElement interpret() {
    Graph g = new Graph(_batfish);
    BDDRouteConfig config = new BDDRouteConfig(true);
    BDDRouteFactory routeFactory = new BDDRouteFactory(g, config);

    NodesSpecifier ns = new NodesSpecifier(_question.getIngressNodeRegex());

    long t = System.currentTimeMillis();
    BDDNetwork network = BDDNetwork.create(g, ns, config);
    System.out.println("Time to build BDDs: " + (System.currentTimeMillis() - t));

    ReachabilityAbstractDomainBDD domain = new ReachabilityAbstractDomainBDD(routeFactory);

    // t = System.currentTimeMillis();
    // ReachabilityAbstractDomainAP domain2 =
    //    new ReachabilityAbstractDomainAP(domain, g.getRouters(), network);
    // System.out.println("Time for atomic preds: " + (System.currentTimeMillis() - t));

    Map<String, BDD> reachable = computeFixedPoint(g, network, routeFactory, domain);
    return new StringAnswerElement("Foo Bar");
  }
}
