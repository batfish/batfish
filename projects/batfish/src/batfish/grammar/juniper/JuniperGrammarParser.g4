parser grammar JuniperGrammarParser;

import
JuniperGrammarCommonParser, JuniperGrammar_firewall, JuniperGrammar_interface, JuniperGrammar_policy_options, JuniperGrammar_protocols, JuniperGrammar_ospf, JuniperGrammar_bgp, JuniperGrammar_routing_options, JuniperGrammar_system;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = JuniperGrammarLexer;
}

@header {
package batfish.grammar.juniper;
}

empty_neighbor_stanza
:
   NEIGHBOR SEMICOLON
;

groups_stanza
:
   GROUPS OPEN_BRACE
   (
      empty_neighbor_stanza
      | group_stanza
   )+ CLOSE_BRACE
;

group_stanza
:
   name = VARIABLE OPEN_BRACE gr_stanza+ CLOSE_BRACE
;

gr_stanza
:
   firewall_stanza
   | null_stanza
   | policy_options_stanza
   | protocols_stanza
   | routing_options_stanza
   | interfaces_stanza
   | system_stanza
;

juniper_configuration
:
   j_stanza+ EOF
;

j_stanza_list
:
   j_stanza+
;

j_stanza
:
   apply_groups_stanza
   | firewall_stanza
   | protocols_stanza
   | routing_options_stanza
   | groups_stanza
   | interfaces_stanza
   | policy_options_stanza
   | system_stanza
   | null_stanza
;
