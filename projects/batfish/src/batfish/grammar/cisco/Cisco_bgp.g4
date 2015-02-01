parser grammar Cisco_bgp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

activate_bgp_tail
:
   ACTIVATE NEWLINE
;

address_family_header
:
   ADDRESS_FAMILY
   (
      (
         IPV4 MDT?
      )
      | IPV6
      | VPNV4
      | VPNV6
   )
   (
      UNICAST
      | MULTICAST
   )?
   (
      VRF vrf_name = VARIABLE
   )? NEWLINE
;

address_family_footer
:
   (
      EXIT_ADDRESS_FAMILY NEWLINE
   )?
;

address_family_rb_stanza
:
   address_family_header
   (
      aggregate_address_rb_stanza
      | bgp_tail
      | neighbor_rb_stanza
      | no_neighbor_activate_rb_stanza
      | no_neighbor_shutdown_rb_stanza
      | null_no_neighbor_rb_stanza
      | peer_group_assignment_rb_stanza
      | peer_group_creation_rb_stanza
   )* address_family_footer
;

aggregate_address_rb_stanza
:
   AGGREGATE_ADDRESS
   (
      (
         network = IP_ADDRESS subnet = IP_ADDRESS
      )
      | prefix = IP_PREFIX
      | ipv6_prefix = IPV6_PREFIX
   ) AS_SET? SUMMARY_ONLY? NEWLINE
;

allowas_in_bgp_tail
:
   ALLOWAS_IN
   (
      num = DEC
   )? NEWLINE
;

always_compare_med_rb_stanza
:
   BGP ALWAYS_COMPARE_MED NEWLINE
;

as_override_bgp_tail
:
   AS_OVERRIDE NEWLINE
;

auto_summary_bgp_tail
:
   NO? AUTO_SUMMARY NEWLINE
;

bgp_listen_range_rb_stanza
:
   BGP LISTEN RANGE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) PEER_GROUP name = variable
   (
      REMOTE_AS as = DEC
   )? NEWLINE
;

bgp_tail
:
   activate_bgp_tail
   | allowas_in_bgp_tail
   | as_override_bgp_tail
   | cluster_id_bgp_tail
   | default_metric_bgp_tail
   | default_originate_bgp_tail
   | default_shutdown_bgp_tail
   | description_bgp_tail
   | distribute_list_bgp_tail
   | ebgp_multihop_bgp_tail
   | local_as_bgp_tail
   | maximum_peers_bgp_tail
   | network_bgp_tail
   | network6_bgp_tail
   | next_hop_self_bgp_tail
   | null_bgp_tail
   | prefix_list_bgp_tail
   | redistribute_aggregate_bgp_tail
   | redistribute_connected_bgp_tail
   | redistribute_ospf_bgp_tail
   | redistribute_static_bgp_tail
   | remove_private_as_bgp_tail
   | route_map_bgp_tail
   | route_reflector_client_bgp_tail
   | router_id_bgp_tail
   | send_community_bgp_tail
   | shutdown_bgp_tail
   | update_source_bgp_tail
;

cluster_id_bgp_tail
:
   CLUSTER_ID
   (
      DEC
      | IP_ADDRESS
   ) NEWLINE
;

cluster_id_rb_stanza
:
   BGP cluster_id_bgp_tail
;

default_metric_bgp_tail
:
   DEFAULT_METRIC metric = DEC NEWLINE
;

default_originate_bgp_tail
:
   DEFAULT_ORIGINATE
   (
      ROUTE_MAP map = VARIABLE
   )? NEWLINE
;

default_shutdown_bgp_tail
:
   DEFAULT SHUTDOWN NEWLINE
;

description_bgp_tail
:
   description_line
;

distribute_list_bgp_tail
:
   DISTRIBUTE_LIST ~NEWLINE* NEWLINE
;

ebgp_multihop_bgp_tail
:
   EBGP_MULTIHOP hop = DEC NEWLINE
;

filter_list_bgp_tail
:
   FILTER_LIST num = DEC
   (
      IN
      | OUT
   ) NEWLINE
;

inherit_peer_session_bgp_tail
:
   INHERIT PEER_SESSION name = variable NEWLINE
;

local_as_bgp_tail
:
   LOCAL_AS as = DEC
   (
      NO_PREPEND
      | REPLACE_AS
   )* NEWLINE
;

maximum_peers_bgp_tail
:
   MAXIMUM_PEERS DEC NEWLINE
;

maximum_prefix_bgp_tail
:
   MAXIMUM_PREFIX DEC NEWLINE
;

neighbor_rb_stanza
:
   NEIGHBOR
   (
      ip = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
   )
   (
      bgp_tail
      | inherit_peer_session_bgp_tail
      | filter_list_bgp_tail
      | remote_as_bgp_tail
   )
;

