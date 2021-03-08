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
   | rec_metric
   | rec_no
   | rec_null
   | re_default_metric
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | re_redistribute
   | re_shutdown
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
      re_eigrp_null
      | re_eigrp_stub
      | re_eigrp_router_id
   )
;

re_eigrp_null
:
   (
      DEFAULT_ROUTE_TAG
      | EVENT_LOG_SIZE
      | LOG_NEIGHBOR_CHANGES
      | LOG_NEIGHBOR_WARNINGS
   ) null_rest_of_line
;

re_eigrp_router_id
:
   ROUTER_ID id = IP_ADDRESS NEWLINE
;

re_eigrp_stub
:
   STUB
   (
      rees_leak_map
      | rees_null
   )* NEWLINE
;

rees_null
:
   (
      RECEIVE_ONLY
      | CONNECTED
      | STATIC
      | SUMMARY
      | REDISTRIBUTED
   )
;

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

reaf_interface_null
:
   NO?
   (
      ADD_PATHS
      | AUTHENTICATION
      | BANDWIDTH_PERCENT
      | BFD
      | DAMPENING_CHANGE
      | DAMPENING_INTERVAL
      | HELLO_INTERVAL
      | HOLD_TIME
      | NEXT_HOP_SELF
      | SHUTDOWN
      | SPLIT_HORIZON
      | SUMMARY_ADDRESS
   ) null_rest_of_line
;

reaf_interface_tail
:
   reaf_interface_null
   | reafi_passive_interface
;

reaf_topology_tail
:
   re_default_metric
   | re_eigrp_null
   | re_redistribute
   | reaf_topology_null
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

reaf_topology_null
:
   NO?
   (
      AUTO_SUMMARY
      | CTS
      | DEFAULT_INFORMATION
      | DISTANCE
      | DISTRIBUTE_LIST
      | FAST_REROUTE
      | MAXIMUM_PATHS
      | (METRIC MAXIMUM_HOPS)
      | OFFSET_LIST
      | SNMP
      | SUMMARY_METRIC
      | TIMERS
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
;

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

rec_address_family_null
:
   NO?
   (
      AUTO_SUMMARY
      | BFD
      | DEFAULT_INFORMATION
      | MAXIMUM_PATHS
      | MAXIMUM_PREFIX
      | NSF
      | OFFSET_LIST
      | TIMERS
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
;

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
   | rec_address_family_null
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
     recno_eigrp
     | rec_null
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

rec_null
:
   (
      AUTO_SUMMARY
      | BFD
      | DEFAULT_INFORMATION
      | HELLO_INTERVAL
      | MAXIMUM_PATHS
      | NEIGHBOR
      | NSF
      | OFFSET_LIST
      | SPLIT_HORIZON
      | TIMERS
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
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

ren_address_family_null
:
   NO?
   (
      MAXIMUM_PREFIX
      | (METRIC RIB_SCALE)
      | NEIGHBOR
      | NSF
      | REMOTE_NEIGHBORS
      | SHUTDOWN
      | TIMERS
   ) null_rest_of_line
;

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
   | ren_address_family_null
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
   | resf_interface_default
   | resf_interface
   | resf_null
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

resf_interface_null
:
   NO?
   (
      AUTHENTICATION
      | BANDWIDTH_PERCENT
      | DAMPENING_CHANGE
      | DAMPENING_INTERVAL
      | HELLO_INTERVAL
      | HOLD_TIME
      | SHUTDOWN
      | SPLIT_HORIZON
   ) null_rest_of_line
;

resf_interface_tail
:
   resf_interface_null
;

resf_null
:
   NO?
   (
      EIGRP
      | NEIGHBOR
      | REMOTE_NEIGHBORS
      | SHUTDOWN
      | TIMERS
   ) null_rest_of_line
;

resf_topology_tail
:
   re_eigrp_null
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
