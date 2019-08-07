parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_address
:
  IP_ADDRESS
  | SUBNET_MASK
;

prefix
:
  IP_PREFIX
;

word
:
  WORD
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

