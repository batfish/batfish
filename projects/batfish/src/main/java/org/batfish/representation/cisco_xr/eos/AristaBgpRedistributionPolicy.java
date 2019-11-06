package org.batfish.representation.cisco_xr.eos;

import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Combination of what type of routes to redistribute and route map to apply, if any */
public class AristaBgpRedistributionPolicy implements Serializable {
  @Nonnull private final AristaRedistributeType _type;
  @Nullable private final String _routeMap;

  public AristaBgpRedistributionPolicy(AristaRedistributeType type, @Nullable String routeMap) {
    _type = type;
    _routeMap = routeMap;
  }

  @Nonnull
  public AristaRedistributeType getType() {
    return _type;
  }

  @Nullable
  public String getRouteMap() {
    return _routeMap;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AristaBgpRedistributionPolicy)) {
      return false;
    }
    AristaBgpRedistributionPolicy that = (AristaBgpRedistributionPolicy) o;
    return _type == that._type && Objects.equals(_routeMap, that._routeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_type, _routeMap);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(AristaBgpRedistributionPolicy.class)
        .add("type", _type)
        .add("routeMap", _routeMap)
        .toString();
  }
}
