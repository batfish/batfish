parser grammar CiscoNxos_ospfv3;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ospfv3_default_metric
:
// 1-16777214
  uint32
;

ospfv3_lsa_arrival_interval
:
// 10-600000
  uint32
;

ospfv3_lsa_group_pacing_interval
:
// 1-1800
  uint16
;

ospfv3_max_lsa_count
:
// 1-4294967295
  uint32
;

ospfv3_maximum_prefix_num
:
// 1-65535
  uint16
;

ospfv3_spf_time
:
// 1-600000
  uint32
;

ospfv3_throttle_lsa_start
:
// 0-5000
  uint16
;

ospfv3_throttle_lsa_hold
:
// 50-30000
  uint16
;

ospfv3_throttle_lsa_max
:
// 50-30000
  uint16
;

ospfv3_virtual_link_interval
:
// 1-65535
  uint16
;

ospfv3_virtual_link_tx_delay
:
// 1-450
  uint16
;

router_ospfv3
:
  OSPFV3 name = router_ospfv3_name NEWLINE
  (
    ro3_common
    | ro3_flush_routes
    | ro3_isolate
    | ro3_vrf
  )*
;

ro3_common
:
  ro3_address_family
  | ro3_area
  | ro3_auto_cost
  | ro3_bfd
  | ro3_discard_route
  | ro3_graceful_restart
  | ro3_log_adjacency_changes
  | ro3_max_lsa
  | ro3_max_metric
  | ro3_name_lookup
  | ro3_passive_interface
  | ro3_router_id
  | ro3_shutdown
  | ro3_timers
;

ro3_address_family
:
  ADDRESS_FAMILY IPV6 UNICAST NEWLINE
  (
    ro3_af6_area
    | ro3_af6_default_information
    | ro3_af6_default_metric
    | ro3_af6_distance
    | ro3_af6_maximum_paths
    | ro3_af6_redistribute
    | ro3_af6_summary_address
    | ro3_af6_table_map
    | ro3_af6_timers
  )*
;

ro3_af6_area
:
  AREA id = ospf_area_id
  (
    ro3_af6_a_default_cost
    | ro3_af6_a_filter_list
    | ro3_af6_a_range
  )
;

ro3_af6_a_default_cost
:
  DEFAULT_COST cost = ospf_area_default_cost NEWLINE
;

ro3_af6_a_filter_list
:
  FILTER_LIST ROUTE_MAP name = route_map_name (in = IN | out = OUT) NEWLINE
;

ro3_af6_a_range
:
  RANGE network = ipv6_prefix
  (
    COST cost = ospf_area_range_cost not_advertise = NOT_ADVERTISE?
    | not_advertise = NOT_ADVERTISE (COST cost = ospf_area_range_cost)?
  )? NEWLINE
;

ro3_af6_default_information
:
  DEFAULT_INFORMATION ORIGINATE always = ALWAYS? (ROUTE_MAP rm = route_map_name)? NEWLINE
;

ro3_af6_default_metric
:
  DEFAULT_METRIC metric = ospfv3_default_metric NEWLINE
;

ro3_af6_distance
:
  DISTANCE dist = protocol_distance NEWLINE
;

ro3_af6_maximum_paths
:
  MAXIMUM_PATHS paths = maximum_paths NEWLINE
;

ro3_af6_redistribute
:
  REDISTRIBUTE
  (
    | ro3_af6_rd_maximum_prefix
    | ro3_af6_rd_routing_instance
  )
;

ro3_af6_rd_maximum_prefix
:
  MAXIMUM_PREFIX num = ospfv3_maximum_prefix_num NEWLINE
;

ro3_af6_rd_routing_instance
:
  routing_instance_v6 ROUTE_MAP map = route_map_name NEWLINE
;

ro3_af6_summary_address
:
  SUMMARY_ADDRESS network = ipv6_prefix
  (
    not_advertise = NOT_ADVERTISE
    | TAG tag = uint32
  )? NEWLINE
;

ro3_af6_table_map
:
  TABLE_MAP map = route_map_name FILTER? NEWLINE
;

ro3_af6_timers
:
  TIMERS THROTTLE SPF start = ospfv3_spf_time hold = ospfv3_spf_time wait = ospfv3_spf_time NEWLINE
;

ro3_area
:
  AREA id = ospf_area_id
  (
    ro3a_authentication
    | ro3a_nssa
    | ro3a_stub
    | ro3a_virtual_link
  )
;

ro3a_authentication
:
  AUTHENTICATION digest = MESSAGE_DIGEST? NEWLINE
;

ro3a_nssa
:
// non-zero area only
  NSSA
  (
    ro3a_nssa_nssa
    | ro3a_nssa_translate
    | ro3a_nssa_other
  )
;

