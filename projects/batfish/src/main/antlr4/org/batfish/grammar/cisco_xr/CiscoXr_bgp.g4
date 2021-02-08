parser grammar CiscoXr_bgp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

activate_bgp_tail
:
   ACTIVATE NEWLINE
;

address_family_header returns [String addressFamilyStr]
:
   ADDRESS_FAMILY af = bgp_address_family
   {$addressFamilyStr = $af.ctx.getText();}

   NEWLINE
;

bgp_address_family
:
   (
      (
         IPV4
         (
            MDT
            | LABELED_UNICAST
         )?
      )
      | IPV6
      (
         LABELED_UNICAST
      )?
      | L2VPN
      | VPNV4
      | VPNV6
   )
   (
      UNICAST
      | MULTICAST
      | VPLS
   )?
   (
      VRF vrf_name = VARIABLE
   )?
;

address_family_rb_stanza
:
   address_family_header
   (
      additional_paths_rb_stanza
      | additional_paths_receive_xr_rb_stanza
      | additional_paths_selection_xr_rb_stanza
      | additional_paths_send_xr_rb_stanza
      | aggregate_address_rb_stanza
      | bgp_tail
      | no_neighbor_activate_rb_stanza
      | no_neighbor_shutdown_rb_stanza
      | null_no_neighbor_rb_stanza
      | peer_group_assignment_rb_stanza
      | peer_group_creation_rb_stanza
   )* address_family_footer
;

address_family_enable_rb_stanza
:
   ADDRESS_FAMILY af = bgp_address_family ENABLE NEWLINE
;

af_group_rb_stanza
:
   AF_GROUP name = variable ADDRESS_FAMILY bgp_address_family NEWLINE bgp_tail*
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
  )
  (
    as_set = AS_SET
    | summary_only = SUMMARY_ONLY
    | ROUTE_POLICY rp = variable
  )* NEWLINE
;

additional_paths_rb_stanza
:
   BGP ADDITIONAL_PATHS
   (
      INSTALL
      | SELECT ALL
      | SEND RECEIVE?
      | RECEIVE SEND?
   ) NEWLINE
;

additional_paths_receive_xr_rb_stanza
:
  ADDITIONAL_PATHS RECEIVE NEWLINE
;

additional_paths_selection_xr_rb_stanza
:
  ADDITIONAL_PATHS SELECTION ROUTE_POLICY name = variable NEWLINE
;

additional_paths_send_xr_rb_stanza
:
  ADDITIONAL_PATHS SEND NEWLINE
;

advertise_bgp_tail
:
   ADVERTISE ADDITIONAL_PATHS ALL NEWLINE
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

as_path_multipath_relax_rb_stanza
:
   NO? BGP? BESTPATH AS_PATH MULTIPATH_RELAX NEWLINE
;

auto_summary_bgp_tail
:
   NO? AUTO_SUMMARY NEWLINE
;

bgp_advertise_inactive_rb_stanza
:
   BGP ADVERTISE_INACTIVE NEWLINE
;

//confederations are not currently implemented
//not putting this under null so we can warn the user

bgp_confederation_rb_stanza
:
   BGP CONFEDERATION ~NEWLINE+ NEWLINE
;

bgp_listen_range_rb_stanza
:
   BGP LISTEN RANGE
   (
      IP_PREFIX
      | IPV6_PREFIX
   ) PEER_GROUP name = variable
   (
      REMOTE_AS bgp_asn
   )? NEWLINE
;

bgp_maxas_limit_rb_stanza
:
   BGP MAXAS_LIMIT limit = DEC NEWLINE
;

bgp_redistribute_internal_rb_stanza
:
   BGP REDISTRIBUTE_INTERNAL NEWLINE
;

bgp_scan_time_bgp_tail
:
   BGP SCAN_TIME secs = DEC NEWLINE
;

bgp_tail
:
   activate_bgp_tail
   | advertise_bgp_tail
   | allowas_in_bgp_tail
   | as_override_bgp_tail
   | cluster_id_bgp_tail
   | bgp_scan_time_bgp_tail
   | default_metric_bgp_tail
   | default_originate_bgp_tail
   | default_shutdown_bgp_tail
   | description_bgp_tail
   | distribute_list_bgp_tail
   | ebgp_multihop_bgp_tail
   | local_as_bgp_tail
   | maximum_paths_bgp_tail
   | maximum_peers_bgp_tail
   | network_bgp_tail
   | network6_bgp_tail
   | next_hop_self_bgp_tail
   | no_network_bgp_tail
   | null_bgp_tail
   | prefix_list_bgp_tail
   | redistribute_aggregate_bgp_tail
   | redistribute_connected_bgp_tail
   | redistribute_eigrp_bgp_tail
   | redistribute_ospf_bgp_tail
   | redistribute_ospfv3_bgp_tail
   | redistribute_rip_bgp_tail
   | redistribute_static_bgp_tail
   | remove_private_as_bgp_tail
   | route_policy_bgp_tail
   | route_reflector_client_bgp_tail
   | router_id_bgp_tail
   | send_community_bgp_tail
   | shutdown_bgp_tail
   | subnet_bgp_tail
   | update_source_bgp_tail
   | weight_bgp_tail
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

