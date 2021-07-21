package org.batfish.dataplane.rib;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixTrieMultiMap;
import org.batfish.datamodel.route.nh.NextHopIp;

/**
 * A helper structure for determining which {@link NextHopIp} routes' resolvability may change when
 * a route is added or removed. Clients must register the prefixes of all active routes, and the
 * next hop IPs of all {@link NextHopIp} routes under consideration. Then they may determine next
 * hop IPs affected by a route change for a given prefix via {@link #getAffectedNextHopIps(Prefix)}.
 */
public final class RibResolutionTrie {

  private static final class ResolutionTrieValue {}

  private static final ResolutionTrieValue PREFIX = new ResolutionTrieValue();

  private static final ResolutionTrieValue NHIP = new ResolutionTrieValue();

  private final PrefixTrieMultiMap<ResolutionTrieValue> _prefixesAndNextHops;

  public RibResolutionTrie() {
    _prefixesAndNextHops = new PrefixTrieMultiMap<>();
  }

  /** Add the prefix of a forwarding route to the trie. */
  public void addPrefix(Prefix prefix) {

    _prefixesAndNextHops.put(prefix, PREFIX);
  }

  /** Remove the prefix of a forwarding route from the trie. */
  public void removePrefix(Prefix prefix) {
    _prefixesAndNextHops.remove(prefix, PREFIX);
  }

  /** Add the next hop IP of a {@link NextHopIp} route to the trie. */
  public void addNextHopIp(Ip nextHopIp) {
    _prefixesAndNextHops.put(Prefix.create(nextHopIp, Prefix.MAX_PREFIX_LENGTH), NHIP);
  }

  /** Remove the next hop IP of a {@link NextHopIp} route from the trie. */
  public void removeNextHopIp(Ip nextHopIp) {
    _prefixesAndNextHops.remove(Prefix.create(nextHopIp, Prefix.MAX_PREFIX_LENGTH), NHIP);
  }

  /**
   * Return the set of next hop IPs whose resolution could potentially be affected by the addition
   * or removal of a route whose network is {@code newPrefix}.
   */
  public @Nonnull Set<Ip> getAffectedNextHopIps(Prefix newPrefix) {
    int newPrefixLength = newPrefix.getPrefixLength();
    ImmutableSet.Builder<Ip> affectedNextHopIps = ImmutableSet.builder();
    // If a node contains a NHIP, add it to the result.
    BiConsumer<Prefix, Set<ResolutionTrieValue>> consumer =
        (prefix, values) -> {
          if (values.contains(NHIP)) {
            affectedNextHopIps.add(prefix.getStartIp());
          }
        };
    // Visit nodes either:
    // - whose prefix contains newPrefix
    // - whose prefix is contained within newPrefix and either:
    //   - do not contain a previously added prefix,
    //     i.e., an intermediate node, or one with just an NHIP
    //   - contain a previously added prefix that is not more specific than newPrefix
    BiPredicate<Prefix, Set<ResolutionTrieValue>> visitNode =
        (prefix, values) ->
            prefix.containsPrefix(newPrefix)
                || (newPrefix.containsPrefix(prefix)
                    && (!values.contains(PREFIX) || prefix.getPrefixLength() <= newPrefixLength));
    _prefixesAndNextHops.traverseEntries(consumer, visitNode);
    return affectedNextHopIps.build();
  }
}
