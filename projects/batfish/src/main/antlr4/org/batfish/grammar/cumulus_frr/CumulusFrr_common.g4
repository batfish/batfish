parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

word
:
  ~NEWLINE
;