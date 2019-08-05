parser grammar CumulusPortsParser;

options {
  superClass =
  'org.batfish.grammar.cumulus_ports.parsing.CumulusPortsBaseParser';
  tokenVocab = CumulusPortsLexer;
}

cumulus_ports_configuration
:
  EOF
;


