parser grammar CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

interface_address
:
  address = ip_address mask = ip_address
  | iaddress = ip_prefix
;

interface_name
:
  prefix = interface_prefix middle = interface_middle? parent_suffix =
  interface_parent_suffix? first = uint16
;

interface_prefix
:
  ETHERNET
  | LOOPBACK
  | MGMT
  | PORT_CHANNEL
  | VLAN
;

interface_middle
:
  (
    uint8 FORWARD_SLASH
  )+
;

interface_parent_suffix
:
  num = uint16 period = PERIOD
;

ip_address
:
  IP_ADDRESS
;

ip_prefix
:
  IP_PREFIX
;

null_rest_of_line
:
  ~NEWLINE* NEWLINE
;

route_network
:
  address = ip_address mask = ip_address
  | prefix = ip_prefix
;

subdomain_name
:
  SUBDOMAIN_NAME
;

track_object_number
:
// 1-512
  UINT8
  | UINT16
;

uint8
:
  UINT8
;

uint16
:
  UINT8
  | UINT16
;

uint32
:
  UINT8
  | UINT16
  | UINT32
;

vlan_id
:
// 1-4094
  UINT8
  | UINT16
;

vlan_id_range
:
  intervals += vlan_id_interval
  (
    COMMA intervals += vlan_id_interval
  )*
;

vlan_id_interval
:
  low = vlan_id
  (
    DASH high = vlan_id
  )?
;

vrf_name
:
  WORD
;
