parser grammar FlatJuniperGrammar_routing_instances;

import FlatJuniperGrammarCommonParser;

options {
   tokenVocab = FlatJuniperGrammarLexer;
}

martians_rot
:
   MARTIANS ~NEWLINE*
;

routing_instances_header
:
   ROUTING_INSTANCES
   (
      wildcard
      | name = VARIABLE
   )
;

routing_instances_statement
:
   routing_instances_header routing_instances_tail
;

routing_instances_tail
:
   routing_options_rit
;

routing_options_rit
:
   ROUTING_OPTIONS routing_options_rit_tail
;

routing_options_rit_tail
:
   martians_rot
;
