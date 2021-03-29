parser grammar CiscoNxos_object_group;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_object_group
:
  OBJECT_GROUP og_ip
;

og_ip
:
  IP
  (
    ogip_address
    | ogip_port
  )
;

ogip_address
:
  ADDRESS name = object_group_name NEWLINE ogipa_line*
;

object_group_name
:
// 1-64 characters
  WORD
;

ogipa_line
:
  seq = object_group_sequence?
  (
    address = ip_address wildcard = ip_address
    | prefix = ip_prefix
    | HOST address = ip_address
  ) NEWLINE
;

ogip_port
:
  PORT name = object_group_name NEWLINE ogipp_line*
;

ogipp_line
:
  seq = object_group_sequence?
  (
    EQ eq = uint16
    | GT gt = uint16  // gt: 0-65534
    | LT lt = uint16  // lt: 1-65535
    | NEQ neq = uint16
    | RANGE range1 = uint16 range2 = uint16  // NX-OS will swap lower and higher
  ) NEWLINE
;

object_group_sequence
:
// 1-4294967295
  uint32
;
