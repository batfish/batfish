parser grammar CiscoNxos_bgp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

router_bgp
:
  BGP asn = bgp_asn NEWLINE
  (
    rb_enforce_first_as
    | rb_event_history
    | rb_fast_external_fallover
    | rb_flush_routes
    | rb_isolate
    | rb_no_enforce_first_as
    | rb_proc_vrf_common
    | rb_shutdown
    | rb_template_peer
    | rb_template_peer_policy
    | rb_template_peer_session
    | rb_vrf
  )*
;

rb_proc_vrf_common
:
  rb_address_family
  | rb_bestpath
  | rb_cluster_id
  | rb_confederation_identifier
  | rb_confederation_peers
  | rb_graceful_restart
  | rb_graceful_restart_helper
  | rb_log_neighbor_changes
  | rb_maxas_limit
  | rb_neighbor
  | rb_reconnect_interval
  | rb_router_id
  | rb_suppress_fib_pending
  | rb_timers_bestpath_limit
  | rb_timers_bgp
  | rb_timers_prefix_peer_timeout
  | rb_timers_prefix_peer_wait

;

rb_address_family
:
  ADDRESS_FAMILY first =
  (
    IPV4
    | IPV6
    | L2VPN
  ) second = (EVPN | MULTICAST | MVPN | UNICAST) NEWLINE rb_af_inner*
;

rb_af_inner
:
  rb_af_additional_paths
  | rb_af_advertise
  | rb_af_aggregate_address
  | rb_af_client_to_client
  | rb_af_dampen_igp_metric
  | rb_af_dampening
  | rb_af_default_information
  | rb_af_default_metric
  | rb_af_distance
  | rb_af_inject_map
  | rb_af_maximum_paths
  | rb_af_network
  | rb_af_nexthop_route_map
  | rb_af_nexthop_trigger_delay
  | rb_af_redistribute_direct
  | rb_af_redistribute_eigrp
  | rb_af_redistribute_isis
  | rb_af_redistribute_lisp
  | rb_af_redistribute_ospf
  | rb_af_redistribute_ospfv3
  | rb_af_redistribute_rip
  | rb_af_redistribute_static
  | rb_af_suppress_inactive
  | rb_af_table_map
  | rb_af_wait_igp_convergence
;

rb_af_additional_paths
:
  ADDITIONAL_PATHS
  (
    INSTALL BACKUP
    | RECEIVE
    | SELECTION ROUTE_MAP mapname = route_map_name
    | SEND
  ) NEWLINE
;

rb_af_advertise
:
  ADVERTISE L2VPN EVPN NEWLINE
;

rb_af_aggregate_address
:
  AGGREGATE_ADDRESS
  (
    network = ip_address MASK subnet = subnet_mask
    | prefix = ip_prefix
    | prefix6 = ipv6_prefix
  ) rb_af_aa_tail* NEWLINE
;

rb_af_aa_tail
:
  ADVERTISE_MAP mapname = route_map_name
  | AS_SET
  | ATTRIBUTE_MAP mapname = route_map_name
  | SUMMARY_ONLY
  | SUPPRESS_MAP mapname = route_map_name
;

rb_af_client_to_client
:
  CLIENT_TO_CLIENT REFLECTION NEWLINE
;

rb_af_dampen_igp_metric
:
  DAMPEN_IGP_METRIC interval_secs = dampen_igp_metric_interval NEWLINE
;

dampen_igp_metric_interval
:
// 20-3600
  UINT8
  | UINT16
;

rb_af_dampening
:
  DAMPENING
  (
    half_life = decay_half_life start_reuse = start_reuse_val start_suppress =
    start_suppress_val max_suppress = uint8
    | ROUTE_MAP mapname = route_map_name
  ) NEWLINE
;

decay_half_life
:
// 1-45
  UINT8
;

start_reuse_val
:
// 1-20000
  UINT8
  | UINT16
;

start_suppress_val
:
// 1-20000
  UINT8
  | UINT16
;

