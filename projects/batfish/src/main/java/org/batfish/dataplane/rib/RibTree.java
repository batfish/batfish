package org.batfish.dataplane.rib;

import static org.batfish.dataplane.rib.RouteAdvertisement.Reason.REPLACE;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.graph.Traverser;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMap;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * Used to store the routes, supports longest prefix match operation.
 *
 * <p>This data structure is a more restrictive version of a ddNF (disjoint difference Normal Form),
 * where the wildcard symbols can appear only after (to-the-right-of) non wildcard symbols in the
 * bit vector. E.g., 101010**, but not 1*001***
 */
final class RibTree<R extends AbstractRoute> implements Serializable {

  private static final long serialVersionUID = 1L;

  private PrefixTrieMap<R> _root;
  private AbstractRib<R> _owner;

  RibTree(AbstractRib<R> owner) {
    _root = new PrefixTrieMap<>(Prefix.ZERO);
    _owner = owner;
  }

  /**
   * Remove a single route from the RIB, if it exists
   *
   * @param route route to remove
   * @return {@link RibDelta} if the route was removed, otherwise {@code null};
   */
  @Nonnull
  RibDelta<R> removeRouteGetDelta(R route, Reason reason) {
    PrefixTrieMap<R> node = _root.findNode(route.getNetwork());
    if (node == null) {
      return RibDelta.empty();
    }
    if (!node.remove(route)) {
      return RibDelta.empty();
    }
    Builder<R> b = RibDelta.builder();
    b.remove(route, reason);
    if (node.getElements().isEmpty() && _owner._backupRoutes != null) {
      SortedSet<? extends R> backups =
          _owner._backupRoutes.getOrDefault(route.getNetwork(), ImmutableSortedSet.of());
      if (backups.isEmpty()) {
        return b.build();
      }
      node.add(backups.first());
      b.add(backups.first());
    }
    // Return new delta
    return b.build();
  }

  /**
   * Check if the route is present in the RIB
   *
   * @param route route to find
   * @return true if the route exists in the RIB
   */
  boolean containsRoute(R route) {
    return _root.getElements(route.getNetwork()).contains(route);
  }

  private Set<R> getForwardingRoutes(PrefixTrieMap<R> node) {
    return node.getElements().stream()
        .filter(r -> !r.getNonForwarding())
        .collect(ImmutableSet.toImmutableSet());
  }

  private boolean hasForwardingRoute(PrefixTrieMap<R> node) {
    return !getForwardingRoutes(node).isEmpty();
  }

  /**
   * Returns a set of routes in this tree which 1) are forwarding routes, 2) match the given IP
   * address, and 3) have the longest prefix length within the specified maximum.
   *
   * <p>Returns the empty set if there are no forwarding routes that match.
   */
  @Nonnull
  Set<R> getLongestPrefixMatch(Ip address, int maxPrefixLength) {
    int pl = maxPrefixLength;
    PrefixTrieMap<R> node = _root.longestPrefixMatch(address, pl);
    while ((node == null || !hasForwardingRoute(node)) && pl >= 0) {
      pl--;
      node = _root.longestPrefixMatch(address, pl);
    }
    return node == null || !hasForwardingRoute(node)
        ? ImmutableSet.of()
        : getForwardingRoutes(node);
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
    return _root.getElements(prefix);
  }

  /**
   * Add a new route into the RIB, potentially replacing other routes
   *
   * @param route route to add
   * @return a {@link RibDelta} objects indicating which routes where added and evicted from this
   *     RIB
   */
  @Nonnull
  RibDelta<R> mergeRoute(R route) {
    PrefixTrieMap<R> node = _root.findOrCreateNode(route.getNetwork());
    if (node.getElements().isEmpty()) {
      node.add(route);
      return RibDelta.<R>builder().add(route).build();
    }
    /*
     * Check if the route we are adding is preferred to the routes we already have.
     * We only need to compare to one route, because all routes already in this node have the
     * same preference level. Hence, the route we are checking will be better than all,
     * worse than all, or at the same preference level.
     */

    R oldRoute = node.getElements().iterator().next();
    int preferenceComparison = _owner.comparePreference(route, oldRoute);
    if (preferenceComparison < 0) { // less preferable, so route doesn't get added
      return RibDelta.empty();
    }
    if (preferenceComparison == 0) { // equal preference, so add for multipath routing
      // Otherwise add the route
      if (node.add(route)) {
        return RibDelta.<R>builder().add(route).build();
      } else {
        return RibDelta.empty();
      }
    }
    // Last case, preferenceComparison > 0
    /*
     * Better than all existing routes for this prefix, so
     * replace them with this one.
     */
    RibDelta<R> delta =
        RibDelta.<R>builder().remove(node.getElements(), REPLACE).add(route).build();
    node.clear();
    node.add(route);
    return delta;
  }

  @Override
  public int hashCode() {
    return _root.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj == this) || (obj instanceof RibTree && this._root.equals(((RibTree<?>) obj)._root));
  }

  RibDelta<R> clearRoutes(Prefix prefix) {
    PrefixTrieMap<R> node = _root.findNode(prefix);
    if (node == null) {
      return RibDelta.empty();
    }
    RibDelta<R> delta = RibDelta.<R>builder().remove(node.getElements(), Reason.REPLACE).build();
    node.clear();
    return delta;
  }

  Map<Prefix, IpSpace> getMatchingIps() {
    ImmutableMap.Builder<Prefix, IpSpace> builder = ImmutableMap.builder();
    IpWildcardSetIpSpace.Builder matchingIps = IpWildcardSetIpSpace.builder();
    Traverser.<PrefixTrieMap<R>>forTree(PrefixTrieMap::getChildren)
        // Post order ensures exclusions are properly handled
        .depthFirstPostOrder(_root)
        .forEach(
            n -> {
              if (hasForwardingRoute(n)) {
                matchingIps
                    .excluding(matchingIps.build().getWhitelist())
                    .including(new IpWildcard(n.getPrefix()));
                builder.put(n.getPrefix(), matchingIps.build());
              }
            });

    return builder.build();
  }

  IpSpace getRoutableIps() {
    IpWildcardSetIpSpace.Builder builder = IpWildcardSetIpSpace.builder();
    Traverser.<PrefixTrieMap<R>>forTree(PrefixTrieMap::getChildren)
        .breadthFirst(_root)
        .forEach(
            n -> {
              if (hasForwardingRoute(n)) {
                builder.including(new IpWildcard(n.getPrefix()));
              }
            });
    return builder.build();
  }
}
