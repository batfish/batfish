parser grammar FlatJuniper_routing_instances;

import
   FlatJuniper_common,
   FlatJuniper_bmp,
   FlatJuniper_bridge_domains,
   FlatJuniper_forwarding_options,
   FlatJuniper_protocols,
   FlatJuniper_snmp;

options {
   tokenVocab = FlatJuniperLexer;
}

ri_class_of_service
:
   s_class_of_service
;

ri_description
:
   description
;

ri_instance_type
:
   INSTANCE_TYPE
   (
      FORWARDING
      | L2VPN
      | VIRTUAL_ROUTER
      | VIRTUAL_SWITCH
      | VRF
   )
;

ri_interface
:
   INTERFACE id = interface_id
;

ri_named_routing_instance
:
   name = junos_name
   (
      apply
      | s_bridge_domains
      | s_forwarding_options
      | s_routing_options
      | ri_description
      | ri_instance_type
      | ri_interface
      | ri_null
      | ri_protocols
      | ri_route_distinguisher
      | ri_snmp
      | ri_vrf_export
      | ri_vrf_import
      | ri_vrf_table_label
      | ri_vrf_target
      | ri_vtep_source_interface
   )
;

ri_null
:
   (
      CHASSIS
      | EVENT_OPTIONS
      | PROVIDER_TUNNEL
      | SERVICES
   ) null_filler
;

ri_protocols
:
   s_protocols
;

ri_route_distinguisher
:
   ROUTE_DISTINGUISHER route_distinguisher
;

ri_snmp
:
   s_snmp
;

ri_vrf_export
:
   VRF_EXPORT name = junos_name
;

ri_vrf_import
:
   VRF_IMPORT name = junos_name
;

ri_vrf_table_label
:
   VRF_TABLE_LABEL
;

ri_vrf_target
:
   VRF_TARGET
   (
      riv_community
      | riv_export
      | riv_import
   )
;

ri_vtep_source_interface
:
   VTEP_SOURCE_INTERFACE iface = interface_id
;

riv_community
:
   extended_community
;

riv_export
:
   EXPORT extended_community
;

riv_import
:
   IMPORT extended_community
;

ro_aggregate
:
  AGGREGATE
  (
    apply
    | roa_defaults
    | roa_route
  )
;

ro_auto_export
:
   AUTO_EXPORT
;

ro_autonomous_system
:
   AUTONOMOUS_SYSTEM asn = bgp_asn?
   (
      apply
      |
      (
         roas_asdot_notation
         | roas_loops
      )*
   )
;

ro_confederation
:
  CONFEDERATION num = dec?
  (
    MEMBERS member += dec
  )*
;

ro_forwarding_table
:
   FORWARDING_TABLE
   (
      rof_export
      | rof_no_ecmp_fast_reroute
      | rof_null
   )
;

ro_generate
:
  GENERATE
  (
    apply
    | rog_defaults
    | rog_route
  )
;

ro_instance_import
:
   INSTANCE_IMPORT name = junos_name
;

ro_interface_routes
:
   INTERFACE_ROUTES
   (
      roi_family
      | roi_rib_group
   )
;

ro_martians
:
   MARTIANS null_filler
;

ro_maximum_prefixes
:
  MAXIMUM_PREFIXES null_filler
;

ro_null
:
   (
      GRACEFUL_RESTART
      | LSP_TELEMETRY
      | MULTICAST
      | MULTIPATH
      | NONSTOP_ROUTING
      | OPTIONS
      | PPM
      | TRACEOPTIONS
   ) null_filler
;

ro_resolution
:
  RESOLUTION
  (
    apply
    | rores_rib
  )
;

rores_rib
:
  RIB name = rib_name
  (
    apply
    | roresr_import
  )
;

roresr_import
:
  IMPORT expr = policy_expression
;

ro_rib
:
   RIB
   (
      ror_inet
      | ror_inet6
      | ror_iso
      | ror_mpls
   )
;


ror_inet
:
    name = inet_rib_name ror_common
;

ror_inet6
:
    name = inet6_rib_name
    (
      apply
      | ro_aggregate
      | ro_generate
      | ro_martians
      | ro6_static
   )
;

ror_iso
:
    name = iso_rib_name ror_common
;

ror_mpls
:
    name = mpls_rib_name ror_common
;


ror_common
:
  apply
  | ro_aggregate
  | ro_generate
  | ro_martians
  | ro_static