network_bgp_tail
:
   NETWORK
   (
      (
         ip = IP_ADDRESS
         (
            MASK mask = IP_ADDRESS
         )?
      )
      | prefix = IP_PREFIX
   )?
   (
      ROUTE_MAP mapname = VARIABLE
   )? NEWLINE
;

network6_bgp_tail
:
   NETWORK
   (
      address = IPV6_ADDRESS
      | prefix = IPV6_PREFIX
   ) NEWLINE
;

next_hop_self_bgp_tail
:
   NO? NEXT_HOP_SELF NEWLINE
;

nexus_neighbor_address_family
:
   address_family_header bgp_tail* address_family_footer
;

nexus_neighbor_inherit
:
   INHERIT PEER name = VARIABLE NEWLINE
;

nexus_neighbor_rb_stanza
locals [java.util.Set<String> addressFamilies]
@init {$addressFamilies = new java.util.HashSet<String>();}
:
   NEIGHBOR
   (
      ip_address = IP_ADDRESS
      | ipv6_address = IPV6_ADDRESS
      | ip_prefix = IP_PREFIX
      | ipv6_prefix = IPV6_PREFIX
   )
   (
      REMOTE_AS asnum = DEC
   )? NEWLINE nexus_neighbor_rb_stanza_tail [$addressFamilies]
;

nexus_neighbor_rb_stanza_tail [java.util.Set<String> addressFamilies]
locals [boolean active]
:
{
   if (_input.LT(1).getType() == ADDRESS_FAMILY) {
      String addressFamilyString = "";
      for (int i = 1, currentType = -1; _input.LT(i).getType() != NEWLINE; i++) {
         addressFamilyString += " " + _input.LT(i).getText();
      }
      if ($addressFamilies.contains(addressFamilyString)) {
         $active = false;
      }
      else {
         $addressFamilies.add(addressFamilyString);
         $active = true;
      }
   }
   else {
      $active = true;
   }
}

   (
      {$active}?

      (
         (
            bgp_tail
            | nexus_neighbor_address_family
            | nexus_neighbor_inherit
            | no_shutdown_rb_stanza
            | remote_as_bgp_tail
         ) nexus_neighbor_rb_stanza_tail [$addressFamilies]
      )
      | //intentional blank

   )
;

nexus_vrf_rb_stanza
:
   VRF name = ~NEWLINE NEWLINE
   (
      address_family_rb_stanza
      | always_compare_med_rb_stanza
      | bgp_listen_range_rb_stanza
      | bgp_tail
      | neighbor_rb_stanza
      | nexus_neighbor_rb_stanza
      | no_neighbor_activate_rb_stanza
      | no_neighbor_shutdown_rb_stanza
      | no_redistribute_connected_rb_stanza
      | null_no_neighbor_rb_stanza
      | peer_group_assignment_rb_stanza
      | peer_group_creation_rb_stanza
      | router_id_rb_stanza
      | template_peer_rb_stanza
      | template_peer_session_rb_stanza
   )*
;

no_neighbor_activate_rb_stanza
:
   NO NEIGHBOR
   (
      ip = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
   ) ACTIVATE NEWLINE
;

no_neighbor_shutdown_rb_stanza
:
   NO NEIGHBOR
   (
      ip = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
   ) SHUTDOWN NEWLINE
;

no_redistribute_connected_rb_stanza
:
   NO REDISTRIBUTE
   (
      CONNECTED
      | DIRECT
   ) ~NEWLINE* NEWLINE
;

no_shutdown_rb_stanza
:
   NO SHUTDOWN NEWLINE
;

null_bgp_tail
:
   NO?
   (
      AUTO_SUMMARY
      |
      (
         AGGREGATE_ADDRESS
         (
            IPV6_ADDRESS
            | IPV6_PREFIX
         )
      )
      | BESTPATH
      |
      (
         BGP
         (
            BESTPATH
            | DAMPENING
            | DEFAULT
            | DETERMINISTIC_MED
            | GRACEFUL_RESTART
            |
            (
               LISTEN LIMIT
            )
            | LOG_NEIGHBOR_CHANGES
            | NEXTHOP
         )
      )
      | DESCRIPTION
      | DONT_CAPABILITY_NEGOTIATE
      | EVENT_HISTORY
      | FALL_OVER
      | LOG_NEIGHBOR_CHANGES
      | MAXIMUM_PATHS
      | MAXIMUM_PREFIX
      | MAXIMUM_PREFIX
      | MAXIMUM_ROUTES
      |
      (
         NO
         (
            REMOTE_AS
            | ROUTE_MAP
         )
      )
      | PASSWORD
      | SEND_LABEL
      | SOFT_RECONFIGURATION
      | SYNCHRONIZATION
      | TIMERS
      | TRANSPORT
      | VERSION
   ) ~NEWLINE* NEWLINE
;

