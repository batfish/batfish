parser grammar FlatJuniperParser;

import

FlatJuniper_applications, FlatJuniper_bridge_domains, FlatJuniper_class_of_service, FlatJuniper_common, FlatJuniper_fabric, FlatJuniper_firewall, FlatJuniper_forwarding_options, FlatJuniper_interfaces, FlatJuniper_policy_options, FlatJuniper_protocols, FlatJuniper_routing_instances, FlatJuniper_security, FlatJuniper_snmp, FlatJuniper_switch_options, FlatJuniper_system;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperLexer;
}

activate_line
:
   ACTIVATE activate_line_tail NEWLINE
;

deactivate_line
:
   DEACTIVATE deactivate_line_tail NEWLINE
;

hierarchy_element:
  interface_id
  | ~NEWLINE
;


activate_line_tail
:
   hierarchy_element*
;

deactivate_line_tail
:
   hierarchy_element*
;

delete_line
:
   DELETE delete_line_tail NEWLINE
;

replace_line
:
   // Replace abuses delete_line_tail since we will delete everything anyway.
   REPLACE delete_line_tail NEWLINE
;

delete_line_tail
:
   hierarchy_element*
;

insert_line
:
  INSERT insert_src (AFTER|BEFORE) insert_dst NEWLINE
;

insert_src
:
  insert_src_element+
;

insert_src_element
:
  interface_id
  |
  ~(
    NEWLINE
    | AFTER
    | BEFORE
  )
;

insert_dst
:
  hierarchy_element name=hierarchy_element
;

flat_juniper_configuration
:
   (
      activate_line
      | deactivate_line
      | delete_line
      | insert_line
      | protect_line
      | replace_line
      | set_line
      | newline
   )+ EOF
;

newline
:
   NEWLINE
;

protect_line
:
   PROTECT protect_line_tail NEWLINE
;

protect_line_tail
:
   hierarchy_element*
;

statement
:
   s_common
   | s_logical_systems
;

s_common
:
   s_access
   | s_applications
   | apply_groups
   | s_bridge_domains
   | s_class_of_service
   | s_fabric
   | s_firewall
   | s_forwarding_options
   | s_interfaces
   | s_null
   | s_policy_options
   | s_protocols
   | s_routing_instances
   | s_routing_options
   | s_security
   | s_snmp
   | s_switch_options
   | s_system
   | s_vlans
;

s_access
:
   ACCESS
   (
      sa_profile
      | sa_radius_server_null
   )
;

sa_radius_server_null
:
   RADIUS_SERVER null_filler
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/access-edit-profile.html
sa_profile
:
   PROFILE name = junos_name null_filler
;

s_groups
:
   GROUPS s_groups_named
;

s_groups_named
:
   name = junos_name s_groups_tail
;

s_groups_tail
:
// intentional blank

   | statement
;

s_logical_systems
:
   LOGICAL_SYSTEMS name = junos_name s_logical_systems_tail
;

s_logical_systems_tail
:
// intentional blank

   | statement
;

s_null
:
   (
      (
         APPLY_MACRO
         | ETHERNET_SWITCHING_OPTIONS
         | MULTI_CHASSIS
         | POE
         | VIRTUAL_CHASSIS
      ) null_filler
   )
   | ri_chassis
   | ri_event_options_null
   | ri_provider_tunnel_null
   | ri_services_null
;

s_version
:
   VERSION VERSION_STRING
;

s_vlans
:
   VLANS
   (
      apply
      | s_vlans_named
   )
;

s_vlans_named
:
  name = junos_name
  (
    apply
    | vlt_description
    | vlt_filter
    | vlt_forwarding_options
    | vlt_interface
    | vlt_isolated_vlan
    | vlt_l3_interface
    | vlt_private_vlan
    | vlt_switch_options
    | vlt_vlan_id
    | vlt_vlan_id_list
    | vlt_vni_id
  )
;

set_line
:
   SET set_line_tail NEWLINE
;

set_line_tail
:
   s_groups
   | statement
   | s_version
;

vlt_description
:
   DESCRIPTION M_Description_DESCRIPTION
;

vlt_filter
:
   FILTER
   (
      INPUT
      | OUTPUT
   ) name = filter_name
;

vlt_forwarding_options
:
   FORWARDING_OPTIONS
   (
      apply
      | vltfo_filter_null
      | vltfo_null
   )
;

// Filter applied to traffic in this VLAN. Not modeled, as with vlt_filter.
vltfo_filter_null
:
   filter
;

vltfo_null
:
   (
      DHCP_SECURITY
   ) null_filler
;

vlt_interface
:
   INTERFACE interface_id
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/isolated-vlan-bridging-ex-series.html
vlt_isolated_vlan
:
   ISOLATED_VLAN name = junos_name
;

vlt_l3_interface
:
   L3_INTERFACE interface_id
;

// https://www.juniper.net/documentation/us/en/software/junos/cli-reference/topics/ref/statement/private-vlan-bridging-ex-series.html
vlt_private_vlan
:
   PRIVATE_VLAN ISOLATED
;

vlt_switch_options
:
   SWITCH_OPTIONS
   (
      apply
      | vltso_mac_move_limit
   )
;

// How many times a MAC address may move between interfaces in this VLAN, and what to do once the
// limit is exceeded. Not modeled, as with switch-options interface-mac-limit.
vltso_mac_move_limit
:
   MAC_MOVE_LIMIT
   (
      vltsomml_interface
      | vltsomml_limit_null
      | vltsomml_packet_action_null
   )
;

vltsomml_interface
:
   INTERFACE interface_id vltsommli_action_priority_null
;

vltsommli_action_priority_null
:
   ACTION_PRIORITY priority = uint8
;

vltsomml_limit_null
:
   uint32
;

vltsomml_packet_action_null
:
   PACKET_ACTION
   (
      DROP
      | DROP_AND_LOG
      | LOG
      | NONE
      | SHUTDOWN
   )
;

vlt_vlan_id
:
   VLAN_ID id = vlan_number
;

vlt_vlan_id_list
:
   VLAN_ID_LIST vlan_range
;

vlt_vni_id
:
   VXLAN VNI vni_number
;
