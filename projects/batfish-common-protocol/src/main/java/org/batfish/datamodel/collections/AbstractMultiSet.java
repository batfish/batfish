package org.batfish.datamodel.collections;

import java.util.Map;
import java.util.Set;

public abstract class AbstractMultiSet<E> implements MultiSet<E> {

  private final Map<E, Integer> _map;

  public AbstractMultiSet() {
    _map = initializeMap();
  }

  @Override
  public final void add(E e) {
    _map.merge(e, 1, (a, b) -> a + b);
  }

  @Override
  public final void add(Set<E> set) {
    for (E e : set) {
      add(e);
    }
  }

  @Override
  public final int count(E e) {
    Integer count = _map.get(e);
    if (count == null) {
      return 0;
    } else {
      return count;
    }
  }

  protected abstract <F> MultiSet<F> create();

  @Override
  public final Set<E> elements() {
    return _map.keySet();
  }

  protected abstract Map<E, Integer> initializeMap();

  @Override
  public MultiSet<Integer> quantityHistogram() {
    MultiSet<Integer> quantityHistogram = this.create();
    for (int i : _map.values()) {
      quantityHistogram.add(i);
    }
    return quantityHistogram;
  }
}
