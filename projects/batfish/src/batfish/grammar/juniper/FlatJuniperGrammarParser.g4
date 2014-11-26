parser grammar FlatJuniperGrammarParser;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperGrammarLexer;
}

@header {
package batfish.grammar.juniper;
}

color_statement
:
   COLOR color = DEC
;

direction
:
   INPUT | OUTPUT
;

family
:
   INET
   | MPLS
;

family_header
:
   FAMILY family
;

family_statement
:
   family_header family_tail
;

family_tail
:
   filter_statement
;

filter_header
:
   FILTER direction
;

filter_statement
:
   filter_header filter_tail
;

filter_tail
:
   VARIABLE
;

groups_header
:
   GROUPS name = VARIABLE
;

groups_statement
:
   groups_header groups_tail
;

groups_tail
:
   interfaces_statement
;

interfaces_header
:
   INTERFACES
   (
      wildcard
      | name = VARIABLE
   )
;

interfaces_statement
:
   interfaces_header interfaces_tail
;

interfaces_tail
:
   unit_statement
;

flat_juniper_configuration
:
   set_statement+ EOF
;

policy_options_statement
:
   POLICY_OPTIONS policy_options_tail
;

policy_options_tail
:
   policy_statement_statement
;

policy_statement_header
:
   POLICY_STATEMENT
   (
      wildcard
      | name = VARIABLE
   )
;

policy_statement_statement
:
   policy_statement_header policy_statement_tail
;

policy_statement_tail
:
   term_statement
;

set_statement
:
   SET set_tail NEWLINE
;

set_tail
:
   groups_statement
;

term_header
:
   TERM VARIABLE
;

term_statement
:
   term_header term_tail
;

term_tail
:
   then_statement
;

then_statement
:
   THEN then_tail
;

then_tail
:
   color_statement
;

unit_header
:
   UNIT
   (
      wildcard
      | name = VARIABLE
   )
;

unit_statement
:
   unit_header unit_tail
;

unit_tail
:
   family_statement
;

version_statement
:
   VERSION M_Version_VERSION_STRING
;

wildcard
:
   WILDCARD_OPEN WILDCARD WILDCARD_CLOSE
;

