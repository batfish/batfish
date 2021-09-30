package org.batfish.vendor.a10.representation;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/** Handles the static routes for a prefix. */
public final class StaticRouteManager implements Serializable {

  /** Map of forwarding router {@link Ip} to its {@link StaticRoute} */
  public @Nonnull Map<Ip, StaticRoute> getVariants() {
    return _variants;
  }

  public StaticRouteManager() {
    _variants = new HashMap<>();
  }

  /** Adds the given {@link StaticRoute} as one of the alternatives for the current prefix. */
  public void add(StaticRoute route) {
    // Same forwarding router address means the new route overwrites an existing one
    _variants.put(route.getForwardingRouterAddress(), route);
  }

  /**
   * Deletes the specified static route. Returns {@link Optional#empty()} if this deletion succeeds,
   * otherwise returns a string explaining why it failed.
   */
  public Optional<String> delete(StaticRoute route) {
    Ip ip = route.getForwardingRouterAddress();
    StaticRoute candidate = _variants.get(ip);
    if (candidate == null) {
      return Optional.of(String.format("No route exists for forwarding router address %s", ip));
    }

    String deleteDescription = route.getDescription();
    if (deleteDescription != null && !deleteDescription.equals(candidate.getDescription())) {
      return Optional.of(String.format("No route exists with description '%s'", deleteDescription));
    }

    Integer deleteDistance = route.getDistance();
    if (deleteDistance != null
        && !deleteDistance.equals(
            firstNonNull(candidate.getDistance(), StaticRoute.DEFAULT_STATIC_ROUTE_DISTANCE))) {
      return Optional.of(String.format("No route exists with distance %s", deleteDistance));
    }

    _variants.remove(ip);
    return Optional.empty();
  }

  private final Map<Ip, StaticRoute> _variants;
}
