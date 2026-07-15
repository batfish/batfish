parser grammar CiscoNxos_policy_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

policy_map_description
:
// 1-200 characters
  REMARK_TEXT
;

s_policy_map
:
  POLICY_MAP
  (
    pm_type
    | pm_qos // default type is qos
  )
;

pm_type
:
  TYPE
  (
     pmt_control_plane
     | pmt_network_qos
     | pmt_qos
     | pmt_queuing
   )
;

pmt_control_plane
:
  CONTROL_PLANE pm_control_plane
;

pmt_network_qos
:
  NETWORK_QOS pm_network_qos
;

pmt_qos
:
  QOS pm_qos
;

pmt_queuing
:
  QUEUING pm_queuing
;

pm_control_plane
:
  name = policy_map_cp_name NEWLINE
  pmcp_class*
;

pmcp_class
:
  CLASS name = class_map_cp_name NEWLINE
  (
    pmcpc_logging
    | pmcpc_police
    | pmcpc_set
  )*
;

pmcpc_logging
:
// threshold: 1-80000000000 (80 billion)
// level: 1-7
  LOGGING DROP THRESHOLD threshold = uint64 (LEVEL level = uint8)? NEWLINE
;

pmcpc_police
:
// semantics valid completions differ heavily with NX-OS version
  POLICE null_rest_of_line
;

pmcpc_set
:
  SET null_rest_of_line
;

pm_network_qos
:
  name = policy_map_network_qos_name NEWLINE
  (
    pmnq_class
    | pmnq_description
  )*
;

pmnq_class
:
// type mandatory
  CLASS TYPE NETWORK_QOS name = class_map_network_qos_name NEWLINE
  (
    pmnqc_congestion_control_null
    | pmnqc_mtu
    | pmnqc_no
    | pmnqc_pause_null
  )*
;

pmnqc_mtu
:
  MTU qos_mtu NEWLINE
;

pmnqc_no
:
  NO ( pmnqc_no_congestion_control_null | pmnqc_no_pause_null )
;

pmnqc_no_congestion_control_null
:
   CONGESTION_CONTROL null_rest_of_line
;
pmnqc_no_pause_null
:
   PAUSE null_rest_of_line
;

pmnqc_congestion_control_null
:
   CONGESTION_CONTROL null_rest_of_line
;
pmnqc_pause_null
:
   PAUSE null_rest_of_line
;

pmnq_description
:
  DESCRIPTION policy_map_description NEWLINE
;

qos_mtu
:
// 576-9216
  uint16
;

pm_qos
:
  name = policy_map_qos_name NEWLINE
  (
    pmq_class
    | pmq_description
  )*
;

pmq_class
:
// type optional
  CLASS (TYPE QOS)? name = class_map_qos_name NEWLINE
  pmqc_set*
;

pmqc_set
:
  SET pmqcs_qos_group
;

pmqcs_qos_group
:
  QOS_GROUP qg = qos_group NEWLINE
;

pmq_description
:
  DESCRIPTION policy_map_description NEWLINE
;

pm_queuing
:
  name = policy_map_queuing_name NEWLINE
  pmqu_class*
;

pmqu_class
:
  CLASS TYPE QUEUING name = class_map_queuing_name NEWLINE
  ( pmquc_bandwidth_null | pmquc_pause_null | pmquc_priority_null | pmquc_queue_limit_null | pmquc_random_detect_null | pmquc_shape_null )*
;

pmquc_bandwidth_null
:
   BANDWIDTH null_rest_of_line
;
pmquc_pause_null
:
   PAUSE null_rest_of_line
;
pmquc_priority_null
:
   PRIORITY null_rest_of_line
;
pmquc_queue_limit_null
:
   QUEUE_LIMIT null_rest_of_line
;
pmquc_random_detect_null
:
   RANDOM_DETECT null_rest_of_line
;
pmquc_shape_null
:
   SHAPE null_rest_of_line
;
