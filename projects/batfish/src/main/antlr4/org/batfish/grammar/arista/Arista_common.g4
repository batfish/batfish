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
  | id = uint32
;

uint8: UINT8;
uint16: UINT8 | UINT16;
uint32: UINT8 | UINT16 | UINT32;
// TODO: delete all uses of dec, replace with named rules that have a toInt/LongInSpace function
dec: UINT8 | UINT16 | UINT32 | DEC;

vrf_name
:
//1-100 characters
  WORD
;

word
:
  WORD
;