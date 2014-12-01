parser grammar FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_apply_groups
:
   APPLY_GROUPS name = VARIABLE
;

s_description
:
   DESCRIPTION description = M_Description_DESCRIPTION?
;

wildcard
:
   WILDCARD_OPEN WILDCARD WILDCARD_CLOSE
;
