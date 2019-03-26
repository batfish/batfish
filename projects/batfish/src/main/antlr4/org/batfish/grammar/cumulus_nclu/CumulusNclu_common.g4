parser grammar CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

glob
:
  glob_range_set
  (
    COMMA glob_range_set
  )*
;

glob_range_set
:
  glob_word
  |
  (
    base_word = numbered_word
    (
      DASH first_interval_end = DEC
    )?
    (
      COMMA range_set
    )?
  )
;

glob_word
:
  ~( NEWLINE | NUMBERED_WORD | DEC )
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

numbered_word
:
  NUMBERED_WORD
;

range
:
  low = DEC
  (
    DASH high = DEC
  )?
;

range_set
:
  range
  (
    COMMA range
  )*
;

uint16
:
  d = DEC
  {isUint16($d)}?

;

uint32
:
  d = DEC
  {isUint32($d)}?

;

vlan_id
:
  v = DEC
  {isVlanId($v)}?

;

vni_number
:
  v = DEC
  {isVniNumber($v)}?

;

word
:
  ~NEWLINE
;