compare_routerid_rb_stanza
:
   (
      BGP? BESTPATH
   )? COMPARE_ROUTERID NEWLINE
;

default_information_originate_rb_stanza
:
   DEFAULT_INFORMATION ORIGINATE NEWLINE
;

default_metric_bgp_tail
:
   DEFAULT_METRIC metric = DEC NEWLINE
;

default_originate_bgp_tail
:
   DEFAULT_ORIGINATE (ROUTE_POLICY policy = VARIABLE)? NEWLINE
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
   DISTRIBUTE_LIST  list_name = variable
   (
      IN
      | OUT
   ) NEWLINE
;

ebgp_multihop_bgp_tail
:
   EBGP_MULTIHOP
   (
      hop = DEC
   )? NEWLINE
;

empty_neighbor_block_address_family
:
   address_family_header address_family_footer
;

inherit_peer_policy_bgp_tail
:
   INHERIT PEER_POLICY name = variable_permissive num = DEC? NEWLINE
;

inherit_peer_session_bgp_tail
:
   INHERIT PEER_SESSION name = variable_permissive NEWLINE
;

local_as_bgp_tail
:
   LOCAL_AS bgp_asn
   (
      NO_PREPEND
      | REPLACE_AS
   )* NEWLINE
;

maximum_peers_bgp_tail
:
   MAXIMUM_PEERS DEC NEWLINE
;

maximum_paths_bgp_tail
:
   MAXIMUM_PATHS
   (
      EBGP
      | IBGP
      | EIBGP
   )? paths = DEC
   (
      ECMP ecmp_paths = DEC
   )? SELECTIVE? NEWLINE
;

maximum_prefix_bgp_tail
:
   MAXIMUM_PREFIX DEC NEWLINE
;

neighbor_block_address_family
:
   address_family_header
   (
      bgp_tail
      | use_af_group_bgp_tail
   )+ address_family_footer
;

neighbor_block_rb_stanza
locals
[java.util.Set<String> addressFamilies, java.util.Set<String> consumedAddressFamilies]
@init {
   $addressFamilies = new java.util.HashSet<String>();
   $consumedAddressFamilies = new java.util.HashSet<String>();
}
:
   NEIGHBOR
   (
      ip_address = IP_ADDRESS
      | ipv6_address = IPV6_ADDRESS
      | ip_prefix = IP_PREFIX
      | ipv6_prefix = IPV6_PREFIX
   )
   (
      REMOTE_AS asnum = bgp_asn
   )?
   NEWLINE
   (
      bgp_tail
      | no_shutdown_rb_stanza
      | remote_as_bgp_tail
      | use_neighbor_group_bgp_tail
      | use_session_group_bgp_tail
   )*
   (
      (
         empty_neighbor_block_address_family
         | neighbor_block_address_family
      )* neighbor_block_address_family
   )?
;

neighbor_group_rb_stanza
:
   NEIGHBOR_GROUP name = variable NEWLINE
   (
      address_family_rb_stanza
      | remote_as_bgp_tail
      | update_source_bgp_tail
      | use_neighbor_group_bgp_tail
      | use_session_group_bgp_tail
      | null_bgp_tail
   )*
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
      ROUTE_POLICY policyname = VARIABLE
   )? NEWLINE
;

network6_bgp_tail
:
   NETWORK prefix = IPV6_PREFIX
   (
      ROUTE_POLICY policyname = VARIABLE
   )? NEWLINE
;

next_hop_self_bgp_tail
:
   NO? NEXT_HOP_SELF NEWLINE
;

