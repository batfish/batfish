parser grammar Arista_common;

options {
   tokenVocab = AristaLexer;
}

bgp_local_pref
:
  // full range of values: 0-4294967295
  p = uint32
;

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

port_number
:
// 1-65535
  uint16
;

protocol_distance:
// 1-255
   uint8
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

vni_number
:
// 0-16777214
  uint32
;

vlan_id
:
// 1-4094
  uint16
;


vlan_range
:
   (
      vlan_range_list += vlan_subrange
      (
         COMMA vlan_range_list += vlan_subrange
      )*
   )
   | NONE
;

vlan_subrange
:
   low = vlan_id
   (
      DASH high = vlan_id
   )?
;

vni_range
:
   (
      vni_range_list += vni_subrange
      (
         COMMA vni_range_list += vni_subrange
      )*
   )
;

vni_subrange
:
   low = vni_number
   (
      DASH high = vni_number
   )?
;