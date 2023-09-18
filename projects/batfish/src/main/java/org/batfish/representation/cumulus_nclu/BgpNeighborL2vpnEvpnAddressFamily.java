package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** L2VPN EVPN settings for BGP neighbor (or peer group) */
public class BgpNeighborL2vpnEvpnAddressFamily implements Serializable {
  private @Nullable Boolean _activated;
  private @Nullable Boolean _routeReflectorClient;

  /** Whether this address family has been explicitly activated for this neighbor */
  public @Nullable Boolean getActivated() {
    return _activated;
  }

  public void setActivated(@Nullable Boolean activated) {
    _activated = activated;
  }

  /** Whether the neighbor is a route reflector client */
  public @Nullable Boolean getRouteReflectorClient() {
    return _routeReflectorClient;
  }

  public void setRouteReflectorClient(@Nullable Boolean routeReflectorClient) {
    _routeReflectorClient = routeReflectorClient;
  }

  void inheritFrom(@Nonnull BgpNeighborL2vpnEvpnAddressFamily other) {
    if (_activated == null) {
      _activated = other.getActivated();
    }

    if (_routeReflectorClient == null) {
      _routeReflectorClient = other.getRouteReflectorClient();
    }
  }
}
