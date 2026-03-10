package org.batfish.representation.juniper;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

/** Utility to convert a JunOS group wildcard into a Java regex. */
public final class GroupWildcard {
  private static final LoadingCache<String, String> CACHE =
      Caffeine.newBuilder()
          .maximumSize(1_000_000)
          .build(org.batfish.representation.juniper.parboiled.GroupWildcard::toJavaRegex);

  public static String toJavaRegex(String wildcard) {
    return CACHE.get(wildcard);
  }

  private GroupWildcard() {} // prevent instantiation of utility class.
}
