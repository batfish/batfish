package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nonnull;

/**
 * Immutable IPv6 main RIB used after routing simulation is complete.
 *
 * <p>This is the IPv6 counterpart to {@link FinalMainRib}. It stores only
 * active routes and intentionally omits protocol-RIB state and backup routes.
 */
public final class FinalMainRib6 implements Serializable {

  public static FinalMainRib6 of(
      Iterable<? extends AbstractRoute6> routes) {
    return of(
        StreamSupport.stream(
            routes.spliterator(), false));
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <T extends AbstractRoute6>
      FinalMainRib6 of(T... routes) {
    return of(Arrays.stream(routes));
  }

  public static FinalMainRib6 of(
      Stream<? extends AbstractRoute6> routes) {

    SortedMap<Prefix6, ImmutableSet.Builder<AbstractRoute6>>
        grouped = new TreeMap<>();

    routes.sequential()
        .forEach(
            route ->
                grouped
                    .computeIfAbsent(
                        route.getNetwork(),
                        ignored ->
                            ImmutableSet.builder())
                    .add(route));

    ImmutableSortedMap.Builder<
            Prefix6, Set<AbstractRoute6>>
        builder =
            ImmutableSortedMap.naturalOrder();

    grouped.forEach(
        (prefix, routeBuilder) ->
            builder.put(
                prefix,
                routeBuilder.build()));

    return new FinalMainRib6(builder.build());
  }

  /** Return all active IPv6 routes. */
  public @Nonnull Set<AbstractRoute6> getRoutes() {
    return _routes.values().stream()
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  /** Return active routes for exactly {@code prefix}. */
  public @Nonnull Set<AbstractRoute6> getRoutes(
      Prefix6 prefix) {
    return _routes.getOrDefault(
        prefix, ImmutableSet.of());
  }

  /**
   * Perform IPv6 longest-prefix match.
   *
   * <p>The current IPv6 route foundation uses a linear lookup. This preserves
   * the same behavior in the finalized dataplane until a dedicated IPv6 trie
   * is introduced.
   */
  public @Nonnull Set<AbstractRoute6>
      longestPrefixMatch(Ip6 address) {

    int longestPrefixLength = -1;
    ImmutableSet.Builder<AbstractRoute6>
        matches = ImmutableSet.builder();

    for (Map.Entry<
            Prefix6, Set<AbstractRoute6>>
        entry : _routes.entrySet()) {

      Prefix6 prefix = entry.getKey();

      if (!prefix.contains(address)) {
        continue;
      }

      int prefixLength =
          prefix.getPrefixLength();

      if (prefixLength
          > longestPrefixLength) {
        longestPrefixLength =
            prefixLength;
        matches = ImmutableSet.builder();
        matches.addAll(entry.getValue());
      } else if (
          prefixLength
              == longestPrefixLength) {
        matches.addAll(entry.getValue());
      }
    }

    return matches.build();
  }

  private FinalMainRib6(
      SortedMap<Prefix6, Set<AbstractRoute6>>
          routes) {
    _routes =
        ImmutableSortedMap.copyOf(routes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FinalMainRib6)) {
      return false;
    }
    FinalMainRib6 rhs =
        (FinalMainRib6) o;
    return _routes.equals(rhs._routes);
  }

  @Override
  public int hashCode() {
    return _routes.hashCode();
  }

  private final @Nonnull
      SortedMap<
          Prefix6, Set<AbstractRoute6>>
          _routes;
}