;

ro_rib_groups
:
   RIB_GROUPS name = junos_name
   (
      ror_export_rib
      | ror_import_policy
      | ror_import_rib
   )
;

ro_route_distinguisher_id
:
   ROUTE_DISTINGUISHER_ID addr = ip_address
;

ro_router_id
:
   ROUTER_ID id = ip_address
;

ro_srlg
:
   SRLG name = junos_name
   (
      roslrg_srlg_cost
      | roslrg_srlg_value
   )
;

ro_static
:
   STATIC
   (
      apply
      | ros_rib_group
      | ros_route4
   )
;

ro6_static
:
   STATIC
   (
      apply
      | ros_rib_group
      | ros_route6
   )
;

ro_validation
:
   VALIDATION
   (
     rov_group
     | rov_notification_rib
   )
;

rov_group
:
   GROUP name = junos_name
   (
      rovg_max_sessions
      | rovg_session
   )
;

rovg_max_sessions: MAX_SESSIONS num = dec;
rovg_session
:
   SESSION server_ip = ip_address
   (
      rovgs_hold_time
      | rovgs_local_address
      | rovgs_port
      | rovgs_record_lifetime
      | rovgs_refresh_time
   )
;

rovgs_hold_time
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/session-edit-routing-options-validation.html
   // 10-3600, default 600
   HOLD_TIME secs=uint16
;

rovgs_local_address
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/session-edit-routing-options-validation.html
   LOCAL_ADDRESS ip = ip_address
;

rovgs_port
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/session-edit-routing-options-validation.html
   // 1-255, default 100
   PORT port
;

rovgs_record_lifetime
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/session-edit-routing-options-validation.html
   // 60-604800, default 3600
   RECORD_LIFETIME secs=uint32
;

rovgs_refresh_time
:
   // https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/session-edit-routing-options-validation.html
   // 1-1800, default 300
   REFRESH_TIME secs=uint16
;

rov_notification_rib: NOTIFICATION_RIB rib = junos_name;

roa_active
:
  ACTIVE
;

roa_as_path
:
   AS_PATH?
   (
      roaa_aggregator
      | roaa_origin
      | roaa_path
   )
;

roa_common
:
  apply
  | roa_active
  | roa_as_path
  | roa_community
  | roa_discard
  | roa_passive
  | roa_policy
  | roa_preference
  | roa_tag
;

roa_community
:
   COMMUNITY community = STANDARD_COMMUNITY
;

roa_defaults
:
  DEFAULTS roa_common
;

roa_discard
:
  DISCARD
;

roa_passive
:
  PASSIVE
;

roa_policy
:
  POLICY expr = policy_expression
;

roa_preference
:
   PREFERENCE preference = dec
;

roa_route
:
  ROUTE
  (
    prefix = ip_prefix_default_32
    | prefix6 = ipv6_prefix
  )
  (
    apply
    | roa_common
  )
;

roa_tag
:
   TAG tag = dec
;

roaa_aggregator
:
   AGGREGATOR as = dec ip = IP_ADDRESS
;

roaa_origin
:
   ORIGIN IGP
;

roaa_path
:
   PATH path = as_path_expr
;

roas_asdot_notation
:
   ASDOT_NOTATION
;

roas_loops
:
   LOOPS dec
;

rof_export
:
   EXPORT expr = policy_expression
;

rof_no_ecmp_fast_reroute
:
   NO_ECMP_FAST_REROUTE
;

rof_null
:
   (
      INDIRECT_NEXT_HOP
      | INDIRECT_NEXT_HOP_CHANGE_ACKNOWLEDGEMENTS
   ) null_filler
;

rog_active
:
   ACTIVE
;

rog_common
:
  apply
  | rog_active
  | rog_community
  | rog_discard
  | rog_metric
  | rog_passive
  | rog_policy
  | rog_tag
;

rog_community
:
   COMMUNITY standard_community
;

rog_defaults
:
  DEFAULTS rog_common
;

rog_discard
:
   DISCARD
;

rog_metric
:
   METRIC metric = dec
;

rog_passive
:
  PASSIVE
;

rog_policy
:
   POLICY expr = policy_expression
;

rog_tag
:
   TAG tag = uint32
;

rog_route
:
  ROUTE
  (
    prefix = ip_prefix_default_32
    | ipv6_prefix
  ) rog_common
;

roi_family
:
   FAMILY
   (
      roif_inet
      | roif_null
   )
