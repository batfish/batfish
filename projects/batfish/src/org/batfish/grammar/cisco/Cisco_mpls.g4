parser grammar Cisco_mpls;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

implst_stanza
:
   null_implst_stanza
   | null_mplst_stanza
;

interface_mplst_stanza
:
   INTERFACE name = interface_name NEWLINE implst_stanza*
;

mpls_ldp_stanza
:
   MPLS
   (
      LABEL PROTOCOL
   )? LDP NEWLINE mplsl_stanza*
;

mpls_label_range_stanza
:
   MPLS LABEL RANGE DEC+ NEWLINE
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
      | ADVERTISE
      | ALLOCATE
      | DISABLE
      | DISCOVERY
      | IGP
      | INTERFACE
      | FOR
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
      | SOFT_PREEMPTION
      | TUNNEL_ID
   ) ~NEWLINE* NEWLINE null_mplst_substanza*
;

null_mplst_substanza
:
   (
      TIMEOUT
   ) ~NEWLINE* NEWLINE
;

