parser grammar FlatJuniperGrammar_policy_options;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

color_tht
:
   COLOR color = DEC
;

from_tt
:
   FROM from_tt_tail
;

from_tt_tail
:
   interface_from_tail
   | protocol_from_tail
;

interface_from_tail
:
   INTERFACE VARIABLE
;

policy_options_statement
:
   POLICY_OPTIONS policy_options_tail
;

policy_options_tail
:
   policy_statement_pot
;

policy_statement_pot_header
:
   POLICY_STATEMENT
   (
      wildcard
      | name = VARIABLE
   )
;

policy_statement_pot
:
   policy_statement_pot_header policy_statement_pot_tail
;

policy_statement_pot_tail
:
   term_pst
;

protocol
:
   DIRECT
;

protocol_from_tail
:
   PROTOCOL protocol
;

term_pst_header
:
   TERM
   (
      wildcard
      | name = VARIABLE
   )
;

term_pst
:
   term_pst_header term_pst_tail
;

term_pst_tail
:
   from_tt
   | then_tt
;

then_tt
:
   THEN then_tt_tail
;

then_tt_tail
:
   color_tht
;

