parser grammar F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

ip_spec
:
  ANY
  | prefix = word
;

line_action
:
  DENY
  | PERMIT
;
