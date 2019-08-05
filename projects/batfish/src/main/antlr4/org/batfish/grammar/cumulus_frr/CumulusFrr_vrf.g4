parser grammar CumulusFrr_vrf;

options {
  tokenVocab = CumulusFrrLexer;
}

s_vrf
:
  VRF name = word NEWLINE
    null_rest_of_line *
  EXIT_VRF
;

word
:
  ~NEWLINE
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;