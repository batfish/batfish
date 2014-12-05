parser grammar FlatJuniperGrammarParser;

import
FlatJuniperGrammarCommonParser, FlatJuniperGrammar_bgp, FlatJuniperGrammar_firewall, FlatJuniperGrammar_interfaces, FlatJuniperGrammar_isis, FlatJuniperGrammar_mpls, FlatJuniperGrammar_ospf, FlatJuniperGrammar_policy_options, FlatJuniperGrammar_routing_instances;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperGrammarLexer;
}

@header {
package batfish.grammar.flatjuniper;
}

deactivate_line
:
   DEACTIVATE set_line_tail NEWLINE
;

flat_juniper_configuration
:
   (
      deactivate_line
      | set_line
   )+ EOF
;

statement
:
   s_firewall
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
   statement
;

s_null
:
   (
      CHASSIS
      | CLASS_OF_SERVICE
      | EVENT_OPTIONS
      | FORWARDING_OPTIONS
      | SNMP
   ) ~NEWLINE*
;

s_protocols
:
   PROTOCOLS s_protocols_tail
;

s_protocols_tail
:
   s_protocols_bgp
   | s_protocols_isis
   | s_protocols_mpls
   | s_protocols_null
   | s_protocols_ospf
;

s_protocols_null
:
   (
      BFD
      | LDP
      | LLDP
      | PIM
      | RSVP
   ) ~NEWLINE*
;

s_system
:
   SYSTEM s_system_tail
;

s_system_tail
:
   st_host_name
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

st_host_name
:
   HOST_NAME variable
;

st_null
:
   (
      ACCOUNTING
      | AUTHENTICATION_ORDER
      | DOMAIN_NAME
      | DOMAIN_SEARCH
      | LOGIN
      | NAME_SERVER
      | NTP
      | PORTS
      | ROOT_AUTHENTICATION
      | SERVICES
      | SYSLOG
      | TACPLUS_SERVER
      | TIME_ZONE
   ) ~NEWLINE*
;
