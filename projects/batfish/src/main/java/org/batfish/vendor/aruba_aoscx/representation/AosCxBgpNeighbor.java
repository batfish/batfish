package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** Vendor-specific representation of an Aruba AOS-CX BGP neighbor. */
public final class AosCxBgpNeighbor implements Serializable {

  public AosCxBgpNeighbor(Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(long remoteAs) {
    _remoteAs = remoteAs;
  }

  public boolean getIpv4UnicastActive() {
    return _ipv4UnicastActive;
  }

  public void setIpv4UnicastActive(boolean ipv4UnicastActive) {
    _ipv4UnicastActive = ipv4UnicastActive;
  }

  public @Nullable String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  public @Nullable String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  private final @Nonnull Ip _ip;
  private @Nullable Long _remoteAs;
  private boolean _ipv4UnicastActive;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;
}
