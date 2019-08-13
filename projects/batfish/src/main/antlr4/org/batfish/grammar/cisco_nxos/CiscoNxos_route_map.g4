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
    rmm_as_path
    | rmm_community
    | rmm_interface
    | rmm_ip
    | rmm_metric
    | rmm_tag
  )
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
  IP rmmip_address
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

rmm_metric
:
  METRIC metric = uint32 NEWLINE
;

rmm_tag
:
  TAG tag = uint32 NEWLINE
;

rm_set
:
  SET
  (
    rms_as_path_prepend
    | rms_community
    | rms_ip
    | rms_local_preference
    | rms_metric
    | rms_metric_type
    | rms_origin
    | rms_tag
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

rms_local_preference
:
  LOCAL_PREFERENCE local_preference = uint32 NEWLINE
;

rms_metric
:
  METRIC metric = uint32 NEWLINE
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

route_map_pbr_statistics
:
  PBR_STATISTICS NEWLINE
;
