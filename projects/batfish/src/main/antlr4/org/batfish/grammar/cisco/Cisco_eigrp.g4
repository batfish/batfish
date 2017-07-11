parser grammar Cisco_eigrp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_router_eigrp
:
   ROUTER EIGRP procnum = DEC NEWLINE s_router_eigrp_tail*
;

s_router_eigrp_tail
:
   re_auto_summary
   | re_distribute_list
   | re_network
   | re_nsf
   | re_passive_interface
   | re_redistribute
;

re_auto_summary
:
   NO? AUTO_SUMMARY NEWLINE
;

re_distribute_list
:
   DISTRIBUTE_LIST DEC
   (
      OUT
      | IN
   ) NEWLINE
;

re_network
:
   NETWORK address = IP_ADDRESS mask = IP_ADDRESS? NEWLINE
;

re_nsf
:
   NSF NEWLINE
;

re_passive_interface
locals [boolean no] @init {$no = false;}
:
   (
      NO
      {$no = true;}

   )? PASSIVE_INTERFACE re_passive_interface_tail [$no]
;

re_passive_interface_tail [boolean no]
:
   repi_default [$no]
   | repi_interface [$no]
;

re_redistribute
:
   REDISTRIBUTE re_redistribute_tail
;

re_redistribute_tail
:
   rer_static
;

repi_default [boolean no]
:
   DEFAULT NEWLINE
;

repi_interface [boolean no]
:
   iname = interface_name NEWLINE
;

rer_static
:
   STATIC
   (
      (
         ROUTE_MAP map = VARIABLE
      )
      |
      (
         METRIC bandwidth_metric_kbps = DEC delay_metric_10us_units = DEC
         reliability_metric = DEC loading_metric = DEC eigrp_path_mtu = DEC
      )
   )* NEWLINE
;
