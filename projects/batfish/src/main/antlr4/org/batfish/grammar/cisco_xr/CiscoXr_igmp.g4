parser grammar CiscoXr_igmp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_igmp: IGMP NEWLINE router_igmp_inner*;

router_igmp_inner
:
  rigmp_null
  | rigmp_vrf
  | rigmp_vrf_inner
;

rigmp_vrf_inner
:
  rigmp_access_group
  | rigmp_explicit_tracking
  | rigmp_interface
  | rigmp_maximum
  | rigmp_ssm
  | rigmp_traffic
  | rigmpv_null
;

rigmp_interface_inner
:
  rigmpi_access_group
  | rigmpi_explicit_tracking
  | rigmpi_maximum
  | rigmpi_null
;

rigmp_null
:
  (
    ACCOUNTING
    | NSF
    | UNICAST_QOS_ADJUST
  ) null_rest_of_line
;

rigmpv_null
:
  (
    QUERY_INTERVAL
    | QUERY_MAX_RESPONSE_TIME
    | QUERY_TIMEOUT
    | ROBUSTNESS_COUNT
    | VERSION
  ) null_rest_of_line
;

rigmpi_null
:
  (
    JOIN_GROUP
    | QUERY_INTERVAL
    | QUERY_MAX_RESPONSE_TIME
    | QUERY_TIMEOUT
    | ROUTER
    | STATIC_GROUP
    | VERSION
  ) null_rest_of_line
;

rigmp_access_group: ACCESS_GROUP name = access_list_name NEWLINE;

rigmp_explicit_tracking: EXPLICIT_TRACKING name = access_list_name? NEWLINE;

rigmp_maximum
:
  MAXIMUM
  (
    rigmpm_groups_per_interface
    | rigmpm_null
  )
;

rigmpm_null
:
  (
    GROUPS
  ) null_rest_of_line
;

rigmpm_groups_per_interface
:
  GROUPS_PER_INTERFACE igmp_max_groups_per_interface
  (THRESHOLD ONE_LITERAL)?
  name = access_list_name?
  NEWLINE
;

igmp_max_groups_per_interface
:
  // 1-40000
  uint16
;

rigmp_ssm
:
  SSM MAP
  (
    rigmps_null
    | rigmps_static
  )
;

rigmps_null
:
  (
    QUERY
  ) null_rest_of_line
;

rigmps_static: STATIC IP_ADDRESS name = access_list_name NEWLINE;

rigmp_traffic: TRAFFIC PROFILE name = policy_map_name NEWLINE;

rigmp_vrf: VRF name = vrf_name NEWLINE rigmp_vrf_inner*;

rigmp_interface
:
  INTERFACE
  (
    rigmp_interface_all
    | rigmp_interface_named
  )
;

rigmp_interface_all: ALL ROUTER DISABLE NEWLINE;

rigmp_interface_named: name = interface_name NEWLINE rigmp_interface_inner*;

rigmpi_access_group: ACCESS_GROUP name = access_list_name NEWLINE;

rigmpi_explicit_tracking: EXPLICIT_TRACKING (DISABLE | name = access_list_name)? NEWLINE;

rigmpi_maximum
:
  MAXIMUM GROUPS_PER_INTERFACE igmp_max_groups_per_interface
  (THRESHOLD ONE_LITERAL)?
  name = access_list_name?
  NEWLINE
;
