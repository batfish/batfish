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