package org.batfish.representation.cumulus;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum CumulusStructureUsage implements StructureUsage {
  BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP(
      "bgp ipv4 unicast redistribute connected route-map"),
  BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP("bgp ipv4 unicast redistribute static route-map"),
  BGP_NEIGHBOR_INTERFACE("bgp neighbor interface"),
  BGP_VRF("bgp vrf"),
  BOND_SELF_REFERENCE("bond self-reference"),
  BOND_SLAVE("bond slave"),
  BOND_VRF("bond vrf"),
  BRIDGE_PORT("bridge ports"),
  INTERFACE_CLAG_BACKUP_IP_VRF("interface clag backup-ip vrf"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VRF("interface vrf"),
  LOOPBACK_SELF_REFERENCE("loopback self-reference"),
  ROUTE_MAP_MATCH_INTERFACE("route-map match interface"),
  VLAN_SELF_REFERENCE("vlan self-reference"),
  VLAN_VRF("vlan vrf"),
  VRF_SELF_REFERENCE("vrf self-reference"),
  VXLAN_SELF_REFERENCE("vxlan self-reference");

  private final @Nonnull String _description;

  private CumulusStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
