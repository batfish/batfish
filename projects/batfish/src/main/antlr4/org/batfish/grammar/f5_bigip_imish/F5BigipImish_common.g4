parser grammar F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

ip_prefix
:
  IP_PREFIX
;

line_action
:
  DENY
  | PERMIT
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

uint32
:
  d = DEC
  {isUint32($d)}?

;

word
:
  ~NEWLINE
;