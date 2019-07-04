parser grammar CiscoNxos_static;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ip_route
:
  ROUTE network = route_network
  (
    null0 = NULL0
    | nhint = interface_name? nhip = ip_address
  )
  (
    VRF nhvrf = vrf_name
  )?
  (
    TRACK track = track_object_number
  )?
  (
    NAME name = static_route_name
  )?
  (
    TAG tag = uint32 pref = static_route_pref?
    | pref = static_route_pref
    (
      TAG tag = uint32
    )?
  )? NEWLINE
;

static_route_name
:
// 1-50 characters
  WORD
;

static_route_pref
:
// 1-255
  UINT8
;