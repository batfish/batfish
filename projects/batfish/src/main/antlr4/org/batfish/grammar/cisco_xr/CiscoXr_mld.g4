parser grammar CiscoXr_mld;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

router_mld: MLD NEWLINE router_mld_inner*;

router_mld_inner
:
  rmld_null
  | rmld_vrf
  | rmld_vrf_inner
;

rmld_vrf_inner
:
  rmld_access_group
  | rmld_explicit_tracking
  | rmld_interface
  | rmld_maximum
  | rmld_query_interval_null
  | rmld_query_max_response_time_null
  | rmld_query_timeout_null
  | rmld_robustness_variable_null
  | rmld_ssm
  | rmld_version_null
;

rmld_interface_inner
:
  rmldi_access_group
  | rmldi_explicit_tracking
  | rmldi_join_group_null
  | rmldi_maximum
  | rmldi_query_interval_null
  | rmldi_query_max_response_time_null
  | rmldi_query_timeout_null
  | rmldi_router_null
  | rmldi_static_group_null
  | rmldi_version_null
;

rmld_null
:
  (
    NSF
  ) null_rest_of_line
;

rmld_query_interval_null
:
   QUERY_INTERVAL null_rest_of_line
;
rmld_query_max_response_time_null
:
   QUERY_MAX_RESPONSE_TIME null_rest_of_line
;
rmld_query_timeout_null
:
   QUERY_TIMEOUT null_rest_of_line
;
rmld_robustness_variable_null
:
   ROBUSTNESS_VARIABLE null_rest_of_line
;
rmld_version_null
:
   VERSION null_rest_of_line
;

rmldi_join_group_null
:
   JOIN_GROUP null_rest_of_line
;
rmldi_query_interval_null
:
   QUERY_INTERVAL null_rest_of_line
;
rmldi_query_max_response_time_null
:
   QUERY_MAX_RESPONSE_TIME null_rest_of_line
;
rmldi_query_timeout_null
:
   QUERY_TIMEOUT null_rest_of_line
;
rmldi_router_null
:
   ROUTER null_rest_of_line
;
rmldi_static_group_null
:
   STATIC_GROUP null_rest_of_line
;
rmldi_version_null
:
   VERSION null_rest_of_line
;

rmld_access_group: ACCESS_GROUP name = access_list_name NEWLINE;

rmld_explicit_tracking: EXPLICIT_TRACKING name = access_list_name? NEWLINE;

rmld_interface
:
  INTERFACE name = interface_name NEWLINE rmld_interface_inner*
;

rmld_maximum
:
  MAXIMUM
  (
    rmldm_groups_per_interface
    | rmldm_null
  )
;

rmldm_groups_per_interface
:
  GROUPS_PER_INTERFACE mld_max_groups_per_interface
  (THRESHOLD ONE_LITERAL)?
  name = access_list_name?
  NEWLINE
;

mld_max_groups_per_interface
:
  // 1-40000
  uint16
;

rmldm_null
:
  (
    GROUPS
  ) null_rest_of_line
;

rmld_ssm
:
  SSM MAP
  (
    rmld_ssm_null
    | rmld_ssm_static
  )
;

rmld_ssm_null
:
  (
    QUERY
  ) null_rest_of_line
;

rmld_ssm_static: STATIC IPV6_ADDRESS name = access_list_name NEWLINE;

rmldi_access_group: ACCESS_GROUP name = access_list_name NEWLINE;

rmldi_explicit_tracking: EXPLICIT_TRACKING (DISABLE | name = access_list_name)? NEWLINE;

rmldi_maximum: MAXIMUM rmldm_groups_per_interface;

rmld_vrf: VRF name = vrf_name NEWLINE rmld_vrf_inner*;
