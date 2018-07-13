parser grammar Cisco_eigrp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

address_family_eigrp_classic_stanza
:
   ADDRESS_FAMILY IPV4 UNICAST? VRF vrf = variable
   (
      AUTONOMOUS_SYSTEM asnum = DEC
   )? NEWLINE
   eigrp_classic_af_stanza* address_family_footer
;

address_family_eigrp_named_stanza
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
   eigrp_named_af_stanza* address_family_footer
;

af_interface_eigrp_stanza
:
   AF_INTERFACE iname = interface_name NEWLINE
   eigrp_af_int_stanza*
   (
      EXIT_AF_INTERFACE NEWLINE
   )?
;

eigrp_af_int_null_stanza
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
      | PASSIVE_INTERFACE
      | SHUTDOWN
      | SPLIT_HORIZON
      | STUB_SITE
      | SUMMARY_ADDRESS
   ) null_rest_of_line
;

eigrp_af_int_stanza
:
   eigrp_af_int_null_stanza
;

eigrp_af_topology_null_stanza
:
   NO?
   (
      AUTO_SUMMARY
      | CTS
      | DEFAULT_INFORMATION
      | DEFAULT_METRIC
      | DISTANCE
      | DISTRIBUTE_LIST
      | EIGRP
      | FAST_REROUTE
      | MAXIMUM_PATHS
      | MAXIMUM_SECONDARY_PATHS
      | METRIC
      | OFFSET_LIST
      | REDISTRIBUTE
      | SNMP
      | SUMMARY_METRIC
      | TIMERS
      | TOPO_INTERFACE
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
;

eigrp_af_topology_stanza
:
   eigrp_af_topology_null_stanza
;

eigrp_sf_int_null_stanza
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

eigrp_sf_int_stanza
:
   eigrp_sf_int_null_stanza
;

eigrp_sf_topology_null_stanza
:
   NO?
   (
      EIGRP
      | METRIC
      | TIMERS
   ) null_rest_of_line
;

eigrp_sf_topology_stanza
:
   eigrp_sf_topology_null_stanza
;

eigrp_classic_af_null_stanza
:
   NO?
   (
      AUTO_SUMMARY
      | AUTONOMOUS_SYSTEM
      | BFD
      | DEFAULT_INFORMATION
      | DEFAULT_METRIC
      | DISTANCE
      | DISTRIBUTE_LIST
      | EIGRP
      | MAXIMUM_PATHS
      | MAXIMUM_PREFIX
      | MAXIMUM_SECONDARY_PATHS
      | METRIC
      | NEIGHBOR
      | NSF
      | OFFSET_LIST
      | PASSIVE_INTERFACE
      | REDISTRIBUTE
   ) null_rest_of_line
;

eigrp_classic_af_stanza
:
   network_eigrp_stanza
   | eigrp_classic_af_null_stanza
;

eigrp_classic_stanza
:
   address_family_eigrp_classic_stanza
   | eigrp_classic_null_stanza
   | network_eigrp_stanza
;

eigrp_classic_null_stanza
:
   NO?
   (
      AUTO_SUMMARY
      | BFD
      | DEFAULT_INFORMATION
      | DEFAULT_METRIC
      | DISTANCE
      | DISTRIBUTE_LIST
      | EIGRP
      | HELLO_INTERVAL
      | MAXIMUM_PATHS
      | MAXIMUM_SECONDARY_PATHS
      | METRIC
      | NEIGHBOR
      | NSF
      | OFFSET_LIST
      | PASSIVE_INTERFACE
      | REDISTRIBUTE
      | SHUTDOWN
      | SPLIT_HORIZON
      | SUMMARY_METRIC
      | TIMERS
      | TRAFFIC_SHARE
      | VARIANCE
   ) null_rest_of_line
;

eigrp_named_af_stanza
:
   af_interface_eigrp_stanza
   | eigrp_named_af_null_stanza
   | network_eigrp_stanza
   | topology_af_eigrp_stanza
;

eigrp_named_af_null_stanza
:
   NO?
   (
      EIGRP
      | MAXIMUM_PREFIX
      | METRIC
      | NEIGHBOR
      | NSF
      | REMOTE_NEIGHBORS
      | SHUTDOWN
      | SOFT_SIA
      | TIMERS
   ) null_rest_of_line
;

eigrp_named_null_stanza
:
   NO? SHUTDOWN
;

eigrp_named_stanza
:
   address_family_eigrp_named_stanza
   | eigrp_named_null_stanza
   | service_family_eigrp_stanza
;

eigrp_sf_null_stanza
:
   NO?
   (
      EIGRP
      | MAXIMUM_SERVICE
      | METRIC
      | NEIGHBOR
      | REMOTE_NEIGHBORS
      | SHUTDOWN
      | TIMERS
   ) null_rest_of_line
;

eigrp_sf_stanza
:
   sf_interface_eigrp_stanza
   | eigrp_sf_null_stanza
   | topology_sf_eigrp_stanza
;

network_eigrp_stanza
:
   NETWORK address = IP_ADDRESS mask = IP_ADDRESS? NEWLINE
;

router_eigrp_stanza
:
  router_eigrp_classic_stanza
  | router_eigrp_named_stanza
;

router_eigrp_classic_stanza
:
   ROUTER EIGRP asnum = DEC NEWLINE
   eigrp_classic_stanza*
;
router_eigrp_named_stanza
:
   ROUTER EIGRP virtname = variable NEWLINE
   eigrp_named_stanza*
;

service_family_eigrp_stanza
:
   SERVICE_FAMILY
   (
      IPV4
      | IPV6
   )
   (
      VRF vrf = variable
   )? AUTONOMOUS_SYSTEM asnum = DEC NEWLINE
   eigrp_sf_stanza* service_family_footer
;

service_family_footer
:
   (
      (
         EXIT_SERVICE_FAMILY
         | EXIT
      ) NEWLINE
   )?
;

sf_interface_eigrp_stanza
:
   SF_INTERFACE iname = interface_name NEWLINE eigrp_sf_int_stanza*
   (
      EXIT_SF_INTERFACE NEWLINE
   )?
;

topology_af_eigrp_stanza
:
   (
     topology_base_stanza
     | topology_name_stanza
   ) eigrp_af_topology_stanza*
   (
      EXIT_AF_TOPOLOGY NEWLINE
   )?
;

topology_base_stanza
:
   TOPOLOGY BASE NEWLINE
;

topology_name_stanza
:
   TOPOLOGY topo_name = variable TID topo_num = DEC NEWLINE
;

topology_sf_eigrp_stanza
:
   (
     topology_base_stanza
     | topology_name_stanza
   ) eigrp_sf_topology_stanza*
   (
      EXIT_SF_TOPOLOGY NEWLINE
   )?
;
