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

public class DonutDomain<U, T> extends ProductDomain<U, T> {

  private IDomainDifferencer<U, T> _mixer;

  public DonutDomain(
      IDomainDifferencer<U, T> mixer, IAbstractDomain<U> domain1, IAbstractDomain<T> domain2) {
    super(domain1, domain2);
    _mixer = mixer;
  }

  @Override
  public Tuple<U, T> value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    U val1 = _domain1.value(conf, proto, prefixes);
    T val2 = _domain2.value(conf, proto, prefixes);
    return new Tuple<>(val1, _mixer.difference(val1, val2));
  }

  @Override
  public Tuple<U, T> transform(Tuple<U, T> input, EdgeTransformer t) {
    U newRib1 = _domain1.transform(input.getFirst(), t);
    T newRib2 = _domain2.transform(input.getSecond(), t);
    newRib2 = _mixer.difference(newRib1, newRib2);
    return new Tuple<>(newRib1, newRib2);
  }

  @Override
  public List<Route> toRoutes(Tuple<U, T> value) {
    return this._domain2.toRoutes(value.getSecond());
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, Tuple<U, T>> ribs) {
    Map<String, T> newRibs2 = DomainHelper.convertRibs2(ribs);
    return _domain2.toFib(newRibs2);
  }

  @Override
  @Nullable
  public String nextHop(Tuple<U, T> rib, String node, Flow flow) {
    return _domain2.nextHop(rib.getSecond(), node, flow);
  }
}
