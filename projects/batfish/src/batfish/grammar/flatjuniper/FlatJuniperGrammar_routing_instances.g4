parser grammar FlatJuniperGrammar_routing_instances;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

rgt_import_rib
:
   IMPORT_RIB rib = VARIABLE
;

ribt_static
:
   rot_static
;

rit_apply_groups
:
   s_apply_groups
;

rit_common
:
   rit_apply_groups
   | rit_routing_options
;

rit_named_routing_instance
:
   (
      WILDCARD
      | name = variable
   ) rit_named_routing_instance_tail
;

rit_named_routing_instance_tail
:
   rit_common
;

rit_routing_options
:
   s_routing_options
;

rot_aggregate
:
   AGGREGATE ROUTE IP_ADDRESS_WITH_MASK
   (
      (
         AS_PATH ORIGIN IGP
      )
      |
      (
         COMMUNITY community = COMMUNITY_LITERAL
      )
      |
      (
         TAG tag = DEC
      )
   )*
;

rot_autonomous_system
:
   AUTONOMOUS_SYSTEM as = DEC
;

rot_martians
:
   MARTIANS ~NEWLINE*
;

rot_null
:
   (
      FORWARDING_TABLE
      | OPTIONS
   ) ~NEWLINE*
;

rot_rib_groups
:
   rot_rib_groups_header rot_rib_groups_tail
;

rot_rib_groups_header
:
   RIB_GROUPS name = VARIABLE
;

rot_rib_groups_tail
:
   rgt_import_rib
;

rot_rib
:
   rot_rib_header rot_rib_tail
;

rot_rib_header
:
   RIB name = VARIABLE
;

rot_rib_tail
:
   ribt_static
;

rot_router_id
:
   ROUTER_ID id = IP_ADDRESS
;

rot_static
:
   STATIC ROUTE IP_ADDRESS_WITH_MASK DISCARD
;

s_routing_instances
:
   ROUTING_INSTANCES s_routing_instances_tail
;

s_routing_instances_header
:
   ROUTING_INSTANCES
;

s_routing_instances_tail
:
   rit_common
   | rit_named_routing_instance
;

s_routing_options
:
   ROUTING_OPTIONS s_routing_options_tail
;

s_routing_options_tail
:
   rot_aggregate
   | rot_autonomous_system
   | rot_martians
   | rot_null
   | rot_rib
   | rot_rib_groups
   | rot_router_id
   | rot_static
;
