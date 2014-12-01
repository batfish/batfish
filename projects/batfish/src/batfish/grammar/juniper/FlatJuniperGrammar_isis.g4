parser grammar FlatJuniperGrammar_isis;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_protocols_isis
:
   PROTOCOLS ISIS ~NEWLINE*
;
