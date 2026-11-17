parser grammar Cisco_ospf;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ro_address_family
:
   ADDRESS_FAMILY IPV4 UNICAST? NEWLINE ro_common*
;

ro_area
:
  AREA ospf_area
  (
    roa_default_cost
    | roa_filterlist
    | roa_nssa
    | roa_range
    | roa_stub
  )
;

roa_default_cost
:
   DEFAULT_COST cost = dec NEWLINE
;

roa_filterlist
:
   FILTER_LIST PREFIX list = variable
   (
      IN
      | OUT
   ) NEWLINE
;

roa_nssa
:
   NSSA
   (
      (
         default_information_originate = DEFAULT_INFORMATION_ORIGINATE
         (
            (
               METRIC metric = dec
            )
            |
            (
               METRIC_TYPE metric_type = dec
            )
         )*
      )
      | no_redistribution = NO_REDISTRIBUTION
      | no_summary = NO_SUMMARY
   )* NEWLINE
;

roa_range
:
   RANGE
   (
      (
         area_ip = IP_ADDRESS area_subnet = IP_ADDRESS
      )
      | area_prefix = IP_PREFIX
   )
   (
      ADVERTISE
      | NOT_ADVERTISE
   )?
   (
      COST cost = dec
   )? NEWLINE
;

roa_stub
:
   STUB
   (
      no_summary = NO_SUMMARY
   )* NEWLINE
;

ro_authentication
:
   AUTHENTICATION MESSAGE_DIGEST? NEWLINE
;

ro_auto_cost
:
   AUTO_COST REFERENCE_BANDWIDTH dec
   (
      GBPS
      | MBPS
   )? NEWLINE
;

ro_common
:
   ro_area_null
   | ro_authentication
   | ro_auto_cost_null
   | ro_bfd_null
   | ro_capability_null
   | ro_dead_interval_null
   | ro_discard_route_null
   | ro_fast_reroute_null
   | ro_graceful_restart_null
   | ro_hello_interval_null
   | ro_ip_null
   | ro_ispf_null
   | ro_log_adj_changes_null
   | ro_log_adjacency_changes_null
   | ro_log_null
   | ro_max_lsa_null
   | ro_maximum_null
   | ro_message_digest_key_null
   | ro_mtu_ignore_null
   | ro_no_null
   | ro_nsf_null
   | ro_nsr_null
   | ro_nssa
   | ro_rfc1583_compatibility
   | ro_snmp_null
   | ro_timers_null
;

ro_default_information
:
   DEFAULT_INFORMATION ORIGINATE
   (
      (
         METRIC metric = dec
      )
      |
      (
         METRIC_TYPE metric_type = dec
      )
      | ALWAYS
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | TAG dec
   )* NEWLINE
;

ro_default_metric
:
   NO? DEFAULT_METRIC metric = dec NEWLINE
;

ro_distance
:
   DISTANCE
   (
     ro_distance_distance
     | ro_distance_ospf
   )
;

// Overrides the distance for all protocols
ro_distance_distance
:
  value = dec NEWLINE
;

// Overrides the distance for specific protocols
ro_distance_ospf
:
  OSPF
  (EXTERNAL ext = dec)?
  (INTER_AREA inter = dec)?
  (INTRA_AREA intra = dec)?
  NEWLINE
;

ro_distribute_list
:
  DISTRIBUTE_LIST
  (
    PREFIX
    | ROUTE_MAP
  )? name = variable_distribute_list
  (
    IN
    | OUT
  )
  (
    iname = interface_name_unstructured
  )?
  NEWLINE
;

ro_max_metric
:
   MAX_METRIC ROUTER_LSA
   (
      (
         external_lsa = EXTERNAL_LSA external = dec?
      )
      | stub = INCLUDE_STUB
      |
      (
         on_startup = ON_STARTUP dec?
      )
      |
      (
         summary_lsa = SUMMARY_LSA summary = dec?
      )
      |
      (
         WAIT_FOR_BGP
      )
   )* NEWLINE
;

ro_maximum_paths
:
   (
      MAXIMUM_PATHS
      |
      (
         MAXIMUM PATHS
      )
   ) dec NEWLINE
;

ro_network
:
   NETWORK
   (
      (
         ip = IP_ADDRESS wildcard = IP_ADDRESS
      )
      | prefix = IP_PREFIX
   ) AREA
   (
      area_int = dec
      | area_ip = IP_ADDRESS
   ) NEWLINE
;

ro_nssa
:
   NSSA
   (
      (
         DEFAULT_INFORMATION_ORIGINATE
         (
            (
               METRIC dec
            )
            |
            (
               METRIC_TYPE DIGIT
            )
         )*
      )
      | NO_REDISTRIBUTION
      | NO_SUMMARY
   )* NEWLINE
;

