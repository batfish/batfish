parser grammar Arista_bgp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_router_bgp
:
   ROUTER BGP bgp_asn NEWLINE
   (
      eos_rb_shutdown
   )*
;

eos_rb_shutdown
:
   SHUTDOWN NEWLINE
;