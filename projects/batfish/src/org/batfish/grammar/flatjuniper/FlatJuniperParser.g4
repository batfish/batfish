parser grammar FlatJuniperParser;

import
FlatJuniper_common, FlatJuniper_firewall, FlatJuniper_interfaces, FlatJuniper_policy_options, FlatJuniper_protocols, FlatJuniper_routing_instances, FlatJuniper_security;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperLexer;
}

@header {
package org.batfish.grammar.flatjuniper;
}

deactivate_line
:
   DEACTIVATE deactivate_line_tail NEWLINE
;

deactivate_line_tail
:
   (
      interface_id
      | ~NEWLINE
   )*
;

flat_juniper_configuration
:
   NEWLINE?
   (
      deactivate_line
      | set_line
   )+ NEWLINE? EOF
;

statement
:
   s_common
   | s_logical_systems
;

s_common
:
   s_apply_groups
   | s_firewall
   | s_interfaces
   | s_null
   | s_policy_options
   | s_protocols
   | s_routing_instances
   | s_routing_options
   | s_security
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
   LOGICAL_SYSTEMS
   (
      name = variable
      | WILDCARD
   ) s_logical_systems_tail
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
         | APPLICATIONS
         | ETHERNET_SWITCHING_OPTIONS
         | POE
         | SWITCH_OPTIONS
         | VIRTUAL_CHASSIS
      ) s_null_filler
   )
   | rit_null
;

s_system
:
   SYSTEM s_system_tail
;

s_system_tail
:
   st_default_address_selection
   | st_host_name
   | st_null
;

s_version
:
   VERSION M_Version_VERSION_STRING
;

s_vlans
:
// intentional blank

   | VLANS s_vlans_named
;

s_vlans_named
:
   name = variable s_vlans_tail
;

s_vlans_tail
:
//    intentional blank

   | vlt_description
   | vlt_filter
   | vlt_l3_interface
   | vlt_vlan_id
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

st_default_address_selection
:
   DEFAULT_ADDRESS_SELECTION
;

st_host_name
:
   HOST_NAME variable
;

st_null
:
   (
      ACCOUNTING
      | ARP
      | AUTHENTICATION_ORDER
      | BACKUP_ROUTER
      | COMMIT
      | DDOS_PROTECTION
      | DOMAIN_NAME
      | DOMAIN_SEARCH
      | INTERNET_OPTIONS
      | LICENSE
      | LOCATION
      | LOGIN
      | MAX_CONFIGURATIONS_ON_FLASH
      | MAX_CONFIGURATION_ROLLBACKS
      | NAME_RESOLUTION
      | NAME_SERVER
      | NO_REDIRECTS
      | NTP
      | PORTS
      | PROCESSES
      | RADIUS_OPTIONS
      | RADIUS_SERVER
      | ROOT_AUTHENTICATION
      | SCRIPTS
      | SERVICES
      | SYSLOG
      | TACPLUS_SERVER
      | TIME_ZONE
   ) s_null_filler
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

vlt_l3_interface
:
   L3_INTERFACE interface_id
;

vlt_vlan_id
:
   VLAN_ID name = variable
;
