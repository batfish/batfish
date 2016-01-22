parser grammar FlatVyos_bgp;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

bnt_nexthop_self
:
   NEXTHOP_SELF
;

bnt_null
:
   (
      SOFT_RECONFIGURATION
      | TIMERS
   ) null_filler
;

bnt_remote_as
:
   REMOTE_AS asnum = DEC
;

bnt_route_map
:
   ROUTE_MAP
   (
      IMPORT
      | EXPORT
   ) name = variable
;

bt_neighbor
:
   NEIGHBOR IP_ADDRESS bt_neighbor_tail
;

bt_neighbor_tail
:
   bnt_nexthop_self
   | bnt_null
   | bnt_remote_as
   | bnt_route_map
;

s_protocols_bgp
:
   BGP asnum = DEC s_protocols_bgp_tail
;

s_protocols_bgp_tail
:
   bt_neighbor
;