package org.batfish.representation.cumulus;

import java.io.Serializable;

/**
 * Route map statement that calls another routemap.
 *
 * <p>Executed after any Set Actions have been carried out. If the route-map called returns deny
 * then processing of the route-map finishes and the route is denied, regardless of the Matching
 * Policy or the Exit Policy. If the called route-map returns permit, then Matching Policy and Exit
 * Policy govern further behaviour, as normal.
 *
 * <p>See also <a href="http://docs.frrouting.org/en/latest/routemap.html#route-maps"></a>
 */
public class RouteMapCall implements Serializable {
  private final String _routeMapName;

  public RouteMapCall(String routeMapName) {
    _routeMapName = routeMapName;
  }

  /** Name of the route map to call */
  public String getRouteMapName() {
    return _routeMapName;
  }
}
