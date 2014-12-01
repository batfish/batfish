parser grammar FlatJuniperGrammar_ospf;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_protocols_ospf
:
   PROTOCOLS OSPF ~NEWLINE*
;
