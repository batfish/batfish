parser grammar CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

uint32
:
  DEC
;

uint64
:
  DEC
;

word
:
  ~NEWLINE
;
