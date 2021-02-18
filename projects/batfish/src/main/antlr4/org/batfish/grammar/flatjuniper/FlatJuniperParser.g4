parser grammar FlatJuniperParser;

import
FlatJuniper_applications, FlatJuniper_common, FlatJuniper_fabric, FlatJuniper_firewall, FlatJuniper_forwarding_options, FlatJuniper_interfaces, FlatJuniper_policy_options, FlatJuniper_protocols, FlatJuniper_routing_instances, FlatJuniper_security, FlatJuniper_snmp, FlatJuniper_system;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperLexer;
}

deactivate_line
:
   DEACTIVATE deactivate_line_tail NEWLINE
;

hierarchy_element:
  interface_id
  | ~NEWLINE
;


deactivate_line_tail
:
   hierarchy_element*
;

delete_line
:
   DELETE delete_line_tail NEWLINE
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
      deactivate_line
      | delete_line
      | insert_line
      | protect_line
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
   PROTECT hierarchy_element* NEWLINE
;

statement
:
   s_common
   | s_logical_systems
;

s_common
:
   s_applications
   | apply_groups
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
   | s_system
   | s_vlans
;

s_groups
:
   GROUPS s_groups_named
;

s_groups_named
:
   name = variable s_groups_tail
;

s_groups_tail
:
// intentional blank

   | statement
;

s_logical_systems
:
   LOGICAL_SYSTEMS name = variable s_logical_systems_tail
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
         ACCESS
         | APPLY_MACRO
         | ETHERNET_SWITCHING_OPTIONS
         | MULTI_CHASSIS
         | POE
         | SWITCH_OPTIONS
         | VIRTUAL_CHASSIS
      ) null_filler
   )
   | ri_null
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
  name = variable
  (
    apply
    | vlt_description
    | vlt_filter
    | vlt_interface
    | vlt_l3_interface
    | vlt_vlan_id
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
   ) name = variable
;

vlt_interface
:
   INTERFACE interface_id
;

vlt_l3_interface
:
   L3_INTERFACE interface_id
;

vlt_vlan_id
:
   VLAN_ID id = dec
;
