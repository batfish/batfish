parser grammar FortiosParser;

import
  Fortios_address,
  Fortios_common,
  Fortios_firewall,
  Fortios_interface,
  Fortios_policy,
  Fortios_service,
  Fortios_system,
  Fortios_zone;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseParser';
  tokenVocab = FortiosLexer;
}

fortios_configuration: statement+ EOF;

statement: s_config;

s_config
:
    CONFIG (
        c_system
        | c_firewall
    ) END NEWLINE
;
