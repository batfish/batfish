package org.batfish.representation.cisco_nxos;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the BGP redistribution policy configuration for Cisco NX-OS.
 *
 * <p>These commands can only be configured at the {@code config-router-af} or {@code
 * config-router-vrf-af} levels.
 */
public final class BgpRedistributionPolicy implements Serializable {

  public BgpRedistributionPolicy(String routeMap, @Nullable String sourceTag) {
    _routeMap = routeMap;
    _sourceTag = sourceTag;
  }

  @Nonnull
  public String getRouteMap() {
    return _routeMap;
  }

  @Nullable
  public String getSourceTag() {
    return _sourceTag;
  }

  //////////////////////////////////////////
  ///// Private implementation details /////
  //////////////////////////////////////////

  private final @Nonnull String _routeMap;
  private final @Nullable String _sourceTag;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof BgpRedistributionPolicy)) {
      return false;
    }
    BgpRedistributionPolicy that = (BgpRedistributionPolicy) o;
    return _routeMap.equals(that._routeMap) && Objects.equals(_sourceTag, that._sourceTag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_routeMap, _sourceTag);
  }
}
