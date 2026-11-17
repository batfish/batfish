parser grammar PaloAlto_logical_router;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

lr_bgp_local_pref
:
    // 0-4294967295
    uint32
;

lr_bgp_peer_group_name
:
    // 1-63 chars
    variable
;

lr_bgp_peer_name
:
    // 1-63 chars
    variable
;

lr_non_af_variable
:
    ~(IPV4 | IPV6 | NEWLINE)
;

lr_non_unicast_variable
:
    ~(UNICAST | NEWLINE)
;

lr_protocol_ad
:
    // 10-240
    uint8
;

sn_logical_router
:
    LOGICAL_ROUTER lr_definition?
;

lr_definition
:
    name = variable lr_vrf?
;

lr_vrf
:
    VRF lrv_definition?
;

lrv_definition
:
    name = variable
    (
        lrv_admin_dists
        | lrv_aggregate_routes
        | lrv_bgp
        | lrv_ecmp
        | lrv_global_bfd_null
        | lrv_interface
        | lrv_multicast
        | lrv_ospf
        | lrv_ospfv3
        | lrv_rib_filter
        | lrv_rip
        | lrv_routing_table
    )?
;

lrv_admin_dists
:
    ADMIN_DISTS
    (
        lrvad_bgp_external
        | lrvad_bgp_internal
        | lrvad_bgp_local
        | lrvad_ebgp
        | lrvad_ibgp
        | lrvad_ospf_ext
        | lrvad_ospf_int
        | lrvad_ospf_inter
        | lrvad_ospf_intra
        | lrvad_ospfv3_ext
        | lrvad_ospfv3_int
        | lrvad_ospfv3_inter
        | lrvad_ospfv3_intra
        | lrvad_rip
        | lrvad_static
        | lrvad_static_ipv6
    )?
;

lrvad_bgp_external
:
    BGP_EXTERNAL ad = lr_protocol_ad
;

lrvad_bgp_internal
:
    BGP_INTERNAL ad = lr_protocol_ad
;

lrvad_bgp_local
:
    BGP_LOCAL ad = lr_protocol_ad
;

lrvad_ebgp
:
    EBGP ad = lr_protocol_ad
;

lrvad_ibgp
:
    IBGP ad = lr_protocol_ad
;

lrvad_ospf_ext
:
    OSPF_EXT ad = lr_protocol_ad
;

lrvad_ospf_int
:
    OSPF_INT ad = lr_protocol_ad
;

lrvad_ospf_inter
:
    OSPF_INTER ad = lr_protocol_ad
;

lrvad_ospf_intra
:
    OSPF_INTRA ad = lr_protocol_ad
;

lrvad_ospfv3_ext
:
    OSPFV3_EXT ad = lr_protocol_ad
;

lrvad_ospfv3_int
:
    OSPFV3_INT ad = lr_protocol_ad
;

lrvad_ospfv3_inter
:
    OSPFV3_INTER ad = lr_protocol_ad
;

lrvad_ospfv3_intra
:
    OSPFV3_INTRA ad = lr_protocol_ad
;

lrvad_rip
:
    RIP ad = lr_protocol_ad
;

lrvad_static
:
    STATIC ad = lr_protocol_ad
;

lrvad_static_ipv6
:
    STATIC_IPV6 ad = lr_protocol_ad
;

lrv_aggregate_routes
:
    AGGREGATE_ROUTES null_rest_of_line
;

lrv_ecmp
:
    ECMP
    (
        lrvecmp_algorithm_null
        | lrvecmp_enable
        | lrvecmp_max_path_null
        | lrvecmp_strict_source_path_null
        | lrvecmp_symmetric_return_null
    )
;

lrvecmp_algorithm_null
:
    ALGORITHM null_rest_of_line
;

lrvecmp_enable
:
    ENABLE yn = yes_or_no
;

lrvecmp_max_path_null
:
    MAX_PATH uint8
;

