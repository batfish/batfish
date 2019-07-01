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
  prefix = interface_prefix middle = interface_middle? first = uint8
;

interface_prefix
:
  ETHERNET
  | LOOPBACK
  | MGMT
  | PORT_CHANNEL
;

interface_middle
:
  (
    uint8 FORWARD_SLASH
  )+ parent_suffix = interface_parent_suffix?
;

interface_parent_suffix
:
  uint8 period = PERIOD
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

subdomain_name
:
  SUBDOMAIN_NAME
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
