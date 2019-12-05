parser grammar Arista_bgp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

eos_router_bgp_tail
:
  eos_rb_address_family
  | eos_rb_inner
  | eos_rb_monitoring
  | eos_rb_vlan
  | eos_rb_vlan_aware_bundle
  | eos_rb_vrf
;

eos_rb_address_family
:
  ADDRESS_FAMILY
  (
    eos_rb_af_ipv4
    | eos_rb_af_ipv6
    | eos_rb_af_evpn
//  | eos_rb_af_vpn_v4
//  | eos_rb_af_vpn_v6
  )
;

eos_rb_af_ipv4
:
  IPV4
  (
    eos_rb_af_ipv4_unicast
//  | eos_rb_af_ipv4_multicast
//  | eos_rb_af_ipv4_labeled_multicast
//  | eos_rb_af_ipv4_sr_te
  )
;

eos_rb_af_ipv4_unicast
:
  NEWLINE
  (
    eos_rbafipv4u_bgp
//    | eos_rbafipv4u_graceful_restart
    | eos_rbafipv4u_neighbor
    | eos_rbafipv4_no
//    | eos_rbafipv4u_network
//    | eos_rbafipv4u_redistribute
  )*
;

eos_rbafipv4u_bgp
:
  BGP
  null_rest_of_line
//  (
//    eos_rbafipv4b_additional_paths
//    | eos_rbafipv4b_next_hop
//    | eos_rbafipv4b_redistribute_internal
//    | eos_rbafipv4b_route
//  )
;

eos_rbafipv4u_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rb_af_neighbor_common
;

eos_rbafipv4_no
:
  NO
  (
    eos_rbafipv4_no_neighbor
  )
;

eos_rbafipv4_no_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rb_af_no_neighbor_common
;

eos_rb_af_ipv6
:
  IPV6
  eos_rb_af_ipv6_unicast
//  | eos_rb_af_ipv6_labeled_unicast
//  | eos_rb_af_ipv6_sr_te
;

eos_rb_af_ipv6_unicast
:
  NEWLINE
  eos_rbafipv6u_neighbor*
;

eos_rbafipv6u_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rb_af_neighbor_common
;

eos_rb_af_evpn
:
  EVPN NEWLINE
  (
    eos_rb_af_evpn_bgp
//    | eos_rb_af_evpn_graceful_restart
//    | eos_rb_af_evpn_host_flap
    | eos_rb_af_evpn_neighbor
    | eos_rb_af_evpn_no
  )*
;

eos_rb_af_evpn_bgp
:
  BGP
  (
    eos_rbafeb_additional_paths
    | eos_rbafeb_next_hop_unchanged
  )
;

eos_rb_af_evpn_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rb_af_neighbor_common
;

eos_rb_af_evpn_no:
  NO
  eos_rb_af_evpn_no_neighbor
;

eos_rb_af_evpn_no_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rb_af_no_neighbor_common
;

eos_rb_af_neighbor_common
:
  (
    eos_rbafnc_activate
//    | eos_rbafnc_additional_paths
//    | eos_rbafnc_graceful_restart
//    | eos_rbafnc_next_hop_unchanged
    | eos_rbafnc_route_map
//    | eos_rbafnc_weight
  )
;

eos_rbafeb_additional_paths
:
  ADDITIONAL_PATHS (SEND ANY | RECEIVE) NEWLINE
;

eos_rbafeb_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbafnc_activate
:
  ACTIVATE NEWLINE
;

eos_rbafnc_route_map
:
  ROUTE_MAP name = variable (IN | OUT) NEWLINE
;

eos_rb_af_no_neighbor_common
:
  eos_rbafnonc_activate
;

eos_rbafnonc_activate
:
  ACTIVATE NEWLINE
;

eos_rb_inner
:
  eos_rbi_aggregate_address
  | eos_rbi_bgp
  | eos_rbi_default_metric
  | eos_rbi_distance
  | eos_rbi_graceful_restart
  | eos_rbi_graceful_restart_helper
  | eos_rbi_ip
  | eos_rbi_ipv6
  | eos_rbi_maximum_paths
  | eos_rbi_neighbor
  | eos_rbi_network
  | eos_rbi_no
  | eos_rbi_redistribute
  | eos_rbi_router_id
  | eos_rbi_shutdown
  | eos_rbi_timers
  | eos_rbi_ucmp
  | eos_rbi_update