lrvecmp_strict_source_path_null
:
    STRICT_SOURCE_PATH yes_or_no
;

lrvecmp_symmetric_return_null
:
    SYMMETRIC_RETURN yes_or_no
;

lrv_global_bfd_null
:
    GLOBAL_BFD null_rest_of_line
;

lrv_interface
:
    INTERFACE variable_list
;

lrv_multicast
:
    MULTICAST null_rest_of_line
;

lrv_ospf
:
    OSPF null_rest_of_line
;

lrv_ospfv3
:
    OSPFV3 null_rest_of_line
;

lrv_rib_filter
:
    RIB_FILTER null_rest_of_line
;

lrv_rip
:
    RIP null_rest_of_line
;

lrv_routing_table
:
    ROUTING_TABLE
    (
        lrvrt_ip
        | lrvrt_ipv6_null
    )
;

lrvrt_ip
:
    IP lrvrti_static_route
;

lrvrt_ipv6_null
:
    IPV6 null_rest_of_line
;

lrvrti_static_route
:
    STATIC_ROUTE name = variable
    (
        lrvrtis_admin_dist
        | lrvrtis_bfd_null
        | lrvrtis_destination
        | lrvrtis_interface
        | lrvrtis_metric
        | lrvrtis_nexthop
        | lrvrtis_path_monitor
        | lrvrtis_route_table
    )
;

lrvrtis_admin_dist
:
    ADMIN_DIST distance = lr_protocol_ad
;

lrvrtis_bfd_null
:
    BFD null_rest_of_line
;

lrvrtis_destination
:
    DESTINATION destination = ip_prefix
;

lrvrtis_interface
:
    INTERFACE iface = variable
;

lrvrtis_metric
:
    METRIC metric = uint16
;

lrvrtis_nexthop
:
    NEXTHOP
    (
        lrvrtisn_discard
        | lrvrtisn_ip
        | lrvrtisn_next_lr
        | lrvrtisn_next_vr
    )
;

lrvrtisn_discard
:
    DISCARD
;

lrvrtisn_ip
:
    IP_ADDRESS_LITERAL addr = interface_address_or_reference
;

lrvrtisn_next_lr
:
    NEXT_LR name = variable
;

lrvrtisn_next_vr
:
    NEXT_VR name = variable
;

lrvrtis_path_monitor
:
    PATH_MONITOR
    (
        lrvrtispm_enable
        | lrvrtispm_failure_condition_null
        | lrvrtispm_hold_time_null
        | lrvrtispm_monitor_destinations_null
    )
;

lrvrtispm_enable
:
    ENABLE yn = yes_or_no
;

lrvrtispm_failure_condition_null
:
    FAILURE_CONDITION (ANY | ALL)
;

lrvrtispm_hold_time_null
:
    HOLD_TIME uint16
;

lrvrtispm_monitor_destinations_null
:
    MONITOR_DESTINATIONS null_rest_of_line
;

lrvrtis_route_table
:
    ROUTE_TABLE lrvrtisrt_unicast_null
;

lrvrtisrt_unicast_null
:
    UNICAST
;

lrv_bgp
:
    BGP
    (
        lrb_advertise_network
        | lrb_aggregate_routes
        | lrb_allow_redist_default_route_null
        | lrb_always_advertise_network_route_null
        | lrb_confederation_member_as_null
        | lrb_dampening_profile_null
        | lrb_default_local_preference
        | lrb_ecmp_multi_as_null
        | lrb_enable
        | lrb_enforce_first_as_null
        | lrb_fast_external_failover_null
        | lrb_global_bfd_null
        | lrb_graceful_restart
        | lrb_graceful_shutdown_null
        | lrb_install_route
        | lrb_local_as
        | lrb_med
        | lrb_peer_group
        | lrb_redistribution_profile
        | lrb_reject_default_route
        | lrb_router_id
    )?
;

lrb_advertise_network
:
    ADVERTISE_NETWORK
    (
        lrban_ipv4
        | lrban_ipv6_null
    )?
