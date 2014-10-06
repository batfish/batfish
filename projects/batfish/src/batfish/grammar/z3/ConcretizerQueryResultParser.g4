parser grammar ConcretizerQueryResultParser;

options {
   superClass = 'batfish.grammar.BatfishParser';
   tokenVocab = ConcretizerQueryResultLexer;
}

@header {
package batfish.grammar.z3;
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
      NODE_HEADER node = M_NODE_NODE SAT model
   )
   | UNSAT
;
