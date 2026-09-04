package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.AbstractRouteDecorator;
import org.batfish.datamodel.AnnotatedRoute;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.ResolutionRestriction;
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
public abstract class AbstractRib<R extends AbstractRouteDecorator> implements GenericRib<R> {

  /** Root of our prefix trie */
  private final RibTree<R> _tree;

  /** Memoized set of all routes in this RIB */
  private transient @Nullable Set<R> _allRoutes;

  /**
   * Routes this RIB was offered that are not currently in {@link #_tree}, because a preferred route
   * for the same prefix is. Used to update the RIB if best routes are withdrawn. Most prefixes are
   * only ever offered the routes they hold, so this is far smaller than the RIB.
   */
  protected final @Nullable BackupRoutes<R> _backupRoutes;

  protected AbstractRib(boolean withBackupRoutes) {
    _allRoutes = ImmutableSet.of();
    _backupRoutes = withBackupRoutes ? new BackupRoutes<>() : null;
    _tree = new RibTree<>(this);
  }

  /** Create an AbstractRib without backup routes */
  protected AbstractRib() {
    this(false);
  }

  /**
   * Import routes from one unannotated RIB into another
   *
   * @param importingRib the RIB that imports routes
   * @param exportingRib the RIB that exports routes
   * @param <U> type of {@link AbstractRoute} in importing RIB
   * @param <T> type of {@link AbstractRoute} in exporting RIB; must extend {@code U}
   */
  public static @Nonnull <U extends AbstractRoute, T extends U> RibDelta<U> importRib(
      AbstractRib<U> importingRib, AbstractRib<T> exportingRib) {
    RibDelta.Builder<U> builder = RibDelta.builder();
    exportingRib.getRoutes().forEach(r -> builder.from(importingRib.mergeRouteGetDelta(r)));
    return builder.build();
  }

  /**
   * Import routes from an unannotated RIB into an annotated RIB
   *
   * @param importingRib the RIB that imports routes
   * @param exportingRib the RIB that exports routes
   * @param vrfName Name of source VRF to put in route annotations
   * @param <U> type of {@link AbstractRoute} in importing RIB
   * @param <T> type of {@link AbstractRoute} in exporting RIB; must extend {@code U}
   */
  public static @Nonnull <U extends AbstractRoute, T extends U>
      RibDelta<AnnotatedRoute<U>> importRib(
          AnnotatedRib<U> importingRib, AbstractRib<T> exportingRib, String vrfName) {
    RibDelta.Builder<AnnotatedRoute<U>> builder = RibDelta.builder();
    exportingRib
        .getRoutes()
        .forEach(
            r -> builder.from(importingRib.mergeRouteGetDelta(new AnnotatedRoute<>(r, vrfName))));
    return builder.build();
  }

  /**
   * Import routes from one annotated RIB into another
   *
   * @param importingRib the RIB that imports routes
   * @param exportingRib the RIB that exports routes
   * @param <U> type of {@link AbstractRoute} in importing RIB
   * @param <T> type of {@link AbstractRoute} in exporting RIB; must extend {@code U}
   * @return a {@link RibDelta}
   */
  public static @Nonnull <U extends AbstractRoute, T extends U>
      RibDelta<AnnotatedRoute<U>> importRib(
          AnnotatedRib<U> importingRib, AnnotatedRib<T> exportingRib) {
    RibDelta.Builder<AnnotatedRoute<U>> builder = RibDelta.builder();
    exportingRib
        .getRoutes()
        .forEach(
            r ->
                builder.from(
                    importingRib.mergeRouteGetDelta(
                        new AnnotatedRoute<>(r.getRoute(), r.getSourceVrf()))));
    return builder.build();
  }

  /** Clear all routes from the RIB */
  public final void clear() {
    _tree.clear();
    _allRoutes = null;
  }

