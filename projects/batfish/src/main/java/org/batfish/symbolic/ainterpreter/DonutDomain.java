package org.batfish.symbolic.ainterpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.javabdd.BDD;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Route;
import org.batfish.symbolic.bdd.BDDNetFactory;
import org.batfish.symbolic.utils.Tuple;

public class DonutDomain<U, T> extends ProductDomain<U, T> {

  private IDomainMixable<U, T> _mixer;

  public DonutDomain(
      IDomainMixable<U, T> mixer, IAbstractDomain<U> domain1, IAbstractDomain<T> domain2) {
    super(domain1, domain2);
    _mixer = mixer;
  }

  @Override
  public List<Route> toRoutes(AbstractRib<Tuple<U, T>> value) {
    AbstractRib<U> rib1 = convertRib1(value);
    AbstractRib<T> rib2 = convertRib2(value);
    AbstractRib<T> result = _mixer.difference(rib1, rib2);
    return this._domain2.toRoutes(result);
  }

  @Override
  public Tuple<BDDNetFactory, BDD> toFib(Map<String, AbstractRib<Tuple<U, T>>> ribs) {
    Map<String, AbstractRib<U>> newRibs1 = convertRibs1(ribs);
    Map<String, AbstractRib<T>> newRibs2 = convertRibs2(ribs);
    Map<String, AbstractRib<T>> newRibs = new HashMap<>();
    for (Entry<String, AbstractRib<U>> e : newRibs1.entrySet()) {
      String router = e.getKey();
      AbstractRib<U> rib1 = e.getValue();
      AbstractRib<T> rib2 = newRibs2.get(router);
      AbstractRib<T> newRib = _mixer.difference(rib1, rib2);
      newRibs.put(router, newRib);
    }
    return _domain2.toFib(newRibs);
  }

  // TODO: take away reachable
  @Override
  public boolean reachable(
      Map<String, AbstractRib<Tuple<U, T>>> ribs, String src, String dst, Flow flow) {
    Map<String, AbstractRib<T>> newRibs2 = convertRibs2(ribs);
    /* Map<String, AbstractRib<T>> newRibs2 = convertRibs2(ribs);
    Map<String, AbstractRib<T>> difference = new HashMap<>();
    for (Entry<String, AbstractRib<U>> e : newRibs1.entrySet()) {
      String router = e.getKey();
      AbstractRib<U> rib1 = e.getValue();
      AbstractRib<T> rib2 = newRibs2.get(router);
      AbstractRib<T> diff = _mixer.di
    } */
    return _domain2.reachable(newRibs2, src, dst, flow);
  }

  private AbstractRib<U> convertRib1(AbstractRib<Tuple<U, T>> rib) {
    return new AbstractRib<>(
        rib.getBgpRib().getFirst(),
        rib.getOspfRib().getFirst(),
        rib.getStaticRib().getFirst(),
        rib.getConnectedRib().getFirst(),
        rib.getMainRib().getFirst());
  }

  private AbstractRib<T> convertRib2(AbstractRib<Tuple<U, T>> rib) {
    return new AbstractRib<>(
        rib.getBgpRib().getSecond(),
        rib.getOspfRib().getSecond(),
        rib.getStaticRib().getSecond(),
        rib.getConnectedRib().getSecond(),
        rib.getMainRib().getSecond());
  }

  private Map<String, AbstractRib<U>> convertRibs1(Map<String, AbstractRib<Tuple<U, T>>> ribs) {
    Map<String, AbstractRib<U>> acc = new HashMap<>();
    for (Entry<String, AbstractRib<Tuple<U, T>>> e : ribs.entrySet()) {
      AbstractRib<Tuple<U, T>> value = e.getValue();
      AbstractRib<U> rib =
          new AbstractRib<>(
              value.getBgpRib().getFirst(),
              value.getOspfRib().getFirst(),
              value.getStaticRib().getFirst(),
              value.getConnectedRib().getFirst(),
              value.getMainRib().getFirst());
      acc.put(e.getKey(), rib);
    }
    return acc;
  }

  private Map<String, AbstractRib<T>> convertRibs2(Map<String, AbstractRib<Tuple<U, T>>> ribs) {
    Map<String, AbstractRib<T>> acc = new HashMap<>();
    for (Entry<String, AbstractRib<Tuple<U, T>>> e : ribs.entrySet()) {
      AbstractRib<Tuple<U, T>> value = e.getValue();
      AbstractRib<T> rib =
          new AbstractRib<>(
              value.getBgpRib().getSecond(),
              value.getOspfRib().getSecond(),
              value.getStaticRib().getSecond(),
              value.getConnectedRib().getSecond(),
              value.getMainRib().getSecond());
      acc.put(e.getKey(), rib);
    }
    return acc;
  }
}
