parser grammar QuestionParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = QuestionLexer;
}

@header {
package org.batfish.grammar.question;
}

add_ip_statement
:
   target = VARIABLE PERIOD ADD_IP OPEN_PAREN ip_expr CLOSE_PAREN SEMICOLON
;

add_string_statement
:
   target = VARIABLE PERIOD ADD_STRING OPEN_PAREN printable_expr CLOSE_PAREN SEMICOLON
;

and_expr
:
   AND OPEN_BRACE conjuncts += boolean_expr
   (
      COMMA conjuncts += boolean_expr
   )* CLOSE_BRACE
;

assertion
:
   ASSERT OPEN_BRACE boolean_expr CLOSE_BRACE
   (
      ONFAILURE OPEN_BRACE env_entries += assertion_failure_env_entry
      (
         COMMA env_entries += assertion_failure_env_entry
      )* CLOSE_BRACE
   )?
;

assertion_failure_env_entry
:
   VARIABLE COLON printable_expr
;

assignment
:
   int_assignment
;

bgp_neighbor_boolean_expr
:
   BGP_NEIGHBOR PERIOD bgp_neighbor_has_generated_route_boolean_expr
;

bgp_neighbor_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

bgp_neighbor_int_expr
:
   BGP_NEIGHBOR PERIOD
   (
      bgp_neighbor_local_as_int_expr
      | bgp_neighbor_remote_as_int_expr
   )
;

bgp_neighbor_ip_expr
:
   BGP_NEIGHBOR PERIOD
   (
      bgp_neighbor_local_ip_ip_expr
      | bgp_neighbor_remote_ip_ip_expr
   )
;

bgp_neighbor_local_as_int_expr
:
   LOCAL_AS
;

bgp_neighbor_local_ip_ip_expr
:
   LOCAL_IP
;

bgp_neighbor_remote_as_int_expr
:
   REMOTE_AS
;

bgp_neighbor_remote_ip_ip_expr
:
   REMOTE_IP
;

bgp_neighbor_statement
:
   foreach_generated_route_statement
   | statement
;

boolean_expr
:
   and_expr
   | contains_ip_expr
   | eq_expr
   | gt_expr
   | if_expr
   | false_expr
   | neq_expr
   | not_expr
   | or_expr
   | property_boolean_expr
   | true_expr
;

clear_ips_statement
:
   caller = VARIABLE PERIOD CLEAR_IPS SEMICOLON
;

clear_strings_statement
:
   caller = VARIABLE PERIOD CLEAR_STRINGS SEMICOLON
;

contains_ip_expr
:
   caller = VARIABLE PERIOD CONTAINS_IP OPEN_PAREN ip_expr CLOSE_PAREN
;

eq_expr
:
   lhs = int_expr DOUBLE_EQUALS rhs = int_expr
;

failure_question
:
   FAILURE
;

false_expr
:
   FALSE
;

foreach_bgp_neighbor_statement
:
   FOREACH BGP_NEIGHBOR OPEN_BRACE bgp_neighbor_statement+ CLOSE_BRACE
;

foreach_generated_route_statement
:
   FOREACH GENERATED_ROUTE OPEN_BRACE generated_route_statement+ CLOSE_BRACE
;

foreach_interface_statement
:
   FOREACH INTERFACE OPEN_BRACE interface_statement+ CLOSE_BRACE
;

foreach_node_bgp_generated_route_statement
:
   FOREACH NODE PERIOD BGP PERIOD GENERATED_ROUTE OPEN_BRACE
   generated_route_statement+ CLOSE_BRACE
;

foreach_node_statement
:
   FOREACH NODE OPEN_BRACE node_statement+ CLOSE_BRACE
;

foreach_static_route_statement
:
   FOREACH STATIC_ROUTE OPEN_BRACE static_route_statement+ CLOSE_BRACE
;

generated_route_prefix_expr
:
   GENERATED_ROUTE PERIOD generated_route_prefix_prefix_expr
;

generated_route_prefix_prefix_expr
:
   PREFIX
;

generated_route_statement
:
   statement
