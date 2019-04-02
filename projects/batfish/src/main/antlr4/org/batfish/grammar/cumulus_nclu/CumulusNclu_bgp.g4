parser grammar CumulusNclu_bgp;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

a_bgp
:
  BGP
  (
    b_common
    | b_vrf
  )
;

b_common
:
  b_autonomous_system
  | b_ipv4_unicast
  | b_l2vpn
  | b_neighbor
  | b_router_id
;

b_autonomous_system
:
  AUTONOMOUS_SYSTEM as = uint32 NEWLINE
;

b_ipv4_unicast
:
  IPV4 UNICAST
  (
    bi4_network
    | bi4_redistribute_connected
    | bi4_redistribute_static
  )
;

bi4_network
:
  NETWORK network = ip_prefix NEWLINE
;

bi4_redistribute_connected
:
  REDISTRIBUTE CONNECTED
  (
    ROUTE_MAP rm = word
  )? NEWLINE
;

bi4_redistribute_static
:
  REDISTRIBUTE STATIC
  (
    ROUTE_MAP rm = word
  )? NEWLINE
;

b_l2vpn
:
  L2VPN EVPN
  (
    ble_advertise_all_vni
    | ble_advertise_default_gw
    | ble_advertise_ipv4_unicast
    | ble_neighbor
  )
;

ble_advertise_all_vni
:
  ADVERTISE_ALL_VNI NEWLINE
;

ble_advertise_default_gw
:
  ADVERTISE_DEFAULT_GW NEWLINE
;

ble_advertise_ipv4_unicast
:
  ADVERTISE IPV4 UNICAST NEWLINE
;

ble_neighbor
:
  NEIGHBOR name = word blen_activate
;

blen_activate
:
  ACTIVATE NEWLINE
;

b_neighbor
:
  NEIGHBOR name = word bn_interface
;

bn_interface
:
  INTERFACE bni_remote_as_external
;

bni_remote_as_external
:
  REMOTE_AS EXTERNAL NEWLINE
;

b_router_id
:
  ROUTER_ID id = ip_address NEWLINE
;

b_vrf
:
  VRF name = word b_common
;
