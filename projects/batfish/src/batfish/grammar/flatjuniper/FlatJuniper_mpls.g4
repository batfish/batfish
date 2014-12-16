parser grammar FlatJuniper_mpls;

import FlatJuniperCommonParser;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols_mpls
:
   MPLS s_null_filler
;
