parser grammar CiscoNxos_evpn;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_evpn
:
  EVPN NEWLINE
  (
    ev_vni
  )*
;

ev_vni
:
  VNI vni = vni_number L2 NEWLINE
  (
    evv_rd
    | evv_route_target
  )*
;

evv_rd
:
  RD rd = route_distinguisher_or_auto NEWLINE
;

evv_route_target
:
  ROUTE_TARGET dir = both_export_import rt = route_target_or_auto NEWLINE
;