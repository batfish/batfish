parser grammar CiscoNxos_route_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_route_map
:
  ROUTE_MAP name = route_map_name (route_map_entry | route_map_pbr_statistics)
;

route_map_entry
:
  action = line_action sequence = route_map_sequence NEWLINE
  (
    rm_continue
    | rm_description
    | rm_match
    | rm_set
  )*
;

route_map_sequence
:
// 0-65535
  uint32
;

rm_continue
:
  CONTINUE next = route_map_sequence NEWLINE
;

rm_description
:
  DESCRIPTION text = route_map_description NEWLINE
;

route_map_description
:
// 1-90 characters
  REMARK_TEXT
;

rm_match
:
  MATCH
  (
    rmm_as_number
    | rmm_as_path
    | rmm_community
    | rmm_interface
    | rmm_ip
    | rmm_ipv6
    | rmm_metric
    | rmm_route_type
    | rmm_source_protocol
    | rmm_tag
    | rmm_vlan
  )
;

rmm_as_number
:
  AS_NUMBER numbers = bgp_asn_range NEWLINE
;

rmm_as_path
:
  AS_PATH names += ip_as_path_access_list_name+ NEWLINE
;

rmm_community
:
  COMMUNITY names += ip_community_list_name+ NEWLINE
;

rmm_interface
:
  INTERFACE interfaces += interface_name+ NEWLINE
;

rmm_ip
:
  IP (
    rmmip_address
    | rmmip_multicast
  )
;

rmmip_address
:
  ADDRESS
  (
    rmmipa_pbr
    | rmmipa_prefix_list
  )
;

rmmipa_pbr
:
  name = ip_access_list_name NEWLINE
;

rmmipa_prefix_list
:
  PREFIX_LIST names += ip_prefix_list_name+ NEWLINE
;

rmmip_multicast
:
  MULTICAST null_rest_of_line
;

rmm_ipv6
:
  IPV6 rmmip6_address
;

rmmip6_address
:
  ADDRESS 
  (
    rmmip6a_pbr
    | rmmip6a_prefix_list
  )
;

rmmip6a_pbr
:
  name = ip_access_list_name NEWLINE
;

rmmip6a_prefix_list
:
  PREFIX_LIST names += ip_prefix_list_name+ NEWLINE
;

rmm_metric
:
  METRIC metric = uint32 NEWLINE
;

rmm_route_type
:
  ROUTE_TYPE
  (
    external = EXTERNAL
    | internal = INTERNAL
    | local = LOCAL
    | nssa_external = NSSA_EXTERNAL
    | type_1 = TYPE_1
    | type_2 = TYPE_2
  )+
  NEWLINE
;

rmm_source_protocol
:
  SOURCE_PROTOCOL name = protocol_instance_name NEWLINE
;

protocol_instance_name
:
// 1-32 characters
  WORD
;

rmm_tag
:
  TAG tags += uint32+ NEWLINE
;

rmm_vlan
:
  VLAN range = unreserved_vlan_id_range NEWLINE
;

rm_set
:
  SET
  (
    rms_as_path_prepend
    | rms_comm_list
    | rms_community
    | rms_ip
    | rms_ipv6
    | rms_local_preference
    | rms_metric
    | rms_metric_type
    | rms_origin
    | rms_tag
    | rms_weight
  )
;

rms_as_path_prepend
:
  AS_PATH PREPEND
  (
    rmsapp_last_as
    | rmsapp_literal
  )
;

rmsapp_last_as
:
  LAST_AS num_prepends = last_as_num_prepends NEWLINE
;

last_as_num_prepends
:
//1-10
  uint8
;

rmsapp_literal
:
  asns += bgp_asn+ NEWLINE
;

rms_comm_list
:
  COMM_LIST name = ip_community_list_name DELETE NEWLINE
;

rms_community
:
  COMMUNITY
  (
    additive = ADDITIVE
    | communities += standard_community
  )+ NEWLINE
;

rms_ip
:
  IP rmsip_next_hop
;

rmsip_next_hop
:
  NEXT_HOP
  (
    rmsipnh_literal
    | rmsipnh_unchanged
  )
;

rmsipnh_literal
:
// newline alone is actually valid
  next_hops += ip_address*
  (
    drop_on_fail = DROP_ON_FAIL
    | force_order = FORCE_ORDER
    | load_share = LOAD_SHARE
  )* NEWLINE
;

rmsipnh_unchanged
:
  UNCHANGED NEWLINE
;

rms_ipv6
:
  IPV6 null_rest_of_line
;

rms_local_preference
:
  LOCAL_PREFERENCE local_preference = uint32 NEWLINE
;

rms_metric
:
  METRIC metric = uint32 (delay = uint32 reliability = uint8 load = uint8 mtu = uint32)? NEWLINE
;

rms_metric_type
:
  METRIC_TYPE
  (
    EXTERNAL
    | INTERNAL
    | TYPE_1
    | TYPE_2
  ) NEWLINE
;

rms_origin
:
  ORIGIN
  (
    EGP
    | IGP
    | INCOMPLETE
  ) NEWLINE
;

rms_tag
:
  TAG tag = uint32 NEWLINE
;

rms_weight
:
  WEIGHT weight = uint16 NEWLINE
;

route_map_pbr_statistics
:
  PBR_STATISTICS NEWLINE
;
