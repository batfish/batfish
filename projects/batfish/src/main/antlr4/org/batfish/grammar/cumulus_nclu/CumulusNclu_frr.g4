parser grammar CumulusNclu_frr;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

frr_router_bgp
:
  ROUTER BGP asn = uint32
  (
    frrb_vrf
  )
;

frr_vrf
:
  VRF name = word NEWLINE frrv_ip_route* frr_exit_vrf?
;

frr_exit_vrf
:
  EXIT_VRF NEWLINE
;

frrb_vrf
:
  VRF name = word NEWLINE
  (
    frrbv_neighbor
  )
;

// NOTE: this is in no way complete/sane representation of the grammar here
// Just need extraction for this one line here
// Also, why is INTERFACE needed for an IP neighbor?
frrbv_neighbor
:
  NEIGHBOR name = ip_address INTERFACE PEER_GROUP pg = word NEWLINE
;


frrv_ip_route
:
  IP ROUTE network = ip_prefix nhip = ip_address NEWLINE
;

frr_username
:
  USERNAME frr_null_rest_of_line
;

frr_null_rest_of_line
:
  ~EXTRA_CONFIGURATION_FOOTER*
;

frr_unrecognized
:
  word frr_null_rest_of_line
;