package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Route;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.utils.Tuple;

public class DelayedDonutDomain<U, T> extends ProductDomain<U, T> {

  private IDomainDifferencer<U, T> _mixer;

  public DelayedDonutDomain(
      IDomainDifferencer<U, T> mixer, IAbstractDomain<U> domain1, IAbstractDomain<T> domain2) {
    super(domain1, domain2);
    _mixer = mixer;
  }

  @Override
  public List<Route> toRoutes(Tuple<U, T> value) {
    U rib1 = value.getFirst();
    T rib2 = value.getSecond();
    T result = _mixer.difference(rib1, rib2);
    return this._domain2.toRoutes(result);
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, Tuple<U, T>> ribs) {
    Map<String, U> newRibs1 = DomainHelper.convertRibs1(ribs);
    Map<String, T> newRibs2 = DomainHelper.convertRibs2(ribs);
    Map<String, T> newRibs = new HashMap<>();
    for (Entry<String, U> e : newRibs1.entrySet()) {
      String router = e.getKey();
      U rib1 = e.getValue();
      T rib2 = newRibs2.get(router);
      T newRib = _mixer.difference(rib1, rib2);
      newRibs.put(router, newRib);
    }
    return _domain2.toFib(newRibs);
  }

  @Override
  @Nullable
  public String nextHop(Tuple<U, T> rib, String node, Flow flow) {
    return _domain2.nextHop(rib.getSecond(), node, flow);
  }

  @Override
  public String debug(Tuple<U, T> x) {
    return _domain2.debug(x.getSecond());
  }
}
