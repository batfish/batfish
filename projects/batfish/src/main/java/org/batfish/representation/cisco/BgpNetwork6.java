package org.batfish.representation.cisco;

import java.io.Serializable;

public class BgpNetwork6 implements Serializable {

  private static final long serialVersionUID = 1L;

  private final Integer _routeMapLine;

  private final String _routeMapName;

  public BgpNetwork6(String routeMapName, Integer routeMapLine) {
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
