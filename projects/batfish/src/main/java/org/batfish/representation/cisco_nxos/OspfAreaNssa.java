package org.batfish.representation.cisco_nxos;

import javax.annotation.Nullable;

public class OspfAreaNssa implements OspfAreaTypeSettings {

  public boolean getNoRedistribution() {
    return _noRedistribution;
  }

  public void setNoRedistribution(boolean noRedistribution) {
    _noRedistribution = noRedistribution;
  }

  public boolean getNoSummary() {
    return _noSummary;
  }

  public void setNoSummary(boolean noSummary) {
    _noSummary = noSummary;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private boolean _noRedistribution;
  private boolean _noSummary;
  private @Nullable String _routeMap;
}
