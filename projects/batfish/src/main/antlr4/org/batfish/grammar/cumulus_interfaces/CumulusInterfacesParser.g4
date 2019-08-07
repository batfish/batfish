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
  | i_bond_slaves
  | i_bridge_ports
  | i_link_speed
  | i_vrf
  | i_vrf_table
  )*
;

i_address
:
  ADDRESS IP_PREFIX NEWLINE
;

i_bond_slaves
:
  BOND_SLAVES interface_name+ NEWLINE
;

i_bridge_ports
:
  BRIDGE_PORTS interface_name+ NEWLINE
;

i_link_speed
:
  LINK_SPEED NUMBER NEWLINE
;

i_vrf
:
  VRF vrf_name NEWLINE
;

i_vrf_table
:
  VRF_TABLE vrf_table_name NEWLINE
;