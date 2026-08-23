parser grammar AosCxParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = AosCxLexer;
}

aoscx_configuration
:
  (statement | NEWLINE)* EOF
;

statement
:
  s_access_list_ip
  | s_access_list_ipv6
  | s_acl_entry
  | s_apply_access_list_ip
  | s_apply_access_list_ipv6
  | s_default_gateway
  | s_description
  | s_hostname
  | s_interface
  | s_lag_member
  | s_vrf_attach
  | s_vrf
  | s_ip_address
  | s_ipv6_address
  | s_ipv6_ospfv3_area
  | s_ipv6_ospfv3_cost
  | s_ipv6_ospfv3_dead_interval
  | s_ipv6_ospfv3_hello_interval
  | s_ipv6_ospfv3_network
  | s_ipv6_ospfv3_priority
  | s_ipv6_ospfv3_retransmit_interval
  | s_ipv6_ospfv3_transit_delay
  | s_ipv6_ospfv3_passive
  | s_ipv6_ospfv3_shutdown
  | s_ospfv3_passive_default
  | s_ospfv3_reference_bandwidth
  | s_ipv6_route
  | s_ip_static
  | s_ip_mtu
  | s_ip_ospf_area
  | s_ip_ospf_cost
  | s_ip_ospf_network
  | s_ip_prefix_list
  | s_ipv6_prefix_list
  | s_ip_route
  | s_router_ospf
  | s_router_ospfv3
  | s_ospf_area
  | s_ospf_area_stub
  | s_ospf_area_nssa
  | s_ospfv3_area_default_metric
  | s_ospfv3_default_information
  | s_ospfv3_default_metric
  | s_ospfv3_distance
  | s_ospfv3_distribute_list
  | s_ospfv3_maximum_paths
  | s_ospfv3_process_state
  | s_redistribute_connected
  | s_redistribute_static
  | s_router_bgp
  | s_route_map
  | s_match_ip_address_prefix_list
  | s_match_ipv6_address_prefix_list
  | s_set_local_preference
  | s_set_metric
  | s_set_tag
  | s_bgp_router_id
  | s_bgp_neighbor_remote_as
  | s_bgp_address_family_ipv4
  | s_bgp_neighbor_activate
  | s_bgp_neighbor_route_map
  | s_router_id
  | s_mtu
  | s_no_routing
  | s_vlan_trunk_native
  | s_vlan_trunk_allowed
  | s_no_shutdown
  | s_shutdown
  | s_speed
  | null_statement
;

s_access_list_ip
:
  ACCESS_LIST IP WORD NEWLINE
;

s_access_list_ipv6
:
  ACCESS_LIST IPV6 WORD NEWLINE
;

s_acl_entry
:
  WORD? acl_action acl_protocol acl_address acl_src_port_spec? acl_address acl_dst_port_spec? COUNT? NEWLINE
;

acl_src_port_spec
:
  acl_port_spec
;

acl_dst_port_spec
:
  acl_port_spec
;

acl_port_spec
:
  EQ WORD
  | GT WORD
  | LT WORD
  | RANGE WORD WORD
;

acl_action
:
  PERMIT
  | DENY
;

acl_protocol
:
  ANY
  | IP
  | IPV6
  | OSPF
  | WORD
;

acl_address
:
  ANY
  | WORD
;

s_apply_access_list_ip
:
  APPLY ACCESS_LIST IP WORD acl_direction NEWLINE
;

s_apply_access_list_ipv6
:
  APPLY ACCESS_LIST IPV6 WORD acl_direction NEWLINE
;

acl_direction
:
  IN
  | OUT
  | ROUTED_IN
  | ROUTED_OUT
;



s_default_gateway
:
  DEFAULT_GATEWAY WORD NEWLINE
;

s_description
:
  DESCRIPTION (~NEWLINE)+ NEWLINE
;

s_hostname
:
  HOSTNAME WORD NEWLINE
;

s_interface
:
  INTERFACE interface_name NEWLINE
;

interface_name
:
  LOOPBACK WORD
  | VLAN WORD
  | LAG WORD
  | WORD
;

s_lag_member
:
  LAG WORD NEWLINE
;

s_vrf_attach
:
  VRF ATTACH WORD NEWLINE
;

s_vrf
:
  VRF WORD NEWLINE
;

s_ip_address
:
  IP ADDRESS WORD NEWLINE
;


s_ipv6_address
:
  IPV6 ADDRESS (LINK_LOCAL | WORD) NEWLINE
;

s_ipv6_ospfv3_area
:
  IPV6 OSPFV3 WORD AREA WORD NEWLINE
;

s_ipv6_ospfv3_cost
:
  IPV6 OSPFV3 COST WORD NEWLINE
  | NO IPV6 OSPFV3 COST NEWLINE
