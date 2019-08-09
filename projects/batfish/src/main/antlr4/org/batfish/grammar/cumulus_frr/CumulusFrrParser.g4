parser grammar CumulusFrrParser;

import CumulusFrr_common, CumulusFrr_routemap, CumulusFrr_vrf;

options {
  superClass =
  'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseParser';
  tokenVocab = CumulusFrrLexer;
}

// goal rule
cumulus_frr_configuration
:
  statement+ EOF
;

// other rules
statement
:
  FRR_VERSION_LINE NEWLINE
  | s_vrf
  | s_routemap
;

