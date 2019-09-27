parser grammar F5BigipImish_interface;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

s_interface
:
  INTERFACE name = word NEWLINE i_ip*
;

i_ip
:
  IP iip_ospf
;

iip_ospf
:
  OSPF iipo_network
;

iipo_network
:
  NETWORK type = ospf_network_type NEWLINE
;

ospf_network_type
:
  NON_BROADCAST
;