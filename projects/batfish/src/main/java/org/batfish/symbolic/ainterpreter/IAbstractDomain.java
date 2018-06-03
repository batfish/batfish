package org.batfish.symbolic.ainterpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.utils.Tuple;

public interface IAbstractDomain<T> {

  T bot();

  T value(Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes);

  T transform(T input, EdgeTransformer f);

  T merge(T x, T y);

  T selectBest(T x);

  T aggregate(Configuration conf, List<AggregateTransformer> aggregates, T x);

  String nextHop(T rib, String node, Flow flow);

  List<Route> toRoutes(T value);

  Tuple<BDDNetFactory, BDD> toFib(Map<String, T> ribs);

  String debug(T x);
}
