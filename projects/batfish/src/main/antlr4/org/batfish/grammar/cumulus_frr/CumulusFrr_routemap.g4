parser grammar CumulusFrr_routemap;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_routemap
:
  ROUTE_MAP name = word action = line_action sequence =
  route_map_sequence NEWLINE
  (
    rm_call
    | rm_description
    | rm_match
    | rm_on_match
    | rm_set
  )*
;

rm_call
:
  CALL name = word NEWLINE
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
    rmm_community
    | rmm_interface
    | rmm_ip
    | rmm_tag
  )
;

route_map_sequence
:
// 0-65535
  uint32
;

rmm_community
:
  COMMUNITY names += ip_community_list_name+ NEWLINE
;

rm_set
:
  SET
  (
    | rms_community
    | rms_ip
    | rms_local_preference
    | rms_metric
    | rms_tag
  )
;

rms_metric
:
  METRIC metric = uint32 NEWLINE
;

rmm_ip
:
  IP rmmip_address
;

rmm_tag
:
  TAG tag = uint32 NEWLINE
;

rmmip_address
:
  ADDRESS rmmipa_prefix_list
;

rmmipa_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name NEWLINE
;

rmm_interface
:
  INTERFACE name = WORD NEWLINE
;

rm_on_match
:
  ON_MATCH NEXT NEWLINE
;

rms_ip
:
  IP rmsip_next_hop
;

rms_tag
:
  TAG tag = uint32 NEWLINE
;

rmsip_next_hop
:
  NEXT_HOP
  (
    rmsipnh_literal
  )
;

rmsipnh_literal
:
  next_hop = ip_address NEWLINE
;

rms_community
:
  COMMUNITY communities += literal_standard_community+ NEWLINE
;

rms_local_preference
:
  LOCAL_PREFERENCE pref = uint32 NEWLINE
;
