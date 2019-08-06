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
  )*
;

i_address
:
  ADDRESS IP_PREFIX NEWLINE
;