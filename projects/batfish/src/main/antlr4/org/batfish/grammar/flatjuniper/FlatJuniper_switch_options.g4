parser grammar FlatJuniper_switch_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_switch_options
:
    SWITCH_OPTIONS
    (
    so_vtep_source_interface
    | so_route_distinguisher
    | so_null
    )
;

so_vtep_source_interface
:
   VTEP_SOURCE_INTERFACE iface = interface_id
;

so_route_distinguisher
:
   ROUTE_DISTINGUISHER route_distinguisher
;

so_null
:
  (
    VRF_TARGET
    | VRF_EXPORT
    | VRF_IMPORT
  ) null_filler
;
