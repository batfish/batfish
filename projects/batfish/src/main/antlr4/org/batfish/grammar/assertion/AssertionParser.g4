parser grammar AssertionParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = AssertionLexer;
}

and
:
   AND
   (
      conjuncts += boolean_expr
   )+
;

assertion
:
   boolean_expr
;

boolean_application
:
   PAREN_LEFT boolean_function PAREN_RIGHT
;

boolean_expr
:
   boolean_application
   | FALSE
   | TRUE
;

boolean_function
:
   and
   | boolean_if
   | eq
   | ge
   | gt
   | le
   | lt
   | not
   | or
;

boolean_if
:
   IF guard = boolean_expr trueval = boolean_expr falseval = boolean_expr
;

eq
:
   EQ
   (
      (
         lhs_boolean = boolean_expr rhs_boolean = boolean_expr
      )
      |
      (
         lhs_num = num_expr rhs_num = num_expr
      )
      |
      (
         lhs_string = string_expr rhs_string = string_expr
      )
   )
;

ge
:
   GE lhs = num_expr rhs = num_expr
;

gt
:
   GT lhs = num_expr rhs = num_expr
;

le
:
   LE lhs = num_expr rhs = num_expr
;

lt
:
   LT lhs = num_expr rhs = num_expr
;

not
:
   NOT boolean_expr
;

num_application
:
   PAREN_LEFT num_function PAREN_RIGHT
;

num_double
:
   DOUBLE
;

num_expr
:
   num_application
   | num_double
   | num_float
   | num_int
   | num_long
;

num_float
:
   FLOAT
;

num_function
:
   num_if
   | pathsize
;

num_if
:
   IF guard = boolean_expr trueval = num_expr falseval = num_expr
;

num_int
:
   INT
;

num_long
:
   LONG
;

or
:
   OR
   (
      disjuncts += boolean_expr
   )+
;

pathsize
:
   PATHSIZE string_expr
;

quoted_string
:
   SINGLE_QUOTE text = QUOTED_TEXT? SINGLE_QUOTE
;

string_application
:
   PAREN_LEFT string_function PAREN_RIGHT
;

string_expr
:
   string_application
   | quoted_string
;

string_function
:
   string_if
;

string_if
:
   IF guard = boolean_expr trueval = string_expr falseval = string_expr
;