  @Override
  @SuppressWarnings("unchecked")
  public final boolean containsRoute(AbstractRouteDecorator route) {
    // TODO: FIX this casting bullshit
    try {
      return _tree.containsRoute((R) route);
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public @Nonnull Set<AbstractRoute> getUnannotatedRoutes() {
    return getRoutes().stream()
        .map(AbstractRouteDecorator::getAbstractRoute)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Extract routes stored for this exact prefix, if any.
   *
   * <p>Does not collect routes for any other prefixes. Does not alter memoized routes.
   */
  @Override
  public @Nonnull Set<R> getRoutes(Prefix prefix) {
    return _tree.getRoutes(prefix);
  }

  @Override
  public final @Nonnull Set<R> getRoutes() {
    if (_allRoutes == null) {
      _allRoutes = computeRoutes();
    }
    return _allRoutes;
  }

  protected @Nonnull Set<R> computeRoutes() {
    return ImmutableSet.copyOf(_tree.getRoutes());
  }

  /**
   * Every route this RIB was offered and has not been asked to remove: every route it holds, before
   * any filtering a subclass applies in {@link #getRoutes()}, plus the backups that lost to them.
   * Empty if this RIB keeps no backups.
   */
  @Override
  public @Nonnull Set<R> getBackupRoutes() {
    if (_backupRoutes == null) {
      return ImmutableSet.of();
    }
    return ImmutableSet.<R>builder()
        .addAll(_tree.getRoutes())
        .addAll(_backupRoutes.values())
        .build();
  }

  /**
   * Every route this RIB was offered for {@code prefix} and has not been asked to remove: every
   * route it holds for it, before any filtering a subclass applies in {@link #getRoutes(Prefix)},
   * plus the backups that lost to them.
   */
  protected final @Nonnull Set<R> getRoutesAndBackups(Prefix prefix) {
    Set<R> routes = _tree.getRoutes(prefix);
    if (_backupRoutes == null) {
      return routes;
    }
    Set<R> backups = _backupRoutes.get(prefix);
    return backups.isEmpty() ? routes : Sets.union(routes, backups);
  }

  @Override
  public abstract int comparePreference(R lhs, R rhs);

  @Override
  public @Nonnull Set<R> longestPrefixMatch(Ip address, ResolutionRestriction<R> restriction) {
    return longestPrefixMatch(address, Prefix.MAX_PREFIX_LENGTH, restriction);
  }

  @Override
  public @Nonnull Set<R> longestPrefixMatch(
      Ip address, int maxPrefixLength, ResolutionRestriction<R> restriction) {
    return _tree.getLongestPrefixMatch(address, maxPrefixLength, restriction);
  }

  /**
   * Add a new route to the RIB.
   *
   * @param route the route to add
   * @return {@link RibDelta} with the route if it was added, or empty if the route already existed
   *     or was discarded due to preference comparisons.
   */
  public @Nonnull RibDelta<R> mergeRouteGetDelta(R route) {
    RibDelta<R> delta = _tree.mergeRoute(route);
    if (delta.isEmpty()) {
      if (_backupRoutes != null && !_tree.containsRoute(route)) {
        // Lost to a preferred route for its prefix: keep it in case that route is withdrawn.
        _backupRoutes.put(route.getNetwork(), route);
      }
      return delta;
    }
    if (_backupRoutes != null) {
      for (RouteAdvertisement<R> action : delta.getActions()) {
        if (action.getReason() == Reason.REPLACE) {
          // Displaced by the new route: now a backup.
          _backupRoutes.put(action.getRoute().getNetwork(), action.getRoute());
        }
      }
    }
    // A change to routes has been made
    _allRoutes = null;
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
   * @return a {@link RibDelta} object indicating that the route was removed or @{code null} if the
   *     route was not present in the RIB
   */
  public @Nonnull RibDelta<R> removeRouteGetDelta(R route) {
    RibDelta<R> delta = _tree.removeRouteGetDelta(route, Reason.WITHDRAW);
    if (delta.isEmpty()) {
      if (_backupRoutes != null) {
        // Not held, so if it was offered it is a backup.
        _backupRoutes.remove(route.getNetwork(), route);
      }
      return delta;
    }
    if (_backupRoutes != null) {
      for (RouteAdvertisement<R> action : delta.getActions()) {
        if (!action.isWithdrawn()) {
          // Promoted from backup to replace the removed route.
          _backupRoutes.remove(action.getRoute().getNetwork(), action.getRoute());
        }
      }
    }
    // A change to routes has been made
    _allRoutes = null;
    return delta;
  }

  /**
   * Remove given route from the RIB
   *
   * @param route route to remove
   * @return True if the route was located and removed
   */
  public boolean removeRoute(R route) {
    return !removeRouteGetDelta(route).isEmpty();
  }

  @Override
  public boolean intersectsPrefixSpace(PrefixSpace prefixSpace) {
    return _tree.intersectsPrefixSpace(prefixSpace);
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
    return Objects.hashCode(_tree);
  }
}
