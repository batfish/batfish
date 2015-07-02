lexer grammar QuestionLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.question;
}

tokens {
   STRING_LITERAL
}

// Simple tokens

ACTIVE
:
   'active'
;

AND
:
   'and'
;

ASSERT
:
   'assert'
;

FALSE
:
   'false'
;

FOREACH
:
   'foreach'
;

IF
:
   'if'
;

INPUT
:
   'input'
;

INTERFACE
:
   'interface'
;

ISIS
:
   'isis'
;

ISLOOPBACK
:
   'isLoopback'
;

MULTIPATH
:
   'multipath'
;

NODE
:
   'node'
;

NOT
:
   'not'
;

OR
:
   'or'
;

PASSIVE
:
   'passive'
;

TESTRIG
:
   'testrig'
;

THEN
:
   'then'
;

TRUE
:
   'true'
;

VERIFY
:
   'verify'
;

// Complex tokens

CLOSE_BRACE
:
   '}'
;

CLOSE_PAREN
:
   ')'
;

COLON
:
   ':'
;

COMMA
:
   ','
;

DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , pushMode ( M_QuotedString )
;

NEWLINE
:
   F_NewlineChar+ -> channel ( HIDDEN )
;

OPEN_BRACE
:
   '{'
;

OPEN_PAREN
:
   '('
;

PERIOD
:
   '.'
;

WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
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
   ~[\n\r:, \t\u000C]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

mode M_QuotedString;

M_QuotedString_TEXT
:
   ~'"'+ -> type ( STRING_LITERAL )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , popMode
;