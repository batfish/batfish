package org.batfish.symbolic.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteBuilder;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.BgpRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.OspfInternalRoute;
import org.batfish.datamodel.OspfIntraAreaRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.routing_policy.Environment.Direction;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.symbolic.Graph;
import org.batfish.symbolic.Protocol;
import org.batfish.symbolic.smt.EdgeType;

public class ExactDomain implements IConcreteDomain<AbstractRoute> {

  private Graph _graph;

  public ExactDomain(Graph graph) {
    _graph = graph;
  }

  @Override
  public AbstractRoute create(
      String router,
      Prefix network,
      RoutingProtocol protocol,
      @Nullable Ip nextHopIp,
      long admin,
      long metric) {

    if (protocol.equals(RoutingProtocol.OSPF)) {
      AbstractRoute r = new OspfIntraAreaRoute(network, null, (int) admin, metric, 0);
      r.setNode(router);
      return r;
    }
    if (protocol.equals(RoutingProtocol.STATIC)) {
      AbstractRoute r = new StaticRoute(network, null, null, (int) admin, 0, 0);
      r.setNode(router);
      return r;
    }
    if (protocol.equals(RoutingProtocol.CONNECTED)) {
      AbstractRoute r = new ConnectedRoute(network, "unknown");
      r.setNode(router);
      return r;
    }
    if (protocol.equals(RoutingProtocol.BGP)) {
      AbstractRoute r =
          new BgpRoute(
              network,
              null,
              (int) admin,
              new AsPath(new ArrayList<>()),
              new TreeSet<>(),
              false,
              100,
              80,
              null,
              new TreeSet<>(),
              false,
              OriginType.IGP,
              RoutingProtocol.BGP,
              null,
              RoutingProtocol.BGP,
              0);
      r.setNode(router);
      return r;
    }
    throw new BatfishException("unrecognized protocol: " + protocol.protocolName());
  }

  @Override
  public Map<Prefix, AbstractRoute> value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {

    Map<Prefix, AbstractRoute> map = new HashMap<>();
    if (prefixes == null) {
      return map;
    }

    if (proto.equals(RoutingProtocol.OSPF)) {
      for (Prefix pfx : prefixes) {
        AbstractRoute r = new OspfIntraAreaRoute(pfx, null, 110, 0, 0);
        r.setNode(conf.getHostname());
        map.put(pfx, r);
      }
    }
    if (proto.equals(RoutingProtocol.STATIC)) {
      for (Prefix pfx : prefixes) {
        AbstractRoute r = new StaticRoute(pfx, null, null, 1, 0, 0);
        r.setNode(conf.getHostname());
        map.put(pfx, r);
      }
    }
    if (proto.equals(RoutingProtocol.CONNECTED)) {
      for (Prefix pfx : prefixes) {
        AbstractRoute r = new ConnectedRoute(pfx, "unknown");
        r.setNode(conf.getHostname());
        map.put(pfx, r);
      }
    }
    if (proto.equals(RoutingProtocol.BGP)) {
      for (Prefix pfx : prefixes) {
        AbstractRoute r =
            new BgpRoute(
                pfx,
                null,
                20,
                new AsPath(new ArrayList<>()),
                new TreeSet<>(),
                false,
                100,
                80,
                null,
                new TreeSet<>(),
                false,
                OriginType.IGP,
                RoutingProtocol.BGP,
                null,
                RoutingProtocol.BGP,
                0);
        r.setNode(conf.getHostname());
        map.put(pfx, r);
      }
    }
    return map;
  }

  @Override
  public @Nullable AbstractRoute transform(AbstractRoute input, EdgeTransformer t) {
    Protocol proto = Protocol.fromRoutingProtocol(t.getProtocol());
    assert (proto != null);
    if (t.getEdgeType() == EdgeType.IMPORT) {
      RoutingPolicy pol =
          (t.getEdgeType() == EdgeType.IMPORT)
              ? _graph.findImportRoutingPolicy(t.getEdge().getRouter(), proto, t.getEdge())
              : _graph.findExportRoutingPolicy(t.getEdge().getRouter(), proto, t.getEdge());

      if (pol == null) {
        return null;
      }

      AbstractRouteBuilder<?,?> builder;
      if (t.getProtocol() == RoutingProtocol.OSPF) {
        builder = new OspfInternalRoute.Builder();
      } else if (t.getProtocol() == RoutingProtocol.BGP) {
        builder = new BgpRoute.Builder();
      } else {
        return null;
      }

      Direction dir = (t.getEdgeType() == EdgeType.IMPORT) ? Direction.IN : Direction.OUT;

      boolean accepted = pol.process(input, builder, null, "default", dir);
      if (!accepted) {
        return null;
      }
      return builder.build();
    }
    return null;
  }

  @Override
  public AbstractRoute merge(AbstractRoute x, AbstractRoute y) {
    int cmp = x.routeCompare(y);
    if (cmp >= 0) {
      return x;
    } else {
      return y;
    }
  }

  // @Override public AbstractRoute aggregate(Configuration conf, AggregateTransformer t,
  //     Map<Prefix, AbstractRoute> x) {
  //   return null;
  // }

  // @Override public String nextHop(AbstractRoute rib, String node, Flow flow) {
  //  return null;
  // }

  @Override
  public List<Route> toRoutes(Map<Prefix, AbstractRoute> routes) {
    List<Route> rs = new ArrayList<>();
    for (Entry<Prefix, AbstractRoute> entry : routes.entrySet()) {
      Prefix pfx = entry.getKey();
      AbstractRoute route = entry.getValue();
      Route r = new Route(route.getNode(), "default", pfx, null, null, null, route.getAdministrativeCost(), route.getMetric(), route.getProtocol(), 0);
      rs.add(r);
    }
    return rs;
  }
}