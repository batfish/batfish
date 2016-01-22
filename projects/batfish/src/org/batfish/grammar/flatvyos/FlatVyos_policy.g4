parser grammar FlatVyos_policy;

import
FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

s_policy
:
   POLICY s_null_filler
;