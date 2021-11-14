parser grammar Frr_bgp;

import Frr_common;

options {
  tokenVocab = FrrLexer;
}

s_bgp
:
  ROUTER BGP autonomous_system (VRF vrf_name)? NEWLINE
  bgp_inner*
;

bgp_inner
:
  sb_address_family
| sb_always_compare_med
| sb_bgp
| sb_neighbor
| sb_network
| sb_no
| sb_redistribute
| sb_timers
| sbafi_neighbor
;

sb_bgp
:
  BGP
  (
    sbb_bestpath
    | sbb_confederation
    | sbb_log_neighbor_changes
    | sbb_router_id
    | sbb_cluster_id
    | sbb_max_med_administrative
    | sbb_listen
  )
;

sb_no
:
  NO
  (
    sbno_bgp
  )
;


sbno_bgp
:
  BGP
  (
     sbnob_default
  )
;

sbnob_default
:
  DEFAULT
  (
     sbnobd_ipv4_unicast
  )
;

sbb_confederation
:
  CONFEDERATION IDENTIFIER id = uint32 NEWLINE
;

sbb_bestpath
:
  BESTPATH
  (
    sbbb_aspath_multipath_relax
  )
;

sbb_log_neighbor_changes
:
  LOG_NEIGHBOR_CHANGES NEWLINE
;

sbb_max_med_administrative
:
   MAX_MED ADMINISTRATIVE (med = uint32)? NEWLINE
;

sbbb_aspath_multipath_relax
:
  AS_PATH MULTIPATH_RELAX NEWLINE
;

sb_redistribute
:
  REDISTRIBUTE bgp_redist_type (ROUTE_MAP route_map_name)? NEWLINE
;

sbb_router_id
:
  ROUTER_ID IP_ADDRESS NEWLINE
;

sbb_cluster_id
:
  CLUSTER_ID IP_ADDRESS NEWLINE
;

sb_neighbor
:
  NEIGHBOR (sbn_ip | sbn_ip6 | sbn_name)
;

sb_address_family
:
  ADDRESS_FAMILY sbaf
  (EXIT_ADDRESS_FAMILY NEWLINE)?
;

sbaf
:
    sbaf_ipv4_unicast
  | sbaf_ipv6_unicast
  | sbaf_l2vpn_evpn
;

sbaf_ipv4_unicast
:
  IPV4 UNICAST NEWLINE
  sbafi_inner*
;

sbafi_inner
:
  sbafi_aggregate_address
| sbafi_import
| sbafi_maximum_paths
| sbafi_network
| sbafi_neighbor
| sbafi_no
| sbafi_redistribute
;

sbaf_ipv6_unicast
:
  IPV6 UNICAST NEWLINE
  sbafi6_inner*
;

sbafi6_inner
:
  sbafi6_import
| sbafi6_maximum_paths
| sbafi6_null_tail
;

sbafi6_maximum_paths
:
  MAXIMUM_PATHS (IBGP)? num = uint32 NEWLINE
;

sbafi6_import
:
  IMPORT VRF (vrf_name | ROUTE_MAP route_map_name) NEWLINE
;

sbafi6_null_tail
:
  (
     // there are likely others but haven't seen examples yet, so leaving for later
     NEIGHBOR
     | NO
     | REDISTRIBUTE
  ) null_rest_of_line
;


sbaf_l2vpn_evpn
:
  L2VPN EVPN NEWLINE
  sbafl_inner*
;

sbafl_inner
:
  sbafl_advertise
| sbafl_advertise_all_vni
| sbafl_advertise_default_gw
| sbafl_neighbor
;

sbafl_neighbor
:
   NEIGHBOR neighbor = (IP_ADDRESS | WORD)
   (
      sbafln_activate
      | sbafln_route_map
      | sbafln_route_reflector_client
   )
;

sbafi_aggregate_address
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

sbafi_import
:
  IMPORT VRF (vrf_name | ROUTE_MAP route_map_name) NEWLINE
;

sbafi_maximum_paths
:
  MAXIMUM_PATHS (IBGP)? num = uint32 NEWLINE
;

sbafi_network
:
  NETWORK network = prefix (ROUTE_MAP rm = route_map_name)? NEWLINE
;

sbafi_redistribute
:
  REDISTRIBUTE bgp_redist_type (ROUTE_MAP route_map_name)? NEWLINE
;

sbafl_advertise
:
  ADVERTISE
  (
     sbafla_ipv4_unicast
     | sbafla_ipv6_unicast
  )
;

sbafla_ipv4_unicast
:
  IPV4 UNICAST (ROUTE_MAP rm = route_map_name)? NEWLINE
;

