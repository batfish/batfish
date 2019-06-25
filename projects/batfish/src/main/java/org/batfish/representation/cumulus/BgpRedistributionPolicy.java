package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BgpRedistributionPolicy implements Serializable {

  private final @Nonnull CumulusRoutingProtocol _protocol;
  private final @Nullable String _routeMap;

  public BgpRedistributionPolicy(CumulusRoutingProtocol protocol, @Nullable String routeMap) {
    _protocol = protocol;
    _routeMap = routeMap;
  }

  public @Nonnull CumulusRoutingProtocol getProtocol() {
    return _protocol;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }
}
