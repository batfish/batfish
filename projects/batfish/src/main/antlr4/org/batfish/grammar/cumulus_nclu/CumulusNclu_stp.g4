parser grammar CumulusNclu_stp;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

// Spanning tree options that apply to interfaces and bonds
stp_common
:
  STP
  (
    stp_bpduguard
    | stp_portadminedge
    | stp_portautoedge
    | stp_portbpdufilter
    | stp_portnetwork
    | stp_portrestrrole
  )
;

stp_bpduguard
:
  BPDUGUARD NEWLINE
;

stp_portadminedge
:
  PORTADMINEDGE NEWLINE
;

stp_portautoedge
:
  PORTAUTOEDGE NO NEWLINE
;

stp_portbpdufilter
:
  PORTBPDUFILTER NEWLINE
;

stp_portnetwork
:
  PORTNETWORK NEWLINE
;

stp_portrestrrole
:
  PORTRESTROLE NEWLINE
;

