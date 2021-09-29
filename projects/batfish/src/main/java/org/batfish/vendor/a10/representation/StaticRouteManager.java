package org.batfish.vendor.a10.representation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Handles the static routes for a prefix. */
@ParametersAreNonnullByDefault
public class StaticRouteManager implements Serializable {

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

  // TODO handle deleting static routes

  private final Map<Ip, StaticRoute> _variants;
}
