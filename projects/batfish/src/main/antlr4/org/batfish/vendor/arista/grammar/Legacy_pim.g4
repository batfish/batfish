parser grammar Legacy_pim;

import Legacy_common;

options {
   tokenVocab = AristaLexer;
}

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

pim_bfd_eos: BFD NEWLINE;

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

// ip pim is older syntax (4.20 ; router pim in EOS 4.24)
s_ip_pim: PIM (
  pim_accept_register
  | pim_accept_rp
  | pim_bfd_eos
  | pim_rp_address
  | pim_rp_announce_filter
  | pim_rp_candidate
  | pim_send_rp_announce
  | pim_spt_threshold
  | pim_ssm
);

