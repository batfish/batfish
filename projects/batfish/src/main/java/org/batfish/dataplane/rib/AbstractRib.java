package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Implements general RIB (Routing Information Base) semantics. RIB stores routes for different
 * network prefixes and supports retrieving them based on the longest prefix match between a given
 * IP address and the route's IP prefix.
 *
 * @param <R> Type of route that this RIB will be storing. Required for properly comparing route
 *     preferences.
 */
@ParametersAreNonnullByDefault
public abstract class AbstractRib<R> implements GenericRib<R> {

  private static final long serialVersionUID = 1L;

  /**
   * This logical clock helps us keep track when routes were merged into the RIB to determine their
   * age. It's incremented each time a route is merged into the RIB.
   */
  protected long _logicalClock;

  /** Map to keep track when routes were merged in. */
  protected Map<R, Long> _logicalArrivalTime;

  /** Root of our prefix trie */
  private RibTree<R> _tree;

  /** Memoized set of all routes in this RIB */
  @Nullable private Set<R> _allRoutes;

  /**
   * Keep a Sorted Set of alternative routes. Used to update the RIB if best routes are withdrawn
   */
  @Nullable protected final Map<Prefix, SortedSet<R>> _backupRoutes;

  public AbstractRib(
      @Nullable Map<Prefix, SortedSet<R>> backupRoutes, Function<R, AbstractRoute> routeExtractor) {
    _allRoutes = ImmutableSet.of();
    _backupRoutes = backupRoutes;
    _logicalArrivalTime = new HashMap<>();
    _logicalClock = 0;
    _tree = new RibTree<>(this, routeExtractor);
  }

  /**
   * Import routes into this RIB from another
   *
   * @param exportingRib the RIB from which to import routes
   * @param <T> type of routes in exportingRib (must be more specific than {@link R})
   * @return a {@link RibDelta}
   */
  @Nonnull
  public <T extends R> RibDelta<R> importRoutesFrom(AbstractRib<T> exportingRib) {
    RibDelta.Builder<R> builder = RibDelta.builder(this::getNetwork);
    for (T route : exportingRib.getRoutes()) {
      builder.from(mergeRouteGetDelta(route));
    }
    return builder.build();
  }

  /**
   * Add route to backup route map if the route map is not null
   *
   * @param route Route to add
   */
  public final void addBackupRoute(R route) {
    if (_backupRoutes != null) {
      _backupRoutes.computeIfAbsent(getNetwork(route), k -> new TreeSet<>()).add(route);
    }
  }

  public final boolean containsRoute(R route) {
    return _tree.containsRoute(route);
  }

  @Override
  public final SortedSet<Prefix> getPrefixes() {
    SortedSet<Prefix> prefixes = new TreeSet<>();
    Set<R> routes = getRoutes();
    for (R route : routes) {
      prefixes.add(getNetwork(route));
    }
    return prefixes;
  }

  @Override
  public Set<R> getRoutes() {
    if (_allRoutes == null) {
      _allRoutes = ImmutableSet.copyOf(_tree.getRoutes());
    }
    return _allRoutes;
  }

  public final Set<R> getRoutes(Prefix p) {
    // Collect routes that match the prefix
    return getRoutes().stream()
        .filter(r -> getNetwork(r).equals(p))
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Remove a route from backup route map if it was present and backup route map exists
   *
   * @param route Route to remove
   */
  public final void removeBackupRoute(R route) {
    if (_backupRoutes != null) {
      SortedSet<R> routes = _backupRoutes.get(getNetwork(route));
      if (routes != null) {
        routes.remove(route);
      }
    }
  }

  @Override
  public abstract int comparePreference(R lhs, R rhs);

  @Override
  public @Nonnull Set<R> longestPrefixMatch(Ip address) {
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH);
  }

  @Override
  public @Nonnull Set<R> longestPrefixMatch(Ip address, int maxPrefixLength) {
    return _tree.getLongestPrefixMatch(address, maxPrefixLength);
  }

  /**
   * Add a new route to the RIB.
   *
   * @param route the route to add
   * @return {@link RibDelta} if the route was added. {@code null} if the route already existed or
   *     was discarded due to preference comparisons.
   */
  @Nonnull
  public RibDelta<R> mergeRouteGetDelta(R route) {
    RibDelta<R> delta = _tree.mergeRoute(route);
    if (!delta.isEmpty()) {
      // A change to routes has been made
      _allRoutes = null;
      _logicalArrivalTime.put(route, _logicalClock);
      _logicalClock++;
    }
    return delta;
  }

  /**
   * Add a new route to the RIB.
   *
   * @param route the route to add
   * @return true if the route was added. False if the route already existed or was discarded due to
   *     preference comparisons.
   */
  @Override
  public boolean mergeRoute(R route) {
    return !mergeRouteGetDelta(route).isEmpty();
  }

  /**
   * Remove given route from the RIB
   *
   * @param route route to remove
   * @param reason The reason for route removal (will propagate to the returned delta)
   * @return a {@link RibDelta} object indicating that the route was removed or @{code null} if the
   *     route was not present in the RIB
   */
  @Nonnull
  public RibDelta<R> removeRouteGetDelta(R route, Reason reason) {
    RibDelta<R> delta = _tree.removeRouteGetDelta(route, reason);
    if (!delta.isEmpty()) {
      // A change to routes has been made
      _allRoutes = null;
      delta
          .getActions()
          .forEach(
              a -> {
                if (a.isWithdrawn()) {
                  _logicalArrivalTime.remove(a.getRoute());
                }
              });
    }
    return delta;
  }

  @Nonnull
  public RibDelta<R> removeRouteGetDelta(R route) {
    return removeRouteGetDelta(route, Reason.WITHDRAW);
  }

  /**
   * Remove given route from the RIB
   *
   * @param route route to remove
   * @return True if the route was located and removed
   */
  public boolean removeRoute(R route) {
    return !removeRouteGetDelta(route, Reason.WITHDRAW).isEmpty();
  }

  /**
   * Extract routes stored for this exact prefix, if any.
   *
   * <p>Does not collect routes for any other prefixes. Does not alter memoized routes.
   */
  protected Set<R> extractRoutes(Prefix prefix) {
    return _tree.getRoutes(prefix);
  }

  /**
   * Check if two RIBs have exactly same sets of routes.
   *
   * <p>Designed to be faster (in an average case) than doing two calls to {@link #getRoutes} and
   * then testing the sets for equality.
   *
   * @param other the other RIB
   * @return True if both ribs contain identical routes
   */
  @Override
  public boolean equals(@Nullable Object other) {
    return (this == other)
        || (other instanceof AbstractRib<?> && _tree.equals(((AbstractRib<?>) other)._tree));
  }

  @Override
  public int hashCode() {
    return Objects.hash(_tree);
  }

  @Override
  public final Map<Prefix, IpSpace> getMatchingIps() {
    return _tree.getMatchingIps();
  }

  @Override
  public final IpSpace getRoutableIps() {
    return _tree.getRoutableIps();
  }
}
