package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration for origination of default routes */
public final class AristaBgpDefaultOriginate implements Serializable {
  private final boolean _always;
  @Nullable private final String _routeMap;

  public AristaBgpDefaultOriginate(boolean always, @Nullable String routeMap) {
    _always = always;
    _routeMap = routeMap;
  }

  public boolean getAlways() {
    return _always;
  }

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }
}
