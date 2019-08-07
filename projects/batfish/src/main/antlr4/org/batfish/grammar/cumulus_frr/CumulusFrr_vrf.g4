parser grammar CumulusFrr_vrf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_vrf
:
  VRF name = word NEWLINE
  (
    sv_vni
    | null_rest_of_line*
  )
  EXIT_VRF
;

sv_vni
:
  VNI vni = vni_number NEWLINE
;
