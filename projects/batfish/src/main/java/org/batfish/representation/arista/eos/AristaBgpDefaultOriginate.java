package org.batfish.representation.arista.eos;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for origination of default routes */
public final class AristaBgpDefaultOriginate implements Serializable {
  private final boolean _always;
  private final boolean _enabled;
  @Nullable private final String _routeMap;

  public AristaBgpDefaultOriginate(boolean enabled, boolean always, @Nullable String routeMap) {
    checkArgument(
        enabled || (!always && routeMap == null),
        "Cannot both be disabled and have always or routeMap set");
    _always = always;
    _enabled = enabled;
    _routeMap = routeMap;
  }

  public boolean getAlways() {
    return _always;
  }

  public boolean getEnabled() {
    return _enabled;
  }

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }
}
