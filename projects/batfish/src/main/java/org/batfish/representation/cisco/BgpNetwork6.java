package org.batfish.representation.cisco;

import java.io.Serializable;
import org.batfish.datamodel.Prefix6;

public class BgpNetwork6 implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private final Prefix6 _prefix6;

  private final Integer _routeMapLine;

  private final String _routeMapName;

  public BgpNetwork6(Prefix6 prefix6, String routeMapName, Integer routeMapLine) {
    _prefix6 = prefix6;
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
