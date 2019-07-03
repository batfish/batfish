parser grammar CiscoNxos_vrf;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_vrf_context
:
  VRF CONTEXT name = vrf_name NEWLINE
  (
    vc_address_family
    | vc_no
    | vc_null
    | vc_shutdown
  )*
;

vc_address_family
:
  ADDRESS_FAMILY
  (
    (
      IPV4 UNICAST NEWLINE vcaf4u_null*
    )
    |
    (
      IPV6 UNICAST NEWLINE vcaf6u_null*
    )
  )
;

vcaf4u_null
:
  NO?
  (
    MAXIMUM
  ) null_rest_of_line
;

vcaf6u_null
:
  NO?
  (
    MAXIMUM
  ) null_rest_of_line
;

vc_no
:
  NO vc_no_shutdown
;

vc_no_shutdown
:
  SHUTDOWN NEWLINE
;

vc_null
:
  NO?
  (
    DESCRIPTION
  ) null_rest_of_line
;

vc_shutdown
:
  SHUTDOWN NEWLINE
;
