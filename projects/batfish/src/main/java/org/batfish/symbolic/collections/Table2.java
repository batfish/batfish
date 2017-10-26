package org.batfish.symbolic.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import org.batfish.symbolic.utils.TriConsumer;

public class Table2<K1, K2, V> {

  private Map<K1, Map<K2, V>> _map;

  public Table2() {
    _map = new HashMap<>();
  }

  public Map<K2, V> get(K1 key1) {
    return _map.get(key1);
  }

  @Nullable
  public V get(K1 key1, K2 key2) {
    Map<K2, V> inner = _map.get(key1);
    if (inner == null) {
      return null;
    }
    return inner.get(key2);
  }

  public void put(K1 key1, K2 key2, V val) {
    Map<K2, V> inner = _map.computeIfAbsent(key1, k -> new HashMap<>());
    inner.put(key2, val);
  }

  public void put(K1 key1, Map<K2, V> val) {
    _map.put(key1, val);
  }

  public void forEach(TriConsumer<? super K1, ? super K2, ? super V> action) {
    Objects.requireNonNull(action);
    _map.forEach(
        (key1, map) -> {
          map.forEach(
              (key2, v) -> {
                action.accept(key1, key2, v);
              });
        });
  }

  public void forEach(BiConsumer<? super K1, ? super Map<K2, V>> action) {
    _map.forEach(action);
  }
}
