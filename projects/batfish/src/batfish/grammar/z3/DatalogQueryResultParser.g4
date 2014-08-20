parser grammar DatalogQueryResultParser;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = DatalogQueryResultLexer;
}

@header {
package batfish.grammar.z3;
}

and_expr
:
   LEFT_PAREN AND conjuncts += boolean_expr+ RIGHT_PAREN
;

boolean_expr
:
   and_expr
   | eq_expr
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