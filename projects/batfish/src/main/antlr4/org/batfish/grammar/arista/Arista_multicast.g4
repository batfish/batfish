parser grammar Arista_multicast;

import Arista_common;

options {
   tokenVocab = AristaLexer;
}

s_router_multicast
:
  MULTICAST NEWLINE
  rm_inner*
;

rm_inner
:
  rm_ipv4
;

rm_ipv4
:
  IPV4 NEWLINE
  rm_ipv4_inner*
;

rm_ipv4_inner
:
  rm_v4_routing
;

rm_v4_routing: ROUTING NEWLINE;