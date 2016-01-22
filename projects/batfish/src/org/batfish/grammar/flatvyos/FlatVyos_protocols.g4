parser grammar FlatVyos_protocols;

import
FlatVyos_common, FlatVyos_bgp;

options {
   tokenVocab = FlatVyosLexer;
}

s_protocols
:
   PROTOCOLS s_protocols_tail
;

s_protocols_static
:
   STATIC s_null_filler
;

s_protocols_tail
:
   s_protocols_bgp
   | s_protocols_static
   //| s_protocols_null
;

/*
s_protocols_null
:
   (
   ) s_null_filler
;
*/
