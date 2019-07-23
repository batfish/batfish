parser grammar CiscoNxosParser;

import
CiscoNxos_common, CiscoNxos_bgp, CiscoNxos_evpn, CiscoNxos_interface, CiscoNxos_ip_access_list, CiscoNxos_ip_as_path_access_list, CiscoNxos_ip_community_list, CiscoNxos_ip_prefix_list, CiscoNxos_ospf, CiscoNxos_route_map, CiscoNxos_static, CiscoNxos_vlan, CiscoNxos_vrf;

options {
  superClass = 'org.batfish.grammar.cisco_nxos.parsing.CiscoNxosBaseParser';
  tokenVocab = CiscoNxosLexer;
}

cisco_nxos_configuration
:
  statement+ EOF
;

statement
:
  s_banner
  | s_evpn
  | s_hostname
  | s_interface
  | s_ip
  | s_no
  | s_null
  | s_nv
  | s_route_map
  | s_router
  | s_version
  | s_vlan
  | s_vrf_context
;

s_banner
:
  BANNER
  (
    banner_exec
    | banner_motd
  )
;

banner_exec
:
  EXEC BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

banner_motd
:
  MOTD BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

s_hostname
:
  HOSTNAME hostname = subdomain_name NEWLINE
;

s_ip
:
  IP
  (
    ip_access_list
    | ip_as_path_access_list
    | ip_community_list
    | ip_null
    | ip_prefix_list
    | ip_route
  )
;

ip_null
:
  DOMAIN_LOOKUP
;

s_null
:
  NO?
  (
    BOOT
    | CLI
    | CLOCK
    | ERRDISABLE
    | FEATURE
    | SERVICE
    | USERNAME
  ) null_rest_of_line
;

s_no
:
  NO
  (
    IP
  ) null_rest_of_line
;

s_nv
:
  NV OVERLAY EVPN NEWLINE
;

s_router
:
  ROUTER
  (
    router_bgp
    | router_ospf
  )
;

s_version
:
// arbitray string, not actual command
  VERSION version = REMARK_TEXT NEWLINE
;
