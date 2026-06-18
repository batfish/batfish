parser grammar CiscoNxos_isis;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

router_isis
:
  ISIS tag = router_isis_process_tag NEWLINE
  (
    ri_common
    | ri_distribute_null
    | ri_flush_routes_null
    | ri_isolate_null
    | ri_mpls_null
    | ri_no
    | ri_vrf
  )*
;

// Commands valid both at the top level and inside `vrf <name>`
// (NX-OS config-mode router-isis-vrf-common).
ri_common
:
  ri_address_family
  | ri_authentication_null
  | ri_graceful_restart_null
  | ri_hostname_null
  | ri_is_type
  | ri_log_adjacency_changes_null
  | ri_lsp_gen_interval_null
  | ri_lsp_mtu_null
  | ri_max_lsp_lifetime_null
  | ri_metric_style_null
  | ri_net
  | ri_passive_interface_null
  | ri_queue_limit_null
  | ri_reference_bandwidth_null
  | ri_set_overload_bit_null
  | ri_shutdown_null
  | ri_spf_interval_null
  | ri_summary_address_null
;

ri_vrf
:
  VRF name = vrf_non_default_name NEWLINE
  (
    ri_common
    | ri_no
  )*
;

// Modeled commands

ri_net
:
  NET net = ISO_ADDRESS NEWLINE
;

ri_is_type
:
  IS_TYPE level = isis_level NEWLINE
;

// Ignored process-level commands (parsed, not modeled)

ri_authentication_null
:
  (
    AUTHENTICATION
    | AUTHENTICATION_CHECK
    | AUTHENTICATION_TYPE
  ) null_rest_of_line
;

ri_distribute_null
:
  DISTRIBUTE null_rest_of_line
;

ri_flush_routes_null
:
  FLUSH_ROUTES NEWLINE
;

ri_graceful_restart_null
:
  GRACEFUL_RESTART null_rest_of_line
;

ri_hostname_null
:
  HOSTNAME null_rest_of_line
;

ri_isolate_null
:
  ISOLATE NEWLINE
;

ri_log_adjacency_changes_null
:
  LOG_ADJACENCY_CHANGES NEWLINE
;

ri_lsp_gen_interval_null
:
  LSP_GEN_INTERVAL null_rest_of_line
;

ri_lsp_mtu_null
:
  LSP_MTU null_rest_of_line
;

ri_max_lsp_lifetime_null
:
  MAX_LSP_LIFETIME null_rest_of_line
;

ri_metric_style_null
:
  METRIC_STYLE null_rest_of_line
;

ri_mpls_null
:
  MPLS null_rest_of_line
;

ri_passive_interface_null
:
  PASSIVE_INTERFACE null_rest_of_line
;

ri_queue_limit_null
:
  QUEUE_LIMIT null_rest_of_line
;

ri_reference_bandwidth_null
:
  REFERENCE_BANDWIDTH null_rest_of_line
;

ri_set_overload_bit_null
:
  SET_OVERLOAD_BIT null_rest_of_line
;

ri_shutdown_null
:
  SHUTDOWN NEWLINE
;

ri_spf_interval_null
:
  SPF_INTERVAL null_rest_of_line
;

ri_summary_address_null
:
  SUMMARY_ADDRESS null_rest_of_line
;

// `no` forms of process-level commands.
ri_no
:
  NO
  (
    ri_no_shutdown_null
    | ri_no_other_null
  )
;

ri_no_shutdown_null
:
  SHUTDOWN NEWLINE
;

// Generic `no <command>` lines that we accept and ignore.
ri_no_other_null
:
  (
    ADJACENCY_CHECK
    | AUTHENTICATION
    | AUTHENTICATION_CHECK
    | AUTHENTICATION_TYPE
    | GRACEFUL_RESTART
    | HOSTNAME
    | LOG_ADJACENCY_CHANGES
    | METRIC_STYLE
    | PASSIVE_INTERFACE
    | SET_OVERLOAD_BIT
  ) null_rest_of_line
;

// address-family ipv4|ipv6 unicast

ri_address_family
:
  ADDRESS_FAMILY
  (
    IPV4
    | IPV6
  ) UNICAST NEWLINE
  (
    riaf_inner
    | riaf_no
  )*
;

riaf_inner
:
  riaf_adjacency_check_null
  | riaf_advertise_null
  | riaf_bfd_null
  | riaf_default_information_null
  | riaf_distance_null
  | riaf_distribute_null
  | riaf_locator_null
  | riaf_maximum_paths_null
  | riaf_multi_topology_null
  | riaf_redistribute_null
  | riaf_router_id_null
  | riaf_segment_routing_null
  | riaf_set_attached_bit_null
  | riaf_summary_address_null
  | riaf_table_map_null
;

riaf_adjacency_check_null
:
  ADJACENCY_CHECK null_rest_of_line
;

riaf_advertise_null
:
  ADVERTISE null_rest_of_line
;

riaf_bfd_null
:
  BFD null_rest_of_line
;

riaf_default_information_null
:
  DEFAULT_INFORMATION null_rest_of_line
;

riaf_distance_null
:
  DISTANCE null_rest_of_line
;

riaf_distribute_null
:
  DISTRIBUTE null_rest_of_line
;

riaf_locator_null
:
  LOCATOR null_rest_of_line
;

riaf_maximum_paths_null
:
  MAXIMUM_PATHS null_rest_of_line
;

riaf_multi_topology_null
:
  MULTI_TOPOLOGY null_rest_of_line
;

riaf_redistribute_null
:
  REDISTRIBUTE null_rest_of_line
;

riaf_router_id_null
:
  ROUTER_ID null_rest_of_line
;

riaf_segment_routing_null
:
  SEGMENT_ROUTING null_rest_of_line
;

riaf_set_attached_bit_null
:
  SET_ATTACHED_BIT null_rest_of_line
;

riaf_summary_address_null
:
  SUMMARY_ADDRESS null_rest_of_line
;

riaf_table_map_null
:
  TABLE_MAP null_rest_of_line
;

riaf_no
:
  NO
  (
    ADJACENCY_CHECK
    | ADVERTISE
    | BFD
    | DEFAULT_INFORMATION
    | DISTANCE
    | DISTRIBUTE
    | LOCATOR
    | MAXIMUM_PATHS
    | MULTI_TOPOLOGY
    | REDISTRIBUTE
    | ROUTER_ID
    | SEGMENT_ROUTING
    | SET_ATTACHED_BIT
    | SUMMARY_ADDRESS
    | TABLE_MAP
  ) null_rest_of_line
;
