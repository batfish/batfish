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
  FRR VERSION REMARK_TEXT? NEWLINE
  | FRR DEFAULTS (DATACENTER | TRADITIONAL) NEWLINE
  | USERNAME word word NEWLINE
  | HOSTNAME word NEWLINE
  | NO IPV6 FORWARDING NEWLINE
  | s_agentx
  | s_bgp
  | s_vrf
  | s_routemap
  | s_ip
  | s_service
  | LINE VTY NEWLINE
  | s_log
  | s_interface
  | s_router_ospf
  | s_end
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

s_log
:
  LOG
  (
    SYSLOG INFORMATIONAL
    | FILE REMARK_TEXT
    | COMMANDS
  ) NEWLINE
;

s_agentx
:
  AGENTX NEWLINE
;

s_ip
:
  IP
  (
    ip_community_list
    | ip_prefix_list
  )
;

si_description
:
  DESCRIPTION description = REMARK_TEXT NEWLINE
;

s_end
:
  END NEWLINE
;
