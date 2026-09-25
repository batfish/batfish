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
      | p_dot1x
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

// https://www.juniper.net/documentation/us/en/software/junos/user-access/topics/topic-map/802-1x-authentication-switching-devices.html
p_dot1x
:
   DOT1X
   (
      pd_authenticator
      | pd_traceoptions_null
   )
;

pd_authenticator
:
   AUTHENTICATOR
   (
      pda_authentication_profile_name
      | pda_interface
   )
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/authenticator-802-1x.html
pda_authentication_profile_name
:
   AUTHENTICATION_PROFILE_NAME name = junos_name
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/authenticator-802-1x.html
pda_interface
:
   INTERFACE id = interface_id
   (
      pdai_authentication_order
      | pdai_mac_radius
      | pdai_reauthentication
      | pdai_server_fail
      | pdai_server_reject_vlan
      | pdai_server_timeout
      | pdai_supplicant
      | pdai_transmit_period
   )
;

pdai_authentication_order
:
   AUTHENTICATION_ORDER (DOT1X | MAC_RADIUS)
;

pdai_mac_radius
:
   MAC_RADIUS AUTHENTICATION_PROTOCOL PAP
;

pdai_reauthentication
:
   REAUTHENTICATION uint16
;

pdai_server_fail
:
   SERVER_FAIL VLAN_NAME junos_name
;

pdai_server_reject_vlan
:
   SERVER_REJECT_VLAN junos_name
;

pdai_server_timeout
:
   SERVER_TIMEOUT uint16
;

pdai_supplicant
:
   SUPPLICANT MULTIPLE
;

pdai_transmit_period
:
   TRANSMIT_PERIOD uint16
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/traceoptions-802-1x.html
pd_traceoptions_null
:
   TRACEOPTIONS null_filler
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

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/rstp-edit-protocols.html
p_rstp
:
   RSTP
   (
      prstp_bridge_priority
      | prstp_interface
   )?
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/bridge-priority-edit-protocols-stp.html
prstp_bridge_priority
:
   BRIDGE_PRIORITY priority = junos_name
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/interface-edit-protocols-stp.html
prstp_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   (
      prstpi_edge_null
      | prstpi_mode_null
      | prstpi_no_root_port_null
      | prstpi_priority_null
   )?
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

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/vlan-edit-protocols-vstp.html
pvstp_vlan
:
   VLAN junos_name pvstpv_interface?
;

pvstpv_interface
:
   INTERFACE (ALL | id = interface_id | wildcard)
   pvstpvi_option?
;

pvstpvi_option
:
   pvstpvi_edge_null
   | pvstpvi_mode_null
   | pvstpvi_no_root_port_null
   | pvstpvi_priority_null
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
   )?
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
