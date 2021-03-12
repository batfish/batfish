package org.batfish.representation.cisco_asa;

import java.io.Serializable;

public class BgpNetwork implements Serializable {

  private final String _routeMapName;

  public BgpNetwork(String routeMapName) {
    _routeMapName = routeMapName;
  }

  public String getRouteMapName() {
    return _routeMapName;
  }
}
