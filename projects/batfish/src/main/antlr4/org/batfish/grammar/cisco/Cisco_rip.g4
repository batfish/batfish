parser grammar Cisco_rip;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

rr_distance
:
   DISTANCE distance = DEC NEWLINE
;

rr_distribute_list
:
   DISTRIBUTE_LIST
   (
      (
         PREFIX prefix_list = variable
      )
      | acl = variable
   )
   (
      IN
      | OUT
   ) NEWLINE
;

rr_network
:
   NETWORK network = IP_ADDRESS NEWLINE
;

rr_null
:
   NO?
   (
      AUTO_SUMMARY
      | VERSION
   ) ~NEWLINE* NEWLINE
;

rr_passive_interface
:
   NO? PASSIVE_INTERFACE iname=interface_name NEWLINE
;

rr_passive_interface_default
:
   NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

rr_redistribute
:
   REDISTRIBUTE ~NEWLINE* NEWLINE
;

s_router_rip
:
   ROUTER RIP NEWLINE
   (
      rr_distance
      | rr_distribute_list
      | rr_network
      | rr_null
      | rr_passive_interface
      | rr_passive_interface_default
      | rr_redistribute
   )*
;
