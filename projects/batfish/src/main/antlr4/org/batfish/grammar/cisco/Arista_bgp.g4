parser grammar Arista_bgp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_router_bgp
:
   ROUTER BGP asn = bgp_asn NEWLINE
   (
      eos_rb_router_id
      | eos_rb_shutdown
      | eos_rb_timers
   )*
;

eos_rb_router_id
:
   ROUTER_ID id = IP_ADDRESS NEWLINE
;

eos_rb_shutdown
:
   SHUTDOWN NEWLINE
;

eos_rb_timers
:
   TIMERS BGP keepalive = DEC hold = DEC NEWLINE
;

