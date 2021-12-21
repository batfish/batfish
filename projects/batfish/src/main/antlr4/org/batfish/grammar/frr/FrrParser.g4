parser grammar FrrParser;

import Frr_bgp, Frr_bgp_community_list, Frr_common, Frr_interface, Frr_ip_prefix_list, Frr_ipv6_prefix_list, Frr_ospf, Frr_routemap, Frr_vrf;

options {
  superClass =
  'org.batfish.grammar.frr.parsing.FrrBaseParser';
  tokenVocab = FrrLexer;
}

// goal rule
frr_configuration
:
  NEWLINE? statement+ EOF
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
  | s_router_bgp
  | s_router_ospf
  | s_service
  | s_username
  | s_vrf
;

b_as_path_access_list
:
  AS_PATH ACCESS_LIST name = word action = access_list_action as_path_regex = REGEX NEWLINE
;

ip_forwarding
:
   FORWARDING NEWLINE
;

s_agentx
:
  AGENTX NEWLINE
;

s_bgp
:
   BGP
   (
      b_community_list
      | b_as_path_access_list
   )
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
    b_as_path_access_list     // old variant of as-path access-list. new one use bgp, not ip.
    | b_community_list    // old variant of community-list. new one use bgp, not ip.
    | ip_forwarding
    | ip_prefix_list
    | ip_route
  )
;

s_ipv6
:
   IPV6 (
       ipv6_prefix_list
     | null_rest_of_line
   )
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
    | TIMESTAMP PRECISION uint32
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
