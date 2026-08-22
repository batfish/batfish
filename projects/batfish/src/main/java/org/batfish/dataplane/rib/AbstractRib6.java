package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;

/**
 * Base IPv6 RIB.
 *
 * <p>This implementation is intentionally independent of the existing IPv4
 * {@link AbstractRib} and {@link RibTree}. It provides route preference,
 * backup-route promotion, and IPv6 longest-prefix match. A dedicated IPv6
 * trie can replace the linear LPM implementation later without changing the
 * public route-selection behavior.
 *
 * @param <R> IPv6 route type stored by this RIB
 */
@ParametersAreNonnullByDefault
public abstract class AbstractRib6<R extends AbstractRoute6>
    implements Serializable {

  protected AbstractRib6() {
    _candidates = new TreeMap<>();
    _routes = new TreeMap<>();
  }

  /**
   * Compares route preference.
   *
   * @return a positive value when {@code lhs} is preferred to {@code rhs},
   *     zero when equally preferred, and a negative value otherwise
   */
  public abstract int comparePreference(R lhs, R rhs);

  /** Return all currently active/best routes. */
  public final @Nonnull Set<R> getRoutes() {
    ImmutableSet.Builder<R> routes = ImmutableSet.builder();
    _routes.values().forEach(routes::addAll);
    return routes.build();
  }

  /** Return active/best routes for exactly {@code prefix}. */
  public final @Nonnull Set<R> getRoutes(Prefix6 prefix) {
    Set<R> routes = _routes.get(prefix);
    return routes == null
        ? ImmutableSet.of()
        : ImmutableSet.copyOf(routes);
  }

  /** Return all non-best candidate routes. */
  public final @Nonnull Set<R> getBackupRoutes() {
    ImmutableSet.Builder<R> backups = ImmutableSet.builder();

    _candidates.forEach(
        (prefix, candidates) -> {
          Set<R> active = _routes.get(prefix);
          candidates.stream()
              .filter(
                  route ->
                      active == null || !active.contains(route))
              .forEach(backups::add);
        });

    return backups.build();
  }

  /**
   * Add a route.
   *
   * @return {@code true} iff the set of active routes changed
   */
  public final boolean mergeRoute(R route) {
    Prefix6 prefix = route.getNetwork();

    LinkedHashSet<R> candidates =
        _candidates.computeIfAbsent(
            prefix, ignored -> new LinkedHashSet<>());

    if (!candidates.add(route)) {
      return false;
    }

    Set<R> oldActive = getRoutes(prefix);
    recomputeActiveRoutes(prefix);
    return !oldActive.equals(getRoutes(prefix));
  }

  /**
   * Remove a route.
   *
   * @return {@code true} iff the set of active routes changed
   */
  public final boolean removeRoute(R route) {
    Prefix6 prefix = route.getNetwork();
    LinkedHashSet<R> candidates = _candidates.get(prefix);

    if (candidates == null || !candidates.remove(route)) {
      return false;
    }

    Set<R> oldActive = getRoutes(prefix);

    if (candidates.isEmpty()) {
      _candidates.remove(prefix);
      _routes.remove(prefix);
    } else {
      recomputeActiveRoutes(prefix);
    }

    return !oldActive.equals(getRoutes(prefix));
  }

  /** Remove all routes from this RIB. */
  public final void clear() {
    _candidates.clear();
    _routes.clear();
  }

  /**
   * Perform IPv6 longest-prefix match over active routes.
   *
   * <p>This initial implementation scans active prefixes. Correctness comes
   * first; an IPv6 prefix trie can be introduced once the route model is
   * integrated into the dataplane.
   */
  public final @Nonnull Set<R> longestPrefixMatch(Ip6 address) {
    int longestPrefixLength = -1;
    ImmutableSet.Builder<R> matches = ImmutableSet.builder();

    for (Map.Entry<Prefix6, LinkedHashSet<R>> entry :
        _routes.entrySet()) {
      Prefix6 prefix = entry.getKey();

      if (!prefix.contains(address)) {
        continue;
      }

      int prefixLength = prefix.getPrefixLength();

      if (prefixLength > longestPrefixLength) {
        longestPrefixLength = prefixLength;
        matches = ImmutableSet.builder();
        matches.addAll(entry.getValue());
      } else if (prefixLength == longestPrefixLength) {
        matches.addAll(entry.getValue());
      }
    }

    return matches.build();
  }

  private void recomputeActiveRoutes(Prefix6 prefix) {
    LinkedHashSet<R> candidates = _candidates.get(prefix);

    if (candidates == null || candidates.isEmpty()) {
      _routes.remove(prefix);
      return;
    }

    R best = null;

    for (R route : candidates) {
      if (best == null || comparePreference(route, best) > 0) {
        best = route;
      }
    }

    assert best != null;

    LinkedHashSet<R> active = new LinkedHashSet<>();

    for (R route : candidates) {
      if (comparePreference(route, best) == 0) {
        active.add(route);
      }
    }

    _routes.put(prefix, active);
  }

  /** All candidate routes, including backups. */
  private final @Nonnull
      Map<Prefix6, LinkedHashSet<R>> _candidates;

  /** Currently selected best routes. */
  private final @Nonnull
      Map<Prefix6, LinkedHashSet<R>> _routes;
}