rb_af_default_information
:
  DEFAULT_INFORMATION ORIGINATE NEWLINE
;

rb_af_default_metric
:
  DEFAULT_METRIC metric = uint32 NEWLINE
;

rb_af_distance
:
  DISTANCE ebgp = bgp_distance ibgp = bgp_distance local = bgp_distance NEWLINE
;

bgp_distance
:
// 1-255
  UINT8
;

rb_af_inject_map
:
  INJECT_MAP injectmap = route_map_name EXIST_MAP existmap = route_map_name
  COPY_ATTRIBUTES? NEWLINE
;

rb_af_maximum_paths
:
  MAXIMUM_PATHS
  (
    EIBGP
    | IBGP
  )? numpaths = maximum_paths NEWLINE
;

maximum_paths
:
// 1-64
  UINT8
;

rb_af_network
:
  NETWORK
  (
    address = ip_address MASK mask = subnet_mask
    | prefix = ip_prefix
    | prefix6 = ipv6_prefix
  )
  (
    ROUTE_MAP mapname = route_map_name
  )? NEWLINE
;

rb_af_nexthop_route_map
:
  NEXTHOP ROUTE_MAP mapname = route_map_name NEWLINE
;

rb_af_nexthop_trigger_delay
:
  NEXTHOP TRIGGER_DELAY CRITICAL critical = uint32 NON_CRITICAL noncritical =
  uint32 NEWLINE
;

rb_af_redistribute_direct
:
  REDISTRIBUTE DIRECT ROUTE_MAP mapname = route_map_name NEWLINE
;

rb_af_redistribute_eigrp
:
  REDISTRIBUTE EIGRP source_tag = WORD ROUTE_MAP mapname = route_map_name
  NEWLINE
;

rb_af_redistribute_isis
:
  REDISTRIBUTE ISIS source_tag = WORD ROUTE_MAP mapname = route_map_name
  NEWLINE
;

rb_af_redistribute_lisp
:
  REDISTRIBUTE LISP ROUTE_MAP mapname = route_map_name NEWLINE
;

rb_af_redistribute_ospf
:
  REDISTRIBUTE OSPF source_tag = WORD ROUTE_MAP mapname = route_map_name
  NEWLINE
;

rb_af_redistribute_ospfv3
:
  REDISTRIBUTE OSPFV3 source_tag = WORD ROUTE_MAP mapname = route_map_name
  NEWLINE
;

rb_af_redistribute_rip
:
  REDISTRIBUTE RIP source_tag = WORD ROUTE_MAP mapname = route_map_name NEWLINE
;

rb_af_redistribute_static
:
  REDISTRIBUTE STATIC ROUTE_MAP mapname = route_map_name NEWLINE
;

rb_af_suppress_inactive
:
  SUPPRESS_INACTIVE NEWLINE
;

rb_af_table_map
:
  TABLE_MAP mapname = route_map_name FILTER? NEWLINE
;

rb_af_wait_igp_convergence
:
  WAIT_IGP_CONVERGENCE NEWLINE
;

rb_bestpath
:
  BESTPATH
  (
    ALWAYS_COMPARE_MED
    | AS_PATH MULTIPATH_RELAX
    | COMPARE_ROUTERID
    | COST_COMMUNITY IGNORE
    | MED
    (
      CONFED
      | MISSING_AS_WORST
      | NON_DETERMINISTIC
    )
  ) NEWLINE
;

rb_cluster_id
:
  CLUSTER_ID
  (
    ip_as_int = uint32
    | ip = ip_address
  ) NEWLINE
;

rb_confederation_identifier
:
  CONFEDERATION IDENTIFIER asn = bgp_asn NEWLINE
;

rb_confederation_peers
:
  CONFEDERATION PEERS asns += bgp_asn NEWLINE asns += bgp_asn+ NEWLINE
;

rb_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

