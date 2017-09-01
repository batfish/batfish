package org.batfish.symbolic.abstraction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple data structure that maintains a set of sets that partition
 * some initial collection of elements.
 */

public class UnionSplit<T> {

  private Map<T,Integer> _partitionMap;

  private Map<Integer, Collection<T>> _reverseMap;

  private int _nextCount;

  private void createPartition(Integer i, Collection<T> values) {
    Collection<T> allValues = new ArrayList<>();
    for (T value : values) {
      // Remove the old index if it exists
      Integer oldIdx = _partitionMap.get(value);
      if (oldIdx != null) {
        Collection<T> oldValues = _reverseMap.get(oldIdx);
        oldValues.remove(value);
      }
      // Add the new index for the value
      allValues.add(value);
      _partitionMap.put(value, i);
    }
    _reverseMap.put(i, allValues);
  }

  public UnionSplit(Collection<T> values) {
    _nextCount = 1;
    _partitionMap = new TreeMap<>();
    _reverseMap = new TreeMap<>();
    createPartition(0, values);
  }

  public void split(Collection<T> newElements) {
    createPartition(_nextCount, newElements);
    _nextCount++;
  }

  public Collection<T> getPartition(T element) {
    int idx = _partitionMap.get(element);
    return _reverseMap.get(idx);
  }

  public Collection<Collection<T>> partitions() {
    return _reverseMap.values();
  }

}
