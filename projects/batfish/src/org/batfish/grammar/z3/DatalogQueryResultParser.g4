parser grammar DatalogQueryResultParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = DatalogQueryResultLexer;
}

@header {
package org.batfish.grammar.z3;
}

and_expr
:
   LEFT_PAREN AND conjuncts += boolean_expr+ RIGHT_PAREN
;

boolean_expr
:
   and_expr
   | eq_expr
   | let_expr
   | macro_ref_expr
   | not_expr
   | or_expr
   | TRUE
   | FALSE
;

eq_expr
:
   LEFT_PAREN EQUALS lhs = int_expr rhs = int_expr RIGHT_PAREN
;

extract_expr
:
   LEFT_PAREN LEFT_PAREN UNDERSCORE EXTRACT high = DEC low = DEC RIGHT_PAREN
   var_int_expr RIGHT_PAREN
;

int_expr
:
   lit_int_expr
   | extract_expr
   | var_int_expr
;

lit_int_expr
:
   BIN
   | HEX
;

let_expr
:
   LEFT_PAREN LET LEFT_PAREN
   (
      var_defs += macro_def
   )+ RIGHT_PAREN boolean_expr RIGHT_PAREN
;

macro_def
:
   LEFT_PAREN VARIABLE boolean_expr RIGHT_PAREN
;

macro_ref_expr
:
   VARIABLE
;

not_expr
:
   LEFT_PAREN NOT boolean_expr RIGHT_PAREN
;

or_expr
:
   LEFT_PAREN OR disjuncts += boolean_expr+ RIGHT_PAREN
;

result
:
   (
      SAT boolean_expr
   )
   | UNSAT
;

var_int_expr
:
   LEFT_PAREN VAR DEC RIGHT_PAREN
;

