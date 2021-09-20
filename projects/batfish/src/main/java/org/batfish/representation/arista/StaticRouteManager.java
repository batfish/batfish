package org.batfish.representation.arista;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.representation.arista.StaticRoute.NextHop;

/** Handles the static routes for a prefix. */
@ParametersAreNonnullByDefault
public class StaticRouteManager implements Serializable {

  // If tag is set on any variant, it will be set on all variants. Default is 0.
  private long _tag;

  private final List<StaticRoute> _variants;

  public StaticRouteManager() {
    _tag = 0;
    _variants = new LinkedList<>();
  }

  public Optional<String> addVariant(StaticRoute route) {
    if (_variants.isEmpty()) {
      _variants.add(route);
      return Optional.empty();
    }

    // EOS displays this message try to add a non/null route when there is already a null/non route.
    if (route.getNextHop().getNullRouted()
        && _variants.stream().anyMatch(r -> !r.getNextHop().getNullRouted())) {
      return Optional.of("Cannot ECMP to Null0 interface.");
    } else if (!route.getNextHop().getNullRouted()
        && _variants.stream().anyMatch(r -> r.getNextHop().getNullRouted())) {
      return Optional.of("Cannot ECMP to Null0 interface.");
    }

    // TODO;
    _variants.add(route);
    return Optional.empty();
  }

  private static boolean nextHopRemovable(NextHop current, NextHop removing) {
    if (removing.getNullRouted()) {
      return current.getNullRouted();
    }
    return Objects.equals(removing.getNextHopInterface(), current.getNextHopInterface())
        && Objects.equals(removing.getNextHopIp(), current.getNextHopIp());
  }

  public boolean removeVariant(NextHop hop, @Nullable Integer adminDistance) {
    return _variants.removeIf(
        v ->
            nextHopRemovable(v.getNextHop(), hop)
                && (adminDistance == null || adminDistance.equals(v.getDistance())));
  }

  public @Nonnull List<StaticRoute> getVariants() {
    return _variants;
  }

  public long getTag() {
    return _tag;
  }

  public void setTag(long tag) {
    _tag = tag;
  }
}
