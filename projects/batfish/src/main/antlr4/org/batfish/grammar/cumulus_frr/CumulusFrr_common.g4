parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_address
:
  IP_ADDRESS
  | SUBNET_MASK
;

ip_community_list_name
:
// 1-63 characters
  WORD
;

line_action
:
  deny = DENY
  | permit = PERMIT
;

literal_standard_community
:
  high = uint16 COLON low = uint16
;

prefix
:
  IP_PREFIX
;

vni_number
:
  v = uint32
  {isVniNumber($v.ctx)}?

;

uint16
:
  UINT8
  | UINT16
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