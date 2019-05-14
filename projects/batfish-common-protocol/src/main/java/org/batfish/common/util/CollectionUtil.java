package org.batfish.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.math.IntMath;
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

/** Utility functions for dealing with collections. */
public final class CollectionUtil {
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
          a[0] = a[0] * 31 + Objects.hashCode(o);
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

  /**
   * A collector that returns a hashcode of all the objects in a stream (order-independent).
   *
   * <p>Equivalent to collecting elements into a set and calling {@link Set#hashCode()}
   */
  public static <T> Collector<T, ?, Integer> toUnorderedHashCode() {
    return Collectors.summingInt(Objects::hashCode);
  }

  private CollectionUtil() {} // prevent instantiation
}
