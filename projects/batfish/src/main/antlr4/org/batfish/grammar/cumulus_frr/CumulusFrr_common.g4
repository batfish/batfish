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

ip_prefix_list_name
:
// 1-63 chars
  WORD
;

line_action
:
  deny = DENY
  | permit = PERMIT
;

literal_as_path
:
  (
    asns += uint32
  )+
;

literal_standard_community
:
  high = uint16 COLON low = uint16
;

loglevel
:
    ALERTS
    | CRITICAL
    | DEBUGGING
    | EMERGENCIES
    | ERRORS
    | INFORMATIONAL
    | NOTIFICATIONS
    | WARNINGS
;

prefix
:
  IP_PREFIX
;

route_map_name
:
  WORD
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