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
  mraf_forwarding_latency_null
  | mraf_interface_inheritance_null
  | mraf_maximum_null
  | mraf_mofrr_lockout_timer_null
  | mraf_mofrr_loss_detection_timer_null
  | mraf_oom_handling_null
  | mrvaf_inner
;

mrvaf_inner
:
  mr_interface
  | mrvaf_accounting_null
  | mrvaf_core_tree_protocol
  | mrvaf_export_rt_null
  | mrvaf_import_rt_null
  | mrvaf_log_traps_null
  | mrvaf_multipath_null
  | mrvaf_rate_per_route_null
  | mrvaf_static_rpf_null
  // TODO: bgp and mdt
;

mr_interface_inner
:
  mri_boundary_null
  | mri_disable_null
  | mri_enable_null
  | mri_ttl_threshold_null
;

mraf_forwarding_latency_null
:
   FORWARDING_LATENCY null_rest_of_line
;
mraf_interface_inheritance_null
:
   INTERFACE_INHERITANCE null_rest_of_line
;
mraf_maximum_null
:
   MAXIMUM null_rest_of_line
;
mraf_mofrr_lockout_timer_null
:
   MOFRR_LOCKOUT_TIMER null_rest_of_line
;
mraf_mofrr_loss_detection_timer_null
:
   MOFRR_LOSS_DETECTION_TIMER null_rest_of_line
;
mraf_oom_handling_null
:
   OOM_HANDLING null_rest_of_line
;

mrvaf_accounting_null
:
   ACCOUNTING null_rest_of_line
;
mrvaf_export_rt_null
:
   EXPORT_RT null_rest_of_line
;
mrvaf_import_rt_null
:
   IMPORT_RT null_rest_of_line
;
mrvaf_log_traps_null
:
   LOG_TRAPS null_rest_of_line
;
mrvaf_multipath_null
:
   MULTIPATH null_rest_of_line
;
mrvaf_rate_per_route_null
:
   RATE_PER_ROUTE null_rest_of_line
;
mrvaf_static_rpf_null
:
   STATIC_RPF null_rest_of_line
;

mri_boundary_null
:
   BOUNDARY null_rest_of_line
;
mri_disable_null
:
   DISABLE null_rest_of_line
;
mri_enable_null
:
   ENABLE null_rest_of_line
;
mri_ttl_threshold_null
:
   TTL_THRESHOLD null_rest_of_line
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