parser grammar FlatJuniper_routing_instances;

import FlatJuniper_common, FlatJuniper_protocols;

options {
   tokenVocab = FlatJuniperLexer;
}

agt_as_path
:
   AS_PATH PATH path = as_path_expr
;

agt_preference
:
   PREFERENCE preference = DEC
;

gt_discard
:
   DISCARD
;

gt_metric
:
   METRIC metric = DEC
;

gt_policy
:
   POLICY policy = variable
;

rgt_import_rib
:
   IMPORT_RIB rib = VARIABLE
;

ribt_aggregate
:
   AGGREGATE ROUTE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) ribt_aggregate_tail
;

ribt_aggregate_tail
:
   agt_as_path
   | agt_preference
;

ribt_generate
:
   rot_generate
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
   | rit_description
   | rit_routing_options
;

rit_description
:
   s_description
;

rit_instance_type
:
   INSTANCE_TYPE VRF
;

rit_interface
:
   INTERFACE id = interface_id
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
   | rit_instance_type
   | rit_interface
   | rit_protocols
   | rit_route_distinguisher
   | rit_vrf_export
   | rit_vrf_import
   | rit_vrf_table_label
   | rit_vrf_target
;

rit_protocols
:
   s_protocols
;

rit_route_distinguisher
:
   ROUTE_DISTINGUISHER IP_ADDRESS COLON DEC
;

rit_routing_options
:
   s_routing_options
;

rit_vrf_export
:
   VRF_EXPORT name = variable
;

rit_vrf_import
:
   VRF_IMPORT name = variable
;

rit_vrf_table_label
:
   VRF_TABLE_LABEL
;

rit_vrf_target
:
   VRF_TARGET rit_vrf_target_tail
;

rit_vrf_target_tail
:
   vtt_export
;

rot_aggregate
:
   AGGREGATE ROUTE IP_PREFIX
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

rot_auto_export
:
   AUTO_EXPORT
;

rot_autonomous_system
:
   AUTONOMOUS_SYSTEM as = DEC
;

rot_martians
:
   MARTIANS s_null_filler
;

rot_generate
:
   GENERATE ROUTE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) rot_generate_tail
;

rot_generate_tail
:
   gt_discard
   | gt_metric
   | gt_policy
;

rot_null
:
   (
      FORWARDING_TABLE
      | MULTICAST
      | OPTIONS
   ) s_null_filler
;

rot_rib_groups
:
   RIB_GROUPS name = variable rot_rib_groups_tail
;

rot_rib_groups_tail
:
   rgt_import_rib
;

rot_rib
:
   RIB name = VARIABLE rot_rib_tail
;

rot_rib_tail
:
   ribt_aggregate
   | ribt_generate
   | ribt_static
;

rot_router_id
:
   ROUTER_ID id = IP_ADDRESS
;

rot_static
:
   STATIC ROUTE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) rot_static_tail
;

rot_static_tail
:
   srt_discard
   | srt_install
   | srt_next_hop
   | srt_readvertise
   | srt_reject
   | srt_tag
;

s_routing_instances
:
   ROUTING_INSTANCES s_routing_instances_tail
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
   | rot_auto_export
   | rot_autonomous_system
   | rot_generate
   | rot_martians
   | rot_null
   | rot_rib
   | rot_rib_groups
   | rot_router_id
   | rot_static
;

srt_discard
:
   DISCARD
;

srt_install
:
   INSTALL
;

srt_next_hop
:
   NEXT_HOP
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   )
;

srt_readvertise
:
   READVERTISE
;

srt_reject
:
   REJECT
;

srt_tag
:
   TAG tag = DEC
;

vtt_export
:
   EXPORT extended_community
;
