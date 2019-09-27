parser grammar CumulusFrr_ospf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_router_ospf
:
  ROUTER OSPF NEWLINE
  (
    ro_log_adj_changes
    | ro_passive_interface
  )*
;

ro_log_adj_changes
:
  LOG_ADJACENCY_CHANGES DETAIL? NEWLINE
;


ro_passive_interface
:
  PASSIVE_INTERFACE name = WORD NEWLINE
;
