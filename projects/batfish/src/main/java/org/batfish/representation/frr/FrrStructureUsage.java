package org.batfish.representation.frr;

import javax.annotation.Nonnull;
import org.batfish.vendor.StructureUsage;

public enum FrrStructureUsage implements StructureUsage {
  BGP_ADDRESS_FAMILY_IPV4_IMPORT_VRF("bgp address-family ipv4 unicast import vrf"),
  BGP_ADDRESS_FAMILY_IPV6_IMPORT_VRF("bgp address-family ipv6 unicast import vrf"),
  BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV4_UNICAST(
      "bgp address-family l2vpn advertise ipv4 unicast"),
  BGP_ADDRESS_FAMILY_L2VPN_ADVERTISE_IPV6_UNICAST(
      "bgp address-family l2vpn advertise ipv6 unicast"),
  BGP_AGGREGATE_ADDRESS_ROUTE_MAP("bgp address-family aggregate-address route-map"),
  BGP_AGGREGATE_ADDRESS_SUPPRESS_MAP("bgp address-family aggregate-address suppress-map"),
  BGP_IPV4_UNICAST_REDISTRIBUTE_CONNECTED_ROUTE_MAP(
      "bgp ipv4 unicast redistribute connected route-map"),
  BGP_IPV4_UNICAST_REDISTRIBUTE_STATIC_ROUTE_MAP("bgp ipv4 unicast redistribute static route-map"),
  BGP_IPV4_UNICAST_REDISTRIBUTE_OSPF_ROUTE_MAP("bgp ipv4 unicast redistribute ospf route-map"),
  BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_IN("bgp ipv4 unicast neighbor route-map in"),
  BGP_IPV4_UNICAST_NEIGHBOR_ROUTE_MAP_OUT("bgp ipv4 unicast neighbor route-map out"),
  BGP_IPV4_UNICAST_NEIGHBOR_DEFAULT_ORIGINATE_ROUTE_MAP(
      "bgp ipv4 unicast neighbor default-originate"),
  BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_IN("bgp l2vpn evpn neighbor route-map in"),
  BGP_L2VPN_EVPN_NEIGHBOR_ROUTE_MAP_OUT("bgp l2vpn evpn neighbor route-map out"),
  BGP_NEIGHBOR_INTERFACE("bgp neighbor interface"),
  BGP_NETWORK("bgp network"),
  BGP_VRF("bgp vrf"),
  OSPF_REDISTRIBUTE_BGP_ROUTE_MAP("ospf redistribute bgp route-map"),
  OSPF_REDISTRIBUTE_CONNECTED_ROUTE_MAP("ospf redistribute connected route-map"),
  OSPF_REDISTRIBUTE_STATIC_ROUTE_MAP("ospf redistribute static route-map"),
  STATIC_ROUTE_VRF("static route vrf"),
  ROUTE_MAP_CALL("route map call"),
  ROUTE_MAP_ENTRY_SELF_REFERENCE("route-map entry self-reference"),
  ROUTE_MAP_MATCH_COMMUNITY_LIST("route-map match community"),
  ROUTE_MAP_MATCH_INTERFACE("route-map match interface"),
  ROUTE_MAP_MATCH_AS_PATH("route-map match as-path"),
  ROUTE_MAP_MATCH_IP_ADDRESS_PREFIX_LIST("route-map match ip prefix-list"),
  ROUTE_MAP_SET_COMM_LIST_DELETE("route-map set comm-list delete");

  private final @Nonnull String _description;

  FrrStructureUsage(String description) {
    _description = description;
  }

  @Override
  public @Nonnull String getDescription() {
    return _description;
  }
}
