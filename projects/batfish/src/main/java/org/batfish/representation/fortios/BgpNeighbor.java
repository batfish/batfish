package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** FortiOS datamodel component containing BGP neighbor configuration */
public class BgpNeighbor implements Serializable {
  public BgpNeighbor(Ip ip) {
    _ip = ip;
  }

  public @Nonnull Ip getIp() {
    return _ip;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public @Nullable String getRouteMapIn() {
    return _routeMapIn;
  }

  public @Nullable String getRouteMapOut() {
    return _routeMapOut;
  }

  public @Nullable String getUpdateSource() {
    return _updateSource;
  }

  public @Nullable Boolean getBfd() {
    return _bfd;
  }

  /** Effective BFD setting: {@code true} if BFD is enabled. */
  public boolean getBfdEffective() {
    return Boolean.TRUE.equals(_bfd);
  }

  public void setRemoteAs(Long remoteAs) {
    _remoteAs = remoteAs;
  }

  public void setRouteMapIn(String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  public void setRouteMapOut(String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  public void setUpdateSource(String updateSource) {
    _updateSource = updateSource;
  }

  public void setBfd(Boolean bfd) {
    _bfd = bfd;
  }

  private final @Nonnull Ip _ip;
  private @Nullable Long _remoteAs;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;
  private @Nullable String _updateSource;
  private @Nullable Boolean _bfd;
}
