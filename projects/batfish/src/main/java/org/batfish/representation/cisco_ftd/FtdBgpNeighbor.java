package org.batfish.representation.cisco_ftd;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

public class FtdBgpNeighbor implements Serializable {

  public FtdBgpNeighbor(Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public @Nullable String getDescription() {
    return _description;
  }

  public void setDescription(@Nullable String description) {
    _description = description;
  }

  public @Nullable Integer getKeepalive() {
    return _keepalive;
  }

  public void setKeepalive(@Nullable Integer keepalive) {
    _keepalive = keepalive;
  }

  public @Nullable Integer getHoldTime() {
    return _holdTime;
  }

  public void setHoldTime(@Nullable Integer holdTime) {
    _holdTime = holdTime;
  }

  public @Nullable String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  public @Nullable String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  public boolean isIpv4UnicastActive() {
    return _ipv4UnicastActive;
  }

  public void setIpv4UnicastActive(boolean ipv4UnicastActive) {
    _ipv4UnicastActive = ipv4UnicastActive;
  }

  private final Ip _ip;
  private @Nullable Long _remoteAs;
  private @Nullable String _description;
  private @Nullable Integer _keepalive;
  private @Nullable Integer _holdTime;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;
  private boolean _ipv4UnicastActive;
}
