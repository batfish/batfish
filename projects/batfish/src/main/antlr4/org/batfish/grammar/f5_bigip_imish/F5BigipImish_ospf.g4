parser grammar F5BigipImish_ospf;

import F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

router_ospf
:
  OSPF num = uint16 NEWLINE
  (
    ro_neighbor
    | ro_network
    | ro_ospf
    | ro_passive_interface
  )*
;

ro_neighbor
:
  NEIGHBOR ip = ip_address NEWLINE
;


ro_network
:
  NETWORK prefix = ip_prefix AREA area = uint32 NEWLINE
;

ro_ospf
:
  OSPF roo_router_id
;

roo_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

ro_passive_interface
:
  PASSIVE_INTERFACE name = word NEWLINE
;
