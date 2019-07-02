parser grammar CiscoNxosParser;

import CiscoNxos_common, CiscoNxos_interface, CiscoNxos_vlan;

options {
  superClass = 'org.batfish.grammar.cisco_nxos.parsing.CiscoNxosBaseParser';
  tokenVocab = CiscoNxosLexer;
}

cisco_nxos_configuration
:
  statement+ EOF
;

statement
:
  s_hostname
  | s_interface
  | s_null
  | s_vlan
;

s_hostname
:
  HOSTNAME hostname = subdomain_name NEWLINE
;

s_null
:
  NO?
  (
    FEATURE
  ) null_rest_of_line
;

