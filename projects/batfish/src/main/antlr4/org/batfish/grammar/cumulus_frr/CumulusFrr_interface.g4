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
    | sio_authentication
    | sio_message_digest_key
  )
  NEWLINE
;

sio_area
:
  AREA ip = IP_ADDRESS
;

sio_authentication
:
  AUTHENTICATION MESSAGE_DIGEST
;

sio_message_digest_key
:
  MESSAGE_DIGEST_KEY uint32 MD5 REMARK_TEXT
;
