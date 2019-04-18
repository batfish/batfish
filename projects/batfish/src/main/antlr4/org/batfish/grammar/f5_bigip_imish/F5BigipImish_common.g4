parser grammar F5BigipImish_common;

options {
  tokenVocab = F5BigipImishLexer;
}

ip_prefix
:
  IP_PREFIX
;

ip_prefix_length
:
  d = DEC
  {isIpPrefixLength($d)}?

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