package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureType;

public enum CiscoNxosStructureType implements StructureType {
  BGP_TEMPLATE_PEER("bgp template peer"),
  BGP_TEMPLATE_PEER_POLICY("bgp template peer-policy"),
  BGP_TEMPLATE_PEER_SESSION("bgp template peer-session"),
  INTERFACE("interface"),
  IP_ACCESS_LIST("ip access-list"),
  IP_ACCESS_LIST_ABSTRACT_REF("ip[v4|v6] access-list"),
  IP_AS_PATH_ACCESS_LIST("ip as-path access-list"),
  IP_COMMUNITY_LIST_ABSTRACT_REF("ip community-list [expanded|standard]"),
  IP_COMMUNITY_LIST_EXPANDED("ip community-list expanded"),
  IP_COMMUNITY_LIST_STANDARD("ip community-list standard"),
  IP_PREFIX_LIST("ip prefix-list"),
  IPV6_ACCESS_LIST("ipv6 access-list"),
  IPV6_PREFIX_LIST("ipv6 prefix-list"),
  NVE("nve"),
  OBJECT_GROUP_IP_ADDRESS("object-group ip address"),
  OSPF_AREA("router ospf area"),
  PORT_CHANNEL("port-channel"),
  ROUTE_MAP("route-map"),
  ROUTE_MAP_ENTRY("route-map entry"),
  ROUTER_EIGRP("router eigrp"),
  ROUTER_ISIS("router isis"),
  ROUTER_OSPF("router ospf"),
  ROUTER_OSPFV3("router ospfv3"),
  ROUTER_RIP("router rip"),
  VLAN("vlan"),
  VRF("vrf");

  private final @Nonnull String _description;

  private CiscoNxosStructureType(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
