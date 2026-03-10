package org.batfish.vendor.arista.representation;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.vendor.arista.representation.StaticRoute.NextHop;

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

  /**
   * Adds the given {@link StaticRoute} as one of the alternatives for the current prefix. Returns
   * {@link Optional#empty()} if this addition succeeds, and returns a string explaining why it
   * failed otherwise.
   */
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

    // Arista does not preserve modifiers unless retyped.
    _variants.removeIf(
        r ->
            r.getNextHop().equals(route.getNextHop())
                && Objects.equals(r.getDistance(), route.getDistance()));

    // TODO: do we need to care about name or track?
    _variants.add(route);
    return Optional.empty();
  }

  /**
   * Removes all routes matching the given {@link NextHop} and {@code distance} as alternatives for
   * the current prefix. Returns true iff any routes were removed.
   */
  public boolean removeVariant(NextHop hop, @Nullable Integer adminDistance) {
    return _variants.removeIf(
        v ->
            v.getNextHop().equals(hop)
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
