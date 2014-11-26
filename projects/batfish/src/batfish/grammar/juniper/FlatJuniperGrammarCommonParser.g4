parser grammar FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

apply_groups_statement
:
   APPLY_GROUPS name=VARIABLE
;

description_statement
:
   DESCRIPTION description = DOUBLE_QUOTED_STRING
;

wildcard
:
   WILDCARD_OPEN WILDCARD WILDCARD_CLOSE
;
