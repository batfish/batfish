parser grammar FlatVyos_vpn;

import FlatVyos_common;

options {
   tokenVocab = FlatVyosLexer;
}

s_vpn
:
   VPN s_null_filler
;
