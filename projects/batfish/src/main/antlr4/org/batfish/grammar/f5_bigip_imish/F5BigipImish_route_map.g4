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
  SET rms_community
;

rms_community
:
  COMMUNITY communities += standard_community+ NEWLINE
;

standard_community
:
  word
;

s_route_map
:
  ROUTE_MAP name = word action = line_action num = word NEWLINE
  (
    rm_match
    | rm_set
  )*
;