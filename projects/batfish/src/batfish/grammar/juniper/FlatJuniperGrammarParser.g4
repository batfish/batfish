parser grammar FlatJuniperGrammarParser;

import
FlatJuniperGrammarCommonParser, FlatJuniperGrammar_bgp, FlatJuniperGrammar_interfaces, FlatJuniperGrammar_isis, FlatJuniperGrammar_mpls, FlatJuniperGrammar_ospf, FlatJuniperGrammar_policy_options, FlatJuniperGrammar_routing_instances;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperGrammarLexer;
}

@header {
package batfish.grammar.juniper;
}

flat_juniper_configuration
:
   set_line+ EOF
;

statement
:
   s_interfaces
   | s_null
   | s_policy_options
   | s_protocols
   | s_routing_instances
   | s_routing_options
;

s_groups
:
   s_groups_header s_groups_tail
;

s_groups_header
:
   GROUPS name = VARIABLE
;

s_groups_tail
:
   statement
;

s_null
:
   (
      CHASSIS
      | EVENT_OPTIONS
      | FORWARDING_OPTIONS
      | SNMP
      | SYSTEM
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
   PROTOCOLS
   (
      LDP
      | RSVP
   ) ~NEWLINE*
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
