package org.batfish.representation.cisco.eos;

import java.io.Serializable;
import javax.annotation.Nullable;

/** Configuration on how to advertise a particular network into BGP */
public final class AristaBgpNetworkConfiguration implements Serializable {
  @Nullable private final String _routeMap;

  public AristaBgpNetworkConfiguration(@Nullable String routeMap) {
    _routeMap = routeMap;
  }

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }
}
