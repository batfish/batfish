parser grammar FlatJuniper_switch_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_switch_options
:
  SWITCH_OPTIONS
    (
      so_route_distinguisher
      | so_vrf_target
      | so_vtep_source_interface
   )
;

so_vrf_target:
   VRF_TARGET
        (
          sovt_auto
          | sovt_community
          | sovt_export
          | sovt_import
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

sovt_auto
:
  AUTO
;

sovt_community
:
   extended_community
;

sovt_export
:
   EXPORT extended_community
;

sovt_import
:
   IMPORT extended_community
;
