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
    rm_description
    | rm_match
    | rm_set
  )*
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

route_map_sequence
:
// 0-65535
  uint32
;

rm_match
:
  MATCH
  (
    rmm_community
    | rmm_interface
  )
;

rmm_community
:
  COMMUNITY names += ip_community_list_name+ NEWLINE
;

rm_set
:
  SET
  (
    rms_metric
  )
;

rms_metric
:
  METRIC metric = uint32 NEWLINE
;

rmm_interface
:
  INTERFACE name = WORD NEWLINE
;
