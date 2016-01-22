parser grammar FlatVyos_bgp;

import
FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

s_protocols_bgp
:
   BGP s_null_filler
;