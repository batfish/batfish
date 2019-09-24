parser grammar CumulusFrr_ospf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_router_ospf
:
  ROUTER OSPF NEWLINE
  (
    ro_null
  )*
;

ro_null
:
  LOG_ADJACENCY_CHANGES null_rest_of_line
;