;

eos_rbi_aggregate_address
:
  AGGREGATE_ADDRESS
  (
    eos_rb_aa_v4
    | eos_rb_aa_v6
  )
;

eos_rbi_bgp
:
  BGP
  (
    eos_rbib_additional_paths
//    | eos_rbib_advertise_inactive
//    | eos_rbib_allowas_in
//    | eos_rbib_always_compare_med
//    | eos_rbib_asn
//    | eos_rbib_auto_local_addr
    | eos_rbib_bestpath
//    | eos_rbib_client_to_client
    | eos_rbib_cluster_id
//    | eos_rbib_confederation
//    | eos_rbib_control_plane_filter
//    | eos_rbib_convergence
//    | eos_rbib_default
//    | eos_rbib_enforce_first_as
//    | eos_rbib_host_routes
//    | eos_rbib_labeled_unicast
    | eos_rbib_listen
    | eos_rbib_log_neighbor_changes
//    | eos_rbib_missing_policy
//    | eos_rbib_monitoring
//    | eos_rbib_next_hop_unchanged
//    | eos_rbib_redistribute_internal
//    | eos_rbib_route
//    | eos_rbib_route_reflector
//    | eos_rbib_transport
  )
;

eos_rbib_additional_paths
:
  ADDITIONAL_PATHS
  (
    SEND ANY
    | RECEIVE
    | INSTALL
  )
  NEWLINE
;

eos_rbib_bestpath
:
  BESTPATH
  (
    eos_rbibbp_as_path
    // eos_rbibbp_ecmp_fast
    // eos_rbibbp_med
    // eos_rbibbp_skip
    | eos_rbibbp_tie_break
  )
;

eos_rbibbp_as_path
:
  AS_PATH
  (
    // eos_rbibbpa_ignore |
    eos_rbibbpa_multipath_relax
  )
;

eos_rbibbp_tie_break
:
  TIE_BREAK (ROUTER_ID | CLUSTER_LIST_LENGTH) NEWLINE
;

eos_rbibbpa_multipath_relax
:
  MULTIPATH_RELAX NEWLINE
;

eos_rbib_cluster_id
:
  CLUSTER_ID ip = IP_ADDRESS NEWLINE
;

eos_rbib_listen
:
  LISTEN
  (
    eos_rbibl_limit
    | eos_rbibl_range
  )
;

eos_rbibl_limit
:
  LIMIT num = DEC NEWLINE
;

eos_rbibl_range
:
  RANGE
  (
    ip = IP_ADDRESS MASK mask = IP_ADDRESS
    | prefix = IP_PREFIX
    | ip6prefix = IPV6_PREFIX
  )
  (PEER_GROUP | PEER GROUP) pg = variable
  (
    PEER_FILTER peer_filter = variable
    | REMOTE_AS asn = bgp_asn
  )
  NEWLINE
;

eos_rbib_log_neighbor_changes
:
  LOG_NEIGHBOR_CHANGES NEWLINE
;

eos_rbi_default_metric
:
  DEFAULT_METRIC metric = DEC NEWLINE
;

eos_rbi_distance
:
  DISTANCE BGP external = DEC (internal = DEC local = DEC)? NEWLINE
;

eos_rbi_ip
:
  IP ACCESS_GROUP name = variable IN? NEWLINE
;

eos_rbi_ipv6
:
  IPV6 ACCESS_GROUP name = variable IN? NEWLINE
;

eos_rbi_graceful_restart
:
  GRACEFUL_RESTART
  (
    RESTART_TIME DEC
    | STALEPATH_TIME DEC
  )* NEWLINE
;

eos_rbi_graceful_restart_helper
:
  GRACEFUL_RESTART_HELPER NEWLINE
;

eos_rbi_maximum_paths
:
  MAXIMUM_PATHS num = DEC (ECMP ecmp = DEC)? NEWLINE
;

eos_rbi_neighbor
:
  NEIGHBOR
  (
    eos_rbi_neighbor4
    | eos_rbi_neighbor6
    // Definition of a peer group
    | eos_rbi_peer_group
  )
;

eos_rbi_neighbor4
:
  name = IP_ADDRESS
  (
    eos_rbi_neighbor_common
    | eos_rbin_local_v6_addr
    // Assigning a peer group
    | eos_rbin_peer_group
  )
