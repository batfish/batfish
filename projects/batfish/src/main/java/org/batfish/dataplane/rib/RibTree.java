package org.batfish.dataplane.rib;

import static org.batfish.dataplane.rib.RouteAdvertisement.Reason.REPLACE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.GenericRib;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Used to store the routes, supports longest prefix match operation.
 *
 * <p>This data structure is a more restrictive version of a ddNF (disjoint difference Normal Form),
 * where the wildcard symbols can appear only after (to-the-right-of) non wildcard symbols in the
 * bit vector. E.g., 101010**, but not 1*001***
 */
@ParametersAreNonnullByDefault
final class RibTree<R> implements Serializable {

  private static final long serialVersionUID = 1L;

  @Nonnull private final PrefixTrieMultiMap<R> _root;
  @Nonnull private final AbstractRib<R> _owner;

  RibTree(AbstractRib<R> owner) {
    _root = new PrefixTrieMultiMap<>(Prefix.ZERO);
    _owner = owner;
  }

  /**
   * Remove a single route from the RIB, if it exists
   *
   * @param p {@link Prefix} representing destination network of route to remove
   * @param route route to remove
   * @return {@link RibDelta} if the route was removed, otherwise {@code null};
   */
  @Nonnull
  RibDelta<R> removeRouteGetDelta(Prefix p, R route, Reason reason) {
    boolean removed = _root.remove(p, route);
    if (!removed) {
      return RibDelta.empty();
    }

    Builder<R> b = RibDelta.builder();
    b.remove(p, route, reason);
    if (_root.get(p).isEmpty() && _owner._backupRoutes != null) {
      SortedSet<? extends R> backups =
          _owner._backupRoutes.getOrDefault(p, ImmutableSortedSet.of());
      if (backups.isEmpty()) {
        return b.build();
      }
      _root.put(p, backups.first());
      b.add(p, backups.first());
    }
    // Return new delta
    return b.build();
  }

  /**
   * Check if the route is present in the RIB
   *
   * @param p {@link Prefix} representing destination network of route to check for
   * @param route route to find
   * @return true if the route exists in the RIB
   */
  boolean containsRoute(Prefix p, R route) {
    return _root.get(p).contains(route);
  }

  /**
   * Returns a set of routes in this tree that 1) match the given IP address, and 2) have the
   * longest prefix length within the specified maximum. May include non-forwarding routes.
   *
   * <p>Returns the empty set if there are no routes that match.
   */
  @Nonnull
  Set<R> getLongestPrefixMatch(Ip address, int maxPrefixLength) {
    return ImmutableSet.copyOf(_root.longestPrefixMatch(address, maxPrefixLength));
  }

  /**
   * Return a set of all routes contained in this RIB
   *
   * @return a {@link Set} of routes
   */
  public Set<R> getRoutes() {
    return _root.getAllElements();
  }

  /** Retrieve stored routes for a particular prefix only. */
  public Set<R> getRoutes(Prefix prefix) {
    return _root.get(prefix);
  }

  /**
   * Add a new route into the RIB, potentially replacing other routes
   *
   * @param p {@link Prefix} representing destination network of route to add
   * @param route route to add
   * @return a {@link RibDelta} objects indicating which routes where added and evicted from this
   *     RIB
   */
  @Nonnull
  RibDelta<R> mergeRoute(Prefix p, R route) {
    Set<R> routes = _root.get(p);
    if (routes.isEmpty()) {
      _root.put(p, route);
      return RibDelta.<R>builder().add(p, route).build();
    }
    /*
     * Check if the route we are adding is preferred to the routes we already have.
     * We only need to compare to one route, because all routes already in this node have the
     * same preference level. Hence, the route we are checking will be better than all,
     * worse than all, or at the same preference level.
     */

    R oldRoute = routes.iterator().next();
    int preferenceComparison = _owner.comparePreference(route, oldRoute);
    if (preferenceComparison < 0) { // less preferable, so route doesn't get added
      return RibDelta.empty();
    }
    if (preferenceComparison == 0) { // equal preference, so add for multipath routing
      // Otherwise add the route
      if (_root.put(p, route)) {
        return RibDelta.<R>builder().add(p, route).build();
      } else {
        return RibDelta.empty();
      }
    }
    /*
     * Last case, preferenceComparison > 0
     * Better than all existing routes for this prefix, so
     * replace them with this one.
     */
    if (_root.replaceAll(p, route)) {
      RibDelta.Builder<R> deltaBuilder = RibDelta.builder();
      routes.forEach(r -> deltaBuilder.remove(p, r, REPLACE));
      return deltaBuilder.add(p, route).build();
    } else {
      return RibDelta.empty();
    }
  }

  @Override
  public int hashCode() {
    return _root.hashCode();
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    return (obj == this) || (obj instanceof RibTree && this._root.equals(((RibTree<?>) obj)._root));
  }

  /**
   * Returns a mapping from prefixes in the RIB that contain at least one element matching the given
   * {@code matches} function to the IPs for which that prefix is the longest match in the RIB
   * (among prefixes with elements that match the function).
   *
   * <p>Used for {@link GenericRib#getMatchingIps()}.
   *
   * @param matches Function to specify the condition routes must meet for their prefix to be added
   *     to the returned map. Takes a route of type {@link R} and returns true if it matches.
   */
  Map<Prefix, IpSpace> getMatchingIps(Function<R, Boolean> matches) {
    ImmutableMap.Builder<Prefix, IpSpace> builder = ImmutableMap.builder();

    /* We traverse the tree in post-order, so when we visit each intermediate node the blacklist
     * will contain all prefixes from each of its children. However, when we are visiting the right
     * subtree of a node, the blacklist will already contain all the prefixes of the left subtree.
     * This is wasteful, as the blacklist can include potentially many redundant prefixes. Consider
     * rewriting to avoid this (e.g. use a fold).
     */
    ImmutableSortedSet.Builder<IpWildcard> blacklist = ImmutableSortedSet.naturalOrder();
    _root.traverseEntries(
        (prefix, elems) -> {
          if (elems.stream().anyMatch(matches::apply)) {
            IpWildcard wc = new IpWildcard(prefix);
            builder.put(
                prefix, new IpWildcardSetIpSpace(blacklist.build(), ImmutableSortedSet.of(wc)));
            blacklist.add(wc);
          }
        });
    return builder.build();
  }

  /**
   * Returns the {@link IpSpace} of IPs contained by prefixes with any route that matches the given
   * matching condition.
   *
   * <p>Used for {@link GenericRib#getRoutableIps()}.
   *
   * @param matches Function to specify the condition routes must meet for IPs contained by their
   *     prefix to be added to the returned {@link IpSpace}.
   */
  IpSpace getRoutableIps(Function<R, Boolean> matches) {
    IpWildcardSetIpSpace.Builder builder = IpWildcardSetIpSpace.builder();
    _root.traverseEntries(
        (prefix, elems) -> {
          if (elems.stream().anyMatch(matches::apply)) {
            builder.including(new IpWildcard(prefix));
          }
        });
    return builder.build();
  }
}
