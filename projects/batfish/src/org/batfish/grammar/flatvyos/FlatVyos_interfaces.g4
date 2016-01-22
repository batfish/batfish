parser grammar FlatVyos_interfaces;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

s_interfaces
:
   INTERFACES s_null_filler
;