;

s_ipv6_ospfv3_dead_interval
:
  IPV6 OSPFV3 DEAD_INTERVAL WORD NEWLINE
  | NO IPV6 OSPFV3 DEAD_INTERVAL NEWLINE
;

s_ipv6_ospfv3_hello_interval
:
  IPV6 OSPFV3 HELLO_INTERVAL WORD NEWLINE
  | NO IPV6 OSPFV3 HELLO_INTERVAL NEWLINE
;

s_ipv6_ospfv3_network
:
  IPV6 OSPFV3 NETWORK
    (BROADCAST | POINT_TO_POINT) NEWLINE
  | NO IPV6 OSPFV3 NETWORK NEWLINE
;

s_ipv6_ospfv3_priority
:
  IPV6 OSPFV3 PRIORITY WORD NEWLINE
  | NO IPV6 OSPFV3 PRIORITY NEWLINE
;

s_ipv6_ospfv3_retransmit_interval
:
  IPV6 OSPFV3 RETRANSMIT_INTERVAL WORD NEWLINE
  | NO IPV6 OSPFV3 RETRANSMIT_INTERVAL NEWLINE
;

s_ipv6_ospfv3_transit_delay
:
  IPV6 OSPFV3 TRANSIT_DELAY WORD NEWLINE
  | NO IPV6 OSPFV3 TRANSIT_DELAY NEWLINE
;

s_ipv6_ospfv3_passive
:
  NO? IPV6 OSPFV3 PASSIVE NEWLINE
;

s_ipv6_ospfv3_shutdown
:
  IPV6 OSPFV3 SHUTDOWN NEWLINE
  | NO IPV6 OSPFV3 SHUTDOWN NEWLINE
;

s_ospfv3_passive_default
:
  NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

s_ospfv3_reference_bandwidth
:
  REFERENCE_BANDWIDTH WORD NEWLINE
  | NO REFERENCE_BANDWIDTH NEWLINE
;

s_ipv6_route
:
  IPV6 ROUTE WORD static_route_next_hop ipv6_static_route_option* NEWLINE
;

ipv6_static_route_option
:
  DISTANCE WORD
  | TAG WORD
  | VRF WORD
;

s_ip_static
:
  IP STATIC WORD NEWLINE
;

s_ip_mtu
:
  IP MTU WORD NEWLINE
;

s_ip_ospf_area
:
  IP OSPF WORD AREA WORD NEWLINE
;

s_ip_ospf_cost
:
  IP OSPF COST WORD NEWLINE
;

s_ip_ospf_network
:
  IP OSPF NETWORK POINT_TO_POINT NEWLINE
;


s_ip_prefix_list
:
  IP PREFIX_LIST WORD prefix_list_seq? prefix_list_action WORD prefix_list_ge? prefix_list_le? NEWLINE
;

s_ipv6_prefix_list
:
  IPV6 PREFIX_LIST WORD prefix_list_seq? prefix_list_action WORD prefix_list_ge? prefix_list_le? NEWLINE
;

prefix_list_seq
:
  SEQ WORD
;

prefix_list_action
:
  PERMIT
  | DENY
;

prefix_list_ge
:
  GE WORD
;

prefix_list_le
:
  LE WORD
;

s_ip_route
:
  IP ROUTE WORD static_route_next_hop (VRF WORD)? NEWLINE
;

static_route_next_hop
:
  NULLROUTE
  | REJECT
  | WORD
;




s_route_map
:
  ROUTE_MAP WORD route_map_action SEQ WORD NEWLINE
;

route_map_action
:
  PERMIT
  | DENY
;

s_match_ip_address_prefix_list
:
  MATCH IP ADDRESS PREFIX_LIST WORD NEWLINE
;

s_match_ipv6_address_prefix_list
:
  MATCH IPV6 ADDRESS PREFIX_LIST WORD NEWLINE
;

s_set_local_preference
:
  SET LOCAL_PREFERENCE WORD NEWLINE
;

s_set_metric
:
  SET METRIC WORD NEWLINE
;

s_set_tag
:
  SET TAG WORD NEWLINE
;

s_router_bgp
:
  ROUTER BGP WORD NEWLINE
;


s_bgp_neighbor_remote_as
:
  NEIGHBOR WORD REMOTE_AS WORD NEWLINE
;

s_bgp_address_family_ipv4
:
  ADDRESS_FAMILY IPV4 UNICAST NEWLINE
;

s_bgp_neighbor_activate
:
  NEIGHBOR WORD ACTIVATE NEWLINE
;

s_bgp_neighbor_route_map
:
  NEIGHBOR WORD ROUTE_MAP WORD (IN | OUT) NEWLINE
;

