parser grammar FlatJuniperGrammar_mpls;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

protocols_mpls_statement
:
   PROTOCOLS MPLS ~NEWLINE*
;
