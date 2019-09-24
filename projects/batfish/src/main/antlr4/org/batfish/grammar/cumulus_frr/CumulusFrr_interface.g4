parser grammar CumulusFrr_interface;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_interface
:
  INTERFACE name = word (VRF vrf = word)? NEWLINE
  (
    si_description
    | si_ospf
  )*
;

si_description
:
  DESCRIPTION description = REMARK_TEXT NEWLINE
;

si_ospf
:
  IP OSPF
  (
    sio_area
  )
;

sio_area
:
  AREA ip = IP_ADDRESS
;
