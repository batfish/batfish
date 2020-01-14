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
    | ro_no
    | ro_passive_interface
    | ro_router_id
  )*
;

ro_log_adj_changes
:
  LOG_ADJACENCY_CHANGES DETAIL? NEWLINE
;

ro_no
:
  NO
  (
    rono_passive_interface
  )
;

rono_passive_interface
:
  PASSIVE_INTERFACE name = WORD NEWLINE
;

ro_passive_interface
:
  PASSIVE_INTERFACE name = WORD NEWLINE
;

ro_router_id
:
  // router-id without OSPF is accepted by FRR (even though its not documented)
  OSPF? ROUTER_ID ip = IP_ADDRESS NEWLINE
;