vrf_block_rb_stanza
:
   VRF name = ~NEWLINE NEWLINE
   (
      address_family_rb_stanza
      | aggregate_address_rb_stanza
      | always_compare_med_rb_stanza
      | as_path_multipath_relax_rb_stanza
      | bgp_listen_range_rb_stanza
      | bgp_tail
      | neighbor_block_rb_stanza
      | no_neighbor_activate_rb_stanza
      | no_neighbor_shutdown_rb_stanza
      | no_redistribute_connected_rb_stanza
      | null_no_neighbor_rb_stanza
      | peer_group_assignment_rb_stanza
      | peer_group_creation_rb_stanza
      | router_id_rb_stanza
      | template_peer_session_rb_stanza
   )*
;

no_bgp_enforce_first_as_stanza
:
   NO BGP ENFORCE_FIRST_AS NEWLINE
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
   (
      NO NEIGHBOR
      (
         ip = IP_ADDRESS
         | ip6 = IPV6_ADDRESS
         | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
      ) SHUTDOWN NEWLINE
   )
   |
   (
      NEIGHBOR
      (
         ip = IP_ADDRESS
         | ip6 = IPV6_ADDRESS
         | peergroup = ~( IP_ADDRESS | IPV6_ADDRESS | NEWLINE )
      ) NO SHUTDOWN NEWLINE
   )
;

no_network_bgp_tail
:
   NO NETWORK null_rest_of_line
;

no_redistribute_connected_rb_stanza
:
   NO REDISTRIBUTE
   (
      CONNECTED
      | DIRECT
   ) null_rest_of_line
;

no_shutdown_rb_stanza
:
   NO SHUTDOWN NEWLINE
;

null_bgp_tail
:
   NO?
   (
      ADVERTISEMENT_INTERVAL
      | AUTO_SHUTDOWN_NEW_NEIGHBORS
      | AUTO_SUMMARY
      | AUTO_LOCAL_ADDR
      |
      (
         AGGREGATE_ADDRESS
         (
            IPV6_ADDRESS
            | IPV6_PREFIX
         )
      )
      |
      (
         BESTPATH
         (
            AS_PATH
            (
               CONFED
            )
         )
      )
      | BFD
      | BFD_ENABLE
      |
      (
         BGP
         (
            ATTRIBUTE_DOWNLOAD
            |
            (
               BESTPATH
               (
                  AS_PATH CONFED
                  | MED
               )
            )
            | CLIENT_TO_CLIENT
            | DAMPENING
            | DEFAULT
            | DETERMINISTIC_MED
            | FAST_EXTERNAL_FALLOVER
            | GRACEFUL_RESTART
            |
            (
               LISTEN LIMIT
            )
            | LOG
            | LOG_NEIGHBOR_CHANGES
            | NEXTHOP
            | NON_DETERMINISTIC_MED
            | REDISTRIBUTE_INTERNAL
         )
      )
      | CAPABILITY
      | CLIENT_TO_CLIENT
      | CONNECT_RETRY
      | DAMPEN
      | DAMPEN_IGP_METRIC
      | DAMPENING
      | DESCRIPTION
      | DISABLE_CONNECTED_CHECK
      | DISTANCE
      | DONT_CAPABILITY_NEGOTIATE
      | DYNAMIC_CAPABILITY
      | ENFORCE_FIRST_AS
      | EVENT_HISTORY
      | EXIT
      | FAIL_OVER
      | FALL_OVER
      | FAST_EXTERNAL_FALLOVER
      | GRACEFUL_RESTART
      | LOCAL_V6_ADDR
      | LOG_NEIGHBOR_CHANGES
      | MAXIMUM_PREFIX
      | MAXIMUM_ACCEPTED_ROUTES
      | MAXIMUM_ROUTES
      | MULTIPATH
      |
      (
         NO
         (
            REMOTE_AS
            | UPDATE_SOURCE
            | SHUTDOWN
         )
      )
      | NEIGHBOR_DOWN
      | NEXT_HOP_THIRD_PARTY
      | NEXTHOP
      | NSR
      | PASSWORD
      | RECONNECT_INTERVAL
      | SEND_LABEL
      | SESSION_OPEN_MODE
      | SOFT_RECONFIGURATION
      | SUPPRESS_FIB_PENDING
      | SYNCHRONIZATION
      | TABLE_MAP
      | TIMERS
      | TRANSPORT
      | UPDATE
      |
      (
         USE
         (
            NEXTHOP_ATTRIBUTE
         )
      )
      | VERSION
   ) null_rest_of_line
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
   ) PEER_GROUP name = variable NEWLINE
;

peer_group_creation_rb_stanza
:
   NEIGHBOR name = VARIABLE PEER_GROUP PASSIVE?
   (
      NLRI
      | UNICAST
      | MULTICAST
   )* NEWLINE
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
   REMOTE_AS remote = bgp_asn (ALTERNATE_AS alt_ases += bgp_asn+)? NEWLINE
