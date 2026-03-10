package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nullable;

public final class OspfDefaultOriginate implements Serializable {

  public boolean getAlways() {
    return _always;
  }

  public void setAlways(boolean always) {
    _always = always;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _always;
  private @Nullable String _routeMap;
}
