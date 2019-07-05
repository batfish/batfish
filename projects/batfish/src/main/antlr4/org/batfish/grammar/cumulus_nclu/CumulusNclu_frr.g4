parser grammar CumulusNclu_frr;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

frr_router
:
  ROUTER frr_null_rest_of_line
;

frr_vrf
:
  VRF name = word NEWLINE frrv_ip_route* frr_exit_vrf?
;

frr_exit_vrf
:
  EXIT_VRF NEWLINE
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