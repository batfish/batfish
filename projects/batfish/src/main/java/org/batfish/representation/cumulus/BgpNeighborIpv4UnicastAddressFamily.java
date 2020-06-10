package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** IPv4 unicast BGP configuration for a neighbor (or peer group) */
public class BgpNeighborIpv4UnicastAddressFamily implements Serializable {
  @Nullable private Boolean _activated;
  @Nullable private Integer _allowAsIn;
  @Nullable private Boolean _defaultOriginate;
  @Nullable private Boolean _routeReflectorClient;
  @Nullable private Boolean _nextHopSelf;
  @Nullable private Boolean _nextHopSelfAll;
  @Nullable private String _routeMapIn;
  @Nullable private String _routeMapOut;

  /** Whether this address family has been explicitly activated for this neighbor */
  @Nullable
  public Boolean getActivated() {
    return _activated;
  }

  public void setActivated(@Nullable Boolean activated) {
    _activated = activated;
  }

  /** Whether the neighbor is a route reflector client */
  @Nullable
  public Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  /** Whether to set next-hop to the device's IP in iBGP advertisements to the neighbor. */
  @Nullable
  public Boolean getNextHopSelf() {
    return _nextHopSelf;
  }

  public void setNextHopSelf(boolean nextHopSelf) {
    _nextHopSelf = nextHopSelf;
  }

  @Nullable
  public Boolean getNextHopSelfAll() {
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
      _defaultOriginate = other.getDefaultOriginate();
    }

    if (_routeReflectorClient == null) {
      _routeReflectorClient = other.getRouteReflectorClient();
    }
  }

  @Nullable
  public String getRouteMapIn() {
    return _routeMapIn;
  }

  public void setRouteMapIn(@Nullable String routeMapIn) {
    _routeMapIn = routeMapIn;
  }

  @Nullable
  public String getRouteMapOut() {
    return _routeMapOut;
  }

  public void setRouteMapOut(@Nullable String routeMapOut) {
    _routeMapOut = routeMapOut;
  }

  @Nullable
  public Integer getAllowAsIn() {
    return _allowAsIn;
  }

  public void setAllowAsIn(@Nullable Integer allowAsIn) {
    _allowAsIn = allowAsIn;
  }

  @Nullable
  public Boolean getDefaultOriginate() {
    return _defaultOriginate;
  }

  public void setDefaultOriginate(@Nullable Boolean defaultOriginate) {
    _defaultOriginate = defaultOriginate;
  }
}
