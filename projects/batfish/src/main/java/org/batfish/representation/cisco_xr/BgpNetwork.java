package org.batfish.representation.cisco_xr;

import java.io.Serializable;

public class BgpNetwork implements Serializable {

  private final Integer _routeMapLine;

  private final String _routeMapName;

  public BgpNetwork(String routeMapName, Integer routeMapLine) {
    _routeMapName = routeMapName;
    _routeMapLine = routeMapLine;
  }

  public Integer getRouteMapLine() {
    return _routeMapLine;
  }

  public String getRouteMapName() {
    return _routeMapName;
  }
}
