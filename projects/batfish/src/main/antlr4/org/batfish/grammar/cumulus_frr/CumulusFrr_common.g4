parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

word
:
  WORD
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;