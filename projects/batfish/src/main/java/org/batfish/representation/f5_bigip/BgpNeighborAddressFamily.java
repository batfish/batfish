package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Neighbor-level BGP address-family configuration */
@ParametersAreNonnullByDefault
public abstract class BgpNeighborAddressFamily implements Serializable {

  private boolean _activate;

  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;

  public BgpNeighborAddressFamily() {
    _activate = true;
  }

  public boolean getActivate() {
    return _activate;
  }

  public void setActivate(boolean activate) {
    _activate = activate;
  }

  public @Nullable String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  public @Nullable String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }
}
