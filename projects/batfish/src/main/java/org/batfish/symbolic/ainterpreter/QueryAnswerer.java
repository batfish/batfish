package org.batfish.symbolic.ainterpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;
import org.batfish.symbolic.answers.AiReachabilityAnswerElement;
import org.batfish.symbolic.bdd.BDDFiniteDomain;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.bdd.BDDRoute;
import org.batfish.symbolic.bdd.BDDUtils;
import org.batfish.symbolic.bdd.SatAssignment;
import org.batfish.symbolic.utils.Tuple;

public class QueryAnswerer {

  private Graph _graph;

  public QueryAnswerer(Graph graph) {
    this._graph = graph;
  }

  private BDD isRouter(BDDNetFactory netFactory, BDDFiniteDomain<String> fdom, NodesSpecifier ns) {
    BDD isRouter = netFactory.zero();
    for (String router : ns.getMatchingNodesByName(_graph.getRouters())) {
      BDD r = fdom.value(router);
      isRouter.orWith(r);
    }
    return isRouter;
  }

  private BDD query(HeaderLocationQuestion question, BDDNetFactory netFactory) {
    NodesSpecifier ingress = new NodesSpecifier(question.getIngressNodeRegex());
    NodesSpecifier egress = new NodesSpecifier(question.getFinalNodeRegex());
    BDD headerspace = BDDUtils.headerspaceToBdd(netFactory, question.getHeaderSpace());
    BDD startRouter = isRouter(netFactory, netFactory.routeVariables().getSrcRouter(), ingress);
    BDD finalRouter = isRouter(netFactory, netFactory.routeVariables().getDstRouter(), egress);
    return startRouter.and(finalRouter).and(headerspace);
  }

  public <T> AnswerElement reachability(
      AbstractInterpreter interpreter, IAbstractDomain<T> domain, HeaderLocationQuestion question) {
    AbstractState<T> state = interpreter.computeFixedPoint(domain);
    Map<String, AbstractRib<T>> reachable = state.getPerRouterRoutes();
    Map<String, T> ribMap = new HashMap<>();
    for (Entry<String, AbstractRib<T>> e : reachable.entrySet()) {
      ribMap.put(e.getKey(), e.getValue().getMainRib());
    }
    long t = System.currentTimeMillis();
    Tuple<BDDNetFactory, BDD> fibsTup = domain.toFib(ribMap);
    BDDNetFactory netFactory = fibsTup.getFirst();
    BDDRoute variables = netFactory.routeVariables();
    BDD fibs = fibsTup.getSecond();
    System.out.println("Transitive closure: " + (System.currentTimeMillis() - t));
    BDD query = query(question, netFactory);
    BDD subset = query.and(fibs);
    List<AbstractFlowTrace> traces = new ArrayList<>();
    NodesSpecifier ns = new NodesSpecifier(question.getIngressNodeRegex());
    for (String srcRouter : ns.getMatchingNodesByName(_graph.getRouters())) {
      BDD src = variables.getSrcRouter().value(srcRouter);
      BDD matchingFromSrc = subset.and(src);
      if (matchingFromSrc.isZero()) {
        continue;
      }
      SatAssignment assignment = BDDUtils.satOne(netFactory, matchingFromSrc);
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

  public <T> SortedSet<Route> computeRoutes(
      AbstractInterpreter interpreter, IAbstractDomain<T> domain, NodesSpecifier ns) {
    AbstractState<T> state = interpreter.computeFixedPoint(domain);
    Map<String, AbstractRib<T>> reachable = state.getPerRouterRoutes();

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
      T rib = reachable.get(router).getMainRib();
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

    return routes;
  }
}
