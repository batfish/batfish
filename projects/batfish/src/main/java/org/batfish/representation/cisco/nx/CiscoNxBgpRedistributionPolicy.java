package org.batfish.representation.cisco.nx;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the BGP redistribution policy configuration for Cisco NX-OS.
 *
 * <p>These commands can only be configured at the {@code config-router-af} or {@code
 * config-router-vrf-af} levels.
 */
public final class CiscoNxBgpRedistributionPolicy implements Serializable {
  private static final long serialVersionUID = 1L;

  public CiscoNxBgpRedistributionPolicy(String routeMap) {
    this._routeMap = routeMap;
  }

  @Nonnull
  public String getRouteMap() {
    return _routeMap;
  }

  @Nullable
  public String getSourceTag() {
    return _sourceTag;
  }

  public void setSourceTag(@Nullable String sourceTag) {
    _sourceTag = sourceTag;
  }

  private final String _routeMap;
  @Nullable private String _sourceTag;
}
