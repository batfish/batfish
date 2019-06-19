package org.batfish.minesweeper.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A simple data structure that maintains a set of sets that partition some initial collection of
 * elements.
 */
public class UnionSplit<T> {

  private Map<T, Integer> _partitionMap;

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
    _partitionMap = new HashMap<>();
    _reverseMap = new HashMap<>();
    createPartition(0, new HashSet<>(values));
  }

  public void split(Set<T> newElements) {
    createPartition(_nextCount, new HashSet<>(newElements));
    _nextCount++;
  }

  public void split(T element) {
    Set<T> elements = new HashSet<>();
    elements.add(element);
    createPartition(_nextCount, elements);
    _nextCount++;
  }

  public Integer getHandle(T element) {
    return _partitionMap.get(element);
  }

  public Set<T> getPartition(int idx) {
    return _reverseMap.get(idx);
  }

  public Collection<Set<T>> partitions() {
    return _reverseMap.values();
  }

  public Set<Integer> handles() {
    return _reverseMap.keySet();
  }

  public Map<T, Integer> getParitionMap() {
    return _partitionMap;
  }

  @Override
  public String toString() {
    return "UnionSplit{" + _reverseMap + '}';
  }
}
