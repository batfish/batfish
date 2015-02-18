parser grammar ConcretizerQueryResultParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = ConcretizerQueryResultLexer;
}

@header {
package org.batfish.grammar.z3;
}

define_fun
:
   LEFT_PAREN DEFINE_FUN var = VARIABLE LEFT_PAREN RIGHT_PAREN LEFT_PAREN
   UNDERSCORE BITVEC DEC RIGHT_PAREN
   (
      value = HEX
      | value = BIN
   ) RIGHT_PAREN
;

model
:
   LEFT_PAREN MODEL define_fun+ RIGHT_PAREN
;

result
:
   (
      ID_HEADER id = M_ID_ID SAT model
   )
   | UNSAT
;
