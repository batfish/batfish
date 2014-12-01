parser grammar FlatJuniperGrammar_policy_options;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

fromt_interface
:
   INTERFACE VARIABLE
;

fromt_protocol
:
   PROTOCOL protocol
;

pot_policy_statement
:
   pot_policy_statement_header pot_policy_statement_tail
;

pot_policy_statement_header
:
   POLICY_STATEMENT
   (
      wildcard
      | name = VARIABLE
   )
;

pot_policy_statement_tail
:
   pst_term
;

protocol
:
   DIRECT
;

pst_term
:
   pst_term_header pst_term_tail
;

pst_term_header
:
   TERM
   (
      wildcard
      | name = VARIABLE
   )
;

pst_term_tail
:
   tt_from
   | tt_then
;

s_policy_options
:
   POLICY_OPTIONS s_policy_options_tail
;

s_policy_options_tail
:
   pot_policy_statement
;

tht_color
:
   COLOR color = DEC
;

tt_from
:
   FROM tt_from_tail
;

tt_from_tail
:
   fromt_interface
   | fromt_protocol
;

tt_then
:
   THEN tt_then_tail
;

tt_then_tail
:
   tht_color
;
