parser grammar CiscoXr_igmp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_igmp: IGMP NEWLINE router_igmp_inner*;

router_igmp_inner
:
  rigmp_accounting_null
  | rigmp_nsf_null
  | rigmp_unicast_qos_adjust_null
  | rigmp_vrf
  | rigmp_vrf_inner
;

rigmp_vrf_inner
:
  rigmp_access_group
  | rigmp_explicit_tracking
  | rigmp_interface
  | rigmp_maximum
  | rigmp_query_interval_null
  | rigmp_query_max_response_time_null
  | rigmp_query_timeout_null
  | rigmp_robustness_count_null
  | rigmp_ssm
  | rigmp_traffic
  | rigmp_version_null
;

rigmp_interface_inner
:
  rigmpi_access_group
  | rigmpi_explicit_tracking
  | rigmpi_join_group_null
  | rigmpi_maximum
  | rigmpi_query_interval_null
  | rigmpi_query_max_response_time_null
  | rigmpi_query_timeout_null
  | rigmpi_router_null
  | rigmpi_static_group_null
  | rigmpi_version_null
;

rigmp_accounting_null
:
   ACCOUNTING null_rest_of_line
;
rigmp_nsf_null
:
   NSF null_rest_of_line
;
rigmp_unicast_qos_adjust_null
:
   UNICAST_QOS_ADJUST null_rest_of_line
;

rigmp_query_interval_null
:
   QUERY_INTERVAL null_rest_of_line
;
rigmp_query_max_response_time_null
:
   QUERY_MAX_RESPONSE_TIME null_rest_of_line
;
rigmp_query_timeout_null
:
   QUERY_TIMEOUT null_rest_of_line
;
rigmp_robustness_count_null
:
   ROBUSTNESS_COUNT null_rest_of_line
;
rigmp_version_null
:
   VERSION null_rest_of_line
;

rigmpi_join_group_null
:
   JOIN_GROUP null_rest_of_line
;
rigmpi_query_interval_null
:
   QUERY_INTERVAL null_rest_of_line
;
rigmpi_query_max_response_time_null
:
   QUERY_MAX_RESPONSE_TIME null_rest_of_line
;
rigmpi_query_timeout_null
:
   QUERY_TIMEOUT null_rest_of_line
;
rigmpi_router_null
:
   ROUTER null_rest_of_line
;
rigmpi_static_group_null
:
   STATIC_GROUP null_rest_of_line
;
rigmpi_version_null
:
   VERSION null_rest_of_line
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
