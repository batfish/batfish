lexer grammar ConcretizerQueryResultLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.z3;
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

ID_HEADER
:
   ';' -> pushMode ( M_ID )
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

mode M_ID;

M_ID_NEWLINE
:
   F_NewlineChar+ -> popMode , channel (HIDDEN)
;

M_ID_ID
:
   F_NonNewlineChar+
;
