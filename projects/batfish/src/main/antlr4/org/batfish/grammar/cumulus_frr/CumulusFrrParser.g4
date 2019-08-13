parser grammar CumulusFrrParser;

import CumulusFrr_bgp, CumulusFrr_common, CumulusFrr_ip_community_list, CumulusFrr_routemap, CumulusFrr_vrf;

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
  | s_bgp
  | s_vrf
  | s_routemap
  | s_ip
;

s_ip
:
  IP ip_community_list
;
