package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;

/** Represents a redistribution policy configuration for Cisco NX-OS. */
public final class RedistributionPolicy implements Serializable {
  public RedistributionPolicy(RoutingProtocolInstance instance, String routeMap) {
    _instance = instance;
    _routeMap = routeMap;
  }

  public @Nonnull RoutingProtocolInstance getInstance() {
    return _instance;
  }

  public @Nonnull String getRouteMap() {
    return _routeMap;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull RoutingProtocolInstance _instance;
  private final @Nonnull String _routeMap;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RedistributionPolicy)) {
      return false;
    }
    RedistributionPolicy that = (RedistributionPolicy) o;
    return Objects.equals(_instance, that._instance) && _routeMap.equals(that._routeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_instance, _routeMap);
  }
}
