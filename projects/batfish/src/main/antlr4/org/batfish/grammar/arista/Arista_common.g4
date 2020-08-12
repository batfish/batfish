parser grammar Arista_common;

options {
   tokenVocab = AristaLexer;
}

interface_address
:
  ip = IP_ADDRESS subnet = IP_ADDRESS
  | prefix = IP_PREFIX
;

ip_prefix
:
  address = IP_ADDRESS mask = IP_ADDRESS
  | prefix = IP_PREFIX
;

ipv6_prefix
:
  prefix = IPV6_PREFIX
;

ospf_area
:
  id_ip = IP_ADDRESS
  | id = DEC
;

word
:
  WORD
;