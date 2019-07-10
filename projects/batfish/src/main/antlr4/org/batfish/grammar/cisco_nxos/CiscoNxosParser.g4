parser grammar CiscoNxosParser;

import
CiscoNxos_common, CiscoNxos_bgp, CiscoNxos_interface, CiscoNxos_ip_access_list, CiscoNxos_static, CiscoNxos_vlan, CiscoNxos_vrf;

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
  s_hostname
  | s_interface
  | s_ip
  | s_null
  | s_router
  | s_vlan
  | s_vrf_context
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
    | ip_route
  )
;

s_null
:
  NO?
  (
    FEATURE
  ) null_rest_of_line
;

s_router
:
  ROUTER router_bgp
;
