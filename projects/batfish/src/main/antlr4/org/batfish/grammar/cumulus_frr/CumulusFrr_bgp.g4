parser grammar CumulusFrr_bgp;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_bgp
:
  ROUTER BGP autonomous_system (VRF vrf_name)? NEWLINE
  (
    sb_address_family
  | sb_always_compare_med
  | sb_bgp
  | sb_neighbor
  | sb_no
  )*
;

sb_bgp
:
  BGP
  (
    sbb_bestpath
    | sbb_confederation
    | sbb_log_neighbor_changes
    | sbb_router_id
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
  NEWLINE
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

sbbb_aspath_multipath_relax
:
  AS_PATH MULTIPATH_RELAX NEWLINE
;

sbb_router_id
:
  ROUTER_ID IP_ADDRESS NEWLINE
;

sb_neighbor
:
  NEIGHBOR (sbn_ip | sbn_name) NEWLINE
;

sb_address_family
:
  ADDRESS_FAMILY sbaf
  (EXIT_ADDRESS_FAMILY NEWLINE)?
;

sbaf
:
    sbaf_ipv4_unicast
  | sbaf_l2vpn_evpn
;

sbaf_ipv4_unicast
:
  IPV4 UNICAST NEWLINE
  (
    sbafi_aggregate_address
    // Skeptical that max-paths belongs here.
    // Adding for now to prevent jumping out of parser context.
    | sbafi_maximum_paths
    | sbafi_network
    | sbafi_neighbor
    | sbafi_redistribute
  )*
;

sbaf_l2vpn_evpn
:
  L2VPN EVPN NEWLINE
  sbafl_statement*
;

sbafl_statement
:
  sbafls_advertise_all_vni
| sbafls_advertise_ipv4_unicast
| sbafls_neighbor_activate
;

sbafi_aggregate_address
:
  AGGREGATE_ADDRESS IP_PREFIX SUMMARY_ONLY? NEWLINE
;

sbafi_maximum_paths
:
  MAXIMUM_PATHS num = uint32 NEWLINE
;

sbafi_network
:
  NETWORK IP_PREFIX NEWLINE
;

sbafi_redistribute
:
  REDISTRIBUTE (STATIC | CONNECTED) (ROUTE_MAP route_map_name)? NEWLINE
;


sbafls_advertise_all_vni
:
  ADVERTISE_ALL_VNI NEWLINE
;

sbafls_advertise_ipv4_unicast
:
  ADVERTISE IPV4 UNICAST NEWLINE
;

sbafls_neighbor_activate
:
  NEIGHBOR neighbor = (IP_ADDRESS | WORD) ACTIVATE NEWLINE
;

sb_always_compare_med
:
  BGP ALWAYS_COMPARE_MED NEWLINE
;

sbn_ip
:
  ip = IP_ADDRESS sbn_property
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
  PEER_GROUP
;

sbn_property
:
  sbnp_description
| sbnp_ebgp_multihop
| sbnp_peer_group
| sbnp_bfd
| sbnp_password
| sbnp_remote_as
| sbnp_update_source
;

sbnp_bfd
:
  BFD word*
;

sbnp_description
:
  DESCRIPTION REMARK_TEXT
;

sbnp_ebgp_multihop
:
  EBGP_MULTIHOP num = uint32
;

sbnp_password
:
  PASSWORD REMARK_TEXT
;

sbnp_peer_group
:
  PEER_GROUP name = word
;

sbnp_remote_as
:
  REMOTE_AS (autonomous_system | EXTERNAL | INTERNAL)
;

sbnp_update_source
:
  UPDATE_SOURCE (ip = IP_ADDRESS | name = word)
;

sbafi_neighbor
:
  NEIGHBOR (ip = IP_ADDRESS | name = word)
  (
    sbafin_activate
  | sbafin_allowas_in
  | sbafin_next_hop_self
  | sbafin_route_reflector_client
  | sbafin_send_community
  | sbafin_soft_reconfiguration
  | sbafin_route_map
  )
  NEWLINE
;

sbafin_activate
:
  ACTIVATE
;

sbafin_allowas_in
:
  ALLOWAS_IN count = UINT8
;

sbafin_next_hop_self
:
  NEXT_HOP_SELF
;

sbafin_route_reflector_client
:
  ROUTE_REFLECTOR_CLIENT
;

sbafin_send_community
:
  SEND_COMMUNITY EXTENDED?
;

sbafin_soft_reconfiguration
:
  SOFT_RECONFIGURATION INBOUND
;

sbafin_route_map
:
  ROUTE_MAP name=word (IN | OUT)
;

sbn_bfd
:
  BFD word*
;

sbn_password
:
  PASSWORD REMARK_TEXT
;

sbnobd_ipv4_unicast
:
    IPV4_UNICAST
;
