parser grammar F5BigipImish_route_map;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

rm_match
:
  MATCH rmm_ip_address
;

rmm_ip_address
:
  IP ADDRESS name = word NEWLINE
;

rm_set
:
  SET
  (
    rms_community
    | rms_metric
  )
;

rms_community
:
  COMMUNITY communities += standard_community+ NEWLINE
;

rms_metric
:
  METRIC metric = uint32 NEWLINE
;

standard_community
:
  STANDARD_COMMUNITY
;

s_route_map
:
  ROUTE_MAP name = word action = line_action num = uint32 NEWLINE
  (
    rm_match
    | rm_set
  )*
;