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
   VARIABLE COLON
   (
      boolean_expr
      | int_expr
      | ip_expr
   )
;

assignment
:
   int_assignment
;

bgp_neighbor_statement
:
   statement
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
   BGP_NEIGHBOR PERIOD bgp_neighbor_remote_ip_ip_expr
;

bgp_neighbor_local_as_int_expr
:
   LOCAL_AS
;

bgp_neighbor_remote_as_int_expr
:
   REMOTE_AS
;

bgp_neighbor_remote_ip_ip_expr
:
   REMOTE_IP
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
   | property_expr
   | true_expr
;

clear_ips_statement
:
   caller = VARIABLE PERIOD CLEAR_IPS SEMICOLON
;

contains_ip_expr
:
   caller = VARIABLE PERIOD CONTAINS_IP OPEN_PAREN ip_expr CLOSE_PAREN
;

false_expr
:
   FALSE
;

foreach_bgp_neighbor_statement
:
   FOREACH BGP_NEIGHBOR OPEN_BRACE bgp_neighbor_statement+ CLOSE_BRACE
;

foreach_interface_statement
:
   FOREACH INTERFACE OPEN_BRACE interface_statement+ CLOSE_BRACE
;

foreach_node_statement
:
   FOREACH NODE OPEN_BRACE node_statement+ CLOSE_BRACE
;

eq_expr
:
   lhs = int_expr DOUBLE_EQUALS rhs = int_expr
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

interface_ip_expr
:
   INTERFACE PERIOD interface_ip_ip_expr
;

interface_ip_ip_expr
:
   IP
;

interface_has_ip_boolean_expr
:
   HAS_IP
;

interface_isis_active_expr
:
   ACTIVE
;

interface_isis_passive_expr
:
   PASSIVE
;

interface_isis_property_expr
:
   ISIS PERIOD
   (
      interface_isis_active_expr
      | interface_isis_passive_expr
   )
;

interface_isloopback_expr
:
   IS_LOOPBACK
;

interface_property_expr
:
   INTERFACE PERIOD
   (
      interface_has_ip_boolean_expr
      | interface_isis_property_expr
      | interface_isloopback_expr
   )
;

interface_statement
:
   statement
;

ip_expr
:
   bgp_neighbor_ip_expr
   | interface_ip_expr
;

literal_int_expr
:
   DEC
;

multipath_question
:
   MULTIPATH environment = string
;

node_statement
:
   foreach_bgp_neighbor_statement
   | foreach_interface_statement
   | statement
;

neq_expr
:
   lhs = int_expr NOT_EQUALS rhs = int_expr
;

not_expr
:
   NOT OPEN_BRACE boolean_expr CLOSE_BRACE
;

num_ips_int_expr
:
   caller = VARIABLE PERIOD NUM_IPS
;

or_expr
:
   OR OPEN_BRACE disjuncts += boolean_expr
   (
      COMMA disjuncts += boolean_expr
   )* CLOSE_BRACE
;

property_expr
:
   interface_property_expr
;

question
:
   multipath_question
   | verify_question
;

statement
:
   add_ip_statement
   | clear_ips_statement
   | assertion
   | assignment
   | if_statement
;

string
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

