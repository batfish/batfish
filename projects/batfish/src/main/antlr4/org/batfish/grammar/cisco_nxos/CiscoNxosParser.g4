parser grammar CiscoNxosParser;

import CiscoNxos_common;

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
  | s_null
;

s_hostname
:
  HOSTNAME hostname = word NEWLINE
;

s_null
:
  NO?
  (
    FEATURE
  ) null_rest_of_line
;

