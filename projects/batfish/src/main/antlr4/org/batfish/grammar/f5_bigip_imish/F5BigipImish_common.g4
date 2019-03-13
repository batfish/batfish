parser grammar F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

line_action
:
  DENY
  | PERMIT
;

word
:
  ~NEWLINE
;