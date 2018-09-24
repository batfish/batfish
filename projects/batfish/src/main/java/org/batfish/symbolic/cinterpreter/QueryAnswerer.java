package org.batfish.symbolic.cinterpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.GraphEdge;

public class QueryAnswerer {

  private Graph _graph;

  public QueryAnswerer(Graph graph) {
    this._graph = graph;
  }

  public <T extends AbstractRoute> SortedSet<Route> computeRoutes(
      ConcreteInterpreter interpreter, IConcreteDomain<T> domain, NodesSpecifier ns) {
    ConcreteState<T> state = interpreter.computeFixedPoint(domain);
    Map<String, ConcreteRib<T>> reachable = state.getPerRouterRoutes();

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
      Map<Prefix,T> rib = reachable.get(router).getMainRib();
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
