package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for origination of default routes at the neighbor level. */
public final class AristaBgpNeighborDefaultOriginate implements Serializable {
  /** Whether this neighbor is configured to originate a default route. */
  private final boolean _enabled;
  /** If {@link #getEnabled()} is true, an optional route-map applied. */
  @Nullable private final String _routeMap;

  public static AristaBgpNeighborDefaultOriginate disabled() {
    return new AristaBgpNeighborDefaultOriginate(false, null);
  }

  public static AristaBgpNeighborDefaultOriginate routeMap(@Nullable String routeMap) {
    return new AristaBgpNeighborDefaultOriginate(true, routeMap);
  }

  private AristaBgpNeighborDefaultOriginate(boolean enabled, @Nullable String routeMap) {
    _enabled = enabled;
    _routeMap = routeMap;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }
}