sbafla_ipv6_unicast
:
  IPV6 UNICAST (ROUTE_MAP rm = route_map_name)? NEWLINE
;

sbafl_advertise_all_vni
:
  ADVERTISE_ALL_VNI NEWLINE
;

sbafl_advertise_default_gw
:
  ADVERTISE_DEFAULT_GW NEWLINE
;

sbafln_activate
:
  ACTIVATE NEWLINE
;

sbafln_route_map
:
  ROUTE_MAP name=word (IN | OUT) NEWLINE
;

sbafln_route_reflector_client
:
   ROUTE_REFLECTOR_CLIENT NEWLINE
;

sb_always_compare_med
:
  BGP ALWAYS_COMPARE_MED NEWLINE
;

sbn_ip
:
  ip = IP_ADDRESS sbn_property
;

sbn_ip6
:
   ip6 = IPV6_ADDRESS sbn_property
;

sbn_name
:
  name = word
    (
      sbn_interface       // set an interface neighbor property
    | sbn_peer_group_decl // declare a new peer group
    | sbn_property        // set a peer-group property
    // Nothing else should go in here. New properties should go in sbn_property
    )
;

sbn_interface
:
  INTERFACE sbn_property
;

sbn_peer_group_decl
:
  PEER_GROUP NEWLINE
;

sbn_property
:
 sbnp_advertisement_interval
| sbnp_bfd
| sbnp_description
| sbnp_ebgp_multihop
| sbnp_local_as
| sbnp_password
| sbnp_peer_group
| sbnp_remote_as
| sbnp_timers
| sbnp_update_source
;

sbnp_advertisement_interval
:
   ADVERTISEMENT_INTERVAL uint32 NEWLINE
;


sbnp_bfd
:
  BFD word* NEWLINE
;

sbnp_description
:
  DESCRIPTION REMARK_TEXT NEWLINE
;

sbnp_ebgp_multihop
:
  EBGP_MULTIHOP (num = uint32)? NEWLINE
;

sbnp_local_as
:
  LOCAL_AS asn = autonomous_system (NO_PREPEND REPLACE_AS?)? NEWLINE
;

sbnp_password
:
  PASSWORD REMARK_TEXT NEWLINE
;

sbnp_peer_group
:
  PEER_GROUP name = word NEWLINE
;

sbnp_remote_as
:
  REMOTE_AS (autonomous_system | EXTERNAL | INTERNAL) NEWLINE
;

sbnp_timers
:
  TIMERS CONNECT uint32 NEWLINE
;

sbnp_update_source
:
  UPDATE_SOURCE (ip = IP_ADDRESS | name = word) NEWLINE
;

sb_network
:
  NETWORK network = prefix (ROUTE_MAP rm = route_map_name)? NEWLINE
;

sbafi_neighbor
:
  NEIGHBOR (ip = IP_ADDRESS | name = word)
  (
    sbafin_activate
  | sbafin_allowas_in
  | sbafin_default_originate
  | sbafin_next_hop_self
  | sbafin_route_reflector_client
  | sbafin_send_community
  | sbafin_soft_reconfiguration
  | sbafin_route_map
  )
;

sbafi_no
:
  NO sbafino_neighbor
;

sbafino_neighbor
:
  NEIGHBOR (ipv4=IP_ADDRESS | ipv6=IPV6_ADDRESS | name = word)
  (
    sbafinon_activate
  )
;

sbafinon_activate
:
  ACTIVATE NEWLINE
;

sbafin_activate
:
  ACTIVATE NEWLINE
;

sbafin_allowas_in
:
  ALLOWAS_IN count = UINT8 NEWLINE
;

sbafin_default_originate
:
  DEFAULT_ORIGINATE NEWLINE
;

sbafin_next_hop_self
:
  NEXT_HOP_SELF (FORCE | ALL)? NEWLINE
;

sbafin_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT NEWLINE
;

sbafin_send_community
:
  SEND_COMMUNITY EXTENDED? NEWLINE
;

sbafin_soft_reconfiguration
:
  SOFT_RECONFIGURATION INBOUND NEWLINE
;

sbafin_route_map
:
  ROUTE_MAP name=word (IN | OUT) NEWLINE
;

sbb_listen
:
  LISTEN
  (
    sbbl_limit
    | sbbl_range
  )
;

sbbl_limit
:
  LIMIT uint32 NEWLINE
;

sbbl_range
:
  RANGE
   (
      prefix
      | prefix6
   )
   PEER_GROUP name = word
  NEWLINE
;

sbnobd_ipv4_unicast
:
    IPV4_UNICAST NEWLINE
;

sb_timers
:
    TIMERS BGP uint32 uint32 NEWLINE
;

