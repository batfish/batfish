parser grammar CumulusFrr_bgp;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_bgp
:
  ROUTER BGP autonomousSystem = uint32 (VRF vrfName = word)? NEWLINE
  (
    sb_router_id
  | sb_neighbor
  )*
;

sb_router_id
:
  BGP ROUTER_ID IP_ADDRESS NEWLINE
;

sb_neighbor
:
  NEIGHBOR name = word
    (
      sbn_interface
    | sbn_peer_group
    )
    NEWLINE
;

sbn_interface
:
  INTERFACE
;

sbn_peer_group
:
  PEER_GROUP
;