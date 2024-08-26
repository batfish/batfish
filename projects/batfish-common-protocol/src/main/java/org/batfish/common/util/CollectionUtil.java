package org.batfish.common.util;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.math.IntMath;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Utility functions for dealing with collections. */
public final class CollectionUtil {
  private static final Logger LOGGER = LogManager.getLogger(CollectionUtil.class);

  /**
   * Create a new {@link com.google.common.collect.ImmutableMap} that contains the given map plus
   * one new entry.
   *
   * <p>This is very similar to building a new map, except it guards against an annoying crash in
   * ImmutableMap with duplicate keys.
   */
  public static <K, V> ImmutableMap<K, V> copyMapAndAdd(Map<K, V> map, K newKey, V newValue) {
    ImmutableMap.Builder<K, V> ret = ImmutableMap.builderWithExpectedSize(map.size() + 1);
    for (Map.Entry<K, V> entry : map.entrySet()) {
      if (!entry.getKey().equals(newKey)) {
        ret.put(entry.getKey(), entry.getValue());
      }
    }
    ret.put(newKey, newValue);
    return ret.build();
  }

  /**
   * Create a new {@link com.google.common.collect.ImmutableMap} from the given {@link Map}, unless
   * that map is already an immutable map.
   *
   * <p>The only expected difference from {@link ImmutableMap#copyOf(Map)} is when map is an
   * instance of {@link com.google.common.collect.ImmutableSortedMap}. For sorted maps, the {@link
   * ImmutableMap#copyOf(Map)} implementation is a full copy to protect against a comparator that is
   * not consistent with equals. In contrast, this function will be a no-op.
   */
  public static <K, V> Map<K, V> toImmutableMap(Map<K, V> map) {
    if (map instanceof ImmutableMap) {
      return map;
    }
    return ImmutableMap.copyOf(map);
  }

  public static <K1, K2, V1, V2> Map<K2, V2> toImmutableMap(
      Map<K1, V1> map,
      Function<Entry<K1, V1>, K2> keyFunction,
      Function<Entry<K1, V1>, V2> valueFunction) {
    return map.entrySet().stream().collect(ImmutableMap.toImmutableMap(keyFunction, valueFunction));
  }

  public static <K1, K2, V> Map<K2, V> toMap(
      Set<K1> set, Function<K1, K2> keyFunction, Function<K1, V> valueFunction) {
    return set.stream().collect(Collectors.toMap(keyFunction, valueFunction));
  }

  public static <E, K, V> Map<K, V> toImmutableMap(
      Collection<E> set, Function<E, K> keyFunction, Function<E, V> valueFunction) {
    return set.stream().collect(ImmutableMap.toImmutableMap(keyFunction, valueFunction));
  }

  public static <K1, K2 extends Comparable<? super K2>, V1, V2>
      SortedMap<K2, V2> toImmutableSortedMap(
          Map<K1, V1> map,
          Function<Entry<K1, V1>, K2> keyFunction,
          Function<Entry<K1, V1>, V2> valueFunction) {
    return map.entrySet().stream()
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Comparator.naturalOrder(), keyFunction, valueFunction));
  }

  public static <T, K extends Comparable<? super K>, V>
      Collector<T, ?, ImmutableSortedMap<K, V>> toImmutableSortedMap(
          Function<? super T, ? extends K> keyFunction,
          Function<? super T, ? extends V> valueFunction) {
    return ImmutableSortedMap.toImmutableSortedMap(
        Comparator.naturalOrder(), keyFunction, valueFunction);
  }

  public static <E, K extends Comparable<? super K>, V> NavigableMap<K, V> toImmutableSortedMap(
      Collection<E> set, Function<E, K> keyFunction, Function<E, V> valueFunction) {
    return set.stream()
        .collect(
            ImmutableSortedMap.toImmutableSortedMap(
                Comparator.naturalOrder(), keyFunction, valueFunction));
  }

  public static <T, U> List<U> toArrayList(Collection<T> ts, Function<T, U> f) {
    List<U> us = new ArrayList<>(ts.size());
    for (T t : ts) {
      us.add(f.apply(t));
    }
    return us;
  }

  /**
   * A collector that returns a hashcode of all the objects in a stream (order-dependent).
   *
   * <p>Equivalent to to collecting elements into a list and calling {@link List#hashCode()}
   */
  public static <T> Collector<T, ?, Integer> toOrderedHashCode() {
    // See https://stackoverflow.com/a/39396614 for mode detail
    return Collector.of(
        // Initial state: [0] - current hashcode, [1] - number of elements encountered
        () -> new int[2],
        // accumulator: single element added to hashcode
        (a, o) -> {
          checkArgument(!(o instanceof Stream), "Cannot hash a stream, check the caller");
          int hash = Objects.hashCode(o);
          if (o != null && hash == System.identityHashCode(o)) {
            // Not an assert or a check, since collisions can happen.
            LOGGER.warn(
                "Hashing an object with identityHashCode usually means that object does not"
                    + " implement hashCode: {}",
                o.getClass());
          }
          a[0] = a[0] * 31 + hash;
          a[1]++;
        },
        // combiner: merge two hashcodes
        (a1, a2) -> {
          a1[0] = a1[0] * IntMath.pow(31, a2[1]) + a2[0];
          a1[1] += a2[1];
          return a1;
        },
        // finisher: collapse the state to a single int
        a -> IntMath.pow(31, a[1]) + a[0]);
  }

  /** Return the max values of the input collection according to the input comparator. */
  public static <R> Collection<R> maxValues(Collection<R> values, Comparator<R> comparator) {
    List<R> maxValues = new ArrayList<>();
    for (R value : values) {
      if (maxValues.isEmpty()) {
        maxValues.add(value);
        continue;
      }
      int cmp = comparator.compare(value, maxValues.get(0));
      if (cmp == 0) {
        maxValues.add(value);
      } else if (cmp > 0) {
        maxValues.clear();
        maxValues.add(value);
      }
    }
    return maxValues;
  }

  private CollectionUtil() {} // prevent instantiation
}
