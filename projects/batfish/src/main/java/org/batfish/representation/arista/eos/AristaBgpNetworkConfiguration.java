package org.batfish.representation.arista.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration on how to advertise a particular network into BGP */
public final class AristaBgpNetworkConfiguration implements Serializable {
  @Nullable private String _routeMap;

  public AristaBgpNetworkConfiguration() {}

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }
}
