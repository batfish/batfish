package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * IPv6 FIB built from a finalized IPv6 main RIB.
 *
 * <p>The IPv6 dataplane resolves recursive static next hops before
 * finalizing the RIB. FIB entries therefore carry the resolved outgoing
 * interface and immediate IPv6 next hop needed for forwarding.
 */
@ParametersAreNonnullByDefault
public final class Fib6Impl implements Fib6 {

  public Fib6Impl(FinalMainRib6 rib) {
    _entries = new TreeMap<>();

    for (AbstractRoute6 route : rib.getRoutes()) {
      if (route.getNonForwarding()
          || Route.UNSET_NEXT_HOP_INTERFACE.equals(
              route.getNextHopInterface())) {
        continue;
      }

      _entries
          .computeIfAbsent(
              route.getNetwork(),
              ignored -> new LinkedHashSet<>())
          .add(
              new FibEntry6(
                  route,
                  route.getNextHopInterface(),
                  route.getNextHopIp()));
    }
  }

  @Override
  public @Nonnull Set<FibEntry6> allEntries() {
    ImmutableSet.Builder<FibEntry6> builder =
        ImmutableSet.builder();

    _entries.values().forEach(builder::addAll);

    return builder.build();
  }

  @Override
  public @Nonnull Set<FibEntry6> get(Ip6 ip) {
    int longestPrefixLength = -1;
    Set<FibEntry6> best = ImmutableSet.of();

    for (Map.Entry<Prefix6, LinkedHashSet<FibEntry6>>
        entry : _entries.entrySet()) {

      Prefix6 prefix = entry.getKey();

      if (!prefix.contains(ip)) {
        continue;
      }

      int prefixLength =
          prefix.getPrefixLength();

      if (prefixLength > longestPrefixLength) {
        longestPrefixLength = prefixLength;
        best = entry.getValue();
      }
    }

    return ImmutableSet.copyOf(best);
  }

  private final @Nonnull
      Map<Prefix6, LinkedHashSet<FibEntry6>> _entries;
}
