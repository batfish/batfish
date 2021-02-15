package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Represents a redistribution policy configuration for Cumulus FRR. */
public final class RedistributionPolicy implements Serializable {
  public RedistributionPolicy(
      CumulusRoutingProtocol cumulusRoutingProtocol, @Nullable String routeMap) {
    _cumulusRoutingProtocol = cumulusRoutingProtocol;
    _routeMap = routeMap;
  }

  public @Nonnull CumulusRoutingProtocol getCumulusRoutingProtocol() {
    return _cumulusRoutingProtocol;
  }

  public @Nullable String getRouteMap() {
    return _routeMap;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull CumulusRoutingProtocol _cumulusRoutingProtocol;
  private final @Nullable String _routeMap;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof RedistributionPolicy)) {
      return false;
    }
    RedistributionPolicy that = (RedistributionPolicy) o;
    return Objects.equals(_cumulusRoutingProtocol, that._cumulusRoutingProtocol)
        && Objects.equals(_routeMap, that._routeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_cumulusRoutingProtocol, _routeMap);
  }
}
