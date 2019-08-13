parser grammar CiscoNxos_policy_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_policy_map
:
  POLICY_MAP
  (
    pm_control_plane
    | pm_network_qos
    | pm_qos
  )
;

pm_control_plane
:
// type mandatory
  TYPE CONTROL_PLANE name = policy_map_name NEWLINE pmcp_class*
;

pmcp_class
:
  CLASS name = class_map_name NEWLINE
  (
    pmcpc_police
    | pmcpc_set
  )*
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
  TYPE NETWORK_QOS name = policy_map_name NEWLINE pmnq_class*
;

pmnq_class
:
// type mandatory
  CLASS TYPE NETWORK_QOS name = class_map_name NEWLINE pmnqc_mtu
;

pmnqc_mtu
:
  MTU qos_mtu NEWLINE
;

qos_mtu
:
// 576-9216
  uint16
;

pm_qos
:
// default type is qos
  (TYPE QOS)? name = policy_map_name NEWLINE pmq_class*
;

pmq_class
:
// type optional
  CLASS (TYPE QOS)? name = class_map_name NEWLINE pmqc_set*
;

pmqc_set
:
  SET pmqcs_qos_group
;

pmqcs_qos_group
:
  QOS_GROUP qg = qos_group NEWLINE
;

qos_group
:
// 0-7
  uint8
;
