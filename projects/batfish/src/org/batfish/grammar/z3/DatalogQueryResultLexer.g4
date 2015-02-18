lexer grammar DatalogQueryResultLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.z3;
}

AND
:
   'and'
;

EXTRACT
:
   'extract'
;

FALSE
:
   'false'
;

LET
:
   'let'
;

NOT
:
   'not'
;

OR
:
   'or'
;

SAT
:
   'sat'
;

TRUE
:
   'true'
;

UNSAT
:
   'unsat'
;

VAR
:
   ':var'
;

COMMENT
:
   ';' -> pushMode ( M_COMMENT ) , channel (HIDDEN)
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
   'a!' F_Digit+
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
F_WhitespaceChar
:
   [ \t\u000C\r\n]
;

mode M_COMMENT;

M_COMMENT_NEWLINE
:
   F_NewlineChar+ -> popMode , channel (HIDDEN)
;

M_COMMENT_NON_NEWLINE
:
   F_NonNewlineChar+ -> channel (HIDDEN)
;
