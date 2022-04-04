package org.batfish.representation.juniper;

import java.io.Serializable;

public class SwitchOptions implements Serializable {

  private String _vtepSourceInterface;
  private org.batfish.datamodel.bgp.RouteDistinguisher _routeDistinguisher;

  public String getVtepSourceInterface() {
    return _vtepSourceInterface;
  }

  public org.batfish.datamodel.bgp.RouteDistinguisher getRouteDistinguisher() {
    return _routeDistinguisher;
  }

  public void setVtepSourceInterface(String vtepSourceInterface) {
    _vtepSourceInterface = vtepSourceInterface;
  }

  public void setRouteDistinguisher(
      org.batfish.datamodel.bgp.RouteDistinguisher routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }
}
