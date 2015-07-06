parser grammar Cisco_isis;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

address_family_iis_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      UNICAST
      | MULTICAST
   ) NEWLINE common_iis_stanza*
;

address_family_is_stanza
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      UNICAST
      | MULTICAST
   ) NEWLINE isaf_stanza*
;

advertise_is_stanza
:
   ADVERTISE PASSIVE_ONLY NEWLINE
;

circuit_type_iis_stanza
:
   CIRCUIT_TYPE LEVEL_2_ONLY NEWLINE
;

common_iis_stanza
:
   circuit_type_iis_stanza
   | metric_iis_stanza
   | null_iis_stanza
   | passive_iis_stanza
;

common_is_stanza
:
   advertise_is_stanza
   | is_type_is_stanza
   | metric_is_stanza
   | metric_style_is_stanza
   | net_is_stanza
   | null_is_stanza
   | redistribute_static_is_stanza
   | passive_interface_is_stanza
   | summary_address_is_stanza
;

iis_stanza
:
   address_family_iis_stanza
   | common_iis_stanza
;

interface_is_stanza
:
   INTERFACE iname = interface_name NEWLINE iis_stanza*
;

is_stanza
:
   address_family_is_stanza
   | common_is_stanza
   | interface_is_stanza
;

is_type_is_stanza
:
   IS_TYPE
   (
      LEVEL_1
      | LEVEL_2_ONLY
   ) NEWLINE
;

isaf_stanza
:
   common_is_stanza
;

metric_iis_stanza
:
   METRIC DEC NEWLINE
;

metric_is_stanza
:
   METRIC DEC NEWLINE
;

metric_style_is_stanza
:
   METRIC_STYLE
   (
      WIDE
      | LEVEL_2
   )* NEWLINE
;

net_is_stanza
:
   NET ISO_ADDRESS NEWLINE
;

null_iis_stanza
:
   NO?
   (
      HELLO_PADDING
      | HELLO_PASSWORD
      | POINT_TO_POINT
   ) ~NEWLINE* NEWLINE
;

null_is_stanza
:
   NO?
   (
      AUTHENTICATION
      | FAST_FLOOD
      | HELLO
      | ISPF
      | LOG
      | LOG_ADJACENCY_CHANGES
      | LSP_GEN_INTERVAL
      | LSP_PASSWORD
      | LSP_REFRESH_INTERVAL
      | MAX_LSP_LIFETIME
      | MAXIMUM_PATHS
      | MPLS
      | NSF
      | NSR
      | PRC_INTERVAL
      |
      (
         REDISTRIBUTE MAXIMUM_PREFIX
      )
      | SET_OVERLOAD_BIT
      | SINGLE_TOPOLOGY
      | SPF_INTERVAL
   ) ~NEWLINE* NEWLINE
;

passive_iis_stanza
:
   PASSIVE NEWLINE
;

passive_interface_is_stanza
:
   PASSIVE_INTERFACE name = variable NEWLINE
;

redistribute_connected_is_stanza
:
   REDISTRIBUTE CONNECTED
   (
      IP
      | LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      |
      (
         METRIC metric = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
   )* NEWLINE
;

redistribute_static_is_stanza
:
   REDISTRIBUTE STATIC
   (
      IP
      | LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      |
      (
         METRIC metric = DEC
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
   )* NEWLINE
;

router_isis_stanza
:
   ROUTER ISIS
   (
      name = variable
   )? NEWLINE is_stanza+
;

summary_address_is_stanza
:
   SUMMARY_ADDRESS ip = IP_ADDRESS mask = IP_ADDRESS
   (
      LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      |
      (
         METRIC metric = DEC
      )
      |
      (
         TAG tag = DEC
      )
   )* NEWLINE
;
