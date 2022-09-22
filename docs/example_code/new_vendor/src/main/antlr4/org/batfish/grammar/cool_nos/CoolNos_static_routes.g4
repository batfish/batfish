parser grammar CoolNos_static;

import CoolNos_common;

s_static_routes
:
  STATIC_ROUTES NEWLINE
  ss*
;

ss
:
  ss_add
  | ss_modify
  | ss_delete
;

ss_add
:
  ADD prefix = ipv4_prefix ss_nh NEWLINE
;

ss_modify
:
  MODIFY prefix = ipv4_prefix ss_option NEWLINE
;

ss_option
:
  ss_nh
  | ss_disable
  | ss_enable
;

ss_nh
:
  ssa_discard
  | ssa_gateway
  | ssa_interface
;

ssa_discard: DISCARD;

ssa_gateway: GATEWAY ip = ipv4_address;

ssa_interface: INTERFACE name = interface_name;

ss_delete: DELETE prefix = ipv4_prefix NEWLINE;

ss_enable: ENABLE;

ss_disable: DISABLE;
