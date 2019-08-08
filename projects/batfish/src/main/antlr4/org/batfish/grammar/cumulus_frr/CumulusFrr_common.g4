parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

vni_number
:
  v = DEC
  {isVniNumber($v)}?

;

word
:
  WORD
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;