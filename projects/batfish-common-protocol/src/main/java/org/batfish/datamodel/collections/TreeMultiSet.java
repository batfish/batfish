package org.batfish.datamodel.collections;

import java.util.Map;
import java.util.TreeMap;

public class TreeMultiSet<E> extends AbstractMultiSet<E> {

  @Override
  protected <F> MultiSet<F> create() {
    return new TreeMultiSet<>();
  }

  @Override
  protected Map<E, Integer> initializeMap() {
    return new TreeMap<>();
  }
}
