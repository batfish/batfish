parser grammar Cisco_rip;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

distance_rr_stanza
:
   DISTANCE distance = DEC NEWLINE
;

distribute_list_rr_stanza
:
   DISTRIBUTE_LIST ~NEWLINE* NEWLINE
;

network_rr_stanza
:
   NETWORK network = IP_ADDRESS NEWLINE
;

passive_interface_rr_stanza
:
   NO? PASSIVE_INTERFACE ~NEWLINE* NEWLINE
;

redistribute_rr_stanza
:
   REDISTRIBUTE ~NEWLINE* NEWLINE
;

router_rip_stanza
:
   ROUTER RIP NEWLINE rr_stanza*
;

rr_stanza
:
   distance_rr_stanza
   | distribute_list_rr_stanza
   | network_rr_stanza
   | passive_interface_rr_stanza
   | redistribute_rr_stanza
;
