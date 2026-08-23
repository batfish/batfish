package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.AbstractRoute6;
import org.batfish.datamodel.Ip6;
import org.batfish.datamodel.Prefix6;

/**
 * Base IPv6 RIB.
 *
 * <p>This implementation is intentionally independent of the existing IPv4
 * {@link AbstractRib} and {@link RibTree}. It provides route preference,
 * backup-route promotion, ECMP, and IPv6 longest-prefix match.
 *
 * <p>Active IPv6 prefixes are indexed in a binary trie, so an LPM lookup
 * examines at most 128 address bits rather than scanning the complete active
 * routing table.
 *
 * @param <R> IPv6 route type stored by this RIB
 */
@ParametersAreNonnullByDefault
public abstract class AbstractRib6<R extends AbstractRoute6>
    implements Serializable {

  /**
   * Binary trie node for active IPv6 prefixes.
   *
   * <p>The node stores only a reference to an active prefix. Route objects
   * remain owned by {@link #_routes}, avoiding a second copy of every ECMP
   * route set in the trie.
   */
  private static final class PrefixTrieNode
      implements Serializable {

    boolean isEmpty() {
      return _prefix == null
          && _zero == null
          && _one == null;
    }

    void clear() {
      _prefix = null;
      _zero = null;
      _one = null;
    }

    private @Nullable Prefix6 _prefix;
    private @Nullable PrefixTrieNode _zero;
    private @Nullable PrefixTrieNode _one;
  }

  protected AbstractRib6() {
    _candidates = new TreeMap<>();
    _routes = new TreeMap<>();
    _prefixTrie = new PrefixTrieNode();
  }

  /**
   * Compares route preference.
   *
   * @return a positive value when {@code lhs} is preferred to {@code rhs},
   *     zero when equally preferred, and a negative value otherwise
   */
  public abstract int comparePreference(
      R lhs,
      R rhs);

  /** Return all currently active/best routes. */
  public final @Nonnull Set<R> getRoutes() {
    ImmutableSet.Builder<R> routes =
        ImmutableSet.builder();

    _routes
        .values()
        .forEach(routes::addAll);

    return routes.build();
  }

  /** Return active/best routes for exactly {@code prefix}. */
  public final @Nonnull Set<R> getRoutes(
      Prefix6 prefix) {

    Set<R> routes =
        _routes.get(prefix);

    return routes == null
        ? ImmutableSet.of()
        : ImmutableSet.copyOf(routes);
  }

  /** Return all non-best candidate routes. */
  public final @Nonnull Set<R> getBackupRoutes() {
    ImmutableSet.Builder<R> backups =
        ImmutableSet.builder();

    _candidates.forEach(
        (prefix, candidates) -> {
          Set<R> active =
              _routes.get(prefix);

          candidates.stream()
              .filter(
                  route ->
                      active == null
                          || !active.contains(route))
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
    Prefix6 prefix =
        route.getNetwork();

    LinkedHashSet<R> candidates =
        _candidates.computeIfAbsent(
            prefix,
            ignored ->
                new LinkedHashSet<>());

    if (!candidates.add(route)) {
      return false;
    }

    Set<R> oldActive =
        getRoutes(prefix);

    recomputeActiveRoutes(prefix);

    return !oldActive.equals(
        getRoutes(prefix));
  }

  /**
   * Remove a route.
   *
   * @return {@code true} iff the set of active routes changed
   */
  public final boolean removeRoute(R route) {
    Prefix6 prefix =
        route.getNetwork();

    LinkedHashSet<R> candidates =
        _candidates.get(prefix);

    if (candidates == null
        || !candidates.remove(route)) {
      return false;
    }

    Set<R> oldActive =
        getRoutes(prefix);

    if (candidates.isEmpty()) {
      _candidates.remove(prefix);
      _routes.remove(prefix);
      removeActivePrefix(prefix);
    } else {
      recomputeActiveRoutes(prefix);
    }

    return !oldActive.equals(
        getRoutes(prefix));
  }

  /** Remove all routes from this RIB. */
  public final void clear() {
    _candidates.clear();
    _routes.clear();
    _prefixTrie.clear();
  }

  /**
   * Perform IPv6 longest-prefix match over active routes.
   *
   * <p>The active-prefix trie limits lookup to at most 128 branch operations,
   * independent of the number of routes installed in the RIB.
   */
  public final @Nonnull Set<R> longestPrefixMatch(
      Ip6 address) {

    PrefixTrieNode node =
        _prefixTrie;

    @Nullable Prefix6 bestPrefix =
        node._prefix;

    for (int bit = 0;
        bit < Prefix6.MAX_PREFIX_LENGTH;
        bit++) {

      node =
          address.getBitAtPosition(bit)
              ? node._one
              : node._zero;

      if (node == null) {
        break;
      }

      if (node._prefix != null) {
        bestPrefix =
            node._prefix;
      }
    }

    return bestPrefix == null
        ? ImmutableSet.of()
        : getRoutes(bestPrefix);
  }

  /**
   * Recompute all equally preferred active routes for one prefix.
   */
  private void recomputeActiveRoutes(
      Prefix6 prefix) {

    LinkedHashSet<R> candidates =
        _candidates.get(prefix);

    if (candidates == null
        || candidates.isEmpty()) {
      _routes.remove(prefix);
      removeActivePrefix(prefix);
      return;
    }

    R best = null;

    for (R route : candidates) {
      if (best == null
          || comparePreference(
                  route,
                  best)
              > 0) {
        best = route;
      }
    }

    assert best != null;

    LinkedHashSet<R> active =
        new LinkedHashSet<>();

    for (R route : candidates) {
      if (comparePreference(
              route,
              best)
          == 0) {
        active.add(route);
      }
    }

    _routes.put(
        prefix,
        active);

    installActivePrefix(prefix);
  }

  /** Install or refresh one active prefix in the LPM trie. */
  private void installActivePrefix(
      Prefix6 prefix) {

    PrefixTrieNode node =
        _prefixTrie;

    Ip6 network =
        prefix.getNetworkAddress();

    for (int bit = 0;
        bit < prefix.getPrefixLength();
        bit++) {

      if (network.getBitAtPosition(bit)) {
        if (node._one == null) {
          node._one =
              new PrefixTrieNode();
        }

        node =
            node._one;

      } else {
        if (node._zero == null) {
          node._zero =
              new PrefixTrieNode();
        }

        node =
            node._zero;
      }
    }

    node._prefix =
        prefix;
  }

  /**
   * Remove one inactive prefix and prune now-unused trie branches.
   */
  private void removeActivePrefix(
      Prefix6 prefix) {

    removeActivePrefix(
        _prefixTrie,
        prefix.getNetworkAddress(),
        prefix.getPrefixLength(),
        0);
  }

  /**
   * @return true when {@code node} became empty and can be pruned by its
   *     parent
   */
  private static boolean removeActivePrefix(
      PrefixTrieNode node,
      Ip6 network,
      int prefixLength,
      int depth) {

    if (depth == prefixLength) {
      node._prefix = null;
      return node.isEmpty();
    }

    boolean one =
        network.getBitAtPosition(depth);

    PrefixTrieNode child =
        one
            ? node._one
            : node._zero;

    if (child == null) {
      return node.isEmpty();
    }

    if (removeActivePrefix(
        child,
        network,
        prefixLength,
        depth + 1)) {

      if (one) {
        node._one = null;
      } else {
        node._zero = null;
      }
    }

    return node.isEmpty();
  }

  /** All candidate routes, including backups. */
  private final @Nonnull
      Map<Prefix6, LinkedHashSet<R>>
          _candidates;

  /** Currently selected best routes. */
  private final @Nonnull
      Map<Prefix6, LinkedHashSet<R>>
          _routes;

  /** Active-prefix index used exclusively for IPv6 LPM. */
  private final @Nonnull PrefixTrieNode
      _prefixTrie;
}
