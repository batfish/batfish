parser grammar CumulusFrr_vrf;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

s_vrf
:
  VRF name = word NEWLINE
  (
    sv_vni
  | sv_route
  )*
  EXIT_VRF NEWLINE
;

sv_route
:
  IP ROUTE prefix ip_address NEWLINE
;

sv_vni
:
  VNI vni = vni_number NEWLINE
;
