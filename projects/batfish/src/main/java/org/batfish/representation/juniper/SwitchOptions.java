package org.batfish.representation.juniper;

import java.io.Serializable;
import org.batfish.datamodel.bgp.RouteDistinguisher;

public class SwitchOptions implements Serializable {

  private String _vtepSourceInterface;
  private RouteDistinguisher _routeDistinguisher;

  public String getVtepSourceInterface() {
    return _vtepSourceInterface;
  }

  public RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setVtepSourceInterface(String vtepSourceInterface) {
    _vtepSourceInterface = vtepSourceInterface;
  }

  public void setRouteDistinguisher(RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }
}
