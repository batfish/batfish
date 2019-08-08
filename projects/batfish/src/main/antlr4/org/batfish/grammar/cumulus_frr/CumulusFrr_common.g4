parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_address
:
  IP_ADDRESS
  | SUBNET_MASK
;

line_action
:
  deny = DENY
  | permit = PERMIT
;

prefix
:
  IP_PREFIX
;

vni_number
:
  v = DEC
  {isVniNumber($v)}?

;

uint32
:
  UINT8
  | UINT16
  | UINT32
;

word
:
  WORD
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

