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
  IP ogip_address
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

object_group_sequence
:
// 1-4294967295
  uint32
;
