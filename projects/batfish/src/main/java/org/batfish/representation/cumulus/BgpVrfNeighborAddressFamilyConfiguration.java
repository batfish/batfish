package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nullable;

public class BgpVrfNeighborAddressFamilyConfiguration implements Serializable {

  private boolean _nextHopSelf;

  private @Nullable String _routeMapIn;

  private @Nullable String _routeMapOut;

  public BgpVrfNeighborAddressFamilyConfiguration() {
    _nextHopSelf = false;
  }

  public Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  @Nullable
  public String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(@Nullable String routemapIn) {
    _routeMapIn = routemapIn;
  }

  @Nullable
  public String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }
}