;

gt_expr
:
   lhs = int_expr GT rhs = int_expr
;

if_expr
:
   IF OPEN_PAREN antecedent = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   consequent = boolean_expr CLOSE_BRACE
;

if_statement
:
   IF OPEN_PAREN guard = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   (
      true_statements += statement
   )* CLOSE_BRACE
   (
      ELSE OPEN_BRACE
      (
         false_statements += statement
      )* CLOSE_BRACE
   )?
;

ingress_path_question
:
  INGRESS_PATH
;

int_assignment
:
   VARIABLE COLON_EQUALS int_expr SEMICOLON
;

int_expr
:
   multiplicand1 = int_expr ASTERISK multiplicand2 = int_expr
   | dividend = int_expr FORWARD_SLASH divisor = int_expr
   | addend1 = int_expr PLUS addend2 = int_expr
   | subtrahend = int_expr MINUS minuend = int_expr
   | OPEN_PAREN parenthesized = int_expr CLOSE_PAREN
   | val_int_expr
;

interface_boolean_expr
:
   INTERFACE PERIOD
   (
      interface_enabled_boolean_expr
      | interface_has_ip_boolean_expr
      | interface_isis_boolean_expr
      | interface_isloopback_boolean_expr
      | interface_ospf_boolean_expr
   )
;

interface_enabled_boolean_expr
:
   ENABLED
;

interface_has_ip_boolean_expr
:
   HAS_IP
;

interface_ip_expr
:
   INTERFACE PERIOD interface_ip_ip_expr
;

interface_ip_ip_expr
:
   IP
;

interface_isis_l1_active_boolean_expr
:
   L1_ACTIVE
;

interface_isis_l1_passive_boolean_expr
:
   L1_PASSIVE
;

interface_isis_l2_active_boolean_expr
:
   L2_ACTIVE
;

interface_isis_l2_passive_boolean_expr
:
   L2_PASSIVE
;

interface_isis_boolean_expr
:
   ISIS PERIOD
   (
      interface_isis_l1_active_boolean_expr
      | interface_isis_l1_passive_boolean_expr
      | interface_isis_l2_active_boolean_expr
      | interface_isis_l2_passive_boolean_expr
   )
;

interface_isloopback_boolean_expr
:
   IS_LOOPBACK
;

interface_name_string_expr
:
   NAME
;

interface_ospf_active_boolean_expr
:
   ACTIVE
;

interface_ospf_passive_boolean_expr
:
   PASSIVE
;

interface_ospf_boolean_expr
:
   OSPF PERIOD
   (
      interface_ospf_active_boolean_expr
      | interface_ospf_passive_boolean_expr
   )
;

interface_prefix_expr
:
   INTERFACE PERIOD interface_prefix_prefix_expr
;

interface_prefix_prefix_expr
:
   PREFIX
;

interface_statement
:
   statement
;

interface_string_expr
:
   INTERFACE PERIOD interface_name_string_expr
;

ip_expr
:
   bgp_neighbor_ip_expr
   | interface_ip_expr
   | static_route_ip_expr
;

literal_int_expr
:
   DEC
;

local_path_question
:
   LOCAL_PATH
;

multipath_question
:
   MULTIPATH
;

neq_expr
:
   lhs = int_expr NOT_EQUALS rhs = int_expr
;

node_bgp_boolean_expr
:
   BGP PERIOD
   (
      node_bgp_configured_boolean_expr
      | node_bgp_has_generated_route_boolean_expr
   )
;

node_bgp_configured_boolean_expr
:
   CONFIGURED
;

node_bgp_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

node_boolean_expr
:
   NODE PERIOD
   (
      node_bgp_boolean_expr
      | node_has_generated_route_boolean_expr
      | node_isis_boolean_expr
      | node_ospf_boolean_expr
      | node_static_boolean_expr
   )
;

node_has_generated_route_boolean_expr
:
   HAS_GENERATED_ROUTE
;

node_isis_boolean_expr
:
   ISIS PERIOD node_isis_configured_boolean_expr
;

