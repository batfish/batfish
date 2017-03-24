package org.batfish.representation.cisco;

import org.batfish.representation.StructureUsage;

public enum CiscoStructureUsage implements StructureUsage {

   BGP_AGGREGATE_ATTRIBUTE_MAP("attribute-map"),
   BGP_DEFAULT_ORIGINATE_ROUTE_MAP("bgp default-originate route-map"),
   BGP_INBOUND_PREFIX_LIST("bgp inbound prefix-list"),
   BGP_INBOUND_PREFIX6_LIST("bgp inbound ipv6 prefix-list"),
   BGP_INBOUND_ROUTE_MAP("bgp inbound route-map"),
   BGP_INBOUND_ROUTE6_MAP("bgp inbound ipv6 route-map"),
   BGP_INHERITED_GROUP("inherited BGP group"),
   BGP_INHERITED_SESSION("inherited BGP peer-session"),
   BGP_NETWORK_ORIGINATION_ROUTE_MAP("bgp ipv4 network statement route-map"),
   BGP_NETWORK6_ORIGINATION_ROUTE_MAP("bgp ipv6 network statement route-map"),
   BGP_OUTBOUND_PREFIX_LIST("bgp outbound prefix-list"),
   BGP_OUTBOUND_PREFIX6_LIST("bgp outbound ipv6 prefix-list"),
   BGP_OUTBOUND_ROUTE_MAP("bgp outbound route-map"),
   BGP_OUTBOUND_ROUTE6_MAP("bgp outbound ipv6 route-map"),
   BGP_REDISTRIBUTE_CONNECTED_MAP("bgp redistribute connected route-map"),
   BGP_REDISTRIBUTE_STATIC_MAP("bgp redistribute static route-map"),
   BGP_ROUTE_MAP_OTHER("bgp otherwise in/outbound route-map"),
   BGP_UPDATE_SOURCE_INTERFACE("update-source interface"),
   BGP_VRF_AGGREGATE_ROUTE_MAP("bgp vrf aggregate-address route-map"),
   CLASS_MAP_ACCESS_GROUP("class-map access-group"),
   CONTROL_PLANE_ACCESS_GROUP("control-plane ip access-group"),
   INTERFACE_INCOMING_FILTER("interface incoming ip access-list"),
   INTERFACE_IP_VERIFY_ACCESS_LIST("interface ip verify access-list"),
   INTERFACE_OUTGOING_FILTER("interface outgoing ip access-list"),
   INTERFACE_PIM_NEIGHBOR_FILTER("interface ip pim neighbor-filter"),
   INTERFACE_POLICY_ROUTING_MAP("interface policy-routing route-map"),
   IP_NAT_SOURCE_ACCESS_LIST("ip nat source dynamic access-list"),
   LINE_ACCESS_CLASS_LIST("line access-class list"),
   LINE_ACCESS_CLASS_LIST6("line access-class ipv6 list"),
   MANAGEMENT_ACCESS_GROUP("management ip access-group"),
   MSDP_PEER_SA_LIST("msdp peer sa-list"),
   NTP_ACCESS_GROUP("ntp access-group"),
   OSPF_DEFAULT_ORIGINATE_ROUTE_MAP("ospf default-originate route-map"),
   OSPF_REDISTRIBUTE_BGP_MAP("ospf redistribute bgp route-map"),
   OSPF_REDISTRIBUTE_CONNECTED_MAP("ospf redistribute connected route-map"),
   OSPF_REDISTRIBUTE_STATIC_MAP("ospf redistribute static route-map"),
   PIM_ACL("pim acl"),
   PIM_ROUTE_MAP("pim route-map"),
   ROUTE_MAP_ADD_COMMUNITY("route-map set community additive"),
   ROUTE_MAP_CONTINUE("route-map continue target clause"),
   ROUTE_MAP_DELETE_COMMUNITY("route-map delete community"),
   ROUTE_MAP_MATCH_AS_PATH_ACCESS_LIST("route-map match as-path access-list"),
   ROUTE_MAP_MATCH_COMMUNITY_LIST("route-map match community-list"),
   ROUTE_MAP_MATCH_IP_ACCESS_LIST("route-map match ip access-list"),
   ROUTE_MAP_MATCH_IP_PREFIX_LIST("route-map match ip prefix-list"),
   ROUTE_MAP_MATCH_IPV6_ACCESS_LIST("route-map match ipv6 access-list"),
   ROUTE_MAP_MATCH_IPV6_PREFIX_LIST("route-map match ipv6 prefix-list"),
   ROUTE_MAP_SET_COMMUNITY("route-map set community list"),
   ROUTE_POLICY_AS_PATH_IN("route-policy as-path in"),
   ROUTE_POLICY_PREFIX_SET("route-policy prefix-set"),
   SNMP_ACCESS_LIST("snmp access-list"),
   SSH_IPV4_ACL("ssh ipv4 access-list"),
   SSH_IPV6_ACL("ssh ipv6 access-list");

   private final String _description;

   private CiscoStructureUsage(String description) {
      _description = description;
   }

   @Override
   public String getDescription() {
      return _description;
   }

}
