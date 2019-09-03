parser grammar CiscoNxos_class_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_class_map
:
  CLASS_MAP
  (
    cm_type
    | cm_qos // default type is qos
  )
;

cm_type
:
  TYPE
  (
    cmt_control_plane
    | cmt_network_qos
    | cmt_qos
    | cmt_queuing
  )
;

cmt_control_plane
:
  CONTROL_PLANE cm_control_plane
;

cmt_network_qos
:
  NETWORK_QOS cm_network_qos
;

cmt_qos
:
 QOS cm_qos
;

cmt_queuing
:
 QOS cm_queuing
;

cm_control_plane
:
  (MATCH_ALL | MATCH_ANY)? name = class_map_name NEWLINE cmcp_match*
;

cmcp_match
:
  MATCH
  (
    cmcpm_access_group
    | cmcpm_exception
    | cmcpm_protocol
  )
;

cmcpm_access_group
:
  ACCESS_GROUP NAME name = generic_access_list_name NEWLINE
;

cmcpm_exception
:
  EXCEPTION null_rest_of_line
;

cmcpm_protocol
:
  PROTOCOL ARP NEWLINE
;

cm_network_qos
:
  MATCH_ANY? name = class_map_name NEWLINE
;

cm_qos
:
  (MATCH_ALL | MATCH_ANY)? name = class_map_name NEWLINE cmq_match*
;

cmq_match
:
  MATCH
  (
    cmqm_dscp
    | cmqm_precedence
  )
;

cmqm_dscp
:
  DSCP null_rest_of_line
;

cmqm_precedence
:
  PRECEDENCE null_rest_of_line
;

cm_queuing
:
  MATCH_ANY? name = class_map_name NEWLINE
// TODO  cmqu_match*
;