package org.batfish.representation.cumulus;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** IPv4 unicast BGP configuration for a neighbor (or peer group) */
public class BgpNeighborIpv4UnicastAddressFamily implements Serializable {
  @Nullable private Boolean _activated;
  @Nullable private Boolean _routeReflectorClient;
  @Nullable private Boolean _nextHopSelf;

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

  void inheritFrom(@Nonnull BgpNeighborIpv4UnicastAddressFamily other) {
    if (_activated == null) {
      _activated = other.getActivated();
    }

    if (_routeReflectorClient == null) {
      _routeReflectorClient = other.getRouteReflectorClient();
    }
  }
}
