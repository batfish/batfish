parser grammar FlatVyos_protocols;

import FlatVyos_common, FlatVyos_bgp;

options {
   tokenVocab = FlatVyosLexer;
}

s_protocols
:
   PROTOCOLS s_protocols_tail
;

s_protocols_static
:
   STATIC s_protocols_static_tail
;

s_protocols_static_tail
:
   statict_route
;

s_protocols_tail
:
   s_protocols_bgp
   | s_protocols_static
   //| s_protocols_null

;

/*
s_protocols_null
:
   (
   ) s_null_filler
;
*/
srt_blackhole
:
   BLACKHOLE
;

srt_next_hop
:
   NEXT_HOP nexthop=IP_ADDRESS DISTANCE distance = DEC
;

statict_route
:
   ROUTE IP_PREFIX statict_route_tail
;

statict_route_tail
:
   srt_blackhole
   | srt_next_hop
;