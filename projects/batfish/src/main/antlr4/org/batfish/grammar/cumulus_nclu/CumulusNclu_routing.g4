parser grammar CumulusNclu_routing;

import CumulusNclu_common;

options {
  tokenVocab = CumulusNcluLexer;
}

a_routing
:
  ROUTING
  (
    r_agentx
    | r_defaults_datacenter
    | r_log
    | r_route
    | r_route_map
    | r_service_integrated_vtysh_config
  )
;

r_agentx
:
  AGENTX NEWLINE
;

r_defaults_datacenter
:
  DEFAULTS DATACENTER NEWLINE
;

r_log
:
  LOG rl_syslog
;

rl_syslog
:
  SYSLOG
  (
    ALERTS
    | CRITICAL
    | DEBUGGING
    | EMERGENCIES
    | ERRORS
    | INFORMATIONAL
    | NOTIFICATIONS
    | WARNINGS
  )? NEWLINE
;

r_route
:
  ROUTE prefix = ip_prefix (nhip = ip_address | iface = word) NEWLINE
;

r_route_map
:
  ROUTE_MAP name = word action = line_action num = uint16
  (
    rm_match
    | NEWLINE
  )
;

rm_match
:
  MATCH rmm_interface
;

rmm_interface
:
  INTERFACE interfaces = glob NEWLINE
;

r_service_integrated_vtysh_config
:
  SERVICE INTEGRATED_VTYSH_CONFIG NEWLINE
;