package org.batfish.symbolic.cinterpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.ainterpreter.AggregateTransformer;
import org.batfish.symbolic.ainterpreter.EdgeTransformer;

public interface IConcreteDomain<T extends AbstractRoute> {

  T create(String router, Prefix network, RoutingProtocol protocol, @Nullable Ip nextHopIp, long admin, long metric);

  Map<Prefix, T> value(Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes);

  @Nullable T transform(T input, EdgeTransformer t);

  T merge(T x, T y);

  // T aggregate(Configuration conf, AggregateTransformer t, Map<Prefix, T> x);

  // String nextHop(T rib, String node, Flow flow);

  List<Route> toRoutes(Map<Prefix, T> routes);
}
