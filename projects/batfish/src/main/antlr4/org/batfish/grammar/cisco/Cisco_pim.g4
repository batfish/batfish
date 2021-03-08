parser grammar Cisco_pim;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

ip_pim_tail
:
   pim_accept_register
   | pim_accept_rp
   | pim_null
   | pim_rp_address
   | pim_rp_announce_filter
   | pim_rp_candidate
   | pim_send_rp_announce
   | pim_spt_threshold
   | pim_ssm
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

pim_null
:
   (
      AUTORP
      | BIDIR_ENABLE
      | BIDIR_OFFER_INTERVAL
      | BIDIR_OFFER_LIMIT
      | BIDIR_RP_LIMIT
      | BSR_CANDIDATE
      | DM_FALLBACK
      | EVENT_HISTORY
      | LOG_NEIGHBOR_CHANGES
      | MTU
      | REGISTER_RATE_LIMIT
      | REGISTER_SOURCE
      | RPF_VECTOR
      | SEND_RP_DISCOVERY
      | SG_EXPIRY_TIMER
      | SNOOPING
      | V1_RP_REACHABILITY
   ) null_rest_of_line
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
      DEFAULT
      |
      (
         RANGE name = variable
      )
   ) NEWLINE
;

s_ip_pim
:
   NO? IP PIM
   (
      VRF vrf = variable
   )? ip_pim_tail
;

