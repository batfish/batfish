parser grammar CiscoNxos_interface;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_interface
:
  INTERFACE irange = interface_range NEWLINE
  (
    i_ip_address
    | i_ip_address_secondary
    | i_no_shutdown
    | i_no_switchport
    | i_null
  )*
;

i_ip_address
:
  IP ADDRESS addr = interface_address NEWLINE
;

i_ip_address_secondary
:
  IP ADDRESS addr = interface_address SECONDARY NEWLINE
;

i_no_shutdown
:
  NO SHUTDOWN NEWLINE
;

i_no_switchport
:
  NO SWITCHPORT NEWLINE
;

i_null
:
  NO?
  (
    IP REDIRECTS
  ) null_rest_of_line
;

interface_range
:
  iname = interface_name
  (
    DASH last = uint8
  )?
;
