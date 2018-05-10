package org.batfish.symbolic.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FiniteIndexMap<T> {

  private Map<T, Integer> _indices;

  private Object[] _values;

  public FiniteIndexMap(Set<T> values) {
    _indices = new HashMap<>();
    _values = new Object[values.size()];
    int i = 0;
    for (T value : values) {
      if (!_indices.containsKey(value)) {
        _values[i] = value;
        _indices.put(value, i++);
      }
    }
  }

  public int index(T value) {
    return _indices.get(value);
  }

  @SuppressWarnings("unchecked")
  public T value(int idx) {
    return (T) _values[idx];
  }

  public int size() {
    return _values.length;
  }
}
