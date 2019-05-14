package org.batfish.common.util;

import com.google.common.collect.ImmutableMap;
import java.util.Map;

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

  private CollectionUtil() {} // prevent instantiation
}
