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
  (EXIT_VRF NEWLINE)?
;

sv_route
:
  IP ROUTE network = prefix (next_hop_ip = ip_address | next_hop_interface = word) (distance = uint8)? NEWLINE
;

sv_vni
:
  VNI vni = vni_number NEWLINE
;
