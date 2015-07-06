parser grammar Cisco_mpls;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

implst_stanza
:
   null_implst_stanza | null_mplst_stanza
;

interface_mplst_stanza
:
   INTERFACE name = interface_name NEWLINE implst_stanza*
;

mpls_ldp_stanza
:
   MPLS LDP NEWLINE mplsl_stanza*
;

mpls_traffic_eng_stanza
:
   MPLS TRAFFIC_ENG NEWLINE mplst_stanza*
;

mplsl_stanza
:
   null_mplsl_stanza
;

mplst_stanza
:
   interface_mplst_stanza
   | null_mplst_stanza
;

null_implst_stanza
:
   NO?
   (
      ATTRIBUTE_NAMES
      | ATTRIBUTE_SET
      | AUTO_TUNNEL
      | EXCLUDE
      | NHOP_ONLY
   ) ~NEWLINE* NEWLINE
;

null_mplsl_stanza
:
   NO?
   (
      ADJACENCY
      | ADDRESS_FAMILY
      | ALLOCATE
      | DISCOVERY
      | IGP
      | INTERFACE
      | LABEL
      | LOCAL
      | LOG
      | NEIGHBOR
      | NSR
      | PASSWORD
      | ROUTER_ID
      | SESSION_PROTECTION
   ) ~NEWLINE* NEWLINE
;

null_mplst_stanza
:
   NO?
   (
      AFFINITY
      | AFFINITY_MAP
      | ATTRIBUTE_SET
      | AUTO_TUNNEL
      | LOGGING
      | REOPTIMIZE
      | TUNNEL_ID
   ) ~NEWLINE* NEWLINE
;