ro_area_null: NO? AREA variable AUTHENTICATION null_rest_of_line;
ro_auto_cost_null: NO? AUTO_COST null_rest_of_line;
ro_bfd_null: NO? BFD null_rest_of_line;
ro_capability_null: NO? CAPABILITY null_rest_of_line;
ro_dead_interval_null: NO? DEAD_INTERVAL null_rest_of_line;
ro_discard_route_null: NO? DISCARD_ROUTE null_rest_of_line;
ro_fast_reroute_null: NO? FAST_REROUTE null_rest_of_line;
ro_graceful_restart_null: NO? GRACEFUL_RESTART null_rest_of_line;
ro_hello_interval_null: NO? HELLO_INTERVAL null_rest_of_line;
ro_ip_null: NO? IP OSPF EVENT_HISTORY null_rest_of_line;
ro_ispf_null: NO? ISPF null_rest_of_line;
ro_log_null: NO? LOG null_rest_of_line;
ro_log_adj_changes_null: NO? LOG_ADJ_CHANGES null_rest_of_line;
ro_log_adjacency_changes_null: NO? LOG_ADJACENCY_CHANGES null_rest_of_line;
ro_max_lsa_null: NO? MAX_LSA null_rest_of_line;
ro_maximum_null: NO? MAXIMUM REDISTRIBUTED_PREFIXES null_rest_of_line;
ro_message_digest_key_null: NO? MESSAGE_DIGEST_KEY null_rest_of_line;
ro_mtu_ignore_null: NO? MTU_IGNORE null_rest_of_line;
ro_no_null: NO? NO DEFAULT_INFORMATION null_rest_of_line;
ro_nsf_null: NO? NSF null_rest_of_line;
ro_nsr_null: NO? NSR null_rest_of_line;
ro_snmp_null: NO? SNMP null_rest_of_line;
ro_timers_null: NO? TIMERS null_rest_of_line;

ro_rfc1583_compatibility
:
   NO?
   (
      RFC1583COMPATIBILITY
      | COMPATIBLE RFC1583
   ) NEWLINE
;

ro_passive_interface_default
:
   NO? PASSIVE_INTERFACE DEFAULT NEWLINE
;

ro_passive_interface
:
   NO? PASSIVE_INTERFACE i = interface_name NEWLINE
;

ro_prefix_priority
:
   NO? PREFIX_PRIORITY
   (
     HIGH
     | LOW
   )
   ROUTE_MAP map = VARIABLE NEWLINE
;

ro_redistribute_bgp_cisco
:
   REDISTRIBUTE BGP bgp_asn
   (
      (
         METRIC metric = dec
      )
      |
      (
         METRIC_TYPE type = dec
      )
      |
      (
         ROUTE_MAP map = VARIABLE
      )
      | subnets = SUBNETS
      |
      (
         TAG tag = dec
      )
   )* NEWLINE
;

ro_redistribute_connected
:
   REDISTRIBUTE
   (
      CONNECTED
      | DIRECT
   )
   (
      (
         METRIC metric = dec
      )
      |
      (
         METRIC_TYPE type = dec
      )
      | ROUTE_MAP map = VARIABLE
      | subnets = SUBNETS
      |
      (
         TAG tag = dec
      )
   )* NEWLINE
;

ro_redistribute_eigrp
:
   REDISTRIBUTE EIGRP tag = dec
   (
      METRIC metric = dec
      | METRIC_TYPE type = dec
      | ROUTE_MAP map = variable
      | SUBNETS
   )* NEWLINE
;

ro_redistribute_ospf_null
:
   REDISTRIBUTE OSPF null_rest_of_line
;

ro_redistribute_rip
:
   REDISTRIBUTE RIP null_rest_of_line
;

ro_redistribute_static
:
   REDISTRIBUTE STATIC
   (
      (
         METRIC metric = dec
      )
      |
      (
         METRIC_TYPE type = dec
      )
      | ROUTE_MAP map = VARIABLE
      | subnets = SUBNETS
      |
      (
         TAG tag = dec
      )
   )* NEWLINE
;

ro_router_id
:
   ROUTER_ID ip = IP_ADDRESS NEWLINE
;

ro_summary_address
:
   SUMMARY_ADDRESS network = IP_ADDRESS mask = IP_ADDRESS NOT_ADVERTISE?
   NEWLINE
;

ro_vrf
:
   VRF name = variable NEWLINE
   (
      ro_max_metric
      | ro_redistribute_connected
      | ro_redistribute_static
   )*
;

ro6_area
:
   AREA null_rest_of_line
;

ro6_auto_cost
:
   AUTO_COST REFERENCE_BANDWIDTH dec NEWLINE
;

ro6_default_information
:
   DEFAULT_INFORMATION null_rest_of_line
;

ro6_distance
:
   DISTANCE value = dec NEWLINE
;

ro6_distribute_list
:
   DISTRIBUTE_LIST PREFIX_LIST name = variable_distribute_list
   (
      IN
      | OUT
   )
   (
      iname = interface_name_unstructured
   )?
   NEWLINE
;

ro6_log_adjacency_changes
:
   LOG_ADJACENCY_CHANGES DETAIL? NEWLINE
;

ro6_maximum_paths
:
   (
      MAXIMUM_PATHS
      |
      (
         MAXIMUM PATHS
      )
   ) dec NEWLINE
;

ro6_null
:
   NO?
   (
      TIMERS
   ) null_rest_of_line
;