rb_event_history
:
  EVENT_HISTORY
  (
    CLI
    | DETAIL
    | ERRORS
    | EVENTS
    | OBJSTORE
    | PERIODIC
  )
  (
    SIZE
    (
      DISABLE
      | LARGE
      | MEDIUM
      | SMALL
      | sizebytes = event_history_buffer_size
    )
  )? NEWLINE
;

event_history_buffer_size
:
// 32768-8388608
  UINT8
  | UINT16
  | UINT32
;

rb_fast_external_fallover
:
  FAST_EXTERNAL_FALLOVER NEWLINE
;

rb_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

rb_graceful_restart
:
  GRACEFUL_RESTART
  (
    RESTART_TIME restart_secs = restart_time
    | STALEPATH_TIME stalepath_secs = stalepath_time
  )? NEWLINE
;

restart_time
:
// 1-3600
  UINT8
  | UINT16
;

stalepath_time
:
// 1-3600
  UINT8
  | UINT16
;

rb_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

rb_log_neighbor_changes
:
  LOG_NEIGHBOR_CHANGES NEWLINE
;

rb_isolate
:
  ISOLATE NEWLINE
;

rb_maxas_limit
:
  MAXAS_LIMIT limit = maxas_limit NEWLINE
;

maxas_limit
:
// 1-512
  UINT8
  | UINT16
;

rb_neighbor
:
  NEIGHBOR
  (
    ip = ip_address
    | prefix = ip_prefix
    | ip6 = ipv6_address
    | prefix6 = ipv6_prefix
  )
  (
    REMOTE_AS
    (
      asn = bgp_asn?
      | ROUTE_MAP mapname = route_map_name
    )
  )? NEWLINE rb_n_inner*
;

// We might get to this level of the hierarchy in four ways:
//  1. configuring an actual neighbor (router bgp ; (optional vrf) ; neighbor)
//  2. configuring a template peer (router bgp ; template peer)
//  3. configuring a template peer-session (router bgp ; template peer-session)
//  4. inside of DEFAULT command in any of the above 3 levels.
//     For now, DEFAULT is unhandled.
//
// We use a few different rules to distinguish the rest.
//  rb_n_common are commands valid at any of the levels 1-3.
//  rnbx_n_address_family is only valid at levels 1 and 2.

rb_n_inner
:
  rb_n_common
  | rb_n_address_family
;

rb_n_common
:
  rb_n_bfd
  | rb_n_capability
  | rb_n_default
  | rb_n_description
  | rb_n_disable_connected_check
  | rb_n_dont_capability_negotiate
  | rb_n_dynamic_capability
  | rb_n_ebgp_multihop
  | rb_n_inherit
  | rb_n_local_as
  | rb_n_low_memory
  | rb_n_maximum_peers
  | rb_n_no_shutdown
  | rb_n_password
  | rb_n_remote_as
  | rb_n_remove_private_as
  | rb_n_shutdown
  | rb_n_timers
  | rb_n_transport
  | rb_n_update_source
;

rb_n_address_family
:
  ADDRESS_FAMILY first =
  (
    IPV4
    | IPV6
    | L2VPN
  ) second = (EVPN | MULTICAST | UNICAST) NEWLINE rb_n_af_inner*
;

// We might get to this level of the hierarchy in four ways:
//  1. configuring an actual neighbor (router bgp ; (optional vrf) ; neighbor ; address-family)
//  2. configuring a template peer (router bgp ; template peer ; address-family)
//  3. configuring a template peer-policy (router bgp ; template peer-policy)
//  4. inside of DEFAULT command in any of the above 3 levels.
//     For now, DEFAULT is unhandled.
//
// All the remaining commands are valid at all levels 1-3.

rb_n_af_inner
:
  rb_n_af_advertise_map
  | rb_n_af_allowas_in
  | rb_n_af_as_override
  | rb_n_af_capability
  | rb_n_af_default
  | rb_n_af_default_originate
  | rb_n_af_disable_peer_as_check
  | rb_n_af_filter_list
  | rb_n_af_inherit
  | rb_n_af_maximum_prefix
  | rb_n_af_next_hop_self
  | rb_n_af_next_hop_third_party
  | rb_n_af_prefix_list
  | rb_n_af_route_map
  | rb_n_af_route_reflector_client
  | rb_n_af_send_community
  | rb_n_af_soft_reconfiguration
  | rb_n_af_soo
  | rb_n_af_suppress_inactive
  | rb_n_af_unsuppress_map
  | rb_n_af_weight