;

eos_rbi_neighbor6
:
  name = IPV6_ADDRESS
  (
    eos_rbi_neighbor_common
    //| eos_rbin_local_v4_addr
    // Assigning a peer group
    | eos_rbin_peer_group
  )
;

eos_rbi_neighbor_common
:
  (
    eos_rbinc_additional_paths
    | eos_rbinc_allowas_in
    | eos_rbinc_auto_local_addr
    | eos_rbinc_bfd
    | eos_rbinc_default_originate
    | eos_rbinc_description
    | eos_rbinc_dont_capability_negotiate
    | eos_rbinc_ebgp_multihop
    | eos_rbinc_enforce_first_as
    | eos_rbinc_export_localpref
    | eos_rbinc_fall_over
//    | eos_rbinc_graceful_restart
//    | eos_rbinc_graceful_restart_helper
//    | eos_rbinc_idle_restart_timer
//    | eos_rbinc_import_localpref
//    | eos_rbinc_link_bandwidth
    | eos_rbinc_local_as
    | eos_rbinc_maximum_accepted_routes
    | eos_rbinc_maximum_routes
//    | eos_rbinc_metric_out
//    | eos_rbinc_monitoring
//    | eos_rbinc_next_hop_peer
    | eos_rbinc_next_hop_self
    | eos_rbinc_next_hop_unchanged
//    | eos_rbinc_out_delay
    | eos_rbinc_password
//    | eos_rbinc_prefix_list
    | eos_rbinc_remote_as
    | eos_rbinc_remove_private_as
    | eos_rbafnc_route_map
    | eos_rbinc_route_reflector_client
//    | eos_rbinc_route_to_peer
    | eos_rbinc_send_community
    | eos_rbinc_shutdown
//    | eos_rbinc_soft_reconfiguration
    | eos_rbinc_timers
//    | eos_rbinc_transport
//    | eos_rbinc_ttl
//    | eos_rbinc_weight
    | eos_rbinc_update_source
  )
;

eos_rbinc_additional_paths
:
  ADDITIONAL_PATHS (SEND ANY | RECEIVE) NEWLINE
;

eos_rbinc_allowas_in
:
  ALLOWAS_IN (num = DEC)? NEWLINE
;

eos_rbinc_auto_local_addr
:
  AUTO_LOCAL_ADDR NEWLINE
;

eos_rbinc_bfd
:
  BFD NEWLINE
;

eos_rbinc_default_originate
:
  DEFAULT_ORIGINATE
  (
    ALWAYS
    | ROUTE_MAP rm = variable
  )* NEWLINE
;

eos_rbinc_description
:
  DESCRIPTION (desc = ~NEWLINE) NEWLINE
;

eos_rbinc_dont_capability_negotiate
:
  DONT_CAPABILITY_NEGOTIATE NEWLINE
;

eos_rbinc_ebgp_multihop
:
  EBGP_MULTIHOP (num = DEC)? NEWLINE
;

eos_rbinc_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

eos_rbinc_export_localpref
:
  EXPORT_LOCALPREF value = DEC NEWLINE
;

eos_rbinc_fall_over
:
  FALL_OVER BFD NEWLINE
;

eos_rbinc_local_as
:
  LOCAL_AS asn = bgp_asn NO_PREPEND REPLACE_AS FALLBACK? NEWLINE
;

eos_rbinc_maximum_accepted_routes
:
  MAXIMUM_ACCEPTED_ROUTES num = DEC (WARNING_LIMIT warn_limit = DEC)? NEWLINE
;

eos_rbinc_maximum_routes
:
  MAXIMUM_ROUTES num = DEC
  (
    WARNING_ONLY
    | (WARNING_LIMIT warn_limit = DEC PERCENT_LITERAL?)
  )*
  NEWLINE
;

eos_rbinc_next_hop_self
:
  NEXT_HOP_SELF NEWLINE
;

eos_rbinc_next_hop_unchanged
:
  NEXT_HOP_UNCHANGED NEWLINE
;

eos_rbinc_password
:
  PASSWORD (encrypt_level = DEC)? variable NEWLINE
;

eos_rbinc_remote_as
:
  REMOTE_AS asn = bgp_asn NEWLINE
;

