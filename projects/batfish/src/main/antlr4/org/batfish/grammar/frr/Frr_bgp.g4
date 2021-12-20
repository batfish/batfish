parser grammar Frr_bgp;

import Frr_common;

options {
  tokenVocab = FrrLexer;
}

s_router_bgp
:
  ROUTER BGP autonomous_system (VRF vrf_name)? NEWLINE
  bgp_inner*
;

bgp_inner
:
  rb_address_family
| rb_always_compare_med
| rb_bgp
| rb_neighbor
| rb_network
| rb_no
| rb_redistribute
| rb_timers
| rbafi_neighbor
;

rb_bgp
:
  BGP
  (
    rbb_bestpath
    | rbb_confederation
    | rbb_log_neighbor_changes
    | rbb_router_id
    | rbb_cluster_id
    | rbb_max_med_administrative
    | rbb_listen
  )
;

rb_no
:
  NO
  (
    rbno_bgp
  )
;


rbno_bgp
:
  BGP
  (
     rbnob_default
  )
;

rbnob_default
:
  DEFAULT
  (
     rbnobd_ipv4_unicast
  )
;

rbb_confederation
:
  CONFEDERATION IDENTIFIER id = uint32 NEWLINE
;

rbb_bestpath
:
  BESTPATH
  (
    rbbb_aspath_multipath_relax
  )
;

rbb_log_neighbor_changes
:
  LOG_NEIGHBOR_CHANGES NEWLINE
;

rbb_max_med_administrative
:
   MAX_MED ADMINISTRATIVE (med = uint32)? NEWLINE
;

rbbb_aspath_multipath_relax
:
  AS_PATH MULTIPATH_RELAX NEWLINE
;

rb_redistribute
:
  REDISTRIBUTE bgp_redist_type (ROUTE_MAP route_map_name)? NEWLINE
;

rbb_router_id
:
  ROUTER_ID IP_ADDRESS NEWLINE
;

rbb_cluster_id
:
  CLUSTER_ID IP_ADDRESS NEWLINE
;

rb_neighbor
:
  NEIGHBOR (rbn_ip | rbn_ip6 | rbn_name)
;

rb_address_family
:
  ADDRESS_FAMILY rbaf
  (EXIT_ADDRESS_FAMILY NEWLINE)?
;

rbaf
:
    rbaf_ipv4_unicast
  | rbaf_ipv6_unicast
  | rbaf_l2vpn_evpn
;

rbaf_ipv4_unicast
:
  IPV4 UNICAST NEWLINE
  rbafi_inner*
;

rbafi_inner
:
  rbafi_aggregate_address
| rbafi_import
| rbafi_maximum_paths
| rbafi_network
| rbafi_neighbor
| rbafi_no
| rbafi_redistribute
;

rbaf_ipv6_unicast
:
  IPV6 UNICAST NEWLINE
  rbafi6_inner*
;

rbafi6_inner
:
  rbafi6_import
| rbafi6_maximum_paths
| rbafi6_null_tail
;

rbafi6_maximum_paths
:
  MAXIMUM_PATHS (IBGP)? num = uint32 NEWLINE
;

rbafi6_import
:
  IMPORT VRF (vrf_name | ROUTE_MAP route_map_name) NEWLINE
;

rbafi6_null_tail
:
  (
     // there are likely others but haven't seen examples yet, so leaving for later
     AGGREGATE_ADDRESS
     | NEIGHBOR
     | NETWORK
     | NO
     | REDISTRIBUTE
  ) null_rest_of_line
;


rbaf_l2vpn_evpn
:
  L2VPN EVPN NEWLINE
  rbafl_inner*
;

rbafl_inner
:
  rbafl_advertise
| rbafl_advertise_all_vni
| rbafl_advertise_default_gw
| rbafl_neighbor
;

rbafl_neighbor
:
   NEIGHBOR neighbor = (IP_ADDRESS | WORD)
   (
      rbafln_activate
      | rbafln_route_map
      | rbafln_route_reflector_client
   )
;

rbafi_aggregate_address
:
  AGGREGATE_ADDRESS IP_PREFIX agg_feature* NEWLINE
;

agg_feature
:
  agg_feature_as_set
  | agg_feature_matching_med_only
  | agg_feature_origin
  | agg_feature_route_map
  | agg_feature_summary_only
  | agg_feature_suppress_map
;

agg_feature_as_set: AS_SET;

agg_feature_matching_med_only: MATCHING_MED_ONLY;

agg_feature_origin: ORIGIN origin_type;

agg_feature_route_map: ROUTE_MAP mapname = route_map_name;

agg_feature_summary_only: SUMMARY_ONLY;

agg_feature_suppress_map: SUPPRESS_MAP mapname = route_map_name;

rbafi_import
:
  IMPORT VRF (vrf_name | ROUTE_MAP route_map_name) NEWLINE
;

rbafi_maximum_paths
:
  MAXIMUM_PATHS (IBGP)? num = uint32 NEWLINE
;

rbafi_network
:
  NETWORK network = prefix (ROUTE_MAP rm = route_map_name)? NEWLINE
;

rbafi_redistribute
:
  REDISTRIBUTE bgp_redist_type (ROUTE_MAP route_map_name)? NEWLINE
;

rbafl_advertise
:
  ADVERTISE
  (
     rbafla_ipv4_unicast
     | rbafla_ipv6_unicast
  )
;

