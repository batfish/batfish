lexer grammar ConcretizerQueryResultLexer;

options {
   superClass = 'batfish.grammar.BatfishLexer';
}

@header {
package batfish.grammar.z3;
}

BITVEC
:
   'BitVec'
;

DEFINE_FUN
:
   'define-fun'
;

MODEL
:
   'model'
;

SAT
:
   'sat'
;

UNSAT
:
   'unsat'
;

NODE_HEADER
:
   ';' -> pushMode ( M_NODE )
;

BIN
:
   '#b' F_Digit+
;

DEC
:
   F_Digit+
;

EQUALS
:
   '='
;

HEX
:
   '#x' F_HexDigit+
;

LEFT_PAREN
:
   '('
;

RIGHT_PAREN
:
   ')'
;

UNDERSCORE
:
   '_'
;

VARIABLE
:
   F_Letter F_VarChar+
;

WS
:
   F_WhitespaceChar+ -> channel (HIDDEN)
;

fragment
NEWLINE_CHAR
:
   '\n'
;

fragment
F_HexDigit
:
   (
      '0' .. '9'
      | 'a' .. 'f'
      | 'A' .. 'F'
   )
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_Letter
:
   (
      'A' .. 'Z'
   )
   |
   (
      'a' .. 'z'
   )
;

fragment
F_NewlineChar
:
   [\n\r]
;

fragment
F_NonNewlineChar
:
   ~[\n\r]
;

fragment
F_VarChar
:
   F_Digit
   | F_Letter
   | '_'
;

fragment
F_WhitespaceChar
:
   [ \t\u000C\r\n]
;

mode M_NODE;

M_NODE_NEWLINE
:
   F_NewlineChar+ -> popMode , channel (HIDDEN)
;

M_NODE_NODE
:
   F_NonNewlineChar+
;
