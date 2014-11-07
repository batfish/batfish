parser grammar JuniperGrammarCommonParser;

options {
   tokenVocab = JuniperGrammarLexer;
}

apply_groups_stanza //TODO [P0]: DO NOT IGNORE

:
   (
      APPLY_GROUPS
      | APPLY_GROUPS_EXCEPT
   ) group_name = variable_list SEMICOLON
;

as_id
:
   AS_NUM
;

bfd_liveness_detection_common_stanza
:
   BFD_LIVENESS_DETECTION ignored_substanza
;

bridge_domains_stanza
:
   BRIDGE_DOMAINS ignored_substanza
;

chassis_stanza
:
   CHASSIS ignored_substanza
;

class_of_service_stanza
:
   CLASS_OF_SERVICE ignored_substanza
;

community_literal
:
   as_id
   |
   (
      ASTERISK COLON ASTERISK
   )
   |
   (
      ASTERISK COLON i = DEC
   )
   | NO_EXPORT
   | string_in_double_quotes
;

community_literal_list
:
   elements += community_literal
   |
   (
      OPEN_BRACKET
      (
         elements += community_literal
      )+ CLOSE_BRACKET
   )
;

description_common_stanza
:
   DESCRIPTION
   (
      (
         string_in_double_quotes SEMICOLON
      )
      | string_up_to_semicolon
   )
;

double_num
:
   DEC
   | HEX
;

encapsulation_common_stanza
:
   ENCAPSULATION VARIABLE SEMICOLON
;

empty_neighbor_stanza
:
   NEIGHBOR SEMICOLON
;

forwarding_options_stanza
:
   FORWARDING_OPTIONS ignored_substanza
;

ignored_substanza
:
   OPEN_BRACE substanza* CLOSE_BRACE
;

integer_list
:
   elements += DEC
   |
   (
      OPEN_BRACKET
      (
         elements += DEC
      )+ CLOSE_BRACKET
   )
;

log_updown_common_stanza
:
   LOG_UPDOWN SEMICOLON
;

metric_out_common_stanza
:
   METRIC_OUT
   (
      VARIABLE
      | IGP
   ) SEMICOLON
;

mtu_common_stanza
:
   MTU DEC SEMICOLON
;

multihop_common_stanza
:
   MULTIHOP ignored_substanza
;

not_brace
:
   ~( OPEN_BRACE | CLOSE_BRACE )
;

null_stanza
:
   bridge_domains_stanza
   | chassis_stanza
   | class_of_service_stanza
   | empty_neighbor_stanza
   | forwarding_options_stanza
   | routing_instances_stanza
   | security_stanza
   | services_stanza
   | version_stanza
   | removed_top_level_stanza
;

protocol
:
   AGGREGATE
   | BGP
   | DIRECT
   | ISIS
   | MSDP
   | OSPF
   | STATIC
;

protocol_list
:
   elements += protocol
   |
   (
      OPEN_BRACKET
      (
         elements += protocol
      )+ CLOSE_BRACKET
   )
;

remove_private_common_stanza
:
   REMOVE_PRIVATE SEMICOLON
;

removed_stanza
:
   name = VARIABLE
   (
      DATA_REMOVED
      | STANZA_REMOVED
   )
;

removed_top_level_stanza
:
   VARIABLE
   (
      DATA_REMOVED
      | STANZA_REMOVED
   ) CLOSE_BRACE?
;

rib_common_stanza
:
   RIB VARIABLE SEMICOLON
;

routing_instances_stanza // TODO [Ask Ari]: probably don't ignore 

:
   ROUTING_INSTANCES ignored_substanza
;

security_stanza
:
   SECURITY ignored_substanza
;

services_stanza
:
   SERVICES ignored_substanza
;

string_in_double_quotes
:
   DOUBLE_QUOTED_STRING
;

string_up_to_semicolon
:
   (
      VARIABLE
      | IPV6_ADDRESS
      | COLON
      | DEC
      | FORWARD_SLASH
   )+ SEMICOLON
;

substanza
:
   not_brace
   |
   (
      OPEN_BRACE substanza+ CLOSE_BRACE
   )
   |
   (
      OPEN_BRACE CLOSE_BRACE
   )
;

variable_list
:
   elements += VARIABLE
   |
   (
      OPEN_BRACKET
      (
         elements += VARIABLE
      )+ CLOSE_BRACKET
   )
;

version_stanza
:
   VERSION VERSION_TOKEN SEMICOLON
;
