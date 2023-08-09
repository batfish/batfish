parser grammar CiscoNxos_vlan;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_vlan
:
  VLAN
  (
    vlan_configuration
    | vlan_null
    | vlan_vlan
  )
;

vlan_configuration
:
  CONFIGURATION range = vlan_id_range NEWLINE
    (
      vlanc_service_policy
      | vlanc_null
    )*
;

vlanc_service_policy
:
  SERVICE_POLICY vcsp_type
;

vcsp_type
:
  TYPE
  (
    vcspt_qos
  )
;

vcspt_qos
:
  QOS (INPUT | OUTPUT) name = policy_map_qos_name NEWLINE
;

vlanc_null
:
  NO?
  (
    ACCESS_GROUP
    | EXPLICIT_TRACKING
    | FAST_LEAVE
    | GROUP_TIMEOUT
    | LAST_MEMBER_QUERY_INTERVAL
    | LINK_LOCAL_GROUPS_SUPPRESSION
    | MROUTER
    | PROXY
    | PROXY_LEAVE
    | QUERIER
    | QUERIER_TIMEOUT
    | QUERY_INTERVAL
    | QUERY_MAX_RESPONSE_TIME
    | REPORT_FLOOD
    | REPORT_POLICY
    | REPORT_SUPPRESSION
    | ROBUSTNESS_VARIABLE
    | STARTUP_QUERY_COUNT
    | STARTUP_QUERY_INTERVAL
    | STATIC_GROUP
    | V3_REPORT_SUPPRESSION
    | VERSION
  ) null_rest_of_line
;

vlan_null
:
  NO?
  (
    ACCESS_MAP
    | DOT1Q
    | FILTER
  ) null_rest_of_line
;

vlan_vlan
:
  vlans = unreserved_vlan_id_range NEWLINE
  (
    vv_null
    | vv_vn_segment
  )*
;

vv_null
:
  NO?
  (
    MEDIA
    | NAME
    | SHUTDOWN
    | STATE
    | XCONNECT
  ) null_rest_of_line
;

vv_vn_segment
:
  VN_SEGMENT vni_number NEWLINE
;
