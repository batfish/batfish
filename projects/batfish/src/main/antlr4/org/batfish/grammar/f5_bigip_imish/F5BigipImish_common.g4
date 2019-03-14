parser grammar F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

line_action
:
  DENY
  | PERMIT
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

word
:
  ~NEWLINE
;