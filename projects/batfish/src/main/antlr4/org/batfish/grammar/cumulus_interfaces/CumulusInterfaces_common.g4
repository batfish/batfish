parser grammar CumulusInterfaces_common;

options {
  tokenVocab = CumulusInterfacesLexer;
}

interface_name
:
  WORD
;

number
:
  NUMBER
;

number_or_range
:
  lo = number (DASH hi = number)?
;

vlan_id
:
  v = NUMBER
  {isVlanId($v)}?
;

vrf_name
:
  WORD
;

vrf_table_name
:
  WORD
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;