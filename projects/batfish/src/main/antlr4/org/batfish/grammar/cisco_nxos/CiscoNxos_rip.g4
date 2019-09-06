parser grammar CiscoNxos_rip;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

rip_metric
:
// 1-15
  uint8
;

rip_garbage_period
:
// 1-4294967295
  uint32
;

rip_holddown_period
:
// 0-4294967295
  uint32
;

rip_timeout_period
:
// 1-4294967295
  uint32
;

rip_update_period
:
// 5-4294967295
  uint32
;

router_rip
:
  rip_instance NEWLINE
  (
    rr_flush_routes
    | rr_isolate
    | rr_metric
    | rr_no
    | rr_vrf
    | rrv_address_family
    | rrv_shutdown
  )*
;

rr_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

rr_isolate
:
  ISOLATE NEWLINE
;

rr_metric
:
// only 0 is legal for uint8
  METRIC DIRECT uint8 NEWLINE
;

rr_no
:
  NO
  (
    rr_no_flush_routes
    | rr_no_isolate
    | rrv_no_shutdown
  )
;

rr_no_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

rr_no_isolate
:
  ISOLATE NEWLINE
;

rr_vrf
:
  VRF name = vrf_non_default_name NEWLINE
  (
    rrv_address_family
    | rrv_no
    | rrv_shutdown
  )
;

rrv_address_family
:
  ADDRESS_FAMILY
  (
    rrv_af4
    | rrv_af6
  )
;

rrv_af4
:
  IPV4 UNICAST NEWLINE
  (
    rrv_af4_default_information
    | rrv_af4_default_metric
    | rrv_af4_distance
    | rrv_af4_maximum_paths
    | rrv_af4_redistribute
    | rrv_af4_timers
  )*
;

rrv_af4_default_information
:
  DEFAULT_INFORMATION ORIGINATE ALWAYS? (ROUTE_MAP name = route_map_name)? NEWLINE
;

rrv_af4_default_metric
:
  DEFAULT_METRIC rip_metric NEWLINE
;

rrv_af4_distance
:
  DISTANCE protocol_distance NEWLINE
;

rrv_af4_maximum_paths
:
  MAXIMUM_PATHS maximum_paths NEWLINE
;

rrv_af4_redistribute
:
  REDISTRIBUTE rpi = routing_instance_v4 ROUTE_MAP map = route_map_name NEWLINE
;

rrv_af4_timers
:
  TIMERS BASIC update = rip_update_period timeout = rip_timeout_period holddown = rip_holddown_period garbage = rip_garbage_period NEWLINE
;

rrv_af6
:
  IPV6 UNICAST NEWLINE
  (
    rrv_af6_default_information
    | rrv_af6_default_metric
    | rrv_af6_distance
    | rrv_af6_maximum_paths
    | rrv_af6_redistribute
    | rrv_af6_timers
  )*
;

rrv_af6_default_information
:
  DEFAULT_INFORMATION ORIGINATE ALWAYS? (ROUTE_MAP name = route_map_name)? NEWLINE
;

rrv_af6_default_metric
:
  DEFAULT_METRIC rip_metric NEWLINE
;

rrv_af6_distance
:
  DISTANCE protocol_distance NEWLINE
;

rrv_af6_maximum_paths
:
  MAXIMUM_PATHS maximum_paths NEWLINE
;

rrv_af6_redistribute
:
  REDISTRIBUTE rpi = routing_instance_v6 ROUTE_MAP map = route_map_name NEWLINE
;

rrv_af6_timers
:
  TIMERS BASIC update = rip_update_period timeout = rip_timeout_period holddown = rip_holddown_period garbage = rip_garbage_period NEWLINE
;

rrv_no
:
  NO rrv_no_shutdown
;

rrv_no_shutdown
:
  SHUTDOWN NEWLINE
;

rrv_shutdown
:
  SHUTDOWN NEWLINE
;