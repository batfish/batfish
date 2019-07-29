package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents the BGP configuration for the {@link Type#L2VPN_EVPN} address family at the VRF level.
 */
public final class BgpVrfL2VpnEvpnAddressFamilyConfiguration
    extends BgpVrfAddressFamilyConfiguration {
  public enum RetainRouteType {
    UNSET,
    ALL,
    ROUTE_MAP,
  }

  public BgpVrfL2VpnEvpnAddressFamilyConfiguration() {
    _retainMode = RetainRouteType.UNSET;
  }

  public @Nonnull RetainRouteType getRetainMode() {
    return _retainMode;
  }

  public void setRetainMode(RetainRouteType retainMode) {
    _retainMode = retainMode;
  }

  /** Only valid if {@link #getRetainMode()} is {@link RetainRouteType#ROUTE_MAP}. */
  public @Nullable String getRetainRouteMap() {
    return _retainRouteMap;
  }

  public void setRetainRouteMap(@Nullable String retainRouteMap) {
    _retainRouteMap = retainRouteMap;
  }

  private @Nonnull RetainRouteType _retainMode;
  private @Nullable String _retainRouteMap;
}
