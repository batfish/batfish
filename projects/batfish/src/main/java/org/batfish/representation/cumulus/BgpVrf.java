package org.batfish.representation.cumulus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/** BGP configuration for a particular VRF. */
public class BgpVrf implements Serializable {

  private static final long serialVersionUID = 1L;

  private @Nullable Long _autonomousSystem;
  private final @Nonnull Map<String, BgpInterfaceNeighbor> _interfaceNeighbors;
  private @Nullable BgpIpv4UnicastAddressFamily _ipv4Unicast;
  private @Nullable BgpL2vpnEvpnAddressFamily _l2VpnEvpn;
  private @Nullable Ip _routerId;
  private final @Nonnull String _vrfName;

  public BgpVrf(String vrfName) {
    _vrfName = vrfName;
    _interfaceNeighbors = new HashMap<>();
  }

  public @Nullable Long getAutonomousSystem() {
    return _autonomousSystem;
  }

  public @Nonnull Map<String, BgpInterfaceNeighbor> getInterfaceNeighbors() {
    return _interfaceNeighbors;
  }

  public @Nullable BgpIpv4UnicastAddressFamily getIpv4Unicast() {
    return _ipv4Unicast;
  }

  public @Nullable BgpL2vpnEvpnAddressFamily getL2VpnEvpn() {
    return _l2VpnEvpn;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public @Nonnull String getVrfName() {
    return _vrfName;
  }

  public void setAutonomousSystem(@Nullable Long autonomousSystem) {
    _autonomousSystem = autonomousSystem;
  }

  public void setIpv4Unicast(@Nullable BgpIpv4UnicastAddressFamily ipv4Unicast) {
    _ipv4Unicast = ipv4Unicast;
  }

  public void setL2VpnEvpn(@Nullable BgpL2vpnEvpnAddressFamily l2VpnEvpn) {
    _l2VpnEvpn = l2VpnEvpn;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }
}
