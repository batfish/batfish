parser grammar CumulusInterfacesParser;

options {
  superClass =
  'org.batfish.grammar.cumulus_interfaces.parsing.CumulusInterfacesBaseParser';
  tokenVocab = CumulusInterfacesLexer;
}

import CumulusInterfaces_common;

cumulus_interfaces_configuration
:
  statement* EOF
;

statement
:
  s_auto
  | s_iface
;

s_auto
:
  AUTO interface_name NEWLINE
;

s_iface
:
  (IFACE | INTERFACE) interface_name
  (
    si_inet
    | si_no_inet
  )
;

si_inet
:
  INET
  (
    LOOPBACK NEWLINE i_property*
    | DHCP NEWLINE i_property*
    | MANUAL NEWLINE i_property*
    | STATIC NEWLINE i_property*
  )
;

si_no_inet
:
    NEWLINE i_property*
;

i_property
:
    i_address
  | i_address_virtual
  | i_alias
  | i_bond_lacp_bypass_allow
  | i_bond_lacp_rate
  | i_bond_master
  | i_bond_miimon
  | i_bond_min_links
  | i_bond_mode
  | i_bond_slaves
  | i_bond_xmit_hash_policy
  | i_bridge_access
  | i_bridge_arp_nd_suppress
  | i_bridge_learning
  | i_bridge_ports
  | i_bridge_pvid
  | i_bridge_vids
  | i_bridge_vlan_aware
  | i_clag_id
  | i_clagd_backup_ip
  | i_clagd_peer_ip
  | i_clagd_priority
  | i_clagd_sys_mac
  | i_clagd_vxlan_anycast_ip
  | i_gateway
  | i_hwaddress
  | i_link_speed
  | i_link_autoneg
  | i_mstpctl_bpduguard
  | i_mstpctl_portadminedge
  | i_mstpctl_portbpdufilter
  | i_mtu
  | i_post_up
  | i_vlan_id
  | i_vlan_raw_device
  | i_vrf
  | i_vrf_table
  | i_vxlan_id
  | i_vxlan_local_tunnel_ip
;

i_address
:
  IP? ADDRESS
  (
    v4 = interface_address
    | v6 = interface_address6
  ) NEWLINE
;

i_address_virtual
:
  ADDRESS_VIRTUAL MAC_ADDRESS
  (
    v4 = interface_address
    | v6 = interface_address6
  ) NEWLINE
;

i_alias
:
  ALIAS TEXT NEWLINE
;

i_bond_lacp_bypass_allow
:
  BOND_LACP_BYPASS_ALLOW NEWLINE
;

i_bond_lacp_rate
:
  BOND_LACP_RATE NEWLINE
;

i_bond_master
:
   BOND_MASTER NEWLINE
;

i_bond_miimon
:
  BOND_MIIMON NEWLINE
;

i_bond_min_links
:
  BOND_MIN_LINKS NEWLINE
;

i_bond_mode
:
  BOND_MODE NEWLINE
;

i_bond_slaves
:
  BOND_SLAVES interface_name+ NEWLINE
;

i_bond_xmit_hash_policy
:
  BOND_XMIT_HASH_POLICY NEWLINE
;

i_bridge_access
:
  BRIDGE_ACCESS number NEWLINE
;

i_bridge_arp_nd_suppress
:
  BRIDGE_ARP_ND_SUPPRESS NEWLINE
;

i_bridge_learning
:
  BRIDGE_LEARNING NEWLINE
;

i_bridge_ports
:
  BRIDGE_PORTS interface_name+ NEWLINE
;

i_bridge_pvid
:
  BRIDGE_PVID vlan_id NEWLINE
;

i_bridge_vids
:
  BRIDGE_VIDS number_or_range+ NEWLINE
;

i_bridge_vlan_aware
:
  BRIDGE_VLAN_AWARE (YES | NO) NEWLINE
;

i_clag_id
:
  CLAG_ID number NEWLINE
;

i_clagd_backup_ip
:
  CLAGD_BACKUP_IP address (VRF vrf_name)? NEWLINE
;

i_clagd_peer_ip
:
  CLAGD_PEER_IP (address | LINK_LOCAL) NEWLINE
;

i_clagd_priority
:
  CLAGD_PRIORITY number NEWLINE
;

i_clagd_sys_mac
:
  CLAGD_SYS_MAC MAC_ADDRESS NEWLINE
;

i_clagd_vxlan_anycast_ip
:
  CLAGD_VXLAN_ANYCAST_IP address NEWLINE
;

i_gateway
:
  GATEWAY address NEWLINE
;

i_hwaddress
:
  HWADDRESS MAC_ADDRESS NEWLINE
;

i_link_speed
:
  LINK_SPEED number NEWLINE
;

i_link_autoneg
:
  LINK_AUTONEG (ON | OFF) NEWLINE
;

i_mstpctl_bpduguard
:
  MSTPCTL_BPDUGUARD NEWLINE
;

i_mstpctl_portadminedge
:
  MSTPCTL_PORTADMINEDGE NEWLINE
;

i_mstpctl_portbpdufilter
:
  MSTPCTL_PORTBPDUFILTER NEWLINE
;

i_mtu
:
   MTU number NEWLINE
;

i_post_up
:
  POST_UP
  (
     ipu_ip
  )
;

ipu_ip
:
  IP
  (
     ipui_link
     | ipui_route
  )
;

ipui_link
:
   LINK
   (
     ipuil_set
   )
;

ipuil_set
:
   SET NEWLINE
;

ipui_route
:
  ROUTE
  (
     ipuir_add
  )
;

ipuir_add
:
   ADD prefix
   // this rule is more permissive than reality; it allows for multiple occurrences of dev/via
   // we check for conformance in the extractor
   (
     VIA address
     | DEV interface_name
   )+
   NEWLINE
;

i_vlan_id
:
  VLAN_ID number NEWLINE
;

i_vlan_raw_device
:
  VLAN_RAW_DEVICE interface_name NEWLINE
;

i_vrf
:
  VRF vrf_name NEWLINE
;

i_vrf_table
:
  VRF_TABLE vrf_table_name NEWLINE
;

i_vxlan_id
:
  VXLAN_ID number NEWLINE
;

i_vxlan_local_tunnel_ip
:
  VXLAN_LOCAL_TUNNEL_IP address NEWLINE
;
