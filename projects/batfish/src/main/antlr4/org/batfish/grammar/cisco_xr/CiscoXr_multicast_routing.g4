parser grammar CiscoXr_multicast_routing;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_multicast_routing: MULTICAST_ROUTING NEWLINE multicast_routing_inner*;

multicast_routing_inner
:
  mr_address_family
  | mr_ipv4
  | mr_ipv6
  | mr_vrf
;

mr_vrf_inner
:
  mrv_address_family
  | mrv_ipv4
  | mrv_ipv6
;

mraf4_inner
:
  mraf_inner
;

mraf6_inner
:
  mraf_inner
;

mrvaf4_inner
:
  mrvaf_inner
;

mrvaf6_inner
:
  mrvaf_inner
;

mraf_inner
:
  mraf_null
  | mrvaf_inner
;

mrvaf_inner
:
  mr_interface
  | mrvaf_core_tree_protocol
  | mrvaf_null
  // TODO: bgp and mdt
;

mr_interface_inner
:
  mri_null
;

mraf_null
:
  (
    FORWARDING_LATENCY
    | INTERFACE_INHERITANCE
    | MAXIMUM
    | MOFRR_LOCKOUT_TIMER
    | MOFRR_LOSS_DETECTION_TIMER
    | OOM_HANDLING
  ) null_rest_of_line
;

mrvaf_null
:
  (
    ACCOUNTING
    | EXPORT_RT
    | IMPORT_RT
    | LOG_TRAPS
    | MULTIPATH
    | RATE_PER_ROUTE
    | STATIC_RPF
  ) null_rest_of_line
;

mri_null
:
  (
    BOUNDARY
    | DISABLE
    | ENABLE
    | TTL_THRESHOLD
  ) null_rest_of_line
;

mr_address_family: ADDRESS_FAMILY (mr_ipv4 | mr_ipv6);

mr_ipv4: IPV4 NEWLINE mraf4_inner*;

mr_ipv6: IPV6 NEWLINE mraf6_inner*;

mrv_address_family: ADDRESS_FAMILY (mrv_ipv4 | mrv_ipv6);

mrv_ipv4: IPV4 NEWLINE mrvaf4_inner*;

mrv_ipv6: IPV6 NEWLINE mrvaf6_inner*;

mrvaf_core_tree_protocol
:
  CORE_TREE_PROTOCOL RSVP_TE (GROUP_LIST name = access_list_name)? NEWLINE
;

mr_interface: INTERFACE (mri_all | mri_named);

mr_vrf: VRF name = vrf_name NEWLINE mr_vrf_inner*;

mri_all: ALL ENABLE NEWLINE;

mri_named: name = interface_name NEWLINE mr_interface_inner*;