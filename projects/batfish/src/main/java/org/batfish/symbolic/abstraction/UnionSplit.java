package org.batfish.symbolic.abstraction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A simple data structure that maintains a set of sets that partition
 * some initial collection of elements.
 */

public class UnionSplit<T> {

  private Map<T,Integer> _partitionMap;

  private Map<Integer, Set<T>> _reverseMap;

  private int _nextCount;

  private void createPartition(Integer i, Set<T> values) {
    for (T value : values) {
      // Remove the old index if it exists
      Integer oldIdx = _partitionMap.get(value);
      if (oldIdx != null) {
        Set<T> oldValues = _reverseMap.get(oldIdx);
        oldValues.remove(value);
        if (oldValues.isEmpty()) {
          _reverseMap.remove(oldIdx);
        }
      }
      // Add the new index for the value
      _partitionMap.put(value, i);
    }
    _reverseMap.put(i, values);
  }

  public UnionSplit(Set<T> values) {
    _nextCount = 1;
    _partitionMap = new TreeMap<>();
    _reverseMap = new TreeMap<>();
    createPartition(0, new HashSet<>(values));
  }

  public void split(Set<T> newElements) {
    createPartition(_nextCount, new HashSet<>(newElements));
    _nextCount++;
  }

  public Set<T> getPartition(T element) {
    int idx = _partitionMap.get(element);
    return _reverseMap.get(idx);
  }

  public Set<Set<T>> partitions() {
    return new HashSet<>(_reverseMap.values());
  }

  @Override public String toString() {
    return "UnionSplit{" + _reverseMap + '}';
  }
}
