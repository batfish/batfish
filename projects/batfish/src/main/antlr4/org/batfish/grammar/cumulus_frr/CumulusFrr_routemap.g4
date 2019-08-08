parser grammar CumulusFrr_routemap;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_routemap
:
  ROUTE_MAP name = word action = line_action sequence =
  route_map_sequence NEWLINE
;

route_map_sequence
:
// 0-65535
  uint32
;
