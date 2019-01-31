package org.batfish.dataplane.rib;

import static org.batfish.dataplane.rib.RouteAdvertisement.Reason.REPLACE;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.AbstractRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.dataplane.rib.RibDelta.Builder;
import org.batfish.dataplane.rib.RouteAdvertisement.Reason;

/**
 * RibTree is constructed from nodes of this type. A node has a prefix, a set of routes that match
 * the prefix (and it's length) and two children. The children's prefixes must always be more
 * specific (i.e., their prefix length is larger).
 */
class RibTreeNode<R extends AbstractRoute> implements Serializable {

  private static final long serialVersionUID = 1L;

  private RibTreeNode<R> _left;

  private Prefix _prefix;

  private RibTreeNode<R> _right;

  private final Set<R> _routes;

  private AbstractRib<R> _owner;

  RibTreeNode(Prefix prefix, @Nonnull AbstractRib<R> owner) {
    _routes = new HashSet<>();
    _prefix = prefix;
    _owner = owner;
  }

  void collectRoutes(ImmutableCollection.Builder<R> routes) {
    if (_left != null) {
      _left.collectRoutes(routes);
    }
    if (_right != null) {
      _right.collectRoutes(routes);
    }
    routes.addAll(_routes);
  }

  @Nullable
  private RibTreeNode<R> findRouteNode(long bits, int prefixLength, int firstUnmatchedBitIndex) {
    // If prefix lengths match, this is the node where such route would be stored.
    if (prefixLength == _prefix.getPrefixLength()) {
      return this;
    }

    boolean currentBit = Ip.getBitAtPosition(bits, firstUnmatchedBitIndex);
    /*
     * If prefixes don't match exactly, look at the current bit. That determines whether we look
     * left or right. As long as the child is not null, recurse.
     *
     * Note that:
     * 1) routes are stored in the nodes where lengths of the node prefix and the route prefix
     *    match exactly; and
     * 2) prefix matches only get more specific (longer) the deeper we go in the tree
     *
     * Therefore, we can fast-forward the firstUnmatchedBitIndex to the prefix length of the
     * child node
     */
    if (currentBit) {
      return (_right != null)
          ? _right.findRouteNode(bits, prefixLength, _right._prefix.getPrefixLength())
          : null;
    } else {
      return (_left != null)
          ? _left.findRouteNode(bits, prefixLength, _left._prefix.getPrefixLength())
          : null;
    }
  }

  /**
   * Check if the route exists in our subtree
   *
   * @param route route in question
   * @param bits route's IP address represented as a BitSet
   * @param prefixLength route's prefix length
   * @return true if the route is in the subtree
   */
  boolean containsRoute(R route, long bits, int prefixLength) {
    RibTreeNode<R> node = findRouteNode(bits, prefixLength, 0);
    return node != null && node._routes.contains(route);
  }

