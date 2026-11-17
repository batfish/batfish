parser grammar Cisco_eigrp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

re_classic
:
   ROUTER EIGRP asnum = dec NEWLINE
   re_classic_tail*
;

re_classic_tail
:
   re_distribute_list
   | re_eigrp
   | rec_address_family
   | rec_auto_summary_null
   | rec_bfd_null
   | rec_default_information_null
   | rec_hello_interval_null
   | rec_maximum_paths_null
   | rec_metric
   | rec_neighbor_null
   | rec_no
   | re_default_metric
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | re_redistribute
   | re_shutdown
   | rec_nsf_null
   | rec_offset_list_null
   | rec_split_horizon_null
   | rec_timers_null
   | rec_traffic_share_null
   | rec_variance_null
;

re_default_metric
:
   NO? DEFAULT_METRIC
   (
      metric = eigrp_metric
   )? NEWLINE
;

re_distribute_list
:
   DISTRIBUTE_LIST
   (
      redl_acl
      | redl_gateway
      | redl_prefix
      | redl_route_map
   )
;

re_eigrp
:
   EIGRP
   (
      re_eigrp_default_route_tag_null
      | re_eigrp_event_log_size_null
      | re_eigrp_log_neighbor_changes_null
      | re_eigrp_log_neighbor_warnings_null
      | re_eigrp_stub
      | re_eigrp_router_id
   )
;

re_eigrp_default_route_tag_null
:
   DEFAULT_ROUTE_TAG null_rest_of_line
;
re_eigrp_event_log_size_null
:
   EVENT_LOG_SIZE null_rest_of_line
;
re_eigrp_log_neighbor_changes_null
:
   LOG_NEIGHBOR_CHANGES null_rest_of_line
;
re_eigrp_log_neighbor_warnings_null
:
   LOG_NEIGHBOR_WARNINGS null_rest_of_line
;

re_eigrp_router_id
:
   ROUTER_ID id = IP_ADDRESS NEWLINE
;

re_eigrp_stub
:
   STUB
   (
      rees_connected_null
      | rees_leak_map
      | rees_receive_only_null
      | rees_redistributed_null
      | rees_static_null
      | rees_summary_null
   )* NEWLINE
;

rees_receive_only_null: RECEIVE_ONLY;
rees_connected_null: CONNECTED;
rees_static_null: STATIC;
rees_summary_null: SUMMARY;
rees_redistributed_null: REDISTRIBUTED;

rees_leak_map
:
  LEAK_MAP map = variable
;

re_named
:
   ROUTER EIGRP virtname = variable NEWLINE
   re_named_tail*
;

re_named_tail
:
   re_shutdown
   | ren_address_family
   | ren_no
   | ren_service_family
;

re_network
:
   NETWORK address = IP_ADDRESS mask = IP_ADDRESS? NEWLINE
;

re_passive_interface
:
   NO? PASSIVE_INTERFACE i = interface_name NEWLINE
;

re_passive_interface_default
:
   NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

re_redistribute
:
   re_redistribute_bgp
   | re_redistribute_connected
   | re_redistribute_eigrp
   | re_redistribute_isis
   | re_redistribute_ospf
   | re_redistribute_rip
   | re_redistribute_static
;

