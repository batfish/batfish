parser grammar FlatJuniperGrammar_isis;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

s_protocols_isis
:
   ISIS s_null_filler
;