;

lrban_ipv4
:
    IPV4
    (
        lrban4_network
        | lrban4_prefix
    )?
;

lrban4_network
:
    NETWORK lrban4_prefix?
;

lrban4_prefix
:
    prefix = ip_prefix
    (
        lrban4p_backdoor_null
        | lrban4p_multicast_null
        | lrban4p_unicast
    )?
;

lrban4p_backdoor_null
:
    BACKDOOR yes_or_no
;

lrban4p_multicast_null
:
    MULTICAST yes_or_no
;

lrban4p_unicast
:
    UNICAST yn = yes_or_no
;

lrban_ipv6_null
:
    IPV6 null_rest_of_line
;

lrb_aggregate_routes
:
    AGGREGATE_ROUTES null_rest_of_line
;

lrb_allow_redist_default_route_null
:
    ALLOW_REDIST_DEFAULT_ROUTE yes_or_no
;

lrb_always_advertise_network_route_null
:
    ALWAYS_ADVERTISE_NETWORK_ROUTE yes_or_no
;

lrb_confederation_member_as_null
:
    CONFEDERATION_MEMBER_AS null_rest_of_line
;

lrb_dampening_profile_null
:
    DAMPENING_PROFILE null_rest_of_line
;

lrb_default_local_preference
:
    DEFAULT_LOCAL_PREFERENCE pref = lr_bgp_local_pref
;

lrb_ecmp_multi_as_null
:
    ECMP_MULTI_AS yes_or_no
;

lrb_enable
:
    ENABLE yn = yes_or_no
;

lrb_enforce_first_as_null
:
    ENFORCE_FIRST_AS yes_or_no
;

lrb_fast_external_failover_null
:
    FAST_EXTERNAL_FAILOVER yes_or_no
;

lrb_global_bfd_null
:
    GLOBAL_BFD null_rest_of_line
;

lrb_graceful_restart
:
    GRACEFUL_RESTART
    (
        lrbgr_enable
        | lrbgr_local_restart_time_null
        | lrbgr_max_peer_restart_time_null
        | lrbgr_stale_route_time_null
    )
;

lrbgr_enable
:
    ENABLE yn = yes_or_no
;

lrbgr_local_restart_time_null
:
    LOCAL_RESTART_TIME uint16
;

lrbgr_max_peer_restart_time_null
:
    MAX_PEER_RESTART_TIME uint16
;

lrbgr_stale_route_time_null
:
    STALE_ROUTE_TIME uint16
;

lrb_graceful_shutdown_null
:
    GRACEFUL_SHUTDOWN yes_or_no
;

lrb_install_route
:
    INSTALL_ROUTE yn = yes_or_no
;

lrb_local_as
:
    LOCAL_AS asn = bgp_asn
;

lrb_med
:
    MED
    (
        lrbm_always_compare_med
        | lrbm_deterministic_med_comparison
    )
;

lrbm_always_compare_med
:
    ALWAYS_COMPARE_MED yn = yes_or_no
;

lrbm_deterministic_med_comparison
:
    DETERMINISTIC_MED_COMPARISON yn = yes_or_no
;

lrb_peer_group
:
    PEER_GROUP lrbgppg_definition?
;

lrbgppg_definition
:
    name = lr_bgp_peer_group_name
    (
        lrbgppg_address_family
        | lrbgppg_aggregated_confed_as_path_null
        | lrbgppg_connection_options
        | lrbgppg_enable
        | lrbgppg_filtering_profile
        | lrbgppg_peer
        | lrbgppg_soft_reset_with_stored_info_null
        | lrbgppg_type
    )?
;

lrbgppg_address_family
:
    ADDRESS_FAMILY
    (
        lrbgppgaf_ipv4
        | lrbgppgaf_ipv6
    )
;

lrbgppgaf_ipv4
:
    IPV4
    (
        lrbgppgaf_unicast
        | lrbgppgaf_profile
    )
;

