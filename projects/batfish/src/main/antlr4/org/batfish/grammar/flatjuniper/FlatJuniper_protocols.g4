parser grammar FlatJuniper_protocols;

import
FlatJuniper_common, FlatJuniper_bgp, FlatJuniper_evpn, FlatJuniper_isis, FlatJuniper_mpls, FlatJuniper_ospf;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols
:
   PROTOCOLS
   (
      // empty protocol is valid
      | apply
      | p_bgp
      | p_connections
      | p_evpn
      | p_isis
      | p_mpls
      | p_null
      | p_ospf
      | p_ospf3
      | p_rstp
      | p_stp
      | p_vstp
   )
;

p_null
:
   (
      BFD
      | DCBX
      | ICCP
      | IGMP
      | IGMP_SNOOPING
      | L2_LEARNING
      | L2CIRCUIT
      | L2VPN
      | LACP
      | LAYER2_CONTROL
      | LDP
      | LLDP
      | LLDP_MED
      | MLD
      | MSDP
      | MSTP
      | MVPN
      | NEIGHBOR_DISCOVERY
      | OAM
      | PIM
      | ROUTER_ADVERTISEMENT
      | ROUTER_DISCOVERY
      | RSVP
      | SFLOW
      | UPLINK_FAILURE_DETECTION
      | VRRP
   ) null_filler
;

p_rstp
:
   RSTP prstp_interface
;

prstp_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   (
      prstpi_edge_null
      | prstpi_mode_null
      | prstpi_no_root_port_null
      | prstpi_priority_null
   )
;

prstpi_edge_null
:
   EDGE
;

prstpi_mode_null
:
   MODE null_filler
;

prstpi_no_root_port_null
:
   NO_ROOT_PORT
;

prstpi_priority_null
:
   PRIORITY null_filler
;

p_stp
:
   STP pstp_interface
;

pstp_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   (
      pstpi_edge_null
      | pstpi_mode_null
      | pstpi_no_root_port_null
      | pstpi_priority_null
   )
;

pstpi_edge_null
:
   EDGE
;

pstpi_mode_null
:
   MODE null_filler
;

pstpi_no_root_port_null
:
   NO_ROOT_PORT
;

pstpi_priority_null
:
   PRIORITY null_filler
;

p_vstp
:
   VSTP
   (
      pvstp_vlan
      | pvstp_interface
   )
;

pvstp_vlan
:
   VLAN junos_name pvstpv_interface
;

pvstpv_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   (
      pvstpvi_edge_null
      | pvstpvi_mode_null
      | pvstpvi_no_root_port_null
      | pvstpvi_priority_null
   )
;

pvstpvi_edge_null
:
   EDGE
;

pvstpvi_mode_null
:
   MODE null_filler
;

pvstpvi_no_root_port_null
:
   NO_ROOT_PORT
;

pvstpvi_priority_null
:
   PRIORITY null_filler
;

pvstp_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   (
      pvstpi_edge_null
      | pvstpi_mode_null
      | pvstpi_no_root_port_null
      | pvstpi_priority_null
   )
;

pvstpi_edge_null
:
   EDGE
;

pvstpi_mode_null
:
   MODE null_filler
;

pvstpi_no_root_port_null
:
   NO_ROOT_PORT
;

pvstpi_priority_null
:
   PRIORITY null_filler
;

