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
  | s_acl_entry
  | s_apply_access_list_ip
  | s_hostname
  | s_interface
  | s_vrf_attach
  | s_vrf
  | s_ip_address
  | s_ip_ospf_area
  | s_ip_ospf_network
  | s_ip_prefix_list
  | s_ip_route
  | s_router_ospf
  | s_router_bgp
  | s_route_map
  | s_match_ip_address_prefix_list
  | s_set_local_preference
  | s_bgp_router_id
  | s_bgp_neighbor_remote_as
  | s_bgp_address_family_ipv4
  | s_bgp_neighbor_activate
  | s_bgp_neighbor_route_map
  | s_router_id
  | s_no_shutdown
  | s_shutdown
  | s_speed
  | null_statement
;

s_access_list_ip
:
  ACCESS_LIST IP WORD NEWLINE
;

s_acl_entry
:
  WORD? acl_action acl_protocol acl_address acl_src_port_spec? acl_address acl_dst_port_spec? NEWLINE
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

acl_direction
:
  IN
  | OUT
  | ROUTED_IN
  | ROUTED_OUT
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
  | WORD
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


s_ip_ospf_area
:
  IP OSPF WORD AREA WORD NEWLINE
;

s_ip_ospf_network
:
  IP OSPF NETWORK POINT_TO_POINT NEWLINE
;


s_ip_prefix_list
:
  IP PREFIX_LIST WORD prefix_list_seq? prefix_list_action WORD prefix_list_ge? prefix_list_le? NEWLINE
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

s_set_local_preference
:
  SET LOCAL_PREFERENCE WORD NEWLINE
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
  ROUTER OSPF WORD NEWLINE
;

s_router_id
:
  ROUTER_ID WORD NEWLINE
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
  (ACCESS_LIST | APPLY | ANY | EQ | GT | LT | RANGE | ROUTED_IN | ROUTED_OUT | ADDRESS | AREA | ATTACH | VRF | BGP | IN | OUT | UNICAST | REMOTE_AS | NEIGHBOR | IPV4 | ADDRESS_FAMILY | ACTIVATE | HOSTNAME | INTERFACE | IP | LOOPBACK | NETWORK | NO | NULLROUTE | OSPF | POINT_TO_POINT | REJECT | ROUTE | ROUTER | ROUTER_ID | SHUTDOWN | SPEED | VLAN | PREFIX_LIST | SEQ | PERMIT | DENY | GE | LE | ROUTE_MAP | MATCH | SET | LOCAL_PREFERENCE | WORD)+ NEWLINE
;
