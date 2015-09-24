parser grammar FlatJuniperParser;

import
FlatJuniper_common, FlatJuniper_firewall, FlatJuniper_interfaces, FlatJuniper_policy_options, FlatJuniper_protocols, FlatJuniper_routing_instances;

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
   | s_system
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
         SECURITY
         | SWITCH_OPTIONS
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
      | NAME_SERVER
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
