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
;

bgp_neighbor_statement
:
   statement
;

bgp_neighbor_ip_expr
:
   BGP_NEIGHBOR PERIOD bgp_neighbor_remote_ip_ip_expr
;

bgp_neighbor_remote_ip_ip_expr
:
   REMOTE_IP
;

boolean_expr
:
   and_expr
   | contains_ip_expr
   | if_expr
   | false_expr
   | not_expr
   | or_expr
   | property_expr
   | true_expr
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

interface_ip_expr
:
   INTERFACE PERIOD interface_ip_ip_expr
;

interface_ip_ip_expr
:
   IP
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
      interface_isis_property_expr
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

not_expr
:
   NOT OPEN_BRACE boolean_expr CLOSE_BRACE
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
   | assertion
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

verify_question
:
   VERIFY OPEN_BRACE verify_statement+ CLOSE_BRACE
;

verify_statement
:
   foreach_node_statement
   | statement
;

