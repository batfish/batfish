package org.batfish.representation.frr;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** IPv4 unicast BGP configuration for a neighbor (or peer group) */
public class BgpNeighborIpv4UnicastAddressFamily implements Serializable {

  public enum RemovePrivateAsMode {
    NONE,
    BASIC,
    ALL,
    REPLACE_AS,
  }

  private @Nullable Boolean _activated;
  private @Nullable Integer _allowAsIn;
  private @Nullable Boolean _defaultOriginate;
  private @Nullable String _defaultOriginateRouteMap;
  private @Nullable RemovePrivateAsMode _removePrivateAsMode;
  private @Nullable Boolean _routeReflectorClient;
  private @Nullable Boolean _nextHopSelf;
  private @Nullable Boolean _nextHopSelfAll;
  private @Nullable String _routeMapIn;
  private @Nullable String _routeMapOut;

  /** Whether this address family has been explicitly activated for this neighbor */
  public @Nullable Boolean getActivated() {
    return _activated;
  }

  public void setActivated(@Nullable Boolean activated) {
    _activated = activated;
  }

  public @Nullable RemovePrivateAsMode getRemovePrivateAsMode() {
    return _removePrivateAsMode;
  }

  public void setRemovePrivateAsMode(RemovePrivateAsMode removePrivateAsMode) {
    _removePrivateAsMode = removePrivateAsMode;
  }

  /** Whether the neighbor is a route reflector client */
  public @Nullable Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  /** Whether to set next-hop to the device's IP in iBGP advertisements to the neighbor. */
  public @Nullable Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  public @Nullable Boolean getNextHopSelfAll() {
    return _nextHopSelfAll;
  }

  public void setNextHopSelfAll(boolean nextHopSelfAll) {
    _nextHopSelfAll = nextHopSelfAll;
  }

  void inheritFrom(@Nonnull BgpNeighborIpv4UnicastAddressFamily other) {
    if (_activated == null) {
      _activated = other.getActivated();
    }

    if (_allowAsIn == null) {
      _allowAsIn = other.getAllowAsIn();
    }

    if (_defaultOriginate == null) {
      _defaultOriginate = other._defaultOriginate;
      _defaultOriginateRouteMap = other._defaultOriginateRouteMap;
    }

    if (_nextHopSelf == null) {
      _nextHopSelf = other._nextHopSelf;
      _nextHopSelfAll = other._nextHopSelfAll;
    }

    if (_removePrivateAsMode == null) {
      _removePrivateAsMode = other.getRemovePrivateAsMode();
    }

    if (_routeMapIn == null) {
      _routeMapIn = other.getRouteMapIn();
    }

    if (_routeMapOut == null) {
      _routeMapOut = other.getRouteMapOut();
    }

    if (_routeReflectorClient == null) {
      _routeReflectorClient = other.getRouteReflectorClient();
    }
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

  public @Nullable Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  public @Nullable Boolean getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable Boolean defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }

  public @Nullable String getDefaultOriginateRouteMap() {
    return _defaultOriginateRouteMap;
  }

  public void setDefaultOriginateRouteMap(@Nullable String defaultOriginateRouteMap) {
    _defaultOriginateRouteMap = defaultOriginateRouteMap;
  }
}
