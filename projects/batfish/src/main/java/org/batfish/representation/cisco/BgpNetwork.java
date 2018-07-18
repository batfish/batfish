package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.Prefix;

public class BgpNetwork implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix _prefix;

  private final Integer _routeMapLine;

  private final String _routeMapName;

  public BgpNetwork(Prefix prefix, String routeMapName, Integer routeMapLine) {
    _prefix = prefix;
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
