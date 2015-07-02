parser grammar QuestionParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = QuestionLexer;
}

@header {
package org.batfish.grammar.question;
}

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

boolean_expr
:
   and_expr
   | if_expr
   | false_expr
   | not_expr
   | or_expr
   | property_expr
   | true_expr
;

false_expr
:
   FALSE
;

foreach_interface
:
   FOREACH INTERFACE
;

foreach_node
:
   FOREACH NODE
;

if_expr
:
   IF OPEN_PAREN antecedent = boolean_expr CLOSE_PAREN THEN OPEN_BRACE
   consequent = boolean_expr CLOSE_BRACE
;

interface_context
:
   foreach_interface OPEN_BRACE assertion CLOSE_BRACE
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
   ISLOOPBACK
;

interface_property_expr
:
   INTERFACE PERIOD
   (
      interface_isis_property_expr
      | interface_isloopback_expr
   )
;

multipath_question
:
   MULTIPATH environment = string
;

node_context
:
   foreach_node OPEN_BRACE
   (
      assertion
      | interface_context
   ) CLOSE_BRACE
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
   VERIFY OPEN_BRACE
   (
      assertion
      | node_context
   ) CLOSE_BRACE
;
