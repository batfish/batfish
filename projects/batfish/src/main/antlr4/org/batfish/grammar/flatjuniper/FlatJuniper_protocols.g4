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
      | p_bfd_null
      | p_dcbx_null
      | p_iccp_null
      | p_igmp_null
      | p_igmp_snooping_null
      | p_l2_learning_null
      | p_l2circuit_null
      | p_l2vpn_null
      | p_lacp_null
      | p_layer2_control_null
      | p_ldp_null
      | p_lldp_med_null
      | p_lldp_null
      | p_mld_null
      | p_msdp_null
      | p_mstp_null
      | p_mvpn_null
      | p_neighbor_discovery_null
      | p_oam_null
      | p_pim_null
      | p_router_advertisement_null
      | p_router_discovery_null
      | p_rsvp_null
      | p_sflow_null
      | p_uplink_failure_detection_null
      | p_vrrp_null
      | p_ospf
      | p_ospf3
      | p_rstp
      | p_stp
      | p_vstp
   )
;

p_bfd_null
:
   BFD null_filler
;
p_dcbx_null
:
   DCBX null_filler
;
p_iccp_null
:
   ICCP null_filler
;
p_igmp_null
:
   IGMP null_filler
;
p_igmp_snooping_null
:
   IGMP_SNOOPING null_filler
;
p_l2_learning_null
:
   L2_LEARNING null_filler
;
p_l2circuit_null
:
   L2CIRCUIT null_filler
;
p_l2vpn_null
:
   L2VPN null_filler
;
p_lacp_null
:
   LACP null_filler
;
p_layer2_control_null
:
   LAYER2_CONTROL null_filler
;
p_ldp_null
:
   LDP null_filler
;
p_lldp_null
:
   LLDP null_filler
;
p_lldp_med_null
:
   LLDP_MED null_filler
;
p_mld_null
:
   MLD null_filler
;
p_msdp_null
:
   MSDP null_filler
;
p_mstp_null
:
   MSTP null_filler
;
p_mvpn_null
:
   MVPN null_filler
;
p_neighbor_discovery_null
:
   NEIGHBOR_DISCOVERY null_filler
;
p_oam_null
:
   OAM null_filler
;
p_pim_null
:
   PIM null_filler
;
p_router_advertisement_null
:
   ROUTER_ADVERTISEMENT null_filler
;
p_router_discovery_null
:
   ROUTER_DISCOVERY null_filler
;
p_rsvp_null
:
   RSVP null_filler
;
p_sflow_null
:
   SFLOW null_filler
;
p_uplink_failure_detection_null
:
   UPLINK_FAILURE_DETECTION null_filler
;
p_vrrp_null
:
   VRRP null_filler
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

