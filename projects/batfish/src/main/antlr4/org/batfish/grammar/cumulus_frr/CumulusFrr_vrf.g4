parser grammar CumulusFrr_vrf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_vrf
:
  VRF name = word NEWLINE
    sv_route
    | null_rest_of_line*
  EXIT_VRF
;

sv_route
:
  IP ROUTE prefix ip_address NEWLINE
;
