parser grammar CumulusFrr_vrf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_vrf
:
  VRF name = word NEWLINE
    null_rest_of_line *
  EXIT_VRF
;

