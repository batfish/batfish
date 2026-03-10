package org.batfish.vendor.arista.representation.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration on how to advertise a particular network into BGP */
public final class AristaBgpNetworkConfiguration implements Serializable {
  private @Nullable String _routeMap;

  public AristaBgpNetworkConfiguration() {}

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }
}
