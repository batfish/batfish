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
  IP ROUTE network = IP_PREFIX nhip = IP_ADDRESS NEWLINE
;

frr_unrecognized
:
  word ~EXTRA_CONFIGURATION_FOOTER*
;