re_redistribute_bgp
:
   REDISTRIBUTE BGP asn = bgp_asn
   (
      METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_connected
:
   REDISTRIBUTE CONNECTED
   (
      METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_eigrp
:
   REDISTRIBUTE EIGRP asn = dec
   (
      METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_isis
:
   REDISTRIBUTE ISIS (tag = variable)?
   (
      LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      | METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_ospf
:
   REDISTRIBUTE OSPF proc = dec
   (
      MATCH ospf_route_type+
      | METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_rip
:
   REDISTRIBUTE RIP
   (
      METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_redistribute_static
:
   REDISTRIBUTE STATIC
   (
      METRIC metric = eigrp_metric
      | ROUTE_MAP map = variable
   )* NEWLINE
;

re_topology_base
:
   TOPOLOGY BASE NEWLINE
;

re_topology_name
:
   TOPOLOGY topo_name = variable TID topo_num = dec NEWLINE
;

reaf_interface
:
   AF_INTERFACE iname = interface_name NEWLINE
   reaf_interface_tail*
   (
      EXIT_AF_INTERFACE NEWLINE
   )?
;

reaf_interface_default
:
   AF_INTERFACE DEFAULT NEWLINE
   reaf_interface_tail*
   (
      EXIT_AF_INTERFACE NEWLINE
   )?
;

reaf_interface_add_paths_null: NO? ADD_PATHS null_rest_of_line;
reaf_interface_authentication_null: NO? AUTHENTICATION null_rest_of_line;
reaf_interface_bandwidth_percent_null: NO? BANDWIDTH_PERCENT null_rest_of_line;
reaf_interface_bfd_null: NO? BFD null_rest_of_line;
reaf_interface_dampening_change_null: NO? DAMPENING_CHANGE null_rest_of_line;
reaf_interface_dampening_interval_null: NO? DAMPENING_INTERVAL null_rest_of_line;
reaf_interface_hello_interval_null: NO? HELLO_INTERVAL null_rest_of_line;
reaf_interface_hold_time_null: NO? HOLD_TIME null_rest_of_line;
reaf_interface_next_hop_self_null: NO? NEXT_HOP_SELF null_rest_of_line;
reaf_interface_shutdown_null: NO? SHUTDOWN null_rest_of_line;
reaf_interface_split_horizon_null: NO? SPLIT_HORIZON null_rest_of_line;
reaf_interface_summary_address_null: NO? SUMMARY_ADDRESS null_rest_of_line;

reaf_interface_tail
:
   reaf_interface_add_paths_null
   | reaf_interface_authentication_null
   | reaf_interface_bandwidth_percent_null
   | reaf_interface_bfd_null
   | reaf_interface_dampening_change_null
   | reaf_interface_dampening_interval_null
   | reaf_interface_hello_interval_null
   | reaf_interface_hold_time_null
   | reaf_interface_next_hop_self_null
   | reaf_interface_shutdown_null
   | reaf_interface_split_horizon_null
   | reaf_interface_summary_address_null
   | reafi_passive_interface
;

reaf_topology_tail
:
   re_auto_summary_null
   | re_cts_null
   | re_default_information_null
   | re_default_metric
   | re_distance_null
   | re_distribute_list_null
   | re_eigrp_default_route_tag_null
   | re_eigrp_event_log_size_null
   | re_eigrp_log_neighbor_changes_null
   | re_eigrp_log_neighbor_warnings_null
   | re_fast_reroute_null
   | re_maximum_paths_null
   | re_metric_null
   | re_offset_list_null
   | re_redistribute
   | re_snmp_null
   | re_summary_metric_null
   | re_timers_null
   | re_traffic_share_null
   | re_variance_null
;

reaf_topology
:
   (
     re_topology_base
     | re_topology_name
   ) reaf_topology_tail*
   (
      EXIT_AF_TOPOLOGY NEWLINE
   )?
;

re_auto_summary_null: NO? AUTO_SUMMARY null_rest_of_line;
re_cts_null: NO? CTS null_rest_of_line;
re_default_information_null: NO? DEFAULT_INFORMATION null_rest_of_line;
re_distance_null: NO? DISTANCE null_rest_of_line;
re_distribute_list_null: NO? DISTRIBUTE_LIST null_rest_of_line;
re_fast_reroute_null: NO? FAST_REROUTE null_rest_of_line;
re_maximum_paths_null: NO? MAXIMUM_PATHS null_rest_of_line;
re_metric_null: NO? METRIC MAXIMUM_HOPS null_rest_of_line;
re_offset_list_null: NO? OFFSET_LIST null_rest_of_line;
re_snmp_null: NO? SNMP null_rest_of_line;
re_summary_metric_null: NO? SUMMARY_METRIC null_rest_of_line;
re_timers_null: NO? TIMERS null_rest_of_line;
re_traffic_share_null: NO? TRAFFIC_SHARE null_rest_of_line;
re_variance_null: NO? VARIANCE null_rest_of_line;

reafi_passive_interface
:
   NO? PASSIVE_INTERFACE NEWLINE
;

rec_address_family
:
   ADDRESS_FAMILY IPV4 UNICAST? VRF vrf = variable
   (
      AUTONOMOUS_SYSTEM asnum = dec
   )? NEWLINE
   rec_address_family_tail* address_family_footer
;

re_autonomous_system
:
   NO? AUTONOMOUS_SYSTEM asnum = dec NEWLINE
;

rec_address_family_auto_summary_null: NO? AUTO_SUMMARY null_rest_of_line;
rec_address_family_bfd_null: NO? BFD null_rest_of_line;
rec_address_family_default_information_null: NO? DEFAULT_INFORMATION null_rest_of_line;
rec_address_family_maximum_paths_null: NO? MAXIMUM_PATHS null_rest_of_line;
rec_address_family_maximum_prefix_null: NO? MAXIMUM_PREFIX null_rest_of_line;
rec_address_family_nsf_null: NO? NSF null_rest_of_line;
rec_address_family_offset_list_null: NO? OFFSET_LIST null_rest_of_line;
rec_address_family_timers_null: NO? TIMERS null_rest_of_line;
rec_address_family_traffic_share_null: NO? TRAFFIC_SHARE null_rest_of_line;
rec_address_family_variance_null: NO? VARIANCE null_rest_of_line;

rec_address_family_tail
:
   re_autonomous_system
   | re_distribute_list
   | re_default_metric
   | re_eigrp
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | re_redistribute
   | re_shutdown
   | rec_address_family_auto_summary_null
   | rec_address_family_bfd_null
   | rec_address_family_default_information_null
   | rec_address_family_maximum_paths_null
   | rec_address_family_maximum_prefix_null
   | rec_address_family_nsf_null
   | rec_address_family_offset_list_null
   | rec_address_family_timers_null
   | rec_address_family_traffic_share_null
   | rec_address_family_variance_null
   | rec_metric
;

rec_metric
:
   METRIC
   (
     rec_metric_maximum_hops
     | rec_metric_weights
   )
;

rec_metric_maximum_hops
:
   MAXIMUM_HOPS dec NEWLINE
;

rec_metric_weights
:
   WEIGHTS tos = dec k1 = dec k2 = dec k3 = dec k4 = dec k5 = dec NEWLINE
;

rec_no
:
   NO
   (
     rec_auto_summary_null
     | rec_bfd_null
     | rec_default_information_null
     | rec_hello_interval_null
     | rec_maximum_paths_null
     | rec_neighbor_null
     | rec_nsf_null
     | rec_offset_list_null
     | rec_split_horizon_null
     | rec_timers_null
     | rec_traffic_share_null
     | rec_variance_null
     | recno_eigrp
     | reno_shutdown
   )
;

recno_eigrp
:
  EIGRP
  recno_eigrp_router_id
;

recno_eigrp_router_id
:
  ROUTER_ID NEWLINE
;

rec_auto_summary_null
:
   AUTO_SUMMARY null_rest_of_line
;
rec_bfd_null
:
   BFD null_rest_of_line
;
rec_default_information_null
:
   DEFAULT_INFORMATION null_rest_of_line
;
rec_hello_interval_null
:
   HELLO_INTERVAL null_rest_of_line
;
rec_maximum_paths_null
:
   MAXIMUM_PATHS null_rest_of_line
;
rec_neighbor_null
:
   NEIGHBOR null_rest_of_line
;
rec_nsf_null
:
   NSF null_rest_of_line
;
rec_offset_list_null
:
   OFFSET_LIST null_rest_of_line
;
rec_split_horizon_null
:
   SPLIT_HORIZON null_rest_of_line
;
rec_timers_null
:
   TIMERS null_rest_of_line
;
rec_traffic_share_null
:
   TRAFFIC_SHARE null_rest_of_line
;
rec_variance_null
:
   VARIANCE null_rest_of_line
;

re_shutdown
:
  SHUTDOWN NEWLINE
;

redl_acl
:
   name = variable_distribute_list
   (
      IN
      | OUT
   )
   (iname = interface_name_unstructured)? NEWLINE
;

redl_gateway
:
   GATEWAY name = variable_distribute_list
   (
      IN
      | OUT
   )
   (iname = interface_name_unstructured)? NEWLINE
;

redl_prefix
:
   PREFIX name = variable_distribute_list
   ( GATEWAY gwname = variable_distribute_list)?
   (
      IN
      | OUT
   )
   (iname = interface_name_unstructured)? NEWLINE
;

redl_route_map
:
   ROUTE_MAP name = variable_distribute_list
   (
      IN
      | OUT
   )
   (iname = interface_name_unstructured)? NEWLINE
;

ren_address_family
:
   ADDRESS_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      UNICAST
      | MULTICAST
   )?
   (
      VRF vrf = variable
   )? AUTONOMOUS_SYSTEM asnum = dec NEWLINE
   ren_address_family_tail* address_family_footer
;

ren_address_family_maximum_prefix_null: NO? MAXIMUM_PREFIX null_rest_of_line;
ren_address_family_metric_null: NO? METRIC RIB_SCALE null_rest_of_line;
ren_address_family_neighbor_null: NO? NEIGHBOR null_rest_of_line;
ren_address_family_nsf_null: NO? NSF null_rest_of_line;
ren_address_family_remote_neighbors_null: NO? REMOTE_NEIGHBORS null_rest_of_line;
ren_address_family_shutdown_null: NO? SHUTDOWN null_rest_of_line;
ren_address_family_timers_null: NO? TIMERS null_rest_of_line;

ren_address_family_tail
:
   re_eigrp
   | re_eigrp_router_id
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | reaf_interface_default
   | reaf_interface
   | reaf_topology
   | ren_address_family_maximum_prefix_null
   | ren_address_family_metric_null
   | ren_address_family_neighbor_null
   | ren_address_family_nsf_null
   | ren_address_family_remote_neighbors_null
   | ren_address_family_shutdown_null
   | ren_address_family_timers_null
   | ren_metric_weights
;

ren_metric_weights
:
   METRIC WEIGHTS
   // Looks so far like weights are non-optional
   // https://www.cisco.com/c/en/us/td/docs/ios-xml/ios/iproute_eigrp/configuration/xe-3s/ire-xe-3s-book/ire-wid-met.pdf
   tos = dec k1 = dec k2 = dec k3 = dec k4 = dec k5 = dec k6 = dec NEWLINE
;

ren_no
:
  NO
  (
    reno_shutdown
  )
;

ren_service_family
:
   SERVICE_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      VRF vrf = variable
   )? AUTONOMOUS_SYSTEM asnum = dec NEWLINE
   ren_service_family_tail*
   resf_footer
;

ren_service_family_null
:
   NO? TIMERS null_rest_of_line
;

ren_service_family_tail
:
   re_eigrp
   | ren_metric_weights
   | ren_service_family_null
   | resf_eigrp_null
   | resf_interface_default
   | resf_interface
   | resf_neighbor_null
   | resf_remote_neighbors_null
   | resf_shutdown_null
   | resf_timers_null
   | resf_topology
;

reno_shutdown
:
  SHUTDOWN NEWLINE
;

resf_interface
:
   SF_INTERFACE iname = interface_name NEWLINE
   resf_interface_tail*
   (
      EXIT_SF_INTERFACE NEWLINE
   )?
;

resf_interface_default
:
   SF_INTERFACE DEFAULT NEWLINE
   resf_interface_tail*
   (
      EXIT_SF_INTERFACE NEWLINE
   )?
;

resf_interface_authentication_null: NO? AUTHENTICATION null_rest_of_line;
resf_interface_bandwidth_percent_null: NO? BANDWIDTH_PERCENT null_rest_of_line;
resf_interface_dampening_change_null: NO? DAMPENING_CHANGE null_rest_of_line;
resf_interface_dampening_interval_null: NO? DAMPENING_INTERVAL null_rest_of_line;
resf_interface_hello_interval_null: NO? HELLO_INTERVAL null_rest_of_line;
resf_interface_hold_time_null: NO? HOLD_TIME null_rest_of_line;
resf_interface_shutdown_null: NO? SHUTDOWN null_rest_of_line;
resf_interface_split_horizon_null: NO? SPLIT_HORIZON null_rest_of_line;

resf_interface_tail
:
   resf_interface_authentication_null
   | resf_interface_bandwidth_percent_null
   | resf_interface_dampening_change_null
   | resf_interface_dampening_interval_null
   | resf_interface_hello_interval_null
   | resf_interface_hold_time_null
   | resf_interface_shutdown_null
   | resf_interface_split_horizon_null
;

resf_eigrp_null: NO? EIGRP null_rest_of_line;
resf_neighbor_null: NO? NEIGHBOR null_rest_of_line;
resf_remote_neighbors_null: NO? REMOTE_NEIGHBORS null_rest_of_line;
resf_shutdown_null: NO? SHUTDOWN null_rest_of_line;
resf_timers_null: NO? TIMERS null_rest_of_line;

resf_topology_tail
:
   re_eigrp_default_route_tag_null
   | re_eigrp_event_log_size_null
   | re_eigrp_log_neighbor_changes_null
   | re_eigrp_log_neighbor_warnings_null
   | resf_topology_null
;

resf_topology_null
:
   NO?
   (
      (METRIC MAXIMUM_HOPS)
      | TIMERS
   ) null_rest_of_line
;

resf_footer
:
   (
      (
         EXIT_SERVICE_FAMILY
         | EXIT
      ) NEWLINE
   )?
;

resf_topology
:
   (
     re_topology_base
     | re_topology_name
   ) resf_topology_tail*
   (
      EXIT_SF_TOPOLOGY NEWLINE
   )?
;

s_router_eigrp
:
  re_classic
  | re_named
;
