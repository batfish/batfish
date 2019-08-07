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
  IFACE interface_name NEWLINE
  (
    i_address
  | i_address_virtual
  | i_alias
  | i_bond_slaves
  | i_bridge_access
  | i_bridge_ports
  | i_bridge_vids
  | i_clag_id
  | i_clagd_peer_ip
  | i_link_speed
  | i_vlan_id
  | i_vlan_raw_device
  | i_vrf
  | i_vrf_table
  | i_vxlan_id
  | i_vxlan_local_tunnel_ip
  )*
;

i_address
:
  ADDRESS IP_PREFIX NEWLINE
;

i_address_virtual
:
  ADDRESS_VIRTUAL MAC_ADDRESS IP_PREFIX NEWLINE
;

i_alias
:
  ALIAS TEXT NEWLINE
;

i_bond_slaves
:
  BOND_SLAVES interface_name+ NEWLINE
;

i_bridge_access
:
  BRIDGE_ACCESS number NEWLINE
;

i_bridge_ports
:
  BRIDGE_PORTS interface_name+ NEWLINE
;

i_bridge_vids
:
  BRIDGE_VIDS number+ NEWLINE
;

i_clag_id
:
  CLAG_ID number NEWLINE
;

i_clagd_peer_ip
:
 CLAGD_PEER_IP (IP_ADDRESS | LINK_LOCAL) NEWLINE
;

i_link_speed
:
  LINK_SPEED number NEWLINE
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
  VXLAN_LOCAL_TUNNEL_IP IP_ADDRESS NEWLINE
;