parser grammar FlatJuniper_isis;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols_isis
:
   ISIS s_null_filler
;
