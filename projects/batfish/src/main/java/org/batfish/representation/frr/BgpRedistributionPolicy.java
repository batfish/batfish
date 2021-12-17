package org.batfish.representation.frr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BgpRedistributionPolicy implements Serializable {

  private final @Nonnull FrrRoutingProtocol _protocol;
  private final @Nullable String _routeMap;

  public BgpRedistributionPolicy(FrrRoutingProtocol protocol, @Nullable String routeMap) {
    _protocol = protocol;
    _routeMap = routeMap;
  }

  public @Nonnull FrrRoutingProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }
}
