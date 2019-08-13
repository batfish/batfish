parser grammar CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

autonomous_system
:
  uint32
;

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

prefix
:
  IP_PREFIX
;

vni_number
:
  v = uint32
  {isVniNumber($v.ctx)}?

;

vrf_name
:
  word
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