;

remove_private_as_bgp_tail
:
   REMOVE_PRIVATE_AS ALL? NEWLINE
;

route_policy_bgp_tail
:
   ROUTE_POLICY name = variable
   (
      PAREN_LEFT route_policy_params_list PAREN_RIGHT
   )?
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
      ROUTE_POLICY policy = variable
      |
      (
         METRIC metric = DEC
      )
   )* NEWLINE
;

redistribute_eigrp_bgp_tail
:
   REDISTRIBUTE EIGRP id = DEC null_rest_of_line
;

redistribute_ospf_bgp_tail
:
   REDISTRIBUTE OSPF (procname = variable)?
   (
      (
         METRIC metric = DEC
      )
      |
      (
         MATCH ospf_route_type*
      )
      |
      (
         VRF vrf = variable
      )
   )* NEWLINE
;

redistribute_ospfv3_bgp_tail
:
   REDISTRIBUTE (OSPFV3 | OSPF3) (procname = variable)?
   (
      (
         METRIC metric = DEC
      )
      |
      (
         MATCH ospf_route_type*
      )
      |
      (
         VRF vrf = variable
      )
   )* NEWLINE
;

redistribute_rip_bgp_tail
:
   REDISTRIBUTE RIP
   (
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
         ROUTE_POLICY policy = VARIABLE
      )
      |
      (
         METRIC metric = DEC
      )
   )* NEWLINE
;

router_bgp_stanza
:
   ROUTER BGP
   (
      procnum = bgp_asn
   )? NEWLINE
   router_bgp_stanza_tail*
;

router_bgp_stanza_tail
:
   additional_paths_rb_stanza
   | address_family_rb_stanza
   | address_family_enable_rb_stanza
   | af_group_rb_stanza
   | aggregate_address_rb_stanza
   | always_compare_med_rb_stanza
   | as_path_multipath_relax_rb_stanza
   | bgp_advertise_inactive_rb_stanza
   | bgp_confederation_rb_stanza
   | bgp_listen_range_rb_stanza
   | bgp_maxas_limit_rb_stanza
   | bgp_redistribute_internal_rb_stanza
   | bgp_tail
   | cluster_id_rb_stanza
   | compare_routerid_rb_stanza
   | default_information_originate_rb_stanza
   | neighbor_block_rb_stanza
   | neighbor_group_rb_stanza
   | no_bgp_enforce_first_as_stanza
   | no_neighbor_activate_rb_stanza
   | no_neighbor_shutdown_rb_stanza
   | no_redistribute_connected_rb_stanza
   | null_no_neighbor_rb_stanza
   | peer_group_assignment_rb_stanza
   | peer_group_creation_rb_stanza
   | router_id_rb_stanza
   | session_group_rb_stanza
   | template_peer_policy_rb_stanza
   | template_peer_session_rb_stanza
   | vrf_block_rb_stanza
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
   (
      (
         SEND_COMMUNITY EXTENDED? BOTH?
      )
      | SEND_COMMUNITY_EBGP
      |
      (
         SEND_EXTENDED_COMMUNITY_EBGP INHERITANCE_DISABLE?
      )
   ) NEWLINE
;

session_group_rb_stanza
:
   SESSION_GROUP name = variable NEWLINE
   (
      bgp_tail
      | remote_as_bgp_tail
      | use_session_group_bgp_tail
   )*
;

shutdown_bgp_tail
:
   SHUTDOWN NEWLINE
;

subnet_bgp_tail
:
   SUBNET
   (
      prefix = IP_PREFIX
      | ipv6_prefix = IPV6_PREFIX
   ) NEWLINE
;

template_peer_address_family
:
   address_family_header bgp_tail* address_family_footer
;

template_peer_policy_rb_stanza
:
   TEMPLATE PEER_POLICY name = variable_permissive NEWLINE
   (
      bgp_tail
      | inherit_peer_policy_bgp_tail
   )*
   (
      EXIT_PEER_POLICY NEWLINE
   )
;

template_peer_session_rb_stanza
:
   TEMPLATE PEER_SESSION name = variable_permissive NEWLINE
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

use_af_group_bgp_tail
:
   USE AF_GROUP name = variable NEWLINE
;

use_neighbor_group_bgp_tail
:
   USE NEIGHBOR_GROUP name = variable NEWLINE
;

use_session_group_bgp_tail
:
   USE SESSION_GROUP name = variable NEWLINE
;

weight_bgp_tail
:
   WEIGHT weight = DEC NEWLINE
;
