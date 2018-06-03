package org.batfish.symbolic.ainterpreter;

import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.utils.Tuple;

public class ProductDomain<U, T> implements IAbstractDomain<Tuple<U, T>> {

  protected IAbstractDomain<U> _domain1;

  protected IAbstractDomain<T> _domain2;

  public ProductDomain(IAbstractDomain<U> domain1, IAbstractDomain<T> domain2) {
    this._domain1 = domain1;
    this._domain2 = domain2;
  }

  public IAbstractDomain<U> getDomain1() {
    return _domain1;
  }

  public IAbstractDomain<T> getDomain2() {
    return _domain2;
  }

  @Override
  public Tuple<U, T> bot() {
    return new Tuple<>(_domain1.bot(), _domain2.bot());
  }

  @Override
  public Tuple<U, T> value(
      Configuration conf, RoutingProtocol proto, @Nullable Set<Prefix> prefixes) {
    return new Tuple<>(
        _domain1.value(conf, proto, prefixes), _domain2.value(conf, proto, prefixes));
  }

  @Override
  public Tuple<U, T> transform(Tuple<U, T> input, EdgeTransformer t) {
    return new Tuple<>(
        _domain1.transform(input.getFirst(), t), _domain2.transform(input.getSecond(), t));
  }

  @Override
  public Tuple<U, T> merge(Tuple<U, T> x, Tuple<U, T> y) {
    return new Tuple<>(
        _domain1.merge(x.getFirst(), y.getFirst()), _domain2.merge(x.getSecond(), y.getSecond()));
  }

  @Override
  public Tuple<U, T> selectBest(Tuple<U, T> x) {
    return new Tuple<>(_domain1.selectBest(x.getFirst()), _domain2.selectBest(x.getSecond()));
  }

  @Override
  public Tuple<U, T> aggregate(
      Configuration conf, List<AggregateTransformer> aggregates, Tuple<U, T> x) {
    return new Tuple<>(
        _domain1.aggregate(conf, aggregates, x.getFirst()),
        _domain2.aggregate(conf, aggregates, x.getSecond()));
  }

  @Override
  public List<Route> toRoutes(Tuple<U, T> value) {
    throw new BatfishException("Unimplemented method");
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, Tuple<U, T>> ribs) {
    throw new BatfishException("Unimplemented method");
  }

  @Override
  @Nullable
  public String nextHop(Tuple<U, T> ribs, String node, Flow flow) {
    throw new BatfishException("Unimplemented method");
  }

  @Override
  public String debug(Tuple<U, T> x) {
    String s1 = _domain1.debug(x.getFirst());
    String s2 = _domain2.debug(x.getSecond());
    return "<" + s1 + ", " + s2 + ">";
  }
}
