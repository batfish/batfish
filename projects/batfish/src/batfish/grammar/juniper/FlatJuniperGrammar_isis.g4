parser grammar FlatJuniperGrammar_isis;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

protocols_isis_statement
:
   PROTOCOLS ISIS ~NEWLINE*
;
