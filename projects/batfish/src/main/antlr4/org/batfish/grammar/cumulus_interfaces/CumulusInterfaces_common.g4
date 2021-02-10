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

address
:
  IP_ADDRESS
;

prefix
:
  IP_PREFIX
;

interface_address
:
  addr_32 = address
  | addr_mask = prefix
;

address6
:
  IPV6_ADDRESS
;

prefix6
:
  IPV6_PREFIX
;

interface_address6
:
  addr_128 = address6
  | addr_mask = prefix6
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