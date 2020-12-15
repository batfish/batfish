parser grammar CiscoNxos_eigrp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

// Reused values

eigrp_asn
:
// 1-65535
  value = uint16
;

router_eigrp
:
  EIGRP tag = router_eigrp_process_tag NEWLINE
  (
    re_common
    | re_flush_routes
    | re_isolate
    | re_no
    | re_vrf
  )*
;

re_common
:
  rec_address_family
  | rec_autonomous_system
  | rec_distance
  | rec_no
  | rec_passive_interface
  | rec_router_id
  | rec_shutdown
  | recaf_ipv4_inner // address-family ipv4 unicast inner lines are valid in the VRF as well.
;

re_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

re_isolate
:
  ISOLATE NEWLINE
;

re_no
:
  NO
  (
    re_no_isolate
    | re_no_flush_routes
  )
;

re_no_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

re_no_isolate
:
  ISOLATE NEWLINE
;

re_vrf
:
  VRF name = vrf_non_default_name NEWLINE
  re_common*
;

rec_address_family
:
  ADDRESS_FAMILY
  (
    recaf_ipv4
    | recaf_ipv6
  )
;

recaf_ipv4
:
  IPV4 UNICAST NEWLINE
  recaf_ipv4_inner*
;

recaf_ipv4_inner
:
  recaf_common
  | recaf4_redistribute
;

recaf_ipv6
:
  IPV6 UNICAST NEWLINE
  (
    recaf_common
    | recaf6_redistribute
  )*
;

recaf_common
:
  recaf_default_metric
  | recaf_eigrp
  | recaf_network
  | recaf_router_id
;

recaf_default_metric
:
  DEFAULT_METRIC bandwidth = uint32 delay = uint32 reliability = uint8 load = uint8 mtu = uint32 NEWLINE
;

recaf_eigrp
:
  // EIGRP keyword here is undocumented, but apparently works for backwards compatibility.
  EIGRP recaf_router_id
;

recaf_network
:
  NETWORK network = ip_prefix NEWLINE
;

recaf_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

recaf4_redistribute
:
  REDISTRIBUTE routing_instance_v4 ROUTE_MAP map = route_map_name NEWLINE
;

recaf6_redistribute
:
  REDISTRIBUTE routing_instance_v6 ROUTE_MAP map = route_map_name NEWLINE
;

rec_autonomous_system
:
  AUTONOMOUS_SYSTEM eigrp_asn NEWLINE
;

rec_distance
:
  DISTANCE internal = uint8 external = uint8 NEWLINE
;

rec_no
:
  NO
  (
    rec_no_passive_interface
    | rec_no_router_id
    | rec_no_shutdown
  )
;

rec_no_passive_interface
:
  PASSIVE_INTERFACE DEFAULT NEWLINE
;

rec_no_router_id
:
  ROUTER_ID NEWLINE
;

rec_no_shutdown
:
  SHUTDOWN NEWLINE
;

rec_passive_interface
:
  PASSIVE_INTERFACE DEFAULT NEWLINE
;

rec_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

rec_shutdown
:
  SHUTDOWN NEWLINE
;