;

roi_rib_group
:
   RIB_GROUP (INET | INET6) name = junos_name
;

roif_inet
:
   INET
   (
      roifi_export
   )
;

roif_null
:
   INET6 null_filler
;

roifi_export
:
   EXPORT
   (
      roifie_lan
      | roifie_point_to_point
   )
;

roifie_lan
:
   LAN
;

roifie_point_to_point
:
   POINT_TO_POINT
;

ror_export_rib
:
   EXPORT_RIB rib = junos_name
;

ror_import_policy
:
   IMPORT_POLICY expr = policy_expression
;

ror_import_rib
:
   IMPORT_RIB rib = junos_name
;

ros_rib_group
:
   RIB_GROUP name = junos_name
;


ros_route4
:
    ROUTE prefix = ip_prefix_default_32
   (
      rosr_common
      | rosr_qualified_next_hop
   )
;

ros_route6
:
    ROUTE prefix = ipv6_prefix_default_128
   (
      rosr_common
      | rosr_qualified_next_hop6
   )
;

srlg_cost
:
  // 1-65535, default 1
  uint16
;

srlg_value
:
  // 1-4294967295
  uint32
;

roslrg_srlg_cost
:
   SRLG_COST cost = srlg_cost
;

roslrg_srlg_value
:
   SRLG_VALUE value = srlg_value
;

rosr_active
:
   ACTIVE
;

rosr_as_path
:
   AS_PATH PATH path = as_path_expr
;

rosr_common
:
   rosr_active
   | rosr_as_path
   | rosr_community
   | rosr_discard
   | rosr_install
   | rosr_no_install
   | rosr_metric
   | rosr_next_hop
   | rosr_next_table
   | rosr_no_readvertise
   | rosr_no_retain
   | rosr_passive
   | rosr_preference
   | rosr_readvertise
   | rosr_reject
   | rosr_resolve
   | rosr_retain
   | rosr_tag
   | rosr_tag2
;

rosr_community
:
   COMMUNITY standard_community
;

rosr_discard
:
   DISCARD
;

rosr_install
:
   INSTALL
;

rosr_metric
:
   METRIC metric = dec
   (
      TYPE dec
   )?
;

rosr_next_hop
:
   NEXT_HOP
   (
      ip_address
      | ipv6_address
      | interface_id
   )
;

rosr_next_table
:
   NEXT_TABLE name = junos_name
;

rosr_no_install
:
   NO_INSTALL
;

rosr_no_readvertise
:
   NO_READVERTISE
;

rosr_no_retain
:
   NO_RETAIN
;

rosr_passive
:
   PASSIVE
;

rosr_preference
:
   PREFERENCE pref = dec
;

rosr_qualified_next_hop
:
   QUALIFIED_NEXT_HOP
   (
      ip_address
      | interface_id
   )
   rosrqnh_common?
;

rosr_qualified_next_hop6
:
   QUALIFIED_NEXT_HOP
   (
      ipv6_address
      | interface_id
   )
   rosrqnh_common?
;

rosr_readvertise
:
   READVERTISE
;

rosr_reject
:
   REJECT
;

rosr_resolve
:
   RESOLVE
;

rosr_retain
:
   RETAIN
;

rosr_tag
:
   TAG tag = uint32
;

rosr_tag2
:
   TAG2 tag = uint32
;

rosrqnh_common
:
   (
      rosrqnhc_metric
      | rosrqnhc_preference
      | rosrqnhc_tag
   )
;

rosrqnhc_metric
:
   METRIC metric = dec
;

rosrqnhc_preference
:
   PREFERENCE pref = dec
;

rosrqnhc_tag
:
   TAG tag = uint32
;

s_routing_instances
:
   ROUTING_INSTANCES
   (
      apply
      | ri_named_routing_instance
   )
;

s_routing_options
:
   ROUTING_OPTIONS
   (
      apply
      | ro_aggregate
      | ro_auto_export
      | ro_autonomous_system
      | ro_bmp
      | ro_confederation
      | ro_forwarding_table
      | ro_generate
      | ro_instance_import
      | ro_interface_routes
      | ro_martians
      | ro_maximum_prefixes
      | ro_null
      | ro_resolution
      | ro_rib
      | ro_rib_groups
      | ro_route_distinguisher_id
      | ro_router_id
      | ro_srlg
      | ro_static
      | ro_validation
   )
;
