parser grammar Cisco_static;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

address_family_s_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      UNICAST
      | MULTICAST
   ) NEWLINE common_s_stanza*
;

common_s_stanza
:
   static_route_s_stanza
;

router_static_stanza
:
   ROUTER STATIC NEWLINE s_stanza*
;

s_stanza
:
   address_family_s_stanza
   | common_s_stanza
   | vrf_s_stanza
;

static_route_s_stanza
:
   (IP_PREFIX | IPV6_PREFIX) 
   (
   	  IP_ADDRESS 
   	  | IPV6_ADDRESS 
   	  | interface_name (IP_ADDRESS | IPV6_ADDRESS)?
   ) 
   static_route_s_tail*
   NEWLINE
;

static_route_s_tail
:
   distance = DEC
   | DESCRIPTION ~NEWLINE*
   | PERMANENT
   | TAG DEC
   | TRACK variable
;

vrf_s_stanza
:
   VRF name = variable NEWLINE vs_stanza*
;

vs_stanza
:
   address_family_s_stanza
   | common_s_stanza
;
