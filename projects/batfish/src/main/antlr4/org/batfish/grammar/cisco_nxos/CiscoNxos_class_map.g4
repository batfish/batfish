parser grammar CiscoNxos_class_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

class_map_description
:
// 1-200 characters
  REMARK_TEXT
;

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
  QUEUING cm_queuing
;

cm_control_plane
:
  (MATCH_ALL | MATCH_ANY)? name = class_map_cp_name NEWLINE
  cmcp_match*
;

cmcp_match
:
  MATCH
  (
    cmcpm_access_group
    | cmcpm_exception
    | cmcpm_protocol
    | cmcpm_redirect
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
  PROTOCOL
  (
    ARP
    // only "exp 6" is valid
  | MPLS (EXP uint8 | ROUTER_ALERT)?
  )
  NEWLINE
;

cmcpm_redirect
:
  REDIRECT
  (
    ARP_INSPECT
  | DHCP_SNOOP
  )
  NEWLINE
;

cm_network_qos
:
  MATCH_ANY? name = class_map_network_qos_name NEWLINE
  (
    cmnq_description
    | cmnq_match
  )*
;

cmnq_description
:
  DESCRIPTION desc = class_map_description NEWLINE
;

cmnq_match
:
  MATCH
  (
    cmnqm_cos
    | cmnqm_protocol
    | cmnqm_qos_group
  )
;

cmnqm_cos
:
  COS null_rest_of_line
;

cmnqm_protocol
:
  PROTOCOL null_rest_of_line
;

cmnqm_qos_group
:
  QOS_GROUP group = qos_group NEWLINE
;

cm_qos
:
  (MATCH_ALL | MATCH_ANY)? name = class_map_qos_name NEWLINE
  (
    cmq_description
    | cmq_match
  )*
;

cmq_description
:
  DESCRIPTION class_map_description NEWLINE
;

cmq_match
:
  MATCH
  (
    cmqm_access_group
    | cmqm_dscp
    | cmqm_precedence
  )
;

cmqm_access_group
:
  ACCESS_GROUP NAME name = generic_access_list_name NEWLINE
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
  MATCH_ANY? name = class_map_queuing_name NEWLINE
  cmqu_match*
;

cmqu_match
:
  MATCH
  (
    cmqum_cos
    | cmqum_qos_group
  )
;

cmqum_cos
:
  COS null_rest_of_line
;

cmqum_qos_group
:
  QOS_GROUP group = qos_group NEWLINE
;