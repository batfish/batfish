parser grammar Arista_bgp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_router_bgp
:
  ROUTER BGP asn = bgp_asn NEWLINE
  (
    eos_rb_inner
    | eos_rb_vlan
    | eos_rb_vlan_aware_bundle
    | eos_rb_vrf
  )*
;

eos_rb_inner
:
  eos_rbi_aggregate_address
  | eos_rbi_router_id
  | eos_rbi_shutdown
  | eos_rbi_timers
;

eos_rbi_aggregate_address
:
  AGGREGATE_ADDRESS
  (
    eos_rb_aa_v4
    | eos_rb_aa_v6
  )
;

eos_rbi_router_id
:
  ROUTER_ID id = IP_ADDRESS NEWLINE
;

eos_rbi_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbi_timers
:
  TIMERS BGP keepalive = DEC hold = DEC NEWLINE
;

eos_rb_aa_modifiers
:
  ADVERTISE_ONLY
  | AS_SET
  | ATTRIBUTE_MAP attr_map = VARIABLE
  | MATCH_MAP match_map = VARIABLE
  | SUMMARY_ONLY
;

eos_rb_aa_v4
:
  (
    ip = IP_ADDRESS mask = IP_ADDRESS
    | prefix = IP_PREFIX
  ) eos_rb_aa_modifiers*
  NEWLINE
;

eos_rb_aa_v6
:
  prefix = IPV6_PREFIX eos_rb_aa_modifiers* NEWLINE
;

eos_rb_vlan
:
  VLAN id = DEC NEWLINE
  eos_rb_vlan_tail*
;

eos_rb_vlan_aware_bundle
:
  VLAN_AWARE_BUNDLE name = VARIABLE NEWLINE
  (
    eos_rb_vab_vlan
    | eos_rb_vlan_tail
  )*
;

eos_rb_vlan_tail_rd
:
  RD rd = route_distinguisher NEWLINE
;

eos_rb_vlan_tail_redistribute
:
  REDISTRIBUTE
  (
    HOST_ROUTE
    | LEARNED
    | ROUTER_MAC
    | STATIC
  ) NEWLINE
;

eos_rb_vlan_tail_route_target
:
  ROUTE_TARGET
  ( BOTH | IMPORT | EXPORT )
  rt = route_target NEWLINE
;

eos_rb_vab_vlan
:
  VLAN vlans = eos_vlan_id NEWLINE
;

eos_rb_vlan_tail
:
  eos_rb_vlan_tail_rd
  | eos_rb_vlan_tail_redistribute
  | eos_rb_vlan_tail_route_target
;

eos_rb_vrf
:
  VRF name = VARIABLE NEWLINE
  eos_rb_inner*
;