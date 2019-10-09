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
    | si_ip
  )*
;

si_ip
:
  IP 
  (
    siip_address
    | siip_ospf
  )  
;

siip_address
:
  ADDRESS ip_prefix = IP_PREFIX NEWLINE
;

si_description
:
  DESCRIPTION description = REMARK_TEXT NEWLINE
;

siip_ospf
:
  OSPF
  (
    siipo_area
    | siipo_authentication
    | siipo_message_digest_key
    | siipo_network_p2p
  )
  NEWLINE
;

siipo_area
:
  AREA ip = IP_ADDRESS
;

siipo_authentication
:
  AUTHENTICATION MESSAGE_DIGEST
;

siipo_message_digest_key
:
  MESSAGE_DIGEST_KEY uint32 MD5 REMARK_TEXT
;

siipo_network_p2p
:
  NETWORK POINT_TO_POINT
;
