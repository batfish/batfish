package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A version of {@link org.batfish.dataplane.rib.Rib} that is used for analyzing the main RIB after
 * routing simulation is complete.
 *
 * <p>It omits things like {@link AnnotatedRoute annotations} of the contained routes, backup
 * routes, and more.
 */
public final class FinalMainRib implements Serializable {
  public FinalMainRib(PrefixTrieMultiMap<AbstractRoute> tree) {
    _routeTree = tree;
  }

  /**
   * Returns all the routes in this RIB.
   *
   * @see PrefixTrieMultiMap#getAllElements()
   */
  public @Nonnull Set<AbstractRoute> getRoutes() {
    return _routeTree.getAllElements();
  }

  /**
   * Performs a longest prefix match on the route tree.
   *
   * @see PrefixTrieMultiMap#longestPrefixMatch(Ip)
   */
  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Ip address) {
    return _routeTree.longestPrefixMatch(address);
  }

  /**
   * Performs a longest prefix match on the route tree.
   *
   * @see PrefixTrieMultiMap#longestPrefixMatch(Ip, int)
   */
  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Prefix prefix) {
    return _routeTree.longestPrefixMatch(prefix.getStartIp(), prefix.getPrefixLength());
  }

  //

  private final PrefixTrieMultiMap<AbstractRoute> _routeTree;

  /** Prefer {@link #FinalMainRib(PrefixTrieMultiMap)} for performance. */
  @VisibleForTesting
  public static FinalMainRib forTesting(Iterable<? extends AbstractRoute> routes) {
    PrefixTrieMultiMap<AbstractRoute> routeTree = new PrefixTrieMultiMap<>();
    for (AbstractRoute r : routes) {
      routeTree.put(r.getNetwork(), r);
    }
    return new FinalMainRib(routeTree);
  }
}
