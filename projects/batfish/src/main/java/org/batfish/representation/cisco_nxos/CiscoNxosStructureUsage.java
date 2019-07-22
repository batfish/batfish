package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum CiscoNxosStructureUsage implements StructureUsage {
  INTERFACE_CHANNEL_GROUP("interface channel-group"),
  INTERFACE_SELF_REFERENCE("interface self-reference"),
  INTERFACE_VLAN("interface vlan"),
  INTERFACE_VRF_MEMBER("interface vrf member"),
  IP_ROUTE_NEXT_HOP_INTERFACE("ip route next-hop-interface"),
  IP_ROUTE_NEXT_HOP_VRF("ip route vrf"),
  NVE_SELF_REFERENCE("interface nve"),
  NVE_SOURCE_INTERFACE("interface nve source-interface"),
  OSPF_AREA_FILTER_LIST_IN("router ospf area filter-list in"),
  OSPF_AREA_FILTER_LIST_OUT("router ospf area filter-list out"),
  ROUTE_MAP_MATCH_COMMUNITY("route-map match community"),
  ROUTE_MAP_MATCH_IP_ADDRESS("route-map match ip address"),
  ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST("route-map match ip address prefix-list");

  private final @Nonnull String _description;

  private CiscoNxosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
