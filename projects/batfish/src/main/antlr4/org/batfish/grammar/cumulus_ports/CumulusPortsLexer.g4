lexer grammar CumulusPortsLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_ports.parsing.CumulusPortsBaseLexer';
}

NEWLINE
:
  F_Newline+
;

START_OF_CUMULUS_INTERFACES_FILE
:
  '# This file describes the network interfaces'
;

// Fragments

fragment
F_Newline
:
  [\n\r]
;