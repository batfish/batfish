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
  | sb_neighbor
  | sb_router_id
  )*
;

sb_router_id
:
  BGP ROUTER_ID IP_ADDRESS NEWLINE
;

sb_neighbor
:
  NEIGHBOR (sbn_ip | sbn_name) NEWLINE
;

sb_address_family
:
  ADDRESS_FAMILY sbaf
  EXIT_ADDRESS_FAMILY NEWLINE
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
    sbafi_network
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
| sbnp_remote_as
| sbnp_peer_group
;

sbnp_description
:
  DESCRIPTION REMARK_TEXT
;

sbnp_remote_as
:
  REMOTE_AS (autonomous_system | EXTERNAL | INTERNAL)
;

sbnp_peer_group
:
  PEER_GROUP name = word
;