parser grammar CoolNos_common;

string
:
  STRING
  | DOUBLE_QUOTE STRING DOUBLE_QUOTE
;

interface_name
:
  ETHERNET ethernet_num = uint8
  | VLAN vlan = vlan_number
;

vlan_number
:
  // 1-4094, extractor should validate number
  uint16
;

ipv4_address: IPV4_ADDRESS;

ipv4_prefix: IPV4_PREFIX;

uint8: UINT8;

uint16
:
  UINT8
  | UINT16
;
