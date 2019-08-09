parser grammar CumulusPortsParser;

options {
  superClass =
  'org.batfish.grammar.cumulus_ports.parsing.CumulusPortsBaseParser';
  tokenVocab = CumulusPortsLexer;
}

cumulus_ports_configuration
:
  port_definition* EOF
;

port_definition
:
  PORT EQUALSIGN (disabled|speed) NEWLINE
;

disabled
:
  DISABLED
;

speed
:
  SPEED
;
