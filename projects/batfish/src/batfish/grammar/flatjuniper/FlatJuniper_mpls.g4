parser grammar FlatJuniper_mpls;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_protocols_mpls
:
   MPLS s_null_filler
;
