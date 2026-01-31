package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for origination of default routes */
public final class AristaBgpDefaultOriginate implements Serializable {
  /**
   * Whether this neighbor is configured to originate a default route even if there isn't one in the
   * main RIB.
   */
  private final boolean _always;

  /** Whether this neighbor is configured to originate a default route. */
  private final boolean _enabled;

  /** If {@link #getEnabled()} is true, an optional route-map applied. */
  private final @Nullable String _routeMap;

  public static AristaBgpDefaultOriginate disabled() {
    return new AristaBgpDefaultOriginate(false, false, null);
  }

  public static AristaBgpDefaultOriginate enabled(boolean always, @Nullable String routeMap) {
    return new AristaBgpDefaultOriginate(true, always, routeMap);
  }

  private AristaBgpDefaultOriginate(boolean enabled, boolean always, @Nullable String routeMap) {
    _enabled = enabled;
    _always = always;
    _routeMap = routeMap;
  }

  public boolean getAlways() {
    return _always;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }
}
