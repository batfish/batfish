parser grammar F5BigipStructured_net;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

bundle_speed
:
  FORTY_G
  | ONE_HUNDRED_G
;

net_interface
:
  INTERFACE name = word BRACE_LEFT
  (
    NEWLINE
    (
      ni_bundle_speed
      | u
    )*
  )? BRACE_RIGHT NEWLINE
;

net_self
:
  SELF name = word BRACE_LEFT
  (
    NEWLINE
    (
      ns_address
      | ns_vlan
      | u
    )*
  )? BRACE_RIGHT NEWLINE
;

net_vlan
:
  VLAN name = word BRACE_LEFT
  (
    NEWLINE
    (
      nv_interfaces
      | nv_tag
      | u
    )*
  )? BRACE_RIGHT NEWLINE
;

ni_bundle_speed
:
  BUNDLE_SPEED bundle_speed NEWLINE
;

ns_address
:
  ADDRESS interface_address = word NEWLINE
;

ns_vlan
:
  VLAN name = word NEWLINE
;

nv_interfaces
:
  INTERFACES BRACE_LEFT
  (
    NEWLINE
    (
      nvi_interface
    )*
  )? BRACE_RIGHT NEWLINE
;

nv_tag
:
  TAG tag = word NEWLINE
;

nvi_interface
:
  name = word BRACE_LEFT NEWLINE? BRACE_RIGHT NEWLINE
;

s_net
:
  NET
  (
    net_interface
    | net_self
    | net_vlan
    | u
  )
;
