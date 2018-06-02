package org.batfish.symbolic.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.batfish.symbolic.utils.QuadConsumer;

public class Table3<K1, K2, K3, V> {

  private Table2<K1, K2, Map<K3, V>> _map;

  public Table3() {
    _map = new Table2<>();
  }

  @Nullable
  public V get(K1 key1, K2 key2, K3 key3) {
    Map<K3, V> inner = _map.get(key1, key2);
    if (inner == null) {
      return null;
    }
    return inner.get(key3);
  }

  public Map<K2, Map<K3, V>> get(K1 key1) {
    return _map.get(key1);
  }

  public void put(K1 key1, K2 key2, K3 key3, V val) {
    Map<K3, V> inner = _map.get(key1, key2);
    if (inner == null) {
      inner = new HashMap<>();
      _map.put(key1, key2, inner);
    }
    inner.put(key3, val);
  }

  public void put(K1 key1, Map<K2, Map<K3, V>> val) {
    _map.put(key1, val);
  }

  public void forEach(QuadConsumer<? super K1, ? super K2, ? super K3, ? super V> action) {
    Objects.requireNonNull(action);
    _map.forEach(
        (key1, map) -> {
          map.forEach(
              (key2, map2) -> {
                map2.forEach(
                    (key3, v) -> {
                      action.accept(key1, key2, key3, v);
                    });
              });
        });
  }
}
