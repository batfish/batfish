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
    vlan_action_null
    | vlan_backupcrf_null
    | vlan_bridge_null
    | vlan_match_null
    | vlan_media_null
    | vlan_mtu_null
    | vlan_multicast_null
    | vlan_name_null
    | vlan_parent_null
    | vlan_priority_null
    | vlan_private_vlan_null
    | vlan_remote_span_null
    | vlan_router_interface_null
    | vlan_shutdown_null
    | vlan_spanning_tree_null
    | vlan_state_null
    | vlan_statistics_null
    | vlan_stp_null
    | vlan_tagged_null
    | vlan_tb_vlan1_null
    | vlan_tb_vlan2_null
    | vlan_trunk_null
    | vlan_untagged_null
    | vlan_vn_segment
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
    vlan_action_null
    | vlan_backupcrf_null
    | vlan_bridge_null
    | vlan_match_null
    | vlan_media_null
    | vlan_mtu_null
    | vlan_multicast_null
    | vlan_name_null
    | vlan_parent_null
    | vlan_priority_null
    | vlan_private_vlan_null
    | vlan_remote_span_null
    | vlan_router_interface_null
    | vlan_shutdown_null
    | vlan_spanning_tree_null
    | vlan_state_null
    | vlan_statistics_null
    | vlan_stp_null
    | vlan_tagged_null
    | vlan_tb_vlan1_null
    | vlan_tb_vlan2_null
    | vlan_trunk_null
    | vlan_untagged_null
    | vlan_vn_segment
  )*
;

vlan_action_null: NO? ACTION null_rest_of_line;
vlan_backupcrf_null: NO? BACKUPCRF null_rest_of_line;
vlan_bridge_null: NO? BRIDGE null_rest_of_line;
vlan_match_null: NO? MATCH null_rest_of_line;
vlan_media_null: NO? MEDIA null_rest_of_line;
vlan_mtu_null: NO? MTU null_rest_of_line;
vlan_multicast_null: NO? MULTICAST null_rest_of_line;
vlan_name_null: NO? NAME null_rest_of_line;
vlan_parent_null: NO? PARENT null_rest_of_line;
vlan_priority_null: NO? PRIORITY null_rest_of_line;
vlan_private_vlan_null: NO? PRIVATE_VLAN null_rest_of_line;
vlan_remote_span_null: NO? REMOTE_SPAN null_rest_of_line;
vlan_router_interface_null: NO? ROUTER_INTERFACE null_rest_of_line;
vlan_shutdown_null: NO? SHUTDOWN null_rest_of_line;
vlan_spanning_tree_null: NO? SPANNING_TREE null_rest_of_line;
vlan_state_null: NO? STATE null_rest_of_line;
vlan_statistics_null: NO? STATISTICS null_rest_of_line;
vlan_stp_null: NO? STP null_rest_of_line;
vlan_tagged_null: NO? TAGGED null_rest_of_line;
vlan_trunk_null: NO? TRUNK null_rest_of_line;
vlan_tb_vlan1_null: NO? TB_VLAN1 null_rest_of_line;
vlan_tb_vlan2_null: NO? TB_VLAN2 null_rest_of_line;
vlan_untagged_null: NO? UNTAGGED null_rest_of_line;

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
    vlan_action_null
    | vlan_backupcrf_null
    | vlan_bridge_null
    | vlan_match_null
    | vlan_media_null
    | vlan_mtu_null
    | vlan_multicast_null
    | vlan_name_null
    | vlan_parent_null
    | vlan_priority_null
    | vlan_private_vlan_null
    | vlan_remote_span_null
    | vlan_router_interface_null
    | vlan_shutdown_null
    | vlan_spanning_tree_null
    | vlan_state_null
    | vlan_statistics_null
    | vlan_stp_null
    | vlan_tagged_null
    | vlan_tb_vlan1_null
    | vlan_tb_vlan2_null
    | vlan_trunk_null
    | vlan_untagged_null
    | vlan_vn_segment
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
    vlan_action_null
    | vlan_backupcrf_null
    | vlan_bridge_null
    | vlan_match_null
    | vlan_media_null
    | vlan_mtu_null
    | vlan_multicast_null
    | vlan_name_null
    | vlan_parent_null
    | vlan_priority_null
    | vlan_private_vlan_null
    | vlan_remote_span_null
    | vlan_router_interface_null
    | vlan_shutdown_null
    | vlan_spanning_tree_null
    | vlan_state_null
    | vlan_statistics_null
    | vlan_stp_null
    | vlan_tagged_null
    | vlan_tb_vlan1_null
    | vlan_tb_vlan2_null
    | vlan_trunk_null
    | vlan_untagged_null
    | vlan_vn_segment
  )*
;
