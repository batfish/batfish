parser grammar CumulusFrrParser;

import CumulusFrr_bgp, CumulusFrr_common, CumulusFrr_interface, CumulusFrr_ip_community_list, CumulusFrr_ip_prefix_list, CumulusFrr_ospf, CumulusFrr_routemap, CumulusFrr_vrf;

options {
  superClass =
  'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseParser';
  tokenVocab = CumulusFrrLexer;
}

// goal rule
cumulus_frr_configuration
:
  statement+ EOF
;

// other rules
statement
:
  s_agentx
  | s_bgp
  | s_enable
  | s_end
  | s_interface
  | s_frr
  | s_hostname
  | s_ip
  | s_ipv6
  | s_line
  | s_log
  | s_no
  | s_password
  | s_routemap
  | s_router_ospf
  | s_service
  | s_username
  | s_vrf
;

ip_as_path
:
  AS_PATH ACCESS_LIST name = word action = access_list_action as_path_regex = word NEWLINE
;

s_agentx
:
  AGENTX NEWLINE
;

si_description
:
  DESCRIPTION description = REMARK_TEXT NEWLINE
;

s_enable
:
  ENABLE
  se_password
;

s_end
:
  END NEWLINE
;

s_frr
:
   FRR
   (
     sf_defaults
     | sf_version
   )
;

sf_defaults
:
   DEFAULTS (DATACENTER | TRADITIONAL) NEWLINE
;

sf_version
:
   VERSION REMARK_TEXT? NEWLINE
;

s_hostname
:
   HOSTNAME word NEWLINE
;

se_password
:
  PASSWORD null_rest_of_line
;

s_ip
:
  IP
  (
    ip_as_path
    | ip_community_list
    | ip_prefix_list
    | ip_route
  )
;

s_ipv6
:
   IPV6 null_rest_of_line
;

ip_route
:
  ROUTE network = prefix (next_hop_ip = ip_address | next_hop_interface = word) (distance = uint8)?
    (
      VRF vrf = word
    )? NEWLINE
;

s_line
:
  LINE VTY NEWLINE
;

s_log
:
  LOG
  (
    SYSLOG loglevel?
    | FILE REMARK_TEXT
    | COMMANDS
  ) NEWLINE
;

s_no
:
  NO
  (
     sno_ip
     | sno_ipv6
  )
;

sno_ip
:
   IP
   (
      snoip_forwarding
   )
;

sno_ipv6
:
   IPV6
   (
      snoipv6_forwarding
   )
;

snoip_forwarding
:
   FORWARDING NEWLINE
;

snoipv6_forwarding
:
   FORWARDING NEWLINE
;

s_password
:
  PASSWORD null_rest_of_line
;

s_service
:
  SERVICE
  (
    INTEGRATED_VTYSH_CONFIG
    | PASSWORD_ENCRYPTION
  )
  NEWLINE
;

s_username
:
  USERNAME null_rest_of_line
;
