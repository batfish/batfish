parser grammar Cisco_pim;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ip_pim
:
   PIM (VRF vrf = variable)? ip_pim_tail
;

ip_pim_tail
:
   pim_accept_register
   | pim_accept_rp
   | pim_autorp_null
   | pim_bidir_enable_null
   | pim_bidir_offer_interval_null
   | pim_bidir_offer_limit_null
   | pim_bidir_rp_limit_null
   | pim_bsr_candidate_null
   | pim_dm_fallback_null
   | pim_event_history_null
   | pim_log_neighbor_changes_null
   | pim_mtu_null
   | pim_register_rate_limit_null
   | pim_register_source_null
   | pim_rp_address
   | pim_rp_announce_filter
   | pim_rp_candidate
   | pim_rpf_vector_null
   | pim_send_rp_announce
   | pim_send_rp_discovery_null
   | pim_sg_expiry_timer_null
   | pim_snooping_null
   | pim_spt_threshold
   | pim_ssm
   | pim_v1_rp_reachability_null
;

pim_accept_register
:
   ACCEPT_REGISTER
   (
      (
         LIST name = variable
      )
      |
      (
         ROUTE_MAP name = variable
      )
   ) NEWLINE
;

pim_accept_rp
:
   ACCEPT_RP
   (
      AUTO_RP
      | IP_ADDRESS
   )
   (
      name = variable
   )? NEWLINE
;

pim_autorp_null
:
   AUTORP null_rest_of_line
;
pim_bidir_enable_null
:
   BIDIR_ENABLE null_rest_of_line
;
pim_bidir_offer_interval_null
:
   BIDIR_OFFER_INTERVAL null_rest_of_line
;
pim_bidir_offer_limit_null
:
   BIDIR_OFFER_LIMIT null_rest_of_line
;
pim_bidir_rp_limit_null
:
   BIDIR_RP_LIMIT null_rest_of_line
;
pim_bsr_candidate_null
:
   BSR_CANDIDATE null_rest_of_line
;
pim_dm_fallback_null
:
   DM_FALLBACK null_rest_of_line
;
pim_event_history_null
:
   EVENT_HISTORY null_rest_of_line
;
pim_log_neighbor_changes_null
:
   LOG_NEIGHBOR_CHANGES null_rest_of_line
;
pim_mtu_null
:
   MTU null_rest_of_line
;
pim_register_rate_limit_null
:
   REGISTER_RATE_LIMIT null_rest_of_line
;
pim_register_source_null
:
   REGISTER_SOURCE null_rest_of_line
;
pim_rpf_vector_null
:
   RPF_VECTOR null_rest_of_line
;
pim_send_rp_discovery_null
:
   SEND_RP_DISCOVERY null_rest_of_line
;
pim_sg_expiry_timer_null
:
   SG_EXPIRY_TIMER null_rest_of_line
;
pim_snooping_null
:
   SNOOPING null_rest_of_line
;
pim_v1_rp_reachability_null
:
   V1_RP_REACHABILITY null_rest_of_line
;

pim_rp_address
:
   RP_ADDRESS IP_ADDRESS
   (
      (
         ACCESS_LIST name = variable
      )
      |
      (
         GROUP_LIST prefix = IP_PREFIX
      )
      | OVERRIDE
      | prefix = IP_PREFIX
      | name = variable
   )* NEWLINE
;

pim_rp_announce_filter
:
   RP_ANNOUNCE_FILTER
   (
      GROUP_LIST
      | RP_LIST
   ) name = variable NEWLINE
;

pim_rp_candidate
:
   RP_CANDIDATE interface_name
   (
      (
         GROUP_LIST name = variable
      )
      |
      (
         INTERVAL dec
      )
      |
      (
         PRIORITY dec
      )
   )+ NEWLINE
;

pim_send_rp_announce
:
   SEND_RP_ANNOUNCE interface_name SCOPE ttl = dec
   (
      (
         GROUP_LIST name = variable
      )
      |
      (
         INTERVAL dec
      )
   )+ NEWLINE
;

pim_spt_threshold
:
   SPT_THRESHOLD
   (
      dec
      | INFINITY
   )
   (
      GROUP_LIST name = variable
   )? NEWLINE
;

pim_ssm
:
   SSM
   (
      pim_ssm_default
      | pim_ssm_range
   )
;

pim_ssm_default: DEFAULT NEWLINE;
pim_ssm_range: RANGE name = variable;

no_ip_pim
:
  PIM
  (
    nopim_snooping_null
  )
;

nopim_snooping_null: SNOOPING null_rest_of_line;