eos_rbinc_remove_private_as
:
  REMOVE_PRIVATE_AS (ALL REPLACE_AS?)? NEWLINE
;

eos_rbinc_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

eos_rbinc_send_community
:
  SEND_COMMUNITY
  (ADD | REMOVE)?
  (
    EXTENDED
    | STANDARD
//    TODO: support for link-bandwidth
//    | LINK_BANDWIDTH
//      (
//        AGGREGATE "0.0-4294967295.0 or nn.nn(K|M|G)  Reference link speed in bits/second"
//        | DIVIDE (EQUAL | RATIO)
//      )
  )*
  NEWLINE
;

eos_rbinc_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbinc_timers
:
  TIMERS keepalive = DEC hold = DEC NEWLINE
;

eos_rbinc_update_source
:
  UPDATE_SOURCE iface = interface_name NEWLINE
;

eos_rbin_local_v6_addr
:
  LOCAL_V6_ADDR IPV6_ADDRESS NEWLINE
;

// Assigning a peer group to a neighbor
eos_rbin_peer_group
:
  PEER_GROUP name = variable NEWLINE
  | PEER GROUP name = variable NEWLINE
;

eos_rbi_network
:
  NETWORK
  (
    eos_rbi_network4
    | eos_rbi_network6
  )
;

eos_rbi_network4
:
  (
    ip = IP_ADDRESS mask = IP_ADDRESS
    | prefix = IP_PREFIX
  )
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbi_network6
:
  IPV6_PREFIX (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbi_no
:
  NO
  (
    eos_rbino_bgp
    | eos_rbino_neighbor
  )
;

eos_rbino_bgp
:
  BGP
  (
    eos_rbino_bgp_bestpath
    | eos_rbino_bgp_default
  )
;

eos_rbino_bgp_bestpath
:
  BESTPATH
  (
    eos_rbino_bgp_bp_as_path
    // eos_rbino_bgp_bp_ecmp_fast
    // eos_rbino_bgp_bp_med
    // eos_rbino_bgp_bp_skip
    // eos_rbino_bgp_bp_tie_break
  )
;

eos_rbino_bgp_bp_as_path
:
  AS_PATH
  (
    // eos_rbino_bgp_bpa_ignore |
    eos_rbino_bgp_bpa_multipath_relax
  )
;

eos_rbino_bgp_bpa_multipath_relax
:
  MULTIPATH_RELAX NEWLINE
;

eos_rbino_bgp_default
:
  DEFAULT
  eos_rbino_bgp_default_ipv4_unicast
;

eos_rbino_bgp_default_ipv4_unicast
:
  IPV4_UNICAST NEWLINE
;

eos_rbino_neighbor
:
  NEIGHBOR
  (
    v4 = IP_ADDRESS
    | v6 = IPV6_ADDRESS
    | pg = variable
  )
  eos_rbinon_enforce_first_as
;

eos_rbinon_enforce_first_as
:
  ENFORCE_FIRST_AS NEWLINE
;

// Defining a peer group
eos_rbi_peer_group
:
  name = variable
  (
    (PEER_GROUP | PEER GROUP) NEWLINE
    | eos_rbi_neighbor_common
    | eos_rbin_local_v6_addr
  )
;

eos_rbi_redistribute
:
  REDISTRIBUTE
  (
    eos_rbir_attached_host
    | eos_rbir_connected
    | eos_rbir_dynamic
    | eos_rbir_isis
    | eos_rbir_ospf
    | eos_rbir_ospf3
    | eos_rbir_rip
    | eos_rbir_static
  )
;

eos_rbir_attached_host
:
  ATTACHED_HOST (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_connected
:
  CONNECTED (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_dynamic
:
  DYNAMIC (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_isis
:
  ISIS
  (LEVEL_1 | LEVEL_2 | LEVEL_1_2)?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_ospf
:
  OSPF
  (
    MATCH
    (
      INTERNAL
      | EXTERNAL
      | (NSSA_EXTERNAL DEC?)
    )
  )?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_ospf3
:
  OSPF3
  (
    MATCH
    (
      INTERNAL
      | EXTERNAL
      | (NSSA_EXTERNAL DEC?)
    )
  )?
  (ROUTE_MAP rm = variable)?
  NEWLINE
;

eos_rbir_rip
:
  RIP (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbir_static
:
  STATIC (ROUTE_MAP rm = variable)? NEWLINE
;

eos_rbi_router_id
:
  ROUTER_ID id = IP_ADDRESS NEWLINE
;

eos_rbi_shutdown
:
  SHUTDOWN NEWLINE
;

eos_rbi_timers
:
  TIMERS BGP keepalive = DEC hold = DEC NEWLINE
;

eos_rbi_ucmp
:
  UCMP
  (
    eos_rbiu_fec
    | eos_rbiu_link_bandwidth
    | eos_rbiu_mode
  )
;

eos_rbiu_fec
:
  FEC THRESHOLD TRIGGER trigger = DEC CLEAR clear = DEC WARNING_ONLY NEWLINE
;

eos_rbiu_link_bandwidth
:
  LINK_BANDWIDTH (ENCODING_WEIGHTED | RECURSIVE | UPDATE_DELAY DEC) NEWLINE
;

eos_rbiu_mode
:
  MODE mode_num = DEC
  (
    next_hops = DEC
    (oversubscription = FLOAT)?
  )? NEWLINE
;

eos_rbi_update
:
  UPDATE
  (
    WAIT_FOR_CONVERGENCE
    | WAIT_INSTALL (BATCH_SIZE DEC)?
  ) NEWLINE
;

eos_rb_aa_modifiers
:
  ADVERTISE_ONLY
  | AS_SET
  | ATTRIBUTE_MAP attr_map = variable
  | MATCH_MAP match_map = variable
  | SUMMARY_ONLY
;

eos_rb_aa_v4
:
  (
    ip = IP_ADDRESS mask = IP_ADDRESS
    | prefix = IP_PREFIX
  ) eos_rb_aa_modifiers*
  NEWLINE
;

eos_rb_aa_v6
:
  prefix = IPV6_PREFIX eos_rb_aa_modifiers* NEWLINE
;

eos_rb_monitoring
:
  MONITORING
  (
    eos_rbm_port
    | eos_rbm_received
    | eos_rbm_station
    | eos_rbm_timestamp
  )
;

eos_rbm_port
:
  PORT num = DEC NEWLINE
;

eos_rbm_received
:
  RECEIVED ROUTES ( POST_POLICY | PRE_POLICY ) NEWLINE
;

eos_rbm_station
:
  STATION name = variable NEWLINE
;

eos_rbm_timestamp
:
  TIMESTAMP ( NONE | SEND_TIME ) NEWLINE
;

eos_rb_vlan
:
  VLAN id = DEC NEWLINE
  eos_rb_vlan_tail*
;

eos_rb_vlan_aware_bundle
:
  VLAN_AWARE_BUNDLE name = variable NEWLINE
  (
    eos_rb_vab_vlan
    | eos_rb_vlan_tail
  )*
;

eos_rb_vlan_tail_rd
:
  RD rd = route_distinguisher NEWLINE
;

eos_rb_vlan_tail_redistribute
:
  REDISTRIBUTE
  (
    HOST_ROUTE
    | LEARNED
    | ROUTER_MAC
    | STATIC
  ) NEWLINE
;

eos_rb_vlan_tail_route_target
:
  ROUTE_TARGET
  ( BOTH | IMPORT | EXPORT )
  rt = route_target NEWLINE
;

eos_rb_vab_vlan
:
  VLAN vlans = eos_vlan_id NEWLINE
;

eos_rb_vlan_tail
:
  eos_rb_vlan_tail_rd
  | eos_rb_vlan_tail_redistribute
  | eos_rb_vlan_tail_route_target
;

eos_rbv_address_family
:
  eos_rb_af_ipv4
//  | eos_rb_af_ipv4_multicast
  | eos_rb_af_ipv6
;

eos_rbv_local_as
:
  LOCAL_AS asn = bgp_asn NEWLINE
;

eos_rbv_rd
:
  RD rd = route_distinguisher NEWLINE
;

eos_rbv_route_target
:
  ROUTE_TARGET (IMPORT | EXPORT) p = (EVPN | VPN_IPV4 | VPN_IPV6)? rt = route_target NEWLINE
;

eos_rb_vrf
:
  VRF name = variable NEWLINE
  (
    eos_rbv_address_family
    | eos_rb_inner
    | eos_rbv_local_as
    | eos_rbv_rd
    | eos_rbv_route_target
  )*
;