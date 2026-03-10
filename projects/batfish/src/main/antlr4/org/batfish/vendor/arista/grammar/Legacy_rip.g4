parser grammar Legacy_rip;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

rr_default_metric
:
   DEFAULT_METRIC metric = dec NEWLINE
;

rr_default_information
:
   DEFAULT_INFORMATION ORIGINATE 
   (
      ON_PASSIVE
      | ROUTE_MAP map = variable
   )? NEWLINE
;

// TODO: this information is not plumbed in currently
rr_distance
:
   DISTANCE distance = dec
   (
      prefix = IP_ADDRESS mask = IP_ADDRESS
   )?
   NEWLINE
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
   )
   (
      i = interface_name_unstructured
   )? NEWLINE
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
      |
      (
         NO SHUTDOWN
      )
      | TIMERS
      | VERSION
   ) null_rest_of_line
;

rr_passive_interface
:
   NO? PASSIVE_INTERFACE iname = interface_name NEWLINE
;

rr_passive_interface_default
:
   NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

rr_redistribute
:
   REDISTRIBUTE null_rest_of_line
;

s_router_rip
:
   ROUTER RIP NEWLINE
   (
      rr_default_metric
      | rr_default_information
      | rr_distance
      | rr_distribute_list
      | rr_network
      | rr_null
      | rr_passive_interface
      | rr_passive_interface_default
      | rr_redistribute
   )*
;
