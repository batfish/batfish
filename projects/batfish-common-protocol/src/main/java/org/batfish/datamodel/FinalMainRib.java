package org.batfish.datamodel;

import com.google.common.annotations.VisibleForTesting;
import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;

/** Used for analyzing the main RIB after routing simulation is complete. */
public final class FinalMainRib implements Serializable {
  private final PrefixTrieMultiMap<AbstractRoute> _routeTree;

  public @Nonnull Set<AbstractRoute> getRoutes() {
    return _routeTree.getAllElements();
  }

  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Ip address) {
    return _routeTree.longestPrefixMatch(address);
  }

  public @Nonnull Set<AbstractRoute> longestPrefixMatch(@Nonnull Prefix prefix) {
    return _routeTree.longestPrefixMatch(prefix.getStartIp(), prefix.getPrefixLength());
  }

  public FinalMainRib(PrefixTrieMultiMap<AbstractRoute> tree) {
    _routeTree = tree;
  }

  /** Prefer {@link #FinalMainRib(PrefixTrieMultiMap)} for performance. */
  @VisibleForTesting
  public FinalMainRib(Iterable<? extends AbstractRoute> routes) {
    _routeTree = new PrefixTrieMultiMap<>();
    for (AbstractRoute r : routes) {
      _routeTree.put(r.getNetwork(), r);
    }
  }
}
