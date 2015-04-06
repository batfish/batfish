parser grammar FlatJuniper_isis;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

isist_null
:
   (
      LSP_LIFETIME
   ) s_null_filler
;

s_protocols_isis
:
   ISIS s_protocols_isis_tail
;

s_protocols_isis_tail
:
   isist_null
;