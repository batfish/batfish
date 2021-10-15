parser grammar A10_router_bgp;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

sr_bgp: BGP bgp_asn NEWLINE srb_definition*;

srb_definition
:
   srb_bgp
   | srb_maximum_paths
   | srb_timers
   | srb_synchronization
   | srb_neighbor
   | srb_redistribute
;

srb_bgp: BGP srbb;

srbb
:
   srbb_default_local_preference
   | srbb_fast_external_failover
   | srbb_log_neighbor_changes
   | srbb_nexthop_trigger_count
   | srbb_router_id
   | srbb_scan_time
;

srbb_default_local_preference: DEFAULT LOCAL_PREFERENCE bgp_local_preference NEWLINE;

srbb_fast_external_failover: FAST_EXTERNAL_FAILOVER NEWLINE;

srbb_log_neighbor_changes: LOG_NEIGHBOR_CHANGES NEWLINE;

srbb_nexthop_trigger_count: NEXTHOP_TRIGGER_COUNT null_rest_of_line;

srbb_router_id: ROUTER_ID ip_address NEWLINE;

srbb_scan_time: SCAN_TIME null_rest_of_line;

srb_maximum_paths: MAXIMUM_PATHS bgp_max_paths NEWLINE;

srb_timers: TIMERS BGP bgp_keepalive bgp_holdtime NEWLINE;

srb_synchronization: SYNCHRONIZATION NEWLINE;

srb_neighbor: NEIGHBOR bgp_neighbor srbn;

bgp_neighbor: ip_address;

srbn
:
   srbn_activate
   | srbn_description
   | srbn_maximum_prefix
   | srbn_remote_as
   | srbn_send_community
   | srbn_weight
   | srbn_update_source
;

srbn_null
:
   (
      CAPABILITY
      | FALL_OVER
      | SOFT_RECONFIGURATION
      | TIMERS
   ) null_rest_of_line
;

srbn_activate: ACTIVATE NEWLINE;

srbn_description: DESCRIPTION bgp_neighbor_description NEWLINE;

srbn_maximum_prefix: MAXIMUM_PREFIX bgp_neighbor_max_prefix bgp_neighbor_max_prefix_threshold? NEWLINE;

srbn_remote_as: REMOTE_AS bgp_asn NEWLINE;

srbn_send_community: SEND_COMMUNITY send_community NEWLINE;

send_community: BOTH | EXTENDED | STANDARD | NONE;

srbn_weight: WEIGHT bgp_neighbor_weight NEWLINE;

srbn_update_source: UPDATE_SOURCE bgp_neighbor_update_source NEWLINE;

bgp_neighbor_update_source: ip_address;

srb_redistribute: REDISTRIBUTE srbr;

srbr
:
   srbr_connected
   | srbr_floating_ip
   | srbr_ip_nat
   | srbr_vip
;

srbr_connected: CONNECTED NEWLINE;

srbr_floating_ip: FLOATING_IP NEWLINE;

srbr_ip_nat: IP_NAT NEWLINE;

srbr_vip: VIP srbrv;

srbrv: srbrv_only_flagged | srbrv_only_not_flagged;

srbrv_only_flagged: ONLY_FLAGGED NEWLINE;

srbrv_only_not_flagged: ONLY_NOT_FLAGGED NEWLINE;

// 1-64
bgp_max_paths: uint8;

// 0-65535
bgp_keepalive: uint16;

// 0-65535
bgp_holdtime: uint16;

// 0-4294967295
bgp_local_preference: uint32;

// 1-80 chars
bgp_neighbor_description: word;

// 0-65535
bgp_neighbor_weight: uint16;

// 1-65536
bgp_neighbor_max_prefix: uint32;

// 1-100
bgp_neighbor_max_prefix_threshold: uint8;
