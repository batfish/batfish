package org.batfish.representation.cisco;

import java.io.Serializable;

public class BgpNetwork6 implements Serializable {

  private final String _routeMapName;

  public BgpNetwork6(String routeMapName) {
    _routeMapName = routeMapName;
  }

  public String getRouteMapName() {
    return _routeMapName;
  }
}
