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
  s_static_routes
  | s_system
;
