package org.batfish.dataplane.rib;

import com.carrotsearch.hppc.ObjectHashSet;
import com.carrotsearch.hppc.cursors.ObjectCursor;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A multimap from keys to unordered sets of values, sized for the case where most keys have one or
 * a few values.
 *
 * <p>A {@code SetMultimap} backed by a {@code HashSet} per key spends a hash table on every key and
 * a hash node on every value. Here a key's values are the value itself when there is one, a short
 * list up to {@link #MAX_LIST_SIZE}, and an open-addressing set above that, so a key with few
 * values costs little beyond the values and a key with many values costs a few bytes per value.
 *
 * <p>Values must not themselves be {@link ArrayList}s or {@link ObjectHashSet}s.
 */
@ParametersAreNonnullByDefault
final class CompactSetMultimap<K, V> {
  /** The most values a key holds in a list before they move to a set. */
  @VisibleForTesting static final int MAX_LIST_SIZE = 8;

  private final Map<K, Object> _map = new HashMap<>();

  /** Adds {@code value} under {@code key}. Returns whether it was not already there. */
  @SuppressWarnings("unchecked")
  boolean put(K key, V value) {
    Object existing = _map.get(key);
    if (existing == null) {
      _map.put(key, value);
      return true;
    }
    if (existing instanceof ObjectHashSet) {
      return ((ObjectHashSet<V>) existing).add(value);
    }
    if (existing instanceof ArrayList) {
      ArrayList<V> list = (ArrayList<V>) existing;
      if (list.contains(value)) {
        return false;
      }
      if (list.size() < MAX_LIST_SIZE) {
        list.add(value);
      } else {
        ObjectHashSet<V> set = new ObjectHashSet<>(list.size() + 1);
        for (V v : list) {
          set.add(v);
        }
        set.add(value);
        _map.put(key, set);
      }
      return true;
    }
    if (existing.equals(value)) {
      return false;
    }
    ArrayList<V> list = new ArrayList<>(2);
    list.add((V) existing);
    list.add(value);
    _map.put(key, list);
    return true;
  }

  /** Removes {@code value} from {@code key}. Returns whether it was there. */
  @SuppressWarnings("unchecked")
  boolean remove(K key, V value) {
    Object existing = _map.get(key);
    if (existing == null) {
      return false;
    }
    if (existing instanceof ObjectHashSet) {
      ObjectHashSet<V> set = (ObjectHashSet<V>) existing;
      if (!set.remove(value)) {
        return false;
      }
      if (set.size() <= MAX_LIST_SIZE / 2) {
        // Well under the list limit, so a few more additions do not bring the set right back.
        ArrayList<V> list = new ArrayList<>(set.size());
        for (ObjectCursor<V> c : set) {
          list.add(c.value);
        }
        _map.put(key, list.size() == 1 ? list.get(0) : list);
      }
      return true;
    }
    if (existing instanceof ArrayList) {
      ArrayList<V> list = (ArrayList<V>) existing;
      if (!list.remove(value)) {
        return false;
      }
      if (list.isEmpty()) {
        _map.remove(key);
      } else if (list.size() == 1) {
        _map.put(key, list.get(0));
      }
      return true;
    }
    if (!existing.equals(value)) {
      return false;
    }
    _map.remove(key);
    return true;
  }

  /** Whether any value is stored under {@code key}. */
  boolean containsKey(K key) {
    return _map.containsKey(key);
  }

  /**
   * The values stored under {@code key}, as a read-only view that is valid until this multimap is
   * next modified. Empty if there are none.
   */
  @SuppressWarnings("unchecked")
  @Nonnull
  Collection<V> get(K key) {
    Object existing = _map.get(key);
    if (existing == null) {
      return ImmutableSet.of();
    }
    if (existing instanceof ObjectHashSet) {
      return new SetView<>((ObjectHashSet<V>) existing);
    }
    if (existing instanceof ArrayList) {
      return Collections.unmodifiableList((ArrayList<V>) existing);
    }
    return ImmutableSet.of((V) existing);
  }

  /** Read-only {@link Collection} view of an {@link ObjectHashSet}. */
  private static final class SetView<V> extends AbstractCollection<V> {
    private final ObjectHashSet<V> _set;

    SetView(ObjectHashSet<V> set) {
      _set = set;
    }

    @Override
    public Iterator<V> iterator() {
      return Iterators.transform(_set.iterator(), c -> c.value);
    }

    @Override
    public int size() {
      return _set.size();
    }
  }
}
