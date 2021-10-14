parser grammar Legacy_isis;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
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
   )? NEWLINE isaf_stanza* address_family_footer
;

advertise_is_stanza
:
   ADVERTISE PASSIVE_ONLY NEWLINE
;

circuit_type_iis_stanza
:
   (
      (
         NO CIRCUIT_TYPE
      )
      |
      (
         CIRCUIT_TYPE
         (
            LEVEL_1
            | LEVEL_1_2
            | LEVEL_2_ONLY
         )
      )
   ) NEWLINE
;

common_iis_stanza
:
   circuit_type_iis_stanza
   | metric_iis_stanza
   | null_iis_stanza
   | passive_iis_stanza
   | shutdown_iis_stanza
   | suppressed_iis_stanza
;

common_is_stanza
:
   advertise_is_stanza
   | distribute_list_is_stanza
   | is_type_is_stanza
   | metric_is_stanza
   | metric_style_is_stanza
   | net_is_stanza
   | null_is_stanza
   | redistribute_connected_is_stanza
   | redistribute_static_is_stanza
   | passive_interface_default_is_stanza
   | passive_interface_is_stanza
   | summary_address_is_stanza
;

distribute_list_is_stanza
:
   DISTRIBUTE_LIST name = variable
   (
      IN
      | OUT
   ) CONNECTED NEWLINE
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
      | LEVEL_2
      | LEVEL_2_ONLY
   ) NEWLINE
;

isaf_stanza
:
   common_is_stanza
;

metric_iis_stanza
:
   METRIC
   (
      dec
      | MAXIMUM
   )
   (
      LEVEL dec
   )? NEWLINE
;

metric_is_stanza
:
   METRIC dec
   (
      LEVEL dec
   )? NEWLINE
;

metric_style_is_stanza
:
   METRIC_STYLE
   (
      WIDE
      | LEVEL_1
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
      BFD
      | CSNP_INTERVAL
      | HELLO_INTERVAL
      | HELLO_MULTIPLIER
      | HELLO_PADDING
      | HELLO_PASSWORD
      | POINT_TO_POINT
   ) null_rest_of_line
;

null_is_stanza
:
   NO?
   (
      ADJACENCY_CHECK
      | AREA_PASSWORD
      | AUTHENTICATION
      | BFD
      | ENABLE
      | FAST_FLOOD
      | HELLO
      | IGNORE_ATTACHED_BIT
      | ISPF
      | LOG
      | LOG_ADJACENCY_CHANGES
      | LSP_GEN_INTERVAL
      | LSP_PASSWORD
      | LSP_REFRESH_INTERVAL
      | MAX_LSP_LIFETIME
      | MAXIMUM_PATHS
      | MPLS
      | MULTI_TOPOLOGY
      |
      (
         NO
         (
            SHUTDOWN
         )
      )
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
   ) null_rest_of_line
;

passive_iis_stanza
:
   PASSIVE NEWLINE
;

passive_interface_default_is_stanza
:
   NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

passive_interface_is_stanza
:
   NO? PASSIVE_INTERFACE name = interface_name NEWLINE
;

suppressed_iis_stanza
:
   NO? SUPPRESSED NEWLINE
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
         METRIC metric = dec
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
      CLNS
      | IP
      | LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      |
      (
         METRIC metric = dec
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
   )* NEWLINE
;

router_isis_stanza
:
   ISIS
   (
      name = variable
   )? NEWLINE is_stanza*
;

shutdown_iis_stanza
:
   SHUTDOWN NEWLINE
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
         METRIC metric = dec
      )
      |
      (
         TAG tag = dec
      )
   )* NEWLINE
;
