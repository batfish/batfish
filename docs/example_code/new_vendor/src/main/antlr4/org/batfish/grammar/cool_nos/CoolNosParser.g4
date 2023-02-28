parser grammar CoolNosParser;

import
  CoolNos_common,
  CoolNos_static_routes,
  CoolNos_system;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = CoolNosLexer;
}

cool_nos_configuration
:
  NEWLINE?
  statement+ EOF
;

statement
:
  s_line
  | s_log_null
  | s_static_routes
  | s_system
;

s_line
:
  LINE VTY NEWLINE
;

s_log_null
:
  LOG SYSLOG NEWLINE
;
