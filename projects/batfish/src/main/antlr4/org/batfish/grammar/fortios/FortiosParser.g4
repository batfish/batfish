parser grammar FortiosParser;

import
  Fortios_common,
  Fortios_system;

options {
  superClass = 'org.batfish.grammar.fortios.parsing.FortiosBaseParser';
  tokenVocab = FortiosLexer;
}

fortios_configuration: statement+ EOF;

statement: s_config;

s_config: CONFIG c_system END NEWLINE;

