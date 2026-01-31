package org.batfish.vendor.cisco_nxos.representation;

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
    _maximumPathsEbgp = 1; // multipath disabled by default
    _maximumPathsIbgp = 1; // multipath disabled by default
    _retainMode = RetainRouteType.UNSET;
  }

  public final int getMaximumPathsEbgp() {
    return _maximumPathsEbgp;
  }

  public final void setMaximumPathsEbgp(int maximumPathsEbgp) {
    _maximumPathsEbgp = maximumPathsEbgp;
  }

  public final int getMaximumPathsIbgp() {
    return _maximumPathsIbgp;
  }

  public final void setMaximumPathsIbgp(int maximumPathsIbgp) {
    _maximumPathsIbgp = maximumPathsIbgp;
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

  private int _maximumPathsEbgp;
  private int _maximumPathsIbgp;
  private @Nonnull RetainRouteType _retainMode;
  private @Nullable String _retainRouteMap;
}
