parser grammar CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

word
:
  ~NEWLINE
;
