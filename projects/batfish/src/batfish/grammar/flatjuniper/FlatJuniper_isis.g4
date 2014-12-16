parser grammar FlatJuniper_isis;

import FlatJuniperCommonParser;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols_isis
:
   ISIS s_null_filler
;
