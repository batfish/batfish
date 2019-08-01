parser grammar CumulusPortsParser;

options {
  superClass =
  'org.batfish.grammar.cumulus_ports.parsing.CumulusPortsBaseParser';
  tokenVocab = CumulusPortsLexer;
}

cumulus_ports_configuration
:
  // TODO other lines
  start_of_cumulus_interfaces_file
;

start_of_cumulus_interfaces_file
:
    START_OF_CUMULUS_INTERFACES_FILE
;

