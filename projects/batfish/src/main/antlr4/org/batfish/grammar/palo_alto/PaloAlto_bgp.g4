parser grammar PaloAlto_bgp;

import PaloAlto_common, PaloAlto_policy_rule;

options {
    tokenVocab = PaloAltoLexer;
}

bgp_local_pref
:
// 0-4294967295
   uint32
;

bgp_peer_group_name
:
// 1-31 chars
   variable
;

bgp_peer_name
:
// 1-31 chars
   variable
;

vrp_bgp
:
    BGP
    (
        bgp_enable
        | bgp_install_route
        | bgp_local_as
        | bgp_null
        | bgp_peer_group
        | bgp_policy
        | bgp_redist_rules
        | bgp_reject_default_route
        | bgp_router_id
        | bgp_routing_options
    )
;

bgp_enable
:
    ENABLE yn = yes_or_no
;

bgp_install_route
:
    INSTALL_ROUTE yn = yes_or_no
;

bgp_local_as
:
    LOCAL_AS asn = bgp_asn
;

bgp_null
:
    DAMPENING_PROFILE
    null_rest_of_line
;

bgp_peer_group
:
    PEER_GROUP bgppg_definition?
;

bgp_redist_rules
:
    REDIST_RULES
    (
        bgprr_prefix
        | bgprr_ip_address
        | bgprr_profile_name
    )
;

bgppg_definition
:
    name = bgp_peer_group_name
    (
        bgppg_enable
        | bgppg_peer
        | bgppg_type
    )?
;

bgppg_enable
:
    ENABLE yn = yes_or_no
;

bgppg_peer
:
    PEER name = bgp_peer_name
    (
        bgppgp_bfd
        | bgppgp_connection_options
        | bgppgp_enable
        | bgppgp_local_address
        | bgppgp_max_prefixes
        | bgppgp_peer_address
        | bgppgp_peer_as
//        | bgppgp_peering_type
        | bgppgp_reflector_client
    )?
;

bgppgp_bfd
:
    BFD null_rest_of_line
;

bgppgp_connection_options
:
    CONNECTION_OPTIONS
    (
        bgppgp_co_incoming_bgp_connection
        | bgppgp_co_multihop
        | bgppgp_co_null
        | bgppgp_co_outgoing_bgp_connection
    )
;

bgppgp_co_incoming_bgp_connection
:
    INCOMING_BGP_CONNECTION
    (
        bgppgp_coi_allow
        | bgppgp_coi_remote_port
    )
;

bgppgp_coi_allow
:
    ALLOW yn = yes_or_no
;

bgppgp_coi_remote_port
:
    REMOTE_PORT p = port_number
;

bgppgp_co_multihop
:
    MULTIHOP num = uint8 // 0-255
;

bgppgp_co_null
:
    (
       HOLD_TIME
       | IDLE_HOLD_TIME
       | KEEP_ALIVE_INTERVAL
       | MIN_ROUTE_ADV_INTERVAL
       | OPEN_DELAY_TIME
    )
    null_rest_of_line
;

bgppgp_co_outgoing_bgp_connection
:
    OUTGOING_BGP_CONNECTION
    (
        bgppgp_coo_allow
        | bgppgp_coo_local_port
    )
;

bgppgp_coo_allow
:
    ALLOW yn = yes_or_no
;

bgppgp_coo_local_port
:
    LOCAL_PORT p = port_number
;

bgppgp_enable
:
    ENABLE yn = yes_or_no
;

bgppgp_local_address
:
    LOCAL_ADDRESS
    (
        bgppgp_la_interface
        | bgppgp_la_ip
    )
;

bgppgp_max_prefixes
:
    MAX_PREFIXES num = uint16
;

bgppgp_la_interface
:
    INTERFACE name = variable
;

bgppgp_la_ip
:
    IP addr = interface_address
;

bgppgp_peer_address
:
    PEER_ADDRESS IP addr = ip_address_or_slash32
;

bgppgp_peer_as
:
    PEER_AS asn = bgp_asn
;

bgppgp_reflector_client
:
    REFLECTOR_CLIENT (CLIENT | MESHED_CLIENT | NON_CLIENT)
;

bgppg_type
:
    TYPE
    (
        bgppgt_ebgp
        | bgppgt_ibgp
    )?
;

bgppgt_ebgp
:
    EBGP
    (
        bgppgte_export_nexthop
        | bgppgte_import_nexthop
        | bgppgte_remove_private_as
    )?
;

bgppgte_export_nexthop
:
    EXPORT_NEXTHOP (RESOLVE | USE_SELF)
;

bgppgte_import_nexthop
:
    IMPORT_NEXTHOP (ORIGINAL | USE_PEER)
;

bgppgte_remove_private_as
:
    REMOVE_PRIVATE_AS yn = yes_or_no
;

bgppgt_ibgp
:
    IBGP
    // TODO ibgp-specific options
;

bgp_policy
:
    POLICY
    (
        bgpp_export
        | bgpp_import
    )
;

bgpp_export
:
    EXPORT bgp_policy_rule?
;

bgpp_import
:
    IMPORT bgp_policy_rule?
;

bgp_reject_default_route
:
    REJECT_DEFAULT_ROUTE yn = yes_or_no
;

bgp_router_id
:
    ROUTER_ID addr = ip_address_or_slash32
;

bgp_routing_options
:
    ROUTING_OPTIONS
    (
        bgpro_aggregate
        | bgpro_as_format
        | bgpro_default_local_preference
        | bgpro_graceful_restart
        | bgpro_med
        | bgpro_reflector_cluster_id
    )
;

bgpro_aggregate
:
    AGGREGATE
    (
        bgproa_aggregate_med
    )
;

bgproa_aggregate_med
:
    AGGREGATE_MED yn = yes_or_no
;

bgpro_as_format
:
    AS_FORMAT (TWO_BYTE | FOUR_BYTE)
;

bgpro_default_local_preference
:
    DEFAULT_LOCAL_PREFERENCE pref = bgp_local_pref
;

bgpro_graceful_restart
:
    GRACEFUL_RESTART
    (
        bgprog_enable
    )
;

bgprog_enable
:
    ENABLE yn = yes_or_no
;

bgpro_med
:
    MED
    (
        bgprom_always_compare_med
        | bgprom_deterministic_med_comparison
    )
;

bgprom_always_compare_med
:
    ALWAYS_COMPARE_MED yn = yes_or_no
;

bgprom_deterministic_med_comparison
:
    DETERMINISTIC_MED_COMPARISON yn = yes_or_no
;

bgpro_reflector_cluster_id
:
    REFLECTOR_CLUSTER_ID id = ip_address
;

bgprr_general
:
    (
        bgprrg_address_family_identifier
        | bgprrg_enable
        | bgprrg_route_table
        | bgprrg_set_origin
    )
;

bgprrg_address_family_identifier
:
    ADDRESS_FAMILY_IDENTIFIER (IPV4 | IPV6)
;

bgprrg_enable
:
    ENABLE yn = yes_or_no
;

bgprrg_route_table
:
    ROUTE_TABLE (BOTH | MULTICAST | UNICAST)
;


bgprrg_set_origin
:
    SET_ORIGIN (EGP | IGP | INCOMPLETE)
;

bgprr_ip_address
:
    addr = ip_address bgprr_general
;

bgprr_prefix
:
    prefix = ip_prefix bgprr_general
;

bgprr_profile_name
:
    name = variable bgprr_general
;