node_isis_configured_boolean_expr
:
   CONFIGURED
;

node_name_string_expr
:
   NAME
;

node_ospf_boolean_expr
:
   OSPF PERIOD node_ospf_configured_boolean_expr
;

node_ospf_configured_boolean_expr
:
   CONFIGURED
;

node_statement
:
   foreach_bgp_neighbor_statement
   | foreach_generated_route_statement
   | foreach_interface_statement
   | foreach_node_bgp_generated_route_statement
   | foreach_static_route_statement
   | statement
;

node_static_boolean_expr
:
   STATIC PERIOD node_static_configured_boolean_expr
;

node_static_configured_boolean_expr
:
   CONFIGURED
;

node_string_expr
:
   NODE PERIOD node_name_string_expr
;

not_expr
:
   NOT OPEN_BRACE boolean_expr CLOSE_BRACE
;

num_ips_int_expr
:
   caller = VARIABLE PERIOD NUM_IPS
;

num_strings_int_expr
:
   caller = VARIABLE PERIOD NUM_STRINGS
;

or_expr
:
   OR OPEN_BRACE disjuncts += boolean_expr
   (
      COMMA disjuncts += boolean_expr
   )* CLOSE_BRACE
;

prefix_expr
:
   generated_route_prefix_expr
   | interface_prefix_expr
   | static_route_prefix_expr
;

printable_expr
:
   boolean_expr
   | int_expr
   | ip_expr
   | prefix_expr
   | string_expr
;

printf_statement
:
   PRINTF OPEN_PAREN format_string = printable_expr
   (
      COMMA replacements += printable_expr
   )* CLOSE_PAREN SEMICOLON
;

property_boolean_expr
:
   bgp_neighbor_boolean_expr
   | interface_boolean_expr
   | node_boolean_expr
   | static_route_boolean_expr
;

question
:
   failure_question
   | ingress_path_question
   | local_path_question
   | multipath_question
   | verify_question
;

statement
:
   add_ip_statement
   | add_string_statement
   | clear_ips_statement
   | clear_strings_statement
   | assertion
   | assignment
   | if_statement
   | printf_statement
;

static_route_administrative_cost_int_expr
:
   ADMINISTRATIVE_COST
;

static_route_boolean_expr
:
   STATIC_ROUTE PERIOD
   (
      static_route_has_next_hop_interface_boolean_expr
      | static_route_has_next_hop_ip_boolean_expr
   )
;

static_route_has_next_hop_interface_boolean_expr
:
   HAS_NEXT_HOP_INTERFACE
;

static_route_has_next_hop_ip_boolean_expr
:
   HAS_NEXT_HOP_IP
;

static_route_int_expr
:
   STATIC_ROUTE PERIOD static_route_administrative_cost_int_expr
;

static_route_ip_expr
:
   STATIC_ROUTE PERIOD static_route_next_hop_ip_ip_expr
;

static_route_next_hop_interface_string_expr
:
   NEXT_HOP_INTERFACE
;

static_route_next_hop_ip_ip_expr
:
   NEXT_HOP_IP
;

static_route_prefix_expr
:
   STATIC_ROUTE PERIOD static_route_prefix_prefix_expr
;

static_route_prefix_prefix_expr
:
   PREFIX
;

static_route_statement
:
   statement
;

static_route_string_expr
:
   STATIC_ROUTE PERIOD static_route_next_hop_interface_string_expr
;

string_expr
:
   interface_string_expr
   | node_string_expr
   | static_route_string_expr
   | string_literal_string_expr
;

string_literal_string_expr
:
   STRING_LITERAL
;

true_expr
:
   TRUE
;

val_int_expr
:
   bgp_neighbor_int_expr
   | literal_int_expr
   | num_ips_int_expr
   | num_strings_int_expr
   | static_route_int_expr
   | var_int_expr
;

var_int_expr
:
   VARIABLE
;

verify_question
:
   VERIFY OPEN_BRACE verify_statement+ CLOSE_BRACE
;

verify_statement
:
   foreach_node_statement
   | statement
;

