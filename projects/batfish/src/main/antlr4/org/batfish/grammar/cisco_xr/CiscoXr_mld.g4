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
  | rmld_ssm
  | rmldv_null
;

rmld_interface_inner
:
  rmldi_access_group
  | rmldi_explicit_tracking
  | rmldi_maximum
  | rmldi_null
;

rmld_null
:
  (
    NSF
  ) null_rest_of_line
;

rmldv_null
:
  (
    QUERY_INTERVAL
    | QUERY_MAX_RESPONSE_TIME
    | QUERY_TIMEOUT
    | ROBUSTNESS_VARIABLE
    | VERSION
  ) null_rest_of_line
;

rmldi_null
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
