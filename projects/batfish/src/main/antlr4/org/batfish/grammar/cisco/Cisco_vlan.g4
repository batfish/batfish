parser grammar Cisco_vlan;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_vlan
:
  VLAN
  (
    vlan_access_map
    | vlan_configuration
    | vlan_internal
    | vlan_numbered
  )
;

vlan_access_map
:
  ACCESS_MAP null_rest_of_line
  (
    vlan_vn_segment
    | vlan_null
  )*
;

vlan_configuration
:
  CONFIGURATION vlan_range NEWLINE
  (
    vlanc_device_tracking
  )*
;

vlanc_device_tracking
:
  DEVICE_TRACKING ATTACH_POLICY name = device_tracking_policy_name NEWLINE
;

vlan_internal
:
  INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) NEWLINE
;

vlan_numbered
:
  variable_vlan? dec null_rest_of_line
  (
    vlan_vn_segment
    | vlan_null
  )*
;

vlan_null
:
  NO?
  (
    ACTION
    | BACKUPCRF
    | BRIDGE
    | MATCH
    | MEDIA
    | MTU
    | MULTICAST
    | NAME
    | PARENT
    | PRIORITY
    | PRIVATE_VLAN
    | REMOTE_SPAN
    | ROUTER_INTERFACE
    | SHUTDOWN
    | SPANNING_TREE
    | STATE
    | STATISTICS
    | STP
    | TAGGED
    | TRUNK
    | TB_VLAN1
    | TB_VLAN2
    | UNTAGGED
  ) null_rest_of_line
;

vlan_vn_segment
:
  VN_SEGMENT vni = dec NEWLINE
;

s_vlan_name
:
  VLAN_NAME name = variable_permissive NEWLINE
;

no_vlan
:
  VLAN
  (
    no_vlan_access_map
    | no_vlan_internal
    | no_vlan_numbered
  )
;

no_vlan_access_map
:
  ACCESS_MAP null_rest_of_line
  (
    vlan_vn_segment
    | vlan_null
  )*
;

no_vlan_internal
:
  INTERNAL ALLOCATION POLICY (ASCENDING | DESCENDING) NEWLINE
;

no_vlan_numbered
:
  variable_vlan? dec null_rest_of_line
  (
    vlan_vn_segment
    | vlan_null
  )*
;