lrbgppgaf_ipv6
:
    IPV6
    (
        lrbgppgaf_unicast
        | lrbgppgaf_profile
    )
;

lrbgppgaf_unicast
:
    UNICAST name = variable
;

lrbgppgaf_profile
:
    name = lr_non_unicast_variable
;

lrbgppg_aggregated_confed_as_path_null
:
    AGGREGATED_CONFED_AS_PATH yes_or_no
;

lrbgppg_connection_options
:
    CONNECTION_OPTIONS
    (
        lrbgppgco_authentication_null
        | lrbgppgco_multihop_null
        | lrbgppgco_timers
    )
;

lrbgppgco_authentication_null
:
    AUTHENTICATION null_rest_of_line
;

lrbgppgco_multihop_null
:
    MULTIHOP null_rest_of_line
;

lrbgppgco_timers
:
    TIMERS name = variable
;

lrbgppg_enable
:
    ENABLE yn = yes_or_no
;

lrbgppg_filtering_profile
:
    FILTERING_PROFILE
    (
        lrbgppgfp_ipv4
        | lrbgppgfp_ipv6
    )
;

lrbgppgfp_ipv4
:
    IPV4
    (
        lrbgppgfp_unicast
        | lrbgppgfp_profile
    )
;

lrbgppgfp_ipv6
:
    IPV6
    (
        lrbgppgfp_unicast
        | lrbgppgfp_profile
    )
;

lrbgppgfp_unicast
:
    UNICAST name = variable
;

lrbgppgfp_profile
:
    name = lr_non_unicast_variable
;

lrbgppg_peer
:
    PEER name = lr_bgp_peer_name
    (
        lrbgppg_address_family
        | lrbgppgp_bfd_null
        | lrbgppgp_connection_options
        | lrbgppgp_enable
        | lrbgppgp_enable_mp_bgp_null
        | lrbgppgp_enable_sender_side_loop_detection
        | lrbgppg_filtering_profile
        | lrbgppgp_inherit_null
        | lrbgppgp_local_address
        | lrbgppgp_max_prefixes
        | lrbgppgp_passive_null
        | lrbgppgp_peer_address
        | lrbgppgp_peer_as
        | lrbgppgp_peering_type_null
        | lrbgppgp_reflector_client
        | lrbgppgp_safi_null
    )?
;

lrbgppgp_bfd_null
:
    BFD null_rest_of_line
;

lrbgppgp_connection_options
:
    CONNECTION_OPTIONS
    (
        lrbgppgpco_authentication_null
        | lrbgppgpco_hold_time_null
        | lrbgppgpco_idle_hold_time_null
        | lrbgppgpco_incoming_bgp_connection
        | lrbgppgpco_keep_alive_interval_null
        | lrbgppgpco_min_route_adv_interval_null
        | lrbgppgpco_multihop
        | lrbgppgpco_open_delay_time_null
        | lrbgppgpco_outgoing_bgp_connection
        | lrbgppgpco_passive_null
        | lrbgppgco_timers
    )
;

lrbgppgpco_authentication_null
:
    AUTHENTICATION null_rest_of_line
;

lrbgppgpco_hold_time_null
:
    HOLD_TIME null_rest_of_line
;

lrbgppgpco_idle_hold_time_null
:
    IDLE_HOLD_TIME null_rest_of_line
;

lrbgppgpco_incoming_bgp_connection
:
    INCOMING_BGP_CONNECTION
    (
        lrbgppgpcoi_allow
        | lrbgppgpcoi_remote_port
    )
;

lrbgppgpcoi_allow
:
    ALLOW yn = yes_or_no
;

lrbgppgpcoi_remote_port
:
    REMOTE_PORT p = port_number
;

lrbgppgpco_keep_alive_interval_null
:
    KEEP_ALIVE_INTERVAL null_rest_of_line
;

lrbgppgpco_min_route_adv_interval_null
:
    MIN_ROUTE_ADV_INTERVAL null_rest_of_line
;

