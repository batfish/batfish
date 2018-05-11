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

    Map<String, AbstractRib<T>> reachable = new HashMap<>();
    Set<String> initialRouters = new HashSet<>();

    t = System.currentTimeMillis();
    // initialize for BGP by converting prefixes to BDDs

    for (String router : g.getRouters()) {
      Set<Prefix> bgpPrefixes = originatedBGP.get(router);
      Set<Prefix> ospfPrefixes = originatedOSPF.get(router);
      Set<Prefix> connPrefixes = originatedConnected.get(router);

      if (bgpPrefixes != null && !bgpPrefixes.isEmpty()) {
        initialRouters.add(router);
      }
      if (ospfPrefixes != null && !ospfPrefixes.isEmpty()) {
        initialRouters.add(router);
      }

      T bgp = domain.init(router, Protocol.BGP, bgpPrefixes);
      T ospf = domain.init(router, Protocol.OSPF, ospfPrefixes);
      T conn = domain.init(router, Protocol.CONNECTED, connPrefixes);
      T rib = domain.merge(domain.merge(bgp, ospf), conn);
      AbstractRib<T> abstractRib = new AbstractRib<>(bgp, ospf, conn, rib);
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

      // System.out.println("Looking at router: " + router);

      for (GraphEdge ge : g.getEdgeMap().get(router)) {
        GraphEdge rev = g.getOtherEnd().get(ge);
        if (ge.getPeer() != null && rev != null) {

          String neighbor = ge.getPeer();

          AbstractRib<T> nr = reachable.get(neighbor);
          T neighborConn = nr.getConnectedRib();
          T neighborBgp = nr.getBgpRib();
          T neighborOspf = nr.getOspfRib();
          T neighborRib = nr.getRibEntry();

          // System.out.println("  Got neighbor: " + neighbor);

          T newNeighborOspf = neighborOspf;
          T newNeighborBgp = neighborBgp;

          // Update OSPF
          if (g.isEdgeUsed(conf, Protocol.OSPF, ge)) {
            newNeighborOspf = domain.merge(neighborOspf, routerOspf);
          }

          // Update BGP
          if (g.isEdgeUsed(conf, Protocol.BGP, ge)) {
            BDDTransferFunction exportFilter = network.getExportBgpPolicies().get(rev);
            BDDTransferFunction importFilter = network.getImportBgpPolicies().get(ge);

            T tmpBgp = routerRib;
            if (exportFilter != null) {
              EdgeTransformer exp = new EdgeTransformer(ge, EdgeType.EXPORT, exportFilter);
              tmpBgp = domain.transform(tmpBgp, exp);
            }

            if (importFilter != null) {
              EdgeTransformer imp = new EdgeTransformer(ge, EdgeType.IMPORT, importFilter);
              tmpBgp = domain.transform(tmpBgp, imp);
            }

            newNeighborBgp = domain.merge(neighborBgp, tmpBgp);
          }

          // Update RIB
          T newNeighborRib =
              domain.merge(domain.merge(newNeighborBgp, newNeighborOspf), neighborConn);

          // If changed, then add it to the workset
          if (!newNeighborRib.equals(neighborRib) || !newNeighborOspf.equals(neighborOspf)) {
            AbstractRib<T> newAbstractRib =
                new AbstractRib<>(newNeighborBgp, newNeighborOspf, neighborConn, newNeighborRib);
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
      BDD val = domain.finalize(e.getValue().getRibEntry());
      reach.put(e.getKey(), val);
      System.out.println("Final router: " + e.getKey());
      System.out.println("" + factory.variables().dot(val));
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

    ReachabilityDomain domain = new ReachabilityDomain(routeFactory);

    // t = System.currentTimeMillis();
    // ReachabilityDomainAP domain2 =
    //    new ReachabilityDomainAP(domain, g.getRouters(), network);
    // System.out.println("Time for atomic preds: " + (System.currentTimeMillis() - t));

    Map<String, BDD> reachable = computeFixedPoint(g, network, routeFactory, domain);
    return new StringAnswerElement("Foo Bar");
  }
}
