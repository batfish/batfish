parser grammar CiscoNxos_class_map;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_class_map
:
  CLASS_MAP
  (
    cm_control_plane
    | cm_network_qos
    | cm_qos
  )
;

cm_control_plane
:
  TYPE CONTROL_PLANE (MATCH_ALL | MATCH_ANY)? name = class_map_name NEWLINE cmcp_match*
;

cmcp_match
:
  MATCH
  (
    cmcpm_access_group
    | cmcpm_exception
  )
;

cmcpm_access_group
:
  ACCESS_GROUP NAME name = ip_access_list_name NEWLINE
;

cmcpm_exception
:
  EXCEPTION null_rest_of_line
;

cm_network_qos
:
  TYPE NETWORK_QOS MATCH_ANY? name = class_map_name NEWLINE
;

cm_qos
:
// default type is qos
  (TYPE QOS)? (MATCH_ALL | MATCH_ANY)? name = class_map_name NEWLINE cmq_match*
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