// aka, just 'area <area> nssa'
ro3a_nssa_nssa
:
  NEWLINE
;

ro3a_nssa_translate
:
  TRANSLATE TYPE7
  (
    ALWAYS SUPRESS_FA?
    | NEVER
    | SUPRESS_FA
  ) NEWLINE
;

ro3a_nssa_other
:
  (
    ro3a_nssa_o_default_information_originate
    | ro3a_nssa_o_no_redistribution
    | ro3a_nssa_o_no_summary
  )+ NEWLINE
;

ro3a_nssa_o_default_information_originate
:
  DEFAULT_INFORMATION_ORIGINATE (route_map = ROUTE_MAP map = route_map_name)?
;

ro3a_nssa_o_no_redistribution
:
  NO_REDISTRIBUTION
;

ro3a_nssa_o_no_summary
:
  NO_SUMMARY
;

ro3a_stub
:
// non-zero area only
  STUB no_summary = NO_SUMMARY? NEWLINE
;

ro3a_virtual_link
:
  VIRTUAL_LINK ip = ip_address NEWLINE
  (
    ro3a_vl_dead_interval
    | ro3a_vl_hello_interval
    | ro3a_vl_retransmit_interval
    | ro3a_vl_transmit_delay
  )*
;

ro3a_vl_dead_interval
:
  DEAD_INTERVAL secs = ospfv3_virtual_link_interval NEWLINE
;

ro3a_vl_hello_interval
:
  HELLO_INTERVAL secs = ospfv3_virtual_link_interval NEWLINE
;

ro3a_vl_retransmit_interval
:
  RETRANSMIT_INTERVAL secs = ospfv3_virtual_link_interval NEWLINE
;

ro3a_vl_transmit_delay
:
  TRANSMIT_DELAY secs = ospfv3_virtual_link_tx_delay NEWLINE
;

ro3_auto_cost
:
  AUTO_COST REFERENCE_BANDWIDTH
  (
    gbps = ospf_ref_bw_gbps GBPS
    | mbps = ospf_ref_bw_mbps MBPS?
  ) NEWLINE
;

ro3_bfd
:
  BFD NEWLINE
;

ro3_discard_route
:
  DISCARD_ROUTE (EXTERNAL | INTERNAL) NEWLINE
;

ro3_flush_routes
:
  FLUSH_ROUTES NEWLINE
;

ro3_graceful_restart
:
  GRACEFUL_RESTART
  (
    ro3_gr_graceful_restart
    | ro3_gr_null
  )
;

ro3_gr_graceful_restart
:
  NEWLINE
;

ro3_gr_null
:
  (
    GRACE_PERIOD
    | HELPER_DISABLE
    | PLANNED_ONLY
  ) null_rest_of_line
;

ro3_isolate
:
  ISOLATE NEWLINE
;

ro3_log_adjacency_changes
:
  LOG_ADJACENCY_CHANGES DETAIL? NEWLINE
;

ro3_max_lsa
:
  MAX_LSA count = ospfv3_max_lsa_count NEWLINE
;

ro3_max_metric
:
  MAX_METRIC ROUTER_LSA
  (EXTERNAL_LSA external_lsa = ospf_max_metric_external_lsa?)?
  STUB_PREFIX_LSA?
  (ON_STARTUP wait_period = ospf_on_startup_wait_period? (WAIT_FOR bgp_instance)?)?
  (INTER_AREA_PREFIX_LSA inter_area_prefix_lsa = ospf_max_metric_external_lsa?)?
  NEWLINE
;

ro3_name_lookup
:
  NAME_LOOKUP NEWLINE
;

ro3_passive_interface
:
  PASSIVE_INTERFACE DEFAULT NEWLINE
;

ro3_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

ro3_shutdown
:
  SHUTDOWN NEWLINE
;

ro3_timers
:
  TIMERS
  (
    ro3_tim_lsa_arrival
    | ro3_tim_lsa_group_pacing
    | ro3_tim_throttle
  )
;

ro3_tim_lsa_arrival
:
  LSA_ARRIVAL ospfv3_lsa_arrival_interval NEWLINE
;

ro3_tim_lsa_group_pacing
:
  LSA_GROUP_PACING ospfv3_lsa_group_pacing_interval NEWLINE
;

ro3_tim_throttle
:
  THROTTLE LSA start = ospfv3_throttle_lsa_start hold = ospfv3_throttle_lsa_hold max = ospfv3_throttle_lsa_max NEWLINE
;

ro3_summary_address
:
  SUMMARY_ADDRESS network = route_network
  (
    not_advertise = NOT_ADVERTISE
    | TAG tag = uint32
  )? NEWLINE
;

ro3_vrf
:
  VRF name = vrf_non_default_name NEWLINE
  ro3_common*
;
