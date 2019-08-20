parser grammar CiscoNxos_eigrp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

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
  | rec_no
  | rec_passive_interface
  | rec_redistribute
  | rec_shutdown
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
  VRF name = vrf_name NEWLINE
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
;

recaf_ipv6
:
  IPV6 UNICAST NEWLINE
;

rec_no
:
  NO
  (
    rec_no_passive_interface
    | rec_no_shutdown
  )
;

rec_no_passive_interface
:
  PASSIVE_INTERFACE DEFAULT NEWLINE
;

rec_no_shutdown
:
  SHUTDOWN NEWLINE
;

rec_passive_interface
:
  PASSIVE_INTERFACE DEFAULT NEWLINE
;

rec_redistribute
:
  REDISTRIBUTE
  (
    recr_bgp
    | recr_direct
    | recr_eigrp
    | recr_isis
    | recr_lisp
//    | recr_maximum_prefix
    | recr_ospf
    | recr_rip
    | recr_static
  )
;

recr_bgp
:
  BGP asn = bgp_asn ROUTE_MAP map = route_map_name NEWLINE
;

recr_direct
:
  DIRECT ROUTE_MAP map = route_map_name NEWLINE
;

recr_eigrp
:
  EIGRP tag = router_eigrp_process_tag ROUTE_MAP map = route_map_name NEWLINE
;

recr_isis
:
  ISIS source_tag = router_isis_process_tag ROUTE_MAP map = route_map_name NEWLINE
;

recr_lisp
:
  LISP ROUTE_MAP map = route_map_name NEWLINE
;

recr_ospf
:
  OSPF source_tag = router_ospf_name ROUTE_MAP map = route_map_name NEWLINE
;

recr_rip
:
  RIP source_tag = router_rip_process_id ROUTE_MAP map = route_map_name NEWLINE
;

recr_static
:
  STATIC ROUTE_MAP map = route_map_name NEWLINE
;

rec_shutdown
:
  SHUTDOWN NEWLINE
;

