parser grammar CumulusFrrParser;

options {
  superClass =
  'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseParser';
  tokenVocab = CumulusFrrLexer;
}

cumulus_frr_configuration
:
  statement* EOF
;

statement
:
  FRR_VERSION_LINE NEWLINE
;

