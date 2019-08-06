parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

word
:
  (~NEWLINE)*
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;