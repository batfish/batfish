parser grammar Ftd_ospf;

import Ftd_common, Ftd_interface;

options {
   tokenVocab = FtdLexer;
}

router_ospf_stanza
:
   ROUTER OSPF process_id = dec NEWLINE
   ospf_stanza_tail*
;

ospf_stanza_tail
:
   (
      ospf_area_filter_list
      | ospf_default_information
      | ospf_log_adjacency_changes
      | ospf_network
      | ospf_passive_interface
      | ospf_redistribute
      | ospf_router_id
   )
;

ospf_area_filter_list
:
   AREA area = dec FILTER_LIST PREFIX list = NAME
   (IN | OUT) NEWLINE
;

ospf_default_information
:
   DEFAULT_INFORMATION ORIGINATE
   (
      ALWAYS
      | METRIC dec
      | METRIC_TYPE dec
      | ROUTE_MAP map = NAME
   )* NEWLINE
;

ospf_log_adjacency_changes
:
   LOG_ADJACENCY_CHANGES NEWLINE
;

ospf_network
:
   NETWORK ip = IP_ADDRESS mask = IP_ADDRESS AREA area = dec NEWLINE
;

ospf_passive_interface
:
   PASSIVE_INTERFACE interface_name NEWLINE
;

ospf_redistribute
:
   REDISTRIBUTE
   (
      CONNECTED
      | STATIC
   )
   (
      METRIC dec
      | METRIC_TYPE dec
      | ROUTE_MAP map = NAME
      | SUBNETS
      | TAG dec
   )* NEWLINE
;

ospf_router_id
:
   ROUTER_ID id = IP_ADDRESS NEWLINE
;
