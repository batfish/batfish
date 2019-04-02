parser grammar CumulusNclu_frr;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

frr_vrf
:
  VRF name = word NEWLINE frrv_ip_route*
;

frrv_ip_route
:
  IP ROUTE network = ip_prefix nhip = ip_address NEWLINE
;

frr_unrecognized
:
  word ~EXTRA_CONFIGURATION_FOOTER*
;