parser grammar CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

bgp_asn
:
  large = uint32
  | high = uint16 PERIOD low = uint16
;

// Like bgp_asn, but when it cannot be a 4-byte ASN a dotted ASN
bgp_asn_simple
:
  // 1-4294967295
  uint32
;

bgp_asn_range
:
  intervals += bgp_asn_interval
  (
    COMMA intervals += bgp_asn_interval
  )*
;

bgp_asn_interval
:
  low = bgp_asn_simple
  (
    DASH high = bgp_asn_simple
  )?
;


// Shared NX-OS syntax for route-target, redistribution, route leak, etc.
both_export_import
:
  BOTH
  | EXPORT
  | IMPORT
;

cisco_nxos_password
:
// Lexer mode change required prior to entering this rule
  PASSWORD_0? text = PASSWORD_0_TEXT
  | PASSWORD_3
  (
    text = PASSWORD_3_TEXT
    | malformed = PASSWORD_3_MALFORMED_TEXT
  )
  | PASSWORD_7
  (
    PASSWORD_7_TEXT
    | malformed = PASSWORD_7_MALFORMED_TEXT
  )
;

class_map_cp_name
:
// 1-64 characters
  WORD
;

class_map_network_qos_name
:
// 1-40 characters
  WORD
;

class_map_qos_name
:
// 1-40 characters
  WORD
;

class_map_queuing_name
:
// 1-40 characters
  WORD
;

double_quoted_string
:
  DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE
;

fex_id
:
// 101-199
  uint8
;

// ip or mac access-list
generic_access_list_name
:
// 1-64 characters
  WORD
;

group_name
:
// 1-32 characters
  WORD
;

hex_uint32
:
  HEX_UINT32
;

hsrp_name
:
// 1-250 characters
  WORD
;

interface_address
:
  address = ip_address mask = subnet_mask
  | iaddress = ip_prefix
;

interface_ipv6_address
:
  address6_with_length = ipv6_prefix
;

interface_name
:
  prefix = interface_prefix middle = interface_middle? parent_suffix =
  interface_parent_suffix? first = uint16
;

interface_prefix
:
  ETHERNET
  | LOOPBACK
  | MGMT
  | PORT_CHANNEL
  | VLAN
;

interface_middle
:
  (
    uint8 FORWARD_SLASH
  )+
;

interface_parent_suffix
:
  num = uint16 period = PERIOD
;

ip_access_list_name
:
// 1-64 characters
  WORD
;

ip_address
:
  IP_ADDRESS
  | SUBNET_MASK
;

ip_as_path_access_list_name
:
// 1-63 characters
  WORD
;

ip_community_list_name
:
// 1-63 characters
  WORD
;

ip_prefix
:
  IP_PREFIX
;

ip_prefix_list_description
:
// 1-90 chars
  REMARK_TEXT
;
ip_prefix_list_name
:
// 1-63 chars
  WORD
;

ip_prefix_list_line_number
:
// 1-4294967294
  uint32
;

ipv6_address
:
  IPV6_ADDRESS
;

ipv6_prefix
:
  IPV6_PREFIX
;

key_chain_name
:
// 1-63 characters
  WORD
;

line_action
:
  deny = DENY
  | permit = PERMIT
;

literal_standard_community
:
  high = uint16 COLON low = uint16
;

mac_access_list_name
:
// 1-64 characters
  WORD
;

mac_address_literal
:
  MAC_ADDRESS_LITERAL
;

maximum_paths
:
// 1-64
  UINT8
;

md5_string
:
// 1-15 characters
  WORD
;

monitor_session_id
:
// 1-32
  uint8
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

nve_interface_name
:
  NVE first = uint8
;

ospf_area_default_cost
:
// 0-16777215
  uint32
;

ospf_area_id
:
  num = uint32
  | ip = ip_address
;

ospf_area_range_cost
:
// 0-16777215
  uint32
;

ospf_max_metric_external_lsa
:
// 1-16777215
  uint32
;

ospf_on_startup_wait_period
:
// 5-86400
  uint32
;

ospf_priority
:
// 0-255
  uint8
;

ospf_ref_bw_gbps
:
// 1-4000
  uint16
;

ospf_ref_bw_mbps
:
// 1-4,000,000
  uint32
;

policy_map_cp_name
:
// 1-64 characters
  WORD
;

policy_map_network_qos_name
:
// 1-40 characters
  WORD
;

policy_map_qos_name
:
// 1-40 characters
  WORD
;

policy_map_queuing_name
:
// 1-40 characters
  WORD
;

qos_group
:
// 0-7
  uint8
;

quoted_text
:
  QUOTED_TEXT
;

route_distinguisher
:
// The order of these rules is significant: 1:1 should be parsed as type 0, not type 2.
// That string matches both rules with the same number of tokens; ANTLR4 will prefer the first one.
  hi0 = uint16 COLON lo0 = uint32
  | hi1 = ip_address COLON lo1 = uint16
  | hi2 = uint32 COLON lo2 = uint16
;

route_distinguisher_or_auto
:
  AUTO
  | route_distinguisher
;

route_map_name
:
// 1-63 chars
  WORD
;

route_network
:
  address = ip_address mask = subnet_mask
  | prefix = ip_prefix
;

router_eigrp_process_tag
:
// 1-20 characters
  WORD
;

router_isis_process_tag
:
// 1-20 characters
  WORD
;

router_ospf_name
:
// 1-20 characters
  WORD
;

router_ospfv3_name
:
// 1-20 characters
  WORD
;

router_rip_process_id
:
// 1-20 characters
  WORD
;

bgp_instance
:
  BGP bgp_asn
;

eigrp_instance
:
  EIGRP router_eigrp_process_tag
;

isis_instance
:
  ISIS router_isis_process_tag
;

ospf_instance
:
  OSPF router_ospf_name
;

ospfv3_instance
:
  OSPFV3 router_ospfv3_name
;

// The admin distance (static route calls it preference) for a non-local/direct route.
protocol_distance
:
// 1-255
  uint8
;

rip_instance
:
  RIP router_rip_process_id
;

routing_instance_v4
:
  DIRECT
  | bgp_instance
  | eigrp_instance
  | isis_instance
  | LISP
  | ospf_instance
  | rip_instance
  | STATIC
;

routing_instance_v6
:
  DIRECT
  | bgp_instance
  | eigrp_instance
  | isis_instance
  | LISP
  | ospfv3_instance
  | rip_instance
  | STATIC
;

route_target
:
  hi0 = uint16 COLON lo0 = uint32
  | hi2 = uint32 COLON lo2 = uint16
;

route_target_or_auto
:
  AUTO
  | route_target
;

standard_community
:
  literal = literal_standard_community
  | INTERNET
  | LOCAL_AS
  | NO_ADVERTISE
  | NO_EXPORT
;

subdomain_name
:
  SUBDOMAIN_NAME
;

subnet_mask
:
  SUBNET_MASK
;

tcp_port_number
:
// 0-65535
  uint16
;

template_name
:
// 1-80 chars
  WORD
;

track_object_id
:
// 1-500
  uint16
;

uint8
:
  UINT8
;

uint16
:
  UINT8
  | UINT16
;

uint32
:
  UINT8
  | UINT16
  | UINT32
;

uint64
:
  UINT8
  | UINT16
  | UINT32
  | UINT64
;

uint8_range_set
:
  ranges += uint8_range
  (
    COMMA ranges += uint8_range
  )*
;

uint8_range
:
  low = uint8
  (
    DASH high = uint8
  )?
;

user_name
:
// 1-32 characters
  WORD
;

user_password
:
  WORD
// 1-127 characters
;

unreserved_vlan_id
:
// 1-4094, minus the 128 vlans currently reserved
  uint16
;

unreserved_vlan_id_range
:
  intervals += unreserved_vlan_id_interval
  (
    COMMA intervals += unreserved_vlan_id_interval
  )*
;

unreserved_vlan_id_interval
:
  low = unreserved_vlan_id
  (
    DASH high = unreserved_vlan_id
  )?
;

vlan_id
:
// 1-4094
  uint16
;

vlan_id_range
:
  intervals += vlan_id_interval
  (
    COMMA intervals += vlan_id_interval
  )*
;

vlan_id_interval
:
  low = vlan_id
  (
    DASH high = vlan_id
  )?
;

vni_number
:
// 0-16777214
  uint32
;

vrf_name
:
  WORD
;

vrf_non_default_name
:
  WORD
;