;

rb_n_af_advertise_map
:
  ADVERTISE_MAP mapname = route_map_name
  (
    EXIST_MAP existmap = route_map_name
    | NON_EXIST_MAP nonexistmap = route_map_name
  ) NEWLINE
;

rb_n_af_allowas_in
:
  ALLOWAS_IN
  (
    num = allowas_in_max_occurrences
  )? NEWLINE
;

allowas_in_max_occurrences
:
// 1-10
  UINT8
;

rb_n_af_as_override
:
  AS_OVERRIDE NEWLINE
;

rb_n_af_capability
:
  CAPABILITY ADDITIONAL_PATHS
  (
    RECEIVE
    | SEND
  ) DISABLE? NEWLINE
;

rb_n_af_default
:
  DEFAULT null_rest_of_line
;

rb_n_af_default_originate
:
  DEFAULT_ORIGINATE
  (
    ROUTE_MAP mapname = route_map_name
  )? NEWLINE
;

rb_n_af_disable_peer_as_check
:
  DISABLE_PEER_AS_CHECK NEWLINE
;

rb_n_af_filter_list
:
  FILTER_LIST name = ip_as_path_access_list_name
  (
    IN
    | OUT
  ) NEWLINE
;

rb_n_af_inherit
:
  INHERIT PEER_POLICY template = template_name seq = inherit_sequence_number
  NEWLINE
;

inherit_sequence_number
:
// 1-65535
  UINT8
  | UINT16
;

rb_n_af_maximum_prefix
:
  MAXIMUM_PREFIX limit = uint32
  (
    threshold_pct = threshold_percentage
  )?
  (
    RESTART interval_min = restart_interval
    | WARNING_ONLY
  )? NEWLINE
;

threshold_percentage
:
// 1-100
  UINT8
;

restart_interval
:
// 1-65535
  UINT8
  | UINT16
;

rb_n_af_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

rb_n_af_next_hop_third_party
:
  NEXT_HOP_THIRD_PARTY NEWLINE
;

rb_n_af_prefix_list
:
  PREFIX_LIST listname = ip_prefix_list_name
  (
    IN
    | OUT
  ) NEWLINE
;

rb_n_af_route_map
:
  ROUTE_MAP mapname = route_map_name
  (
    IN
    | OUT
  ) NEWLINE
;

rb_n_af_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

rb_n_af_send_community
:
  SEND_COMMUNITY
  (
    BOTH
    | STANDARD
    | EXTENDED
  )? NEWLINE
;

rb_n_af_soft_reconfiguration
:
  SOFT_RECONFIGURATION INBOUND ALWAYS? NEWLINE
;

rb_n_af_soo
:
  SOO
  (
    asn = bgp_asn
    | ip = ip_address
  ) COLON community_id = uint32 NEWLINE
;

rb_n_af_suppress_inactive
:
  SUPPRESS_INACTIVE
;

rb_n_af_unsuppress_map
:
  UNSUPPRESS_MAP mapname = route_map_name NEWLINE
;

rb_n_af_weight
:
  WEIGHT weight = uint16 NEWLINE
;

rb_n_bfd
:
  BFD NEWLINE
;

rb_n_capability
:
  CAPABILITY SUPPRESS FOUR_BYTE_AS NEWLINE
;

rb_n_default
:
  DEFAULT null_rest_of_line
;

rb_n_description
:
  DESCRIPTION desc = REMARK_TEXT NEWLINE
;

rb_n_disable_connected_check
:
  DISABLE_CONNECTED_CHECK NEWLINE
;

rb_n_dont_capability_negotiate
:
  DONT_CAPABILITY_NEGOTIATE NEWLINE
