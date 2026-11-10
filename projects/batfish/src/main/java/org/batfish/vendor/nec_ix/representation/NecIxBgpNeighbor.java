package org.batfish.vendor.nec_ix.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** A BGP peer, from {@code neighbor <address> ...} commands. */
public final class NecIxBgpNeighbor implements Serializable {

  private final @Nonnull Ip _address;
  private @Nullable Long _remoteAs;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;
  private boolean _sendDefault;
  private boolean _shutdown;
  private @Nullable String _updateSource;
  private @Nullable Integer _ebgpMultihop;
  private boolean _nextHopSelf;

  public NecIxBgpNeighbor(Ip address) {
    _address = address;
    // "Default: send (neighbor ADDRESS send-default)"
    _sendDefault = true;
  }

  public @Nonnull Ip getAddress() {
    return _address;
  }

  public @Nullable Long getRemoteAs() {
    return _remoteAs;
  }

  public void setRemoteAs(@Nullable Long remoteAs) {
    _remoteAs = remoteAs;
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

  /** Whether the default route may be advertised to this peer. */
  public boolean getSendDefault() {
    return _sendDefault;
  }

  public void setSendDefault(boolean sendDefault) {
    _sendDefault = sendDefault;
  }

  public boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  /** The interface from {@code update-source}, whose address the session is sourced from. */
  public @Nullable String getUpdateSource() {
    return _updateSource;
  }

  public void setUpdateSource(@Nullable String updateSource) {
    _updateSource = updateSource;
  }

  public @Nullable Integer getEbgpMultihop() {
    return _ebgpMultihop;
  }

  public void setEbgpMultihop(@Nullable Integer ebgpMultihop) {
    _ebgpMultihop = ebgpMultihop;
  }

  public boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }
}
