package org.batfish.datamodel.collections;

import java.util.Set;

public interface MultiSet<E> {

  void add(E e);

  void add(Set<E> set);

  int count(E e);

  Set<E> elements();

  MultiSet<Integer> quantityHistogram();
}
