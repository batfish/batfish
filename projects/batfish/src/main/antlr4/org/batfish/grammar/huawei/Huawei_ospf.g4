parser grammar Huawei_ospf;

import Huawei_common;

options {
   tokenVocab = HuaweiLexer;
}

// OSPF configuration

// OSPF stanza: ospf <process-id>
s_ospf
:
   OSPF process_id = uint32
   (
      ospf_substanza
   )*
;

// OSPF sub-stanza
ospf_substanza
:
   ospf_area
   | ospf_network
   | ospf_router_id
   | ospf_null
;

// OSPF area configuration
ospf_area
:
   AREA area_id = uint32
   (
      area_substanza
   )*
;

// Area sub-stanza
area_substanza
:
   area_null
;

// OSPF network statement: network <prefix> area <area-id>
ospf_network
:
   NETWORK ip = ip_prefix AREA area_id = uint32
;

// OSPF router-id: router-id A.B.C.D
ospf_router_id
:
   ROUTER_ID router_ip = ip_address
;

// Null area configuration (parse but ignore)
area_null
:
   NO?
   (
      null_rest_of_line
   )
;

// Null OSPF configuration (parse but ignore)
ospf_null
:
   NO?
   (
      null_rest_of_line
   )
;
