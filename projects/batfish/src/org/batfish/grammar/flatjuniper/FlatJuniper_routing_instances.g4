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

bmpt_station_address
:
   STATION_ADDRESS IP_ADDRESS
;

bmpt_station_port
:
   STATION_PORT DEC
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

irfit_export
:
   EXPORT
   (
      LAN
      | POINT_TO_POINT
   )
;

irft_inet
:
   INET irft_inet_tail
;

irft_inet_tail
:
   irfit_export
;

irft_null
:
   INET6 s_null_filler
;

irrgt_inet
:
   INET name = variable
;

irrgt_null
:
   INET6 s_null_filler
;

irt_family
:
   FAMILY irt_family_tail
;

irt_family_tail
:
   irft_inet
   | irft_null
;

irt_rib_group
:
   RIB_GROUP irt_rib_group_tail
;

irt_rib_group_tail
:
   irrgt_inet
   | irrgt_null
;

rgt_export_rib
:
   EXPORT_RIB rib = variable
;

rgt_import_policy
:
   IMPORT_POLICY name = variable
;

rgt_import_rib
:
   IMPORT_RIB rib = variable
;

ribt_aggregate
:
   AGGREGATE ROUTE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) ribt_aggregate_tail
;

ribt_apply_groups
:
   s_apply_groups
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

rit_apply_groups_except
:
   s_apply_groups_except
;

rit_common
:
   rit_apply_groups
   | rit_apply_groups_except
   | rit_description
   | rit_routing_options
;

rit_description
:
   s_description
;

rit_instance_type
:
   INSTANCE_TYPE
   (
      FORWARDING
      | L2VPN
      | VIRTUAL_SWITCH
      | VRF
   )
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
   | rit_null
   | rit_protocols
   | rit_route_distinguisher
   | rit_vrf_export
   | rit_vrf_import
   | rit_vrf_table_label
   | rit_vrf_target
;

rit_null
:
   (
      BRIDGE_DOMAINS
      | CHASSIS
      | CLASS_OF_SERVICE
      | EVENT_OPTIONS
      | FORWARDING_OPTIONS
      | PROVIDER_TUNNEL
      | SERVICES
      | SNMP
   ) s_null_filler
;

rit_protocols
:
   s_protocols
;

rit_route_distinguisher
:
   ROUTE_DISTINGUISHER
   (
      DEC
      | IP_ADDRESS
   ) COLON DEC
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
   vtt_community
   | vtt_export
   | vtt_import
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

rot_bmp
:
   BMP rot_bmp_tail
;

rot_bmp_tail
:
   bmpt_station_address
   | bmpt_station_port
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

rot_interface_routes
:
   INTERFACE_ROUTES rot_interface_routes_tail
;

rot_interface_routes_tail
:
   irt_family
   | irt_rib_group
;

rot_null
:
   (
      FORWARDING_TABLE
      | MULTICAST
      | MULTIPATH
      | NONSTOP_ROUTING
      | OPTIONS
      | PPM
      | RESOLUTION
      | TRACEOPTIONS
   ) s_null_filler
;

rot_rib_groups
:
   RIB_GROUPS name = variable rot_rib_groups_tail
;

rot_rib_groups_tail
:
   rgt_export_rib
   | rgt_import_policy
   | rgt_import_rib
;

rot_rib
:
   RIB
   (
      name = VARIABLE
      | WILDCARD
   ) rot_rib_tail
;

rot_rib_tail
:
// intentional blank

   | ribt_aggregate
   | ribt_apply_groups
   | ribt_generate
   | ribt_static
;

rot_router_id
:
   ROUTER_ID id = IP_ADDRESS
;

rot_static
:
   STATIC rot_static_tail
;

rot_static_tail
:
   rst_rib_group
   | rst_route
;

rot_srlg
:
   SRLG rot_srlg_tail
;

rot_srlg_tail
:
   srlgt_named
;

rst_rib_group
:
   RIB_GROUP name = variable
;

rst_route
:
   ROUTE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) rst_route_tail
;

rst_route_tail
:
   srt_common
   | srt_qualified_next_hop
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
   | rot_bmp
   | rot_generate
   | rot_interface_routes
   | rot_martians
   | rot_null
   | rot_rib
   | rot_rib_groups
   | rot_router_id
   | rot_srlg
   | rot_static
;

srlgnt_srlg_cost
:
   SRLG_COST cost = DEC
;

srlgnt_srlg_value
:
   SRLG_VALUE value = DEC
;

srlgt_named
:
   name = variable srlgt_named_tail
;

srlgt_named_tail
:
   srlgnt_srlg_cost
   | srlgnt_srlg_value
;

srt_active
:
   ACTIVE
;

srt_as_path
:
   AS_PATH PATH
   (
      path += DEC
   )+
;

srt_common
:
   srt_active
   | srt_as_path
   | srt_community
   | srt_discard
   | srt_install
   | srt_next_hop
   | srt_next_table
   | srt_no_readvertise
   | srt_no_retain
   | srt_passive
   | srt_preference
   | srt_readvertise
   | srt_reject
   | srt_resolve
   | srt_retain
   | srt_tag
;

srt_community
:
   COMMUNITY standard_community
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
      | interface_id
   )
;

srt_next_table
:
   NEXT_TABLE name = variable
;

srt_no_readvertise
:
   NO_READVERTISE
;

srt_no_retain
:
   NO_RETAIN
;

srt_passive
:
   PASSIVE
;

srt_preference
:
   PREFERENCE pref = DEC
;

srt_qualified_next_hop
:
   QUALIFIED_NEXT_HOP nexthop = IP_ADDRESS srt_common?
;

srt_readvertise
:
   READVERTISE
;

srt_reject
:
   REJECT
;

srt_resolve
:
   RESOLVE
;

srt_retain
:
   RETAIN
;

srt_tag
:
   TAG tag = DEC
;

vtt_community
:
   extended_community
;

vtt_export
:
   EXPORT extended_community
;

vtt_import
:
   IMPORT extended_community
;
