package org.batfish.representation.palo_alto;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Configuration of a BGP Peer Connection {@code network virtual-router NAME protocol bgp peer-group
 * NAME peer NAME connection-options}.
 */
public final class BgpConnectionOptions implements Serializable {
  public @Nullable Boolean getIncomingAllow() {
    return _incomingAllow;
  }

  public void setIncomingAllow(Boolean incomingAllow) {
    _incomingAllow = incomingAllow;
  }

  public @Nullable Integer getLocalPort() {
    return _localPort;
  }

  public void setLocalPort(Integer localPort) {
    _localPort = localPort;
  }

  public @Nullable Boolean getOutgoingAllow() {
    return _outgoingAllow;
  }

  public void setOutgoingAllow(Boolean outgoingAllow) {
    _outgoingAllow = outgoingAllow;
  }

  public @Nullable Integer getRemotePort() {
    return _remotePort;
  }

  public void setRemotePort(Integer remotePort) {
    _remotePort = remotePort;
  }

  public @Nullable Integer getHoldTime() {
    return _holdTime;
  }

  public void setHoldTime(Integer holdTime) {
    _holdTime = holdTime;
  }

  public @Nullable Integer getIdleHoldTime() {
    return _idleHoldTime;
  }

  public void setIdleHoldTime(Integer idleHoldTime) {
    _idleHoldTime = idleHoldTime;
  }

  public @Nullable Integer getKeepAliveInterval() {
    return _keepAliveInterval;
  }

  public void setKeepAliveInterval(Integer keepAliveInterval) {
    _keepAliveInterval = keepAliveInterval;
  }

  public @Nullable Integer getMinRouteAdvInterval() {
    return _minRouteAdvInterval;
  }

  public void setMinRouteAdvInterval(Integer minRouteAdvInterval) {
    _minRouteAdvInterval = minRouteAdvInterval;
  }

  public @Nullable Integer getOpenDelayTime() {
    return _openDelayTime;
  }

  public void setOpenDelayTime(Integer openDelayTime) {
    _openDelayTime = openDelayTime;
  }

  // private implementation details

  private @Nullable Boolean _incomingAllow;
  private @Nullable Integer _holdTime;
  private @Nullable Integer _idleHoldTime;
  private @Nullable Integer _keepAliveInterval;
  private @Nullable Integer _minRouteAdvInterval;
  private @Nullable Integer _openDelayTime;
  private @Nullable Integer _remotePort;
  private @Nullable Boolean _outgoingAllow;
  private @Nullable Integer _localPort;
}