null_no_neighbor_rb_stanza
:
   NO NEIGHBOR
   (
      ip = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
   ) null_bgp_tail
;

peer_group_assignment_rb_stanza
:
   NEIGHBOR
   (
      address = IP_ADDRESS
      | address6 = IPV6_ADDRESS
   ) PEER_GROUP name = VARIABLE NEWLINE
;

peer_group_creation_rb_stanza
:
   NEIGHBOR name = VARIABLE PEER_GROUP NEWLINE
;

prefix_list_bgp_tail
:
   PREFIX_LIST list_name = VARIABLE
   (
      IN
      | OUT
   ) NEWLINE
;

remote_as_bgp_tail
:
   REMOTE_AS as = DEC NEWLINE
;

remove_private_as_bgp_tail
:
   REMOVE_PRIVATE_AS NEWLINE
;

route_map_bgp_tail
:
   ROUTE_MAP name = VARIABLE
   (
      IN
      | OUT
   ) NEWLINE
;

route_reflector_client_bgp_tail
:
   ROUTE_REFLECTOR_CLIENT NEWLINE
;

redistribute_aggregate_bgp_tail
:
   REDISTRIBUTE AGGREGATE NEWLINE
;

redistribute_connected_bgp_tail
:
   REDISTRIBUTE
   (
      CONNECTED
      | DIRECT
   )
   (
      (
         ROUTE_MAP map = VARIABLE
      )
      |
      (
         METRIC metric = DEC
      )
   )* NEWLINE
;

redistribute_ospf_bgp_tail
:
   REDISTRIBUTE OSPF procnum = DEC
   (
      (
         ROUTE_MAP map = VARIABLE
      )
      |
      (
         METRIC metric = DEC
      )
   )* NEWLINE
;

redistribute_static_bgp_tail
:
   REDISTRIBUTE STATIC
   (
      (
         ROUTE_MAP map = VARIABLE
      )
      |
      (
         METRIC metric = DEC
      )
   )* NEWLINE
;

router_bgp_stanza
:
   ROUTER BGP procnum = DEC NEWLINE
   (
      address_family_rb_stanza
      | aggregate_address_rb_stanza
      | always_compare_med_rb_stanza
      | bgp_listen_range_rb_stanza
      | bgp_tail
      | cluster_id_rb_stanza
      | neighbor_rb_stanza
      | nexus_neighbor_rb_stanza
      | no_neighbor_activate_rb_stanza
      | no_neighbor_shutdown_rb_stanza
      | no_redistribute_connected_rb_stanza
      | null_no_neighbor_rb_stanza
      | peer_group_assignment_rb_stanza
      | peer_group_creation_rb_stanza
      | router_id_rb_stanza
      | template_peer_rb_stanza
      | template_peer_session_rb_stanza
      | nexus_vrf_rb_stanza
   )+
;

router_id_bgp_tail
:
   ROUTER_ID routerid = IP_ADDRESS NEWLINE
;

router_id_rb_stanza
:
   BGP router_id_bgp_tail
;

send_community_bgp_tail
:
   SEND_COMMUNITY EXTENDED? BOTH? NEWLINE
;

shutdown_bgp_tail
:
   SHUTDOWN NEWLINE
;

template_peer_address_family
:
   address_family_header bgp_tail* address_family_footer
;

template_peer_rb_stanza
locals [java.util.Set<String> addressFamilies] @init {
   $addressFamilies = new java.util.HashSet<String>();
}
:
   TEMPLATE PEER name = VARIABLE NEWLINE template_peer_rb_stanza_tail
   [$addressFamilies]
;

template_peer_rb_stanza_tail [java.util.Set<String> addressFamilies]
locals [boolean active]
:
{
   if (_input.LT(1).getType() == ADDRESS_FAMILY) {
      String addressFamilyString = "";
      for (int i = 1, currentType = -1; _input.LT(i).getType() != NEWLINE; i++) {
         addressFamilyString += " " + _input.LT(i).getText();
      }
      if ($addressFamilies.contains(addressFamilyString)) {
         $active = false;
      }
      else {
         $addressFamilies.add(addressFamilyString);
         $active = true;
      }
   }
   else {
      $active = true;
   }
}

   (
      {$active}?

      (
         bgp_tail
         | inherit_peer_session_bgp_tail
         | no_shutdown_rb_stanza
         | remote_as_bgp_tail
         | template_peer_address_family
      ) template_peer_rb_stanza_tail [$addressFamilies]
      |
      // intentional blank

   )
;

template_peer_session_rb_stanza
:
   TEMPLATE PEER_SESSION name = VARIABLE NEWLINE
   (
      bgp_tail
      | remote_as_bgp_tail
   )*
   (
      EXIT_PEER_SESSION NEWLINE
   )?
;

update_source_bgp_tail
:
   UPDATE_SOURCE source = interface_name NEWLINE
;