rbafla_ipv4_unicast
:
  IPV4 UNICAST (ROUTE_MAP rm = route_map_name)? NEWLINE
;

rbafla_ipv6_unicast
:
  IPV6 UNICAST (ROUTE_MAP rm = route_map_name)? NEWLINE
;

rbafl_advertise_all_vni
:
  ADVERTISE_ALL_VNI NEWLINE
;

rbafl_advertise_default_gw
:
  ADVERTISE_DEFAULT_GW NEWLINE
;

rbafln_activate
:
  ACTIVATE NEWLINE
;

rbafln_route_map
:
  ROUTE_MAP name=word (IN | OUT) NEWLINE
;

rbafln_route_reflector_client
:
   ROUTE_REFLECTOR_CLIENT NEWLINE
;

rb_always_compare_med
:
  BGP ALWAYS_COMPARE_MED NEWLINE
;

rbn_ip
:
  ip = IP_ADDRESS rbn_property
;

rbn_ip6
:
   ip6 = IPV6_ADDRESS rbn_property
;

rbn_name
:
  name = word
    (
      rbn_interface       // set an interface neighbor property
    | rbn_peer_group_decl // declare a new peer group
    | rbn_property        // set a peer-group property
    // Nothing else should go in here. New properties should go in rbn_property
    )
;

rbn_interface
:
  INTERFACE rbn_property
;

rbn_peer_group_decl
:
  PEER_GROUP NEWLINE
;

rbn_property
:
 rbnp_advertisement_interval
| rbnp_bfd
| rbnp_description
| rbnp_ebgp_multihop
| rbnp_local_as
| rbnp_password
| rbnp_peer_group
| rbnp_remote_as
| rbnp_timers
| rbnp_update_source
;

rbnp_advertisement_interval
:
   ADVERTISEMENT_INTERVAL uint32 NEWLINE
;


rbnp_bfd
:
  BFD word* NEWLINE
;

rbnp_description
:
  DESCRIPTION REMARK_TEXT NEWLINE
;

rbnp_ebgp_multihop
:
  EBGP_MULTIHOP (num = uint32)? NEWLINE
;

rbnp_local_as
:
  LOCAL_AS asn = autonomous_system (NO_PREPEND REPLACE_AS?)? NEWLINE
;

rbnp_password
:
  PASSWORD REMARK_TEXT NEWLINE
;

rbnp_peer_group
:
  PEER_GROUP name = word NEWLINE
;

rbnp_remote_as
:
  REMOTE_AS (autonomous_system | EXTERNAL | INTERNAL) NEWLINE
;

rbnp_timers
:
  TIMERS
  (
    sbnpt_connect
    | sbnpt_delayopen
    | sbnpt_keepalive_hold
  )
;

sbnpt_connect
:
  CONNECT uint32 NEWLINE
;

sbnpt_delayopen
:
  // timer value should be between 1-240 but we don't enforce
  DELAYOPEN uint8 NEWLINE
;

sbnpt_keepalive_hold
:
   // first number is keepalive time and second is hold timer
   uint32 uint32 NEWLINE
;

rbnp_update_source
:
  UPDATE_SOURCE (ip = IP_ADDRESS | name = word) NEWLINE
;

rb_network
:
  NETWORK network = prefix (ROUTE_MAP rm = route_map_name)? NEWLINE
;

rbafi_neighbor
:
  NEIGHBOR (ip = IP_ADDRESS | ip6=IPV6_ADDRESS | name = word)
  (
    rbafin_activate
  | rbafin_allowas_in
  | rbafin_default_originate
  | rbafin_next_hop_self
  | rbafin_remove_private_as
  | rbafin_route_reflector_client
  | rbafin_send_community
  | rbafin_soft_reconfiguration
  | rbafin_route_map
  )
;

rbafi_no
:
  NO rbafino_neighbor
;

rbafino_neighbor
:
  NEIGHBOR (ipv4=IP_ADDRESS | ipv6=IPV6_ADDRESS | name = word)
  (
    rbafinon_activate
  )
;

rbafinon_activate
:
  ACTIVATE NEWLINE
;

rbafin_activate
:
  ACTIVATE NEWLINE
;

rbafin_allowas_in
:
  ALLOWAS_IN (count = UINT8)? NEWLINE
;

rbafin_default_originate
:
  DEFAULT_ORIGINATE (ROUTE_MAP route_map_name)? NEWLINE
;

rbafin_next_hop_self
:
  NEXT_HOP_SELF (FORCE | ALL)? NEWLINE
;

rbafin_remove_private_as
:
  REMOVE_PRIVATE_AS (ALL REPLACE_AS?)? NEWLINE
;


rbafin_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

rbafin_send_community
:
  SEND_COMMUNITY EXTENDED? NEWLINE
;

rbafin_soft_reconfiguration
:
  SOFT_RECONFIGURATION INBOUND NEWLINE
;

rbafin_route_map
:
  ROUTE_MAP name=word (IN | OUT) NEWLINE
;

rbb_listen
:
  LISTEN
  (
    rbbl_limit
    | rbbl_range
  )
;

rbbl_limit
:
  LIMIT uint32 NEWLINE
;

rbbl_range
:
  RANGE
   (
      prefix
      | prefix6
   )
   PEER_GROUP name = word
  NEWLINE
;

rbnobd_ipv4_unicast
:
    IPV4_UNICAST NEWLINE
;

rb_timers
:
    TIMERS BGP uint32 uint32 NEWLINE
;

