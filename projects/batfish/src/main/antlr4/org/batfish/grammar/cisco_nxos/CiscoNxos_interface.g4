parser grammar CiscoNxos_interface;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_interface
:
  INTERFACE irange = interface_range NEWLINE
  (
    i_ip
    | i_no
    | i_null
  )*
;

i_ip
:
  IP i_ip_address
;

i_ip_address
:
  ADDRESS addr = interface_address SECONDARY? NEWLINE
;

i_no
:
  NO
  (
    i_no_shutdown
    | i_no_switchport
  )
;

i_no_shutdown
:
  SHUTDOWN NEWLINE
;

i_no_switchport
:
  SWITCHPORT NEWLINE
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
