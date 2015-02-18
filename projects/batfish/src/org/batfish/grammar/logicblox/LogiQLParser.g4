parser grammar LogiQLParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = LogiQLLexer;
}

@header {
package org.batfish.grammar.logicblox;
}

alias_all
:
   ALIAS_ALL LEFT_PAREN GRAVE VARIABLE
   (
      COLON VARIABLE
   )* RIGHT_PAREN
;

block
:
   BLOCK LEFT_PAREN GRAVE blockname = VARIABLE RIGHT_PAREN LEFT_BRACE
   block_directive
   (
      COMMA block_directive
   )* RIGHT_BRACE DOUBLE_LEFT_ARROW PERIOD
;

block_directive
:
   alias_all
   | clauses
   | export
;

boolean_predicate_decl
:
   predicate = VARIABLE LEFT_PAREN RIGHT_PAREN RIGHT_ARROW PERIOD
;

clauses
:
   CLAUSES LEFT_PAREN GRAVE LEFT_BRACE constructor_decl+ RIGHT_BRACE
   RIGHT_PAREN
;

constructor_decl
:
   CONSTRUCTOR LEFT_PAREN GRAVE VARIABLE RIGHT_PAREN PERIOD
;

entity_predicate_decl
:
   predicate = VARIABLE LEFT_PAREN VARIABLE RIGHT_PAREN RIGHT_ARROW PERIOD
;

export
:
   EXPORT LEFT_PAREN GRAVE LEFT_BRACE
   (
      predicate_semantics?
      (
         function_decl
         | predicate_decl
         | refmode_decl
      )
   )+ RIGHT_BRACE RIGHT_PAREN
;

function_decl
:
   function = VARIABLE LEFT_BRACKET parameter_list RIGHT_BRACKET EQUALS
   output_var = VARIABLE RIGHT_ARROW type_decl_list PERIOD
;

parameter_list
:
   (
      vars += VARIABLE
      (
         COMMA vars += VARIABLE
      )*
   )?
;

predicate_decl
:
   boolean_predicate_decl
   | entity_predicate_decl
   | regular_predicate_decl
;

predicate_semantics
:
   (
      PREDICATE_SEMANTICS_COMMENT_HEADER lines += M_PredicateSemantics_LINE
      M_PredicatSemantics_NEWLINE
   )+
;

regular_predicate_decl
:
   predicate = VARIABLE LEFT_PAREN parameter_list RIGHT_PAREN RIGHT_ARROW
   type_decl_list PERIOD
;

refmode_decl
:
   refmode_predicate = VARIABLE LEFT_PAREN VARIABLE RIGHT_PAREN COMMA VARIABLE
   LEFT_PAREN VARIABLE COLON VARIABLE RIGHT_PAREN RIGHT_ARROW type_decl PERIOD
;

type_decl
:
   type = VARIABLE LEFT_PAREN var = VARIABLE RIGHT_PAREN
;

type_decl_list
:
   type_decls += type_decl
   (
      COMMA type_decls += type_decl
   )*
;
