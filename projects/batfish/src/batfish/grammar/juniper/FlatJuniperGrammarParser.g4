parser grammar FlatJuniperGrammarParser;

import
FlatJuniperGrammarCommonParser, FlatJuniperGrammar_interfaces, FlatJuniperGrammar_isis, FlatJuniperGrammar_mpls, FlatJuniperGrammar_ospf, FlatJuniperGrammar_policy_options, FlatJuniperGrammar_routing_instances;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = FlatJuniperGrammarLexer;
}

@header {
package batfish.grammar.juniper;
}

configuration_ignored_substatement
:
   (
      CHASSIS
      | SYSTEM
   ) ~NEWLINE*
;

configuration_statement
:
   configuration_ignored_substatement
   | interfaces_statement
   | policy_options_statement
   | protocols_statement
   | routing_instances_statement
;

groups_header
:
   GROUPS name = VARIABLE
;

groups_statement
:
   groups_header groups_tail
;

groups_tail
:
   configuration_statement
;

flat_juniper_configuration
:
   set_statement+ EOF
;

protocols_ignored_substatement
:
   PROTOCOLS
   (
      LDP
      | RSVP
   ) ~NEWLINE*
;

protocols_statement
:
   protocols_ignored_substatement
   | protocols_isis_statement
   | protocols_mpls_statement
   | protocols_ospf_statement
;

set_statement
:
   SET set_tail NEWLINE
;

set_tail
:
   groups_statement
   | configuration_statement
   | version_statement
;

version_statement
:
   VERSION M_Version_VERSION_STRING
;

