package org.batfish.representation.f5_bigip;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class BgpRedistributionPolicy implements Serializable {

  private final @Nonnull F5BigipRoutingProtocol _protocol;

  private @Nullable String _routeMap;

  public BgpRedistributionPolicy(F5BigipRoutingProtocol protocol) {
    _protocol = protocol;
  }

  public @Nonnull F5BigipRoutingProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  public void setRouteMap(@Nullable String routeMap) {
    _routeMap = routeMap;
  }
}
