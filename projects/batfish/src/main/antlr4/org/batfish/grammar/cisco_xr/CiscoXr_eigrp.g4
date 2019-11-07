parser grammar CiscoXr_eigrp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

re_classic
:
   ROUTER EIGRP asnum = DEC NEWLINE
   re_classic_tail*
;

re_classic_tail
:
   re_distribute_list
   | re_eigrp_null
   | re_eigrp_router_id
   | rec_address_family
   | rec_metric_weights
   | rec_null
   | re_default_metric
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | re_redistribute
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
   DISTRIBUTE_LIST name = variable_distribute_list OUT (iname = interface_name_unstructured)? NEWLINE
;

re_eigrp_null
:
   NO? EIGRP
   (
      DEFAULT_ROUTE_TAG
      | EVENT_LOG_SIZE
      | LOG_NEIGHBOR_CHANGES
      | LOG_NEIGHBOR_WARNINGS
      | STUB
   ) null_rest_of_line
;

re_eigrp_router_id
:
   (
      EIGRP ROUTER_ID id = IP_ADDRESS
      | NO EIGRP ROUTER_ID
   ) NEWLINE
;

re_named
:
   ROUTER EIGRP virtname = variable NEWLINE
   re_named_tail*
;

re_named_tail
:
   ren_address_family
   | ren_null
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
   REDISTRIBUTE BGP asn = bgp_asn (METRIC metric = eigrp_metric)? NEWLINE
;

re_redistribute_connected
:
   REDISTRIBUTE CONNECTED (METRIC metric = eigrp_metric)? NEWLINE
;

re_redistribute_eigrp
:
   REDISTRIBUTE EIGRP asn = DEC (METRIC metric = eigrp_metric)? NEWLINE
;

re_redistribute_isis
:
   REDISTRIBUTE ISIS (tag = variable)?
   (
      LEVEL_1
      | LEVEL_1_2
      | LEVEL_2
      | METRIC metric = eigrp_metric
   )* NEWLINE
;

re_redistribute_ospf
:
   REDISTRIBUTE OSPF proc = DEC
   (
      MATCH ospf_route_type+
      | METRIC metric = eigrp_metric
   )* NEWLINE
;

re_redistribute_rip
:
   REDISTRIBUTE RIP (METRIC metric = eigrp_metric)? NEWLINE
;

re_redistribute_static
:
   REDISTRIBUTE STATIC (METRIC metric = eigrp_metric)? NEWLINE
;

re_topology_base
:
   TOPOLOGY BASE NEWLINE
;

re_topology_name
:
   TOPOLOGY topo_name = variable TID topo_num = DEC NEWLINE
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
      AUTONOMOUS_SYSTEM asnum = DEC
   )? NEWLINE
   rec_address_family_tail* address_family_footer
;

re_autonomous_system
:
   NO? AUTONOMOUS_SYSTEM asnum = DEC NEWLINE
;

rec_address_family_null
:
   NO?
   (
      AUTO_SUMMARY
      | BFD
      | DEFAULT_INFORMATION
      | DISTANCE
      | DISTRIBUTE_LIST
      | MAXIMUM_PATHS
      | MAXIMUM_PREFIX
      | (METRIC MAXIMUM_HOPS)
      | NEIGHBOR
      | NSF
      | OFFSET_LIST
   ) null_rest_of_line
;

rec_address_family_tail
:
   re_autonomous_system
   | re_default_metric
   | re_eigrp_null
   | re_eigrp_router_id
   | re_network
   | re_passive_interface_default
   | re_passive_interface
   | re_redistribute
   | rec_address_family_null
   | rec_metric_weights
;

rec_metric_weights
:
   METRIC WEIGHTS tos = DEC k1 = DEC k2 = DEC k3 = DEC k4 = DEC k5 = DEC NEWLINE
;

rec_null
:
   NO?
   (
      AUTO_SUMMARY
      | BFD
      | DEFAULT_INFORMATION
      | DISTANCE
      | DISTRIBUTE_LIST
      | HELLO_INTERVAL
      | MAXIMUM_PATHS
      | (METRIC MAXIMUM_HOPS)
      | NEIGHBOR
      | NSF
      | OFFSET_LIST
      | SHUTDOWN
      | SPLIT_HORIZON
      | SUMMARY_METRIC
      | TIMERS
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
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
   )? AUTONOMOUS_SYSTEM asnum = DEC NEWLINE
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
   re_eigrp_null
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
   tos = DEC k1 = DEC k2 = DEC k3 = DEC k4 = DEC k5 = DEC k6 = DEC NEWLINE
;

ren_null
:
   NO? SHUTDOWN
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
   )? AUTONOMOUS_SYSTEM asnum = DEC NEWLINE
   ren_service_family_tail*
   resf_footer
;

ren_service_family_null
:
   NO? TIMERS null_rest_of_line
;

ren_service_family_tail
:
   re_eigrp_null
   | re_eigrp_router_id
   | ren_metric_weights
   | ren_service_family_null
   | resf_interface_default
   | resf_interface
   | resf_null
   | resf_topology
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
