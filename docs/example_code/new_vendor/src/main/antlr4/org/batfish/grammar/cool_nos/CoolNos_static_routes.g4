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
  ADD prefix = ipv4_prefix ss_nh
;

ss_modify
:
  MODIFY prefix = ipv4_prefix ss_nh
;

ss_nh
:
  ssa_discard
  | ssa_gateway
  | ssa_interface
;

ssa_discard: DISCARD NEWLINE;

ssa_gateway: GATEWAY ip = ipv4_address NEWLINE;

ssa_interface: INTERFACE name = interface_name NEWLINE;

ss_delete: DELETE prefix = ipv4_prefix NEWLINE;