  /** Returns the forwarding routes stored in this node. */
  private Set<R> getForwardingRoutes() {
    return _routes.stream()
        .filter(r -> !r.getNonForwarding())
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Returns a set of routes with the longest prefix match for a given IP address
   *
   * @param address IP address
   * @param bits IP address represented as a set of bits
   * @param maxPrefixLength only return routes with prefix length less than or equal to given value
   * @return a set of routes
   */
  @Nonnull
  Set<R> getLongestPrefixMatch(Ip address, long bits, int maxPrefixLength) {
    // If the current subtree only contains routes that are too long, no matches.
    int index = _prefix.getPrefixLength();
    if (index > maxPrefixLength) {
      return ImmutableSet.of();
    }

    // If the current subtree does not contain the destination IP, no matches.
    if (!_prefix.containsIp(address)) {
      return ImmutableSet.of();
    }

    // If the network of the current node is exactly the desired maximum length, stop here.
    if (index == maxPrefixLength) {
      return getForwardingRoutes();
    }

    // Examine the bit at the given index
    boolean currentBit = Ip.getBitAtPosition(bits, index);
    // If the current bit is 1, go right recursively
    RibTreeNode<R> child = currentBit ? _right : _left;
    if (child == null) {
      return getForwardingRoutes();
    }

    // Represents any potentially longer route matches (than ones stored at this node)
    Set<R> longerMatches = child.getLongestPrefixMatch(address, bits, maxPrefixLength);

    // If we found no better matches, return the ones from this node
    if (longerMatches.isEmpty()) {
      return getForwardingRoutes();
    } else { // otherwise return longer matches
      return longerMatches;
    }
  }

  /** Retrieve an immutable copy of the routes currently available for the given prefix. */
  @Nonnull
  Set<R> getRoutes(Prefix p) {
    RibTreeNode<R> node = findRouteNode(p.getStartIp().asLong(), p.getPrefixLength(), 0);
    if (node == null) {
      return ImmutableSet.of();
    }
    return ImmutableSet.copyOf(node._routes);
  }

  private void assignChild(RibTreeNode<R> parent, RibTreeNode<R> child, boolean branchRight) {
    if (branchRight) {
      parent._right = child;
    } else {
      parent._left = child;
    }
  }

  /**
   * Takes care of adding new nodes to the tree and maintaining correct pointers.
   *
   * @param parent node that we are trying to merge a route into
   * @param route the route to merge
   * @param routeBits the bitSet representation of the route's IP address
   * @param prefixLength the route's prefix length
   * @param firstUnmatchedBitIndex the index of the first bit in the route's prefix that we haven't
   *     checked yet
   * @param rightBranch whether we should recurse down the right side of the tree
   * @return True if a route has been inserted into the tree
   */
  @Nonnull
  private RibDelta<R> mergeHelper(
      RibTreeNode<R> parent,
      R route,
      long routeBits,
      int prefixLength,
      int firstUnmatchedBitIndex,
      boolean rightBranch) {
    RibTreeNode<R> node;

    // Get our node from one of the tree sides
    if (rightBranch) {
      node = parent._right;
    } else {
      node = parent._left;
    }

    // Node doesn't exist, so create one. By construction, it will be the best match
    // for the given route
    if (node == null) {
      node = new RibTreeNode<>(route.getNetwork(), _owner);
      node._routes.add(route);
      // don't forget to assign new node object to parent node
      assignChild(parent, node, rightBranch);
      return RibDelta.<R>builder().add(route).build();
    }

    // Node exists, get some helper data out of the current node we are examining
    Prefix nodePrefix = node._prefix;
    int nodePrefixLength = nodePrefix.getPrefixLength();
    Ip nodeAddress = nodePrefix.getStartIp();
    long nodeAddressBits = nodeAddress.asLong();
    int nextUnmatchedBit;
    // Set up two "pointers" as we scan through the route's and the node's prefixes
    boolean currentAddressBit = false;
    boolean currentNodeAddressBit;

    /*
     * We know we matched up to firstUnmatchedBitIndex. Continue going forward in the bits
     * to find a longer match.
     * At the end of this loop nextUnmatchedBit will be the first place where the route prefix
     * and this node's prefix diverge.
     * Note that nextUnmatchedBit can be outside of the node's or the route's prefix.
     */
    for (nextUnmatchedBit = firstUnmatchedBitIndex + 1;
        nextUnmatchedBit < nodePrefixLength && nextUnmatchedBit < prefixLength;
        nextUnmatchedBit++) {
      currentAddressBit = Ip.getBitAtPosition(routeBits, nextUnmatchedBit);
      currentNodeAddressBit = Ip.getBitAtPosition(nodeAddressBits, nextUnmatchedBit);
      if (currentNodeAddressBit != currentAddressBit) {
        break;
      }
    }

    /*
     * If the next unmatched bit is the same as node prefix length, we "ran off" the node prefix.
     * Recursively merge the route into this node.
     */
    if (nextUnmatchedBit == nodePrefixLength) {
      return node.mergeRoute(route, routeBits, prefixLength, nextUnmatchedBit);
    }

    /*
     * If we reached the route's prefix length (but not the nodes's) we need to create a new node
     * above the current node that matches the route's prefix and re-attach the current node to
     * the newly created node.
     */
    if (nextUnmatchedBit == prefixLength) {
      currentNodeAddressBit = Ip.getBitAtPosition(nodeAddressBits, nextUnmatchedBit);
      RibTreeNode<R> oldNode = node;
      node = new RibTreeNode<>(route.getNetwork(), _owner);
      node._routes.add(route);
      assignChild(parent, node, rightBranch);
      assignChild(node, oldNode, currentNodeAddressBit);
      return RibDelta.<R>builder().add(route).build();
    }

    /*
     * If we are here, there is a bit difference between the node and route prefixes before we
     * reach the end of either prefix. This requires the following:
     * - Compute the max prefix match (up to nextUnmatchedBit)
     * - Create a new node with this new prefix above the current node
     * - Create a new node with the route's full prefix and assign it the parent.
     * - Existing node becomes a sibling of the node with full route prefix
     */
    RibTreeNode<R> oldNode = node;

    // newNetwork has the max prefix match up to nextUnmatchedBit
    Prefix newNetwork = Prefix.create(route.getNetwork().getStartIp(), nextUnmatchedBit);
    node = new RibTreeNode<>(newNetwork, _owner); // node is the node we are inserting in the middle
    RibTreeNode<R> child = new RibTreeNode<>(route.getNetwork(), _owner);
    child._routes.add(route);
    assignChild(parent, node, rightBranch);
    // child and old node become siblings, children of the newly inserted node
    assignChild(node, child, currentAddressBit);
    assignChild(node, oldNode, !currentAddressBit);
    return RibDelta.<R>builder().add(route).build();
  }

  @Nonnull
  RibDelta<R> mergeRoute(
      R route, long routeBits, int routePrefixLength, int firstUnmatchedBitIndex) {
    /*
     * We have reached the node where a route should be inserted, because:
     * 1) the prefix length of this node matches the prefix length of the route exactly, and
     * 2) going deeper can only gets us longer matches
     */
    if (routePrefixLength == _prefix.getPrefixLength()) {

      // No routes with this prefix, so just add it. No comparison necessary
      if (_routes.isEmpty()) {
        _routes.add(route);
        return RibDelta.<R>builder().add(route).build();
      }

      /*
       * Check if the route we are adding is preferred to the routes we already have.
       * We only need to compare to one route, because all routes already in this node have the
       * same preference level. Hence, the route we are checking will be better than all,
       * worse than all, or at the same preference level.
       */
      R oldRoute = _routes.iterator().next();
      int preferenceComparison = _owner.comparePreference(route, oldRoute);
      if (preferenceComparison < 0) { // less preferable, so route doesn't get added
        return RibDelta.empty();
      }
      if (preferenceComparison == 0) { // equal preference, so add for multipath routing
        if (_routes.contains(route)) {
          // route is already here, so nothing to do
          return RibDelta.empty();
        }
        // Otherwise add the route
        if (_routes.add(route)) {
          return RibDelta.<R>builder().add(route).build();
        } else {
          return RibDelta.empty();
        }
      }
      // Last case, preferenceComparison > 0
      /*
       * Better than all pre-existing routes for this prefix, so
       * replace them with this one.
       */
      RibDelta<R> delta = RibDelta.<R>builder().remove(_routes, REPLACE).add(route).build();
      _routes.clear();
      _routes.add(route);
      return delta;
    }
    /*
     * The prefix match is not exact, do some extra insertion logic.
     * Current bit determines which side of the tree to go down (1 = right, 0 = left)
     */
    boolean currentBit = Ip.getBitAtPosition(routeBits, firstUnmatchedBitIndex);
    return mergeHelper(
        this, route, routeBits, routePrefixLength, firstUnmatchedBitIndex, currentBit);
  }

  @Override
  public String toString() {
    return _prefix.toString();
  }

  @Nonnull
  RibDelta<R> removeRoute(
      R route, long bits, int prefixLength, int firstUnmatchedBitIndex, Reason reason) {
    RibTreeNode<R> node = findRouteNode(bits, prefixLength, firstUnmatchedBitIndex);
    if (node == null) {
      // No effect, return empty
      return RibDelta.empty();
    }
    Builder<R> b = RibDelta.builder();
    if (node._routes.remove(route)) {
      b.remove(route, reason);
      if (node._routes.isEmpty() && _owner._backupRoutes != null) {
        SortedSet<? extends R> backups =
            _owner._backupRoutes.getOrDefault(route.getNetwork(), ImmutableSortedSet.of());
        if (!backups.isEmpty()) {
          node._routes.add(backups.first());
          b.add(backups.first());
        }
      }
    }
    // Return new delta
    return b.build();
  }

  @Override
  public int hashCode() {
    int hashCode = _routes.hashCode();
    if (_left != null) {
      hashCode += _left.hashCode();
    }
    if (_right != null) {
      hashCode += _right.hashCode();
    }
    return hashCode;
  }

  @Override
  public boolean equals(Object obj) {
    return (this == obj)
        // Given that obj is not null, check route equality recursively
        || (obj instanceof RibTreeNode
            && _routes.equals(((RibTreeNode<?>) obj)._routes)
            && (_left == null
                ? ((RibTreeNode<?>) obj)._left == null
                : _left.equals(((RibTreeNode<?>) obj)._left))
            && (_right == null
                ? ((RibTreeNode<?>) obj)._right == null
                : _right.equals(((RibTreeNode<?>) obj)._right)));
  }

  RibDelta<R> clearRoutes(Prefix prefix) {
    long bits = prefix.getStartIp().asLong();
    RibTreeNode<R> node = findRouteNode(bits, prefix.getPrefixLength(), 0);
    if (node == null) {
      return null;
    }
    RibDelta<R> delta = RibDelta.<R>builder().remove(node._routes, REPLACE).build();
    node._routes.clear();
    return delta;
  }

  void addMatchingIps(ImmutableMap.Builder<Prefix, IpSpace> builder) {
    if (_left != null) {
      _left.addMatchingIps(builder);
    }
    if (_right != null) {
      _right.addMatchingIps(builder);
    }
    if (hasForwardingRoute()) {
      IpWildcardSetIpSpace.Builder matchingIps = IpWildcardSetIpSpace.builder();
      if (_left != null) {
        _left.excludeRoutableIps(matchingIps);
      }
      if (_right != null) {
        _right.excludeRoutableIps(matchingIps);
      }
      matchingIps.including(new IpWildcard(_prefix));
      builder.put(_prefix, matchingIps.build());
    }
  }

  void addRoutableIps(IpWildcardSetIpSpace.Builder builder) {
    if (hasForwardingRoute()) {
      builder.including(new IpWildcard(_prefix));
    } else {
      if (_left != null) {
        _left.addRoutableIps(builder);
      }
      if (_right != null) {
        _right.addRoutableIps(builder);
      }
    }
  }

  private void excludeRoutableIps(IpWildcardSetIpSpace.Builder builder) {
    if (hasForwardingRoute()) {
      builder.excluding(new IpWildcard(_prefix));
    } else {
      if (_left != null) {
        _left.excludeRoutableIps(builder);
      }
      if (_right != null) {
        _right.excludeRoutableIps(builder);
      }
    }
  }

  private boolean hasForwardingRoute() {
    return !_routes.stream().allMatch(AbstractRoute::getNonForwarding);
  }
}