;

rb_n_dynamic_capability
:
  DYNAMIC_CAPABILITY NEWLINE
;

rb_n_ebgp_multihop
:
  EBGP_MULTIHOP ebgp_ttl = ebgp_multihop_ttl NEWLINE
;

ebgp_multihop_ttl
:
//2-255
  UINT8
;

rb_n_inherit
:
  INHERIT
  (
    PEER
    | PEER_SESSION
  ) peer = template_name NEWLINE
;

rb_n_local_as
:
  LOCAL_AS asn = bgp_asn
  (
    NO_PREPEND
    (
      REPLACE_AS DUAL_AS?
    )?
  )? NEWLINE
;

rb_n_low_memory
:
  LOW_MEMORY EXEMPT NEWLINE
;

rb_n_maximum_peers
:
  MAXIMUM_PEERS max = maximum_peers NEWLINE
;

maximum_peers
:
// 1-1000
  UINT8
  | UINT16
;

rb_n_no_shutdown
:
  NO SHUTDOWN NEWLINE
;

rb_n_password
:
  PASSWORD pass = cisco_nxos_password NEWLINE
;

rb_n_remote_as
:
  REMOTE_AS asn = bgp_asn NEWLINE
;

rb_n_remove_private_as
:
  REMOVE_PRIVATE_AS
  (
    ALL
    | REPLACE_AS
  )? NEWLINE
;

rb_n_shutdown
:
  SHUTDOWN NEWLINE
;

rb_n_timers
:
  TIMERS keepalive_secs = keepalive_interval holdtime_secs = holdtime NEWLINE
;

keepalive_interval
:
// 0-3600
  UINT8
  | UINT16
;

holdtime
:
// 0-3600
  UINT8
  | UINT16
;

rb_n_transport
:
  TRANSPORT CONNECTION_MODE PASSIVE NEWLINE
;

rb_n_update_source
:
  UPDATE_SOURCE interface_name NEWLINE
;

rb_no_enforce_first_as
:
  NO ENFORCE_FIRST_AS NEWLINE
;

rb_reconnect_interval
:
  RECONNECT_INTERVAL secs = reconnect_interval NEWLINE
;

reconnect_interval
:
// 1-60
  UINT8
;

rb_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

rb_shutdown
:
  SHUTDOWN NEWLINE
;

rb_suppress_fib_pending
:
  SUPPRESS_FIB_PENDING NEWLINE
;

rb_template_peer
:
  TEMPLATE PEER peer = template_name NEWLINE rb_n_inner*
;

rb_template_peer_policy
:
  TEMPLATE PEER_POLICY policy = template_name NEWLINE rb_n_af_inner*
;

rb_template_peer_session
:
  TEMPLATE PEER_SESSION session = template_name NEWLINE rb_n_common*
;

rb_timers_bestpath_limit
:
  TIMERS BESTPATH_LIMIT timeout_secs = bestpath_timeout ALWAYS? NEWLINE
;

bestpath_timeout
:
// 1-3600
  UINT8
  | UINT16
;

rb_timers_bgp
:
  TIMERS BGP keepalive_secs = keepalive_interval holdtime_secs = holdtime
  NEWLINE
;

rb_timers_prefix_peer_timeout
:
  TIMERS PREFIX_PEER_TIMEOUT timeout_secs = prefix_peer_timeout NEWLINE
;

prefix_peer_timeout
:
// 0-1200
  UINT8
  | UINT16
;

rb_timers_prefix_peer_wait
:
  TIMERS PREFIX_PEER_WAIT wait_secs = prefix_peer_wait_timer NEWLINE
;

prefix_peer_wait_timer
:
// 0-1200
  UINT8
  | UINT16
;

rb_vrf
:
  VRF name = vrf_name NEWLINE
  (
    rb_proc_vrf_common
    | rb_v_local_as
  )*
;

rb_v_local_as
:
  LOCAL_AS asn = bgp_asn NEWLINE
;
