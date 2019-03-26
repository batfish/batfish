parser grammar CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

glob
:
  glob_range
  (
    COMMA glob_range
  )*
;

glob_range
:
  glob_word
  |
  (
    base_word = numbered_word
    (
      DASH first_interval_end = DEC
    )?
    (
      COMMA range
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
  subrange
  (
    COMMA subrange
  )*
;

subrange
:
  low = DEC
  (
    DASH high = DEC
  )?
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
