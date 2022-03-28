package org.batfish.representation.juniper;

import java.io.Serializable;

public class SwitchOptions implements Serializable {

  private String _vtepSourceInterface;
  private String _routeDistinguisher;

  public String getVtepSourceInterface() { return _vtepSourceInterface; }

  public String getRouteDistinguisher() { return _routeDistinguisher; }

  public void setVtepSourceInterface(String vtepSourceInterface) {
    _vtepSourceInterface = vtepSourceInterface;
  }

  public void setRouteDistinguisher(String routeDistinguisher) {
    _routeDistinguisher = routeDistinguisher;
  }
}
