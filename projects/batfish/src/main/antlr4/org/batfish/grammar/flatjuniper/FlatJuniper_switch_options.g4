parser grammar FlatJuniper_switch_options;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_switch_options
:
  SWITCH_OPTIONS
    (
      so_interface
      | so_vtep_source_interface
      | so_route_distinguisher
      | so_vrf_target
      | so_vrf_export
      | so_vrf_import
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

so_vrf_target
:
  VRF_TARGET (
    sovt_auto
    | sovt_community
    | sovt_export
    | sovt_import
  )
;

sovt_auto
:
  AUTO
;

sovt_community
:
  comm = vrf_target_community
;

sovt_export
:
  EXPORT comm = vrf_target_community
;

sovt_import
:
  IMPORT comm = vrf_target_community
;

so_vrf_export
:
  VRF_EXPORT name = junos_name
;

so_vrf_import
:
  VRF_IMPORT name = junos_name
;

so_interface
:
  INTERFACE interface_id
  (
    soi_interface_mac_limit
  )
;

soi_interface_mac_limit
:
  INTERFACE_MAC_LIMIT
  (
    soiiml_limit_null
    | soiiml_packet_action_null
  )
;

soiiml_limit_null
:
  uint16
;

soiiml_packet_action_null
:
  PACKET_ACTION
  (
    DROP
    | DROP_AND_LOG
    | LOG
    | NONE
    | SHUTDOWN
  )
;