lrbgppgpco_multihop
:
    MULTIHOP num = uint8
;

lrbgppgpco_open_delay_time_null
:
    OPEN_DELAY_TIME null_rest_of_line
;

lrbgppgpco_outgoing_bgp_connection
:
    OUTGOING_BGP_CONNECTION
    (
        lrbgppgpcoo_allow
        | lrbgppgpcoo_local_port
    )
;

lrbgppgpcoo_allow
:
    ALLOW yn = yes_or_no
;

lrbgppgpcoo_local_port
:
    LOCAL_PORT p = port_number
;

lrbgppgpco_passive_null
:
    PASSIVE yes_or_no
;

lrbgppgp_enable
:
    ENABLE yn = yes_or_no
;

lrbgppgp_enable_mp_bgp_null
:
    ENABLE_MP_BGP yes_or_no
;

lrbgppgp_enable_sender_side_loop_detection
:
    ENABLE_SENDER_SIDE_LOOP_DETECTION yn = yes_or_no
;

lrbgppgp_inherit_null
:
    INHERIT null_rest_of_line
;

lrbgppgp_local_address
:
    LOCAL_ADDRESS
    (
        lrbgppgpla_interface
        | lrbgppgpla_ip
    )
;

lrbgppgpla_interface
:
    INTERFACE name = variable
;

lrbgppgpla_ip
:
    IP addr = interface_address_or_reference
;

lrbgppgp_max_prefixes
:
    MAX_PREFIXES num = uint16
;

lrbgppgp_passive_null
:
    PASSIVE yes_or_no
;

lrbgppgp_peer_address
:
    PEER_ADDRESS lrbpa_ip
;

lrbpa_ip
:
    IP addr = interface_address_or_reference
;

lrbgppgp_peer_as
:
    PEER_AS asn = bgp_asn
;

lrbgppgp_peering_type_null
:
    PEERING_TYPE null_rest_of_line
;

lrbgppgp_reflector_client
:
    REFLECTOR_CLIENT (CLIENT | MESHED_CLIENT | NON_CLIENT)
;

lrbgppgp_safi_null
:
    SUBSEQUENT_ADDRESS_FAMILY_IDENTIFIER null_rest_of_line
;

lrbgppg_soft_reset_with_stored_info_null
:
    SOFT_RESET_WITH_STORED_INFO yes_or_no
;

lrbgppg_type
:
    TYPE
    (
        lrbgppgt_ebgp
        | lrbgppgt_ibgp
    )?
;

lrbgppgt_ebgp
:
    EBGP
    (
        lrbgppgte_export_nexthop
        | lrbgppgte_import_nexthop
        | lrbgppgte_remove_private_as
    )?
;

lrbgppgte_export_nexthop
:
    EXPORT_NEXTHOP (RESOLVE | USE_SELF)
;

lrbgppgte_import_nexthop
:
    IMPORT_NEXTHOP (ORIGINAL | USE_PEER)
;

lrbgppgte_remove_private_as
:
    REMOVE_PRIVATE_AS yn = yes_or_no
;

lrbgppgt_ibgp
:
    IBGP
;

lrb_redistribution_profile
:
    REDISTRIBUTION_PROFILE
    (
        lrbrp_ipv4
        | lrbrp_ipv6
        | lrbrp_profile
    )
;

lrbrp_ipv4
:
    IPV4
    (
        lrbrp_unicast
        | lrbrp_af_profile
    )
;

lrbrp_ipv6
:
    IPV6
    (
        lrbrp_unicast
        | lrbrp_af_profile
    )
;

lrbrp_unicast
:
    UNICAST name = variable
;

lrbrp_af_profile
:
    name = lr_non_unicast_variable
;

lrbrp_profile
:
    name = lr_non_af_variable
;

lrb_reject_default_route
:
    REJECT_DEFAULT_ROUTE yn = yes_or_no
;

lrb_router_id
:
    ROUTER_ID addr = ip_address_or_slash32
;
