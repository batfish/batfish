parser grammar CiscoNxos_vlan;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_vlan
:
  VLAN
  (
    vlan_access_map_null
    | vlan_configuration
    | vlan_dot1q_null
    | vlan_filter_null
    | vlan_vlan
  )
;

vlan_configuration
:
  CONFIGURATION range = vlan_id_range NEWLINE
    (
      vlanc_access_group_null
      | vlanc_explicit_tracking_null
      | vlanc_fast_leave_null
      | vlanc_group_timeout_null
      | vlanc_last_member_query_interval_null
      | vlanc_link_local_groups_suppression_null
      | vlanc_mrouter_null
      | vlanc_proxy_leave_null
      | vlanc_proxy_null
      | vlanc_querier_null
      | vlanc_querier_timeout_null
      | vlanc_query_interval_null
      | vlanc_query_max_response_time_null
      | vlanc_report_flood_null
      | vlanc_report_policy_null
      | vlanc_report_suppression_null
      | vlanc_robustness_variable_null
      | vlanc_service_policy
      | vlanc_startup_query_count_null
      | vlanc_startup_query_interval_null
      | vlanc_static_group_null
      | vlanc_v3_report_suppression_null
      | vlanc_version_null
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

vlanc_access_group_null: NO? ACCESS_GROUP null_rest_of_line;
vlanc_explicit_tracking_null: NO? EXPLICIT_TRACKING null_rest_of_line;
vlanc_fast_leave_null: NO? FAST_LEAVE null_rest_of_line;
vlanc_group_timeout_null: NO? GROUP_TIMEOUT null_rest_of_line;
vlanc_last_member_query_interval_null: NO? LAST_MEMBER_QUERY_INTERVAL null_rest_of_line;
vlanc_link_local_groups_suppression_null: NO? LINK_LOCAL_GROUPS_SUPPRESSION null_rest_of_line;
vlanc_mrouter_null: NO? MROUTER null_rest_of_line;
vlanc_proxy_null: NO? PROXY null_rest_of_line;
vlanc_proxy_leave_null: NO? PROXY_LEAVE null_rest_of_line;
vlanc_querier_null: NO? QUERIER null_rest_of_line;
vlanc_querier_timeout_null: NO? QUERIER_TIMEOUT null_rest_of_line;
vlanc_query_interval_null: NO? QUERY_INTERVAL null_rest_of_line;
vlanc_query_max_response_time_null: NO? QUERY_MAX_RESPONSE_TIME null_rest_of_line;
vlanc_report_flood_null: NO? REPORT_FLOOD null_rest_of_line;
vlanc_report_policy_null: NO? REPORT_POLICY null_rest_of_line;
vlanc_report_suppression_null: NO? REPORT_SUPPRESSION null_rest_of_line;
vlanc_robustness_variable_null: NO? ROBUSTNESS_VARIABLE null_rest_of_line;
vlanc_startup_query_count_null: NO? STARTUP_QUERY_COUNT null_rest_of_line;
vlanc_startup_query_interval_null: NO? STARTUP_QUERY_INTERVAL null_rest_of_line;
vlanc_static_group_null: NO? STATIC_GROUP null_rest_of_line;
vlanc_v3_report_suppression_null: NO? V3_REPORT_SUPPRESSION null_rest_of_line;
vlanc_version_null: NO? VERSION null_rest_of_line;

vlan_access_map_null: NO? ACCESS_MAP null_rest_of_line;
vlan_dot1q_null: NO? DOT1Q null_rest_of_line;
vlan_filter_null: NO? FILTER null_rest_of_line;

vlan_vlan
:
  vlans = unreserved_vlan_id_range NEWLINE
  (
    vv_media_null
    | vv_name_null
    | vv_shutdown_null
    | vv_state_null
    | vv_xconnect_null
    | vv_vn_segment
  )*
;

vv_media_null: NO? MEDIA null_rest_of_line;
vv_name_null: NO? NAME null_rest_of_line;
vv_shutdown_null: NO? SHUTDOWN null_rest_of_line;
vv_state_null: NO? STATE null_rest_of_line;
vv_xconnect_null: NO? XCONNECT null_rest_of_line;

vv_vn_segment
:
  VN_SEGMENT vni_number NEWLINE
;
