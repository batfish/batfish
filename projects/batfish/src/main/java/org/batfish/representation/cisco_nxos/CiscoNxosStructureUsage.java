package org.batfish.representation.cisco_nxos;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum CiscoNxosStructureUsage implements StructureUsage {
  BGP_ADDITIONAL_PATHS_ROUTE_MAP("bgp address-family additional-paths route-map"),
  BGP_ADVERTISE_MAP("bgp address-family advertise-map"),
  BGP_ATTRIBUTE_MAP("bgp address-family attribute-map"),
  BGP_DAMPENING_ROUTE_MAP("bgp address-family dampening route-map"),
  BGP_DEFAULT_ORIGINATE_ROUTE_MAP("bgp address-family default-originate route-map"),
  BGP_EXIST_MAP("bgp address-family exist-map"),
  BGP_INJECT_MAP("bgp address-family inject-map"),
  BGP_L2VPN_EVPN_RETAIN_ROUTE_TARGET_ROUTE_MAP(
      "bgp address-family l2vpn evpn retain-target route-map"),
  BGP_NEIGHBOR_ADVERTISE_MAP("bgp neighbor advertise-map"),
  BGP_NEIGHBOR_EXIST_MAP("bgp neighbor exist-map"),
  BGP_NEIGHBOR_FILTER_LIST_IN("bgp neighbor address-family filter-list in"),
  BGP_NEIGHBOR_FILTER_LIST_OUT("bgp neighbor address-family filter-list out"),
  BGP_NEIGHBOR6_FILTER_LIST_IN("bgp neighbor address-family [IPv6] filter-list in"),
  BGP_NEIGHBOR6_FILTER_LIST_OUT("bgp neighbor address-family [IPv6] filter-list out"),
  BGP_NEIGHBOR_INHERIT_PEER("bgp neighbor inherit peer"),
  BGP_NEIGHBOR_INHERIT_PEER_POLICY("bgp neighbor address-family inherit peer"),
  BGP_NEIGHBOR_INHERIT_PEER_SESSION("bgp neighbor inherit peer-session"),
  BGP_NEIGHBOR_PREFIX_LIST_IN("bgp neighbor address-family prefix-list in"),
  BGP_NEIGHBOR_PREFIX_LIST_OUT("bgp neighbor address-family prefix-list out"),
  BGP_NEIGHBOR6_PREFIX_LIST_IN("bgp neighbor address-family [IPv6] prefix-list in"),
  BGP_NEIGHBOR6_PREFIX_LIST_OUT("bgp neighbor address-family [IPv6] prefix-list out"),
  BGP_NEIGHBOR_NON_EXIST_MAP("bgp neighbor non-exist-map"),
  BGP_NEIGHBOR_REMOTE_AS_ROUTE_MAP("bgp neighbor remote-as route-map"),
  BGP_NEIGHBOR_ROUTE_MAP_IN("bgp neighbor address-family route-map in"),
  BGP_NEIGHBOR_ROUTE_MAP_OUT("bgp neighbor address-family route-map out"),
  BGP_NEIGHBOR_UPDATE_SOURCE("bgp neighbor update-source"),
  BGP_NETWORK_ROUTE_MAP("bgp address-family network route-map"),
  BGP_NETWORK6_ROUTE_MAP("bgp address-family [IPv6] network route-map"),
  BGP_NEXTHOP_ROUTE_MAP("bgp address-family nexthop route-map"),
  BGP_REDISTRIBUTE_INSTANCE("bgp address-family redistribute"),
  BGP_REDISTRIBUTE_ROUTE_MAP("bgp address-family redistribute route-map"),
  BGP_SUPPRESS_MAP("bgp address-family suppress-map"),
  BGP_TABLE_MAP("bgp address-family table-map"),
  BGP_UNSUPPRESS_MAP("bgp address-family unsuppress-map"),
  EIGRP_REDISTRIBUTE_INSTANCE("eigrp address-family redistribute"),
  EIGRP_REDISTRIBUTE_ROUTE_MAP("eigrp address-family redistribute route-map"),
  INTERFACE_CHANNEL_GROUP("interface channel-group"),
  INTERFACE_IP_ACCESS_GROUP_IN("interface ip access-group in"),
  INTERFACE_IP_ACCESS_GROUP_OUT("interface ip access-group out"),
  INTERFACE_IP_POLICY("interface ip policy"),
  INTERFACE_IP_ROUTER_EIGRP("interface ip router eigrp"),
  INTERFACE_IP_ROUTER_OSPF("interface ip router ospf"),
  INTERFACE_SELF_REFERENCE("interface"),
  INTERFACE_VLAN("interface vlan"),
  INTERFACE_VRF_MEMBER("interface vrf member"),
  IP_ACCESS_LIST_DESTINATION_ADDRGROUP("ip access-list destination addrgroup"),
  IP_ACCESS_LIST_SOURCE_ADDRGROUP("ip access-list source addrgroup"),
  IP_ROUTE_NEXT_HOP_INTERFACE("ip route next-hop-interface"),
  IP_ROUTE_NEXT_HOP_VRF("ip route vrf"),
  NTP_SOURCE_INTERFACE("ntp source-interface"),
  NVE_SELF_REFERENCE("interface nve"),
  NVE_SOURCE_INTERFACE("interface nve source-interface"),
  OSPF_AREA_FILTER_LIST_IN("router ospf area filter-list in"),
  OSPF_AREA_FILTER_LIST_OUT("router ospf area filter-list out"),
  ROUTE_MAP_CONTINUE("route-map continue"),
  ROUTE_MAP_MATCH_COMMUNITY("route-map match community"),
  ROUTE_MAP_MATCH_IP_ADDRESS("route-map match ip address"),
  ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST("route-map match ip address prefix-list"),
  ROUTER_EIGRP_SELF_REFERENCE("router eigrp");

  private final @Nonnull String _description;

  private CiscoNxosStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