s_bgp_router_id
:
  BGP ROUTER_ID WORD NEWLINE
;

s_router_ospf
:
  ROUTER OSPF WORD (VRF WORD)? NEWLINE
;

s_router_ospfv3
:
  ROUTER OSPFV3 WORD (VRF WORD)? NEWLINE
;



s_ospf_area
:
  AREA WORD NEWLINE
;

s_ospf_area_stub
:
  NO? AREA WORD STUB NO_SUMMARY? NEWLINE
;

s_ospf_area_nssa
:
  NO? AREA WORD NSSA NO_SUMMARY? NEWLINE
;

s_ospfv3_area_default_metric
:
  AREA WORD DEFAULT_METRIC WORD NEWLINE
  | NO AREA WORD DEFAULT_METRIC NEWLINE
;

s_ospfv3_default_metric
:
  DEFAULT_METRIC WORD NEWLINE
  | NO DEFAULT_METRIC NEWLINE
;

ospfv3_distance_type
:
  INTRA_AREA
  | INTER_AREA
  | EXTERNAL
;

ospfv3_distance_value
:
  ospfv3_distance_type WORD
;

s_ospfv3_distance
:
  DISTANCE WORD NEWLINE
  | DISTANCE ospfv3_distance_value+ NEWLINE
  | NO DISTANCE ospfv3_distance_type? WORD? NEWLINE
;

s_ospfv3_distribute_list
:
  NO? DISTRIBUTE_LIST PREFIX WORD (IN | OUT) NEWLINE
;

s_ospfv3_maximum_paths
:
  MAXIMUM_PATHS WORD NEWLINE
  | NO MAXIMUM_PATHS NEWLINE
;

s_ospfv3_process_state
:
  ENABLE NEWLINE
  | DISABLE NEWLINE
;

ospfv3_metric_option
:
  METRIC WORD
;

s_ospfv3_default_information
:
  DEFAULT_INFORMATION ORIGINATE ALWAYS? ospfv3_metric_option? NEWLINE
  | NO DEFAULT_INFORMATION ORIGINATE ALWAYS? (METRIC WORD?)? NEWLINE
;

redistribute_route_map
:
  ROUTE_MAP WORD
;

s_redistribute_connected
:
  NO? REDISTRIBUTE CONNECTED redistribute_route_map? NEWLINE
;

s_redistribute_static
:
  NO? REDISTRIBUTE STATIC redistribute_route_map? NEWLINE
;

s_router_id
:
  ROUTER_ID WORD NEWLINE
;



s_mtu
:
  MTU WORD NEWLINE
;

s_no_routing
:
  NO ROUTING NEWLINE
;

s_vlan_trunk_native
:
  VLAN TRUNK NATIVE WORD TAG? NEWLINE
;

s_vlan_trunk_allowed
:
  VLAN TRUNK ALLOWED WORD NEWLINE
;

s_no_shutdown
:
  NO SHUTDOWN NEWLINE
;

s_shutdown
:
  SHUTDOWN NEWLINE
;

s_speed
:
  SPEED WORD+ NEWLINE
;

null_statement
:
  (ACCESS_LIST | ALLOWED | APPLY | ANY | CONNECTED | COST | COUNT | DEFAULT_GATEWAY | DISTANCE | EQ | GT | LT | RANGE | ROUTED_IN | ROUTED_OUT | ADDRESS | AREA | ATTACH | VRF | BGP | IN | OUT | UNICAST | REMOTE_AS | NEIGHBOR | IPV4 | IPV6 | ADDRESS_FAMILY | ACTIVATE | HOSTNAME | INTERFACE | IP | LOOPBACK | LINK_LOCAL | MTU | NATIVE | NETWORK | NO | NO_SUMMARY | NULLROUTE | OSPF | OSPFV3 | POINT_TO_POINT | REDISTRIBUTE | REJECT | ROUTE | ROUTER | ROUTER_ID | ROUTING | SHUTDOWN | SPEED | STATIC | STUB | TAG | TRUNK | VLAN | PREFIX_LIST | SEQ | PERMIT | DENY | GE | LE | ROUTE_MAP | MATCH | MAXIMUM_PATHS | SET | LOCAL_PREFERENCE | DEFAULT | PASSIVE | PASSIVE_INTERFACE | REFERENCE_BANDWIDTH | BROADCAST | DEAD_INTERVAL | HELLO_INTERVAL | DEFAULT_METRIC | ALWAYS | DEFAULT_INFORMATION | METRIC | ORIGINATE | DISABLE | ENABLE | EXTERNAL | INTER_AREA | INTRA_AREA | NSSA | DISTRIBUTE_LIST | PREFIX | PRIORITY | RETRANSMIT_INTERVAL | TRANSIT_DELAY | WORD)+ NEWLINE
;
