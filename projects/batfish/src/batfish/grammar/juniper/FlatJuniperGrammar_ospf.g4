parser grammar FlatJuniperGrammar_ospf;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

protocols_ospf_statement
:
   PROTOCOLS OSPF ~NEWLINE*
;
