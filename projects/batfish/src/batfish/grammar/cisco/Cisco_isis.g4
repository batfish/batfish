parser grammar Cisco_isis;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

advertise_is_stanza
:
   ADVERTISE PASSIVE_ONLY NEWLINE
;

is_stanza
:
   advertise_is_stanza
   | is_type_is_stanza
   | metric_style_is_stanza
   | net_is_stanza
   | null_is_stanza
   | redistribute_static_is_stanza
   | passive_interface_is_stanza
;

is_type_is_stanza
:
   IS_TYPE LEVEL_2_ONLY NEWLINE
;

metric_style_is_stanza
:
   METRIC_STYLE WIDE LEVEL_2 NEWLINE
;

net_is_stanza
:
   NET ISO_ADDRESS NEWLINE
;

null_is_stanza
:
   NO?
   (
      AUTHENTICATION
      | FAST_FLOOD
      | HELLO
      | LOG_ADJACENCY_CHANGES
      | LSP_GEN_INTERVAL
      | LSP_REFRESH_INTERVAL
      | MAX_LSP_LIFETIME
      | SPF_INTERVAL
      | PRC_INTERVAL
      |
      (
         REDISTRIBUTE MAXIMUM_PREFIX
      )
      | SET_OVERLOAD_BIT
   ) ~NEWLINE* NEWLINE
;

passive_interface_is_stanza
:
   PASSIVE_INTERFACE name = variable NEWLINE
;

redistribute_static_is_stanza
:
   REDISTRIBUTE STATIC IP
   (
      (
         ROUTE_MAP map = VARIABLE
      )
   )* NEWLINE
;

router_isis_stanza
:
   ROUTER ISIS name = variable NEWLINE is_stanza+
;