ro6_passive_interface
:
   NO? PASSIVE_INTERFACE null_rest_of_line
;

ro6_router_id
:
   ROUTER_ID null_rest_of_line
;

ro6_redistribute
:
   REDISTRIBUTE null_rest_of_line
;

roi_priority
:
   PRIORITY dec NEWLINE
;

rov3_address_family
:
   ADDRESS_FAMILY IPV6 UNICAST? NEWLINE
   rov3_common*
   address_family_footer
;

rov3_common
:
   rov3_area_null
   | rov3_auto_cost_null
   | rov3_bfd_null
   | rov3_cost_null
   | rov3_dead_interval_null
   | rov3_default_information_null
   | rov3_discard_route_null
   | rov3_distance_null
   | rov3_fast_reroute_null
   | rov3_graceful_restart_null
   | rov3_hello_interval_null
   | rov3_interface_null
   | rov3_log_adjacency_changes_null
   | rov3_log_null
   | rov3_max_metric_null
   | rov3_maximum_null
   | rov3_maximum_paths_null
   | rov3_mtu_ignore_null
   | rov3_network_null
   | rov3_nsr_null
   | rov3_nssa_null
   | rov3_ospfv3_null
   | rov3_passive_interface_null
   | rov3_passive_null
   | rov3_priority_null
   | rov3_range_null
   | rov3_redistribute_null
   | rov3_router_id_null
   | rov3_timers_null
;

rov3_area_null
:
   AREA null_rest_of_line
;
rov3_auto_cost_null
:
   AUTO_COST null_rest_of_line
;
rov3_bfd_null
:
   BFD null_rest_of_line
;
rov3_cost_null
:
   COST null_rest_of_line
;
rov3_dead_interval_null
:
   DEAD_INTERVAL null_rest_of_line
;
rov3_default_information_null
:
   DEFAULT_INFORMATION null_rest_of_line
;
rov3_discard_route_null
:
   DISCARD_ROUTE null_rest_of_line
;
rov3_distance_null
:
   DISTANCE null_rest_of_line
;
rov3_fast_reroute_null
:
   FAST_REROUTE null_rest_of_line
;
rov3_graceful_restart_null
:
   GRACEFUL_RESTART null_rest_of_line
;
rov3_hello_interval_null
:
   HELLO_INTERVAL null_rest_of_line
;
rov3_interface_null
:
   INTERFACE null_rest_of_line
;
rov3_log_null
:
   LOG null_rest_of_line
;
rov3_log_adjacency_changes_null
:
   LOG_ADJACENCY_CHANGES null_rest_of_line
;
rov3_max_metric_null
:
   MAX_METRIC null_rest_of_line
;
rov3_maximum_null
:
   MAXIMUM null_rest_of_line
;
rov3_maximum_paths_null
:
   MAXIMUM_PATHS null_rest_of_line
;
rov3_mtu_ignore_null
:
   MTU_IGNORE null_rest_of_line
;
rov3_network_null
:
   NETWORK null_rest_of_line
;
rov3_nssa_null
:
   NSSA null_rest_of_line
;
rov3_nsr_null
:
   NSR null_rest_of_line
;
rov3_ospfv3_null
:
   OSPFV3 null_rest_of_line
;
rov3_passive_null
:
   PASSIVE null_rest_of_line
;
rov3_passive_interface_null
:
   PASSIVE_INTERFACE null_rest_of_line
;
rov3_priority_null
:
   PRIORITY null_rest_of_line
;
rov3_range_null
:
   RANGE null_rest_of_line
;
rov3_redistribute_null
:
   REDISTRIBUTE null_rest_of_line
;
rov3_router_id_null
:
   ROUTER_ID null_rest_of_line
;
rov3_timers_null
:
   TIMERS null_rest_of_line
;

s_ipv6_router_ospf
:
   IPV6 ROUTER OSPF procname = variable NEWLINE
   (
      ro6_area
      | ro6_auto_cost
      | ro6_default_information
      | ro6_distance
      | ro6_distribute_list
      | ro6_log_adjacency_changes
      | ro6_maximum_paths
      | ro6_null
      | ro6_passive_interface
      | ro6_redistribute
      | ro6_router_id
   )*
;

s_router_ospf
:
   ROUTER OSPF name = variable
   (
      VRF vrf = variable
   )? NEWLINE
   ro_inner*
;

ro_inner
:
      ro_address_family
      | ro_area
      | ro_auto_cost
      | ro_common
      | ro_default_information
      | ro_default_metric
      | ro_distance
      | ro_distribute_list
      | ro_max_metric
      | ro_maximum_paths
      | ro_network
      | ro_passive_interface_default
      | ro_passive_interface
      | ro_prefix_priority
      | ro_redistribute_bgp_cisco
      | ro_redistribute_connected
      | ro_redistribute_eigrp
      | ro_redistribute_ospf_null
      | ro_redistribute_rip
      | ro_redistribute_static
      | ro_router_id
      | ro_summary_address
      | ro_vrf
      | roi_priority
;

s_router_ospfv3
:
   ROUTER OSPFV3 procname = variable NEWLINE
   (
      rov3_address_family
      | rov3_common
   )*
;
