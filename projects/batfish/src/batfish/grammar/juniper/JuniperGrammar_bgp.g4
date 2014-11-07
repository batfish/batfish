parser grammar JuniperGrammar_bgp;

import JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

/* --- --- --- Protocol->BGP Common Stanza Rules -----------------------------------------------------*/
export_common_stanza
:
   EXPORT list = variable_list SEMICOLON
;

bgp_family_common_stanza
:
   FAMILY
   (
      ft = BRIDGE
      | ft = CCC
      | ft = ETHERNET_SWITCHING
      | ft = INET
      | ft = INET_VPN
      | ft = INET6
      | ft = INET6_VPN
      | ft = ISO
      | ft = L2_VPN
      | ft = MPLS
      | ft = VPLS
   ) ignored_substanza // TODO [Ask Ari]: I'm certain these should not be ignored.

;

import_common_stanza
:
   IMPORT list = variable_list SEMICOLON
;

local_address_common_stanza
:
   LOCAL_ADDRESS
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) SEMICOLON
;

peer_as_common_stanza
:
   PEER_AS as = DEC SEMICOLON
;

bgp_p_stanza
:
   BGP OPEN_BRACE bg_stanza_list CLOSE_BRACE
;

bg_stanza_list
:
   (
      bg_stanza
      | inactive_bg_stanza
   )+
;

bg_stanza
:
   family_bg_stanza
   | group_bg_stanza
   | null_bg_stanza
;

inactive_bg_stanza
:
   INACTIVE COLON bg_stanza
;

family_bg_stanza
:
   bgp_family_common_stanza
;

group_bg_stanza
:
   GROUP name = VARIABLE OPEN_BRACE gbg_stanza_list CLOSE_BRACE
;

gbg_stanza_list
:
   (
      gbg_stanza
      | inactive_gbg_stanza
   )+
;

gbg_stanza
:
   export_gbg_stanza
   | family_gbg_stanza
   | import_gbg_stanza
   | local_address_gbg_stanza
   | local_as_gbg_stanza
   | neighbor_gbg_stanza
   | null_gbg_stanza
   | peer_as_gbg_stanza
   | type_gbg_stanza
;

inactive_gbg_stanza
:
   INACTIVE COLON gbg_stanza
;

null_bg_stanza
:
   log_updown_bg_stanza
   | traceoptions_bg_stanza
;

export_gbg_stanza
:
   export_common_stanza
;

family_gbg_stanza
:
   bgp_family_common_stanza
;

import_gbg_stanza
:
   import_common_stanza
;

local_address_gbg_stanza
:
   local_address_common_stanza
;

local_as_gbg_stanza
:
   LOCAL_AS num = DEC SEMICOLON
;

neighbor_gbg_stanza
:
   NEIGHBOR
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   ) OPEN_BRACE
   (
      (
         ngbg_stanza
         | inactive_ngbg_stanza
      )
   )+ CLOSE_BRACE
;

peer_as_gbg_stanza
:
   peer_as_common_stanza
;

type_gbg_stanza
:
   TYPE
   (
      INTERNAL
      | EXTERNAL
   ) SEMICOLON
;

null_gbg_stanza
:
   bfd_liveness_detection_gbg_stanza
   | log_updown_gbg_stanza
   | metric_out_gbg_stanza
   | multihop_gbg_stanza
   | multipath_gbg_stanza
   | remove_private_gbg_stanza
;

log_updown_bg_stanza
:
   log_updown_common_stanza
;

traceoptions_bg_stanza
:
   TRACEOPTIONS ignored_substanza
;

inactive_ngbg_stanza
:
   INACTIVE COLON ngbg_stanza
;

ngbg_stanza
:
   export_ngbg_stanza
   | family_ngbg_stanza
   | import_ngbg_stanza
   | local_address_ngbg_stanza
   | peer_as_ngbg_stanza
   | null_ngbg_stanza
;

bfd_liveness_detection_gbg_stanza
:
   bfd_liveness_detection_common_stanza
;

log_updown_gbg_stanza
:
   log_updown_common_stanza
;

metric_out_gbg_stanza
:
   metric_out_common_stanza
;

multihop_gbg_stanza
:
   multihop_common_stanza
;

multipath_gbg_stanza
:
   MULTIPATH SEMICOLON
;

remove_private_gbg_stanza
:
   remove_private_common_stanza
;

export_ngbg_stanza
:
   export_common_stanza
;

family_ngbg_stanza
:
   bgp_family_common_stanza
;

import_ngbg_stanza
:
   import_common_stanza
;

local_address_ngbg_stanza
:
   local_address_common_stanza
;

peer_as_ngbg_stanza
:
   peer_as_common_stanza
;

null_ngbg_stanza
:
   bfd_liveness_detection_ngbg_stanza
   | cluster_ngbg_stanza
   | description_ngbg_stanza
   | graceful_restart_ngbg_stanza
   | include_mp_next_hop_ngbg_stanza
   | hold_time_ngbg_stanza
   | local_preference_ngbg_stanza
   | metric_out_ngbg_stanza
   | multihop_ngbg_stanza
   | multipath_ngbg_stanza
   | passive_ngbg_stanza
   | remove_private_ngbg_stanza
   | tcp_mss_ngbg_stanza
;

bfd_liveness_detection_ngbg_stanza
:
   bfd_liveness_detection_common_stanza
;

cluster_ngbg_stanza
:
   CLUSTER
   (
      IP_ADDRESS
   ) SEMICOLON // TODO [Ask Ari]: Make sure this is ok to ignore

;

description_ngbg_stanza
:
   description_common_stanza
;

graceful_restart_ngbg_stanza
:
   GRACEFUL_RESTART SEMICOLON
;

include_mp_next_hop_ngbg_stanza
:
   INCLUDE_MP_NEXT_HOP SEMICOLON
;

hold_time_ngbg_stanza
:
   HOLD_TIME DEC SEMICOLON
;

local_preference_ngbg_stanza
:
   LOCAL_PREFERENCE DEC SEMICOLON
;

metric_out_ngbg_stanza
:
   metric_out_common_stanza
;

multipath_ngbg_stanza
:
   MULTIPATH SEMICOLON
;

multihop_ngbg_stanza
:
   multihop_common_stanza
;

passive_ngbg_stanza
:
   PASSIVE SEMICOLON
;

remove_private_ngbg_stanza
:
   remove_private_common_stanza
;

tcp_mss_ngbg_stanza
:
   TCP_MSS DEC SEMICOLON
;
