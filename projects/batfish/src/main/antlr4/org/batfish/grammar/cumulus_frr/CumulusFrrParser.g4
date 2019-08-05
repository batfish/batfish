parser grammar CumulusFrrParser;

import CumulusFrr_common, CumulusFrr_vrf;

options {
  superClass =
  'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseParser';
  tokenVocab = CumulusFrrLexer;
}

// goal rule

cumulus_frr_configuration
:
  NEWLINE* statement+ NEWLINE* EOF
;

// other rules

statement
:
  s_vrf
;
