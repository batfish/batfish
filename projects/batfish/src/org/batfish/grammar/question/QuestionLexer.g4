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

ADD_IP
:
   'add_ip'
;

AND
:
   'and'
;

ASSERT
:
   'assert'
;

BGP_NEIGHBOR
:
   'bgp_neighbor'
;

CONTAINS_IP
:
   'contains_ip'
;

ELSE
:
   'else'
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

IP
:
   'ip'
;

ISIS
:
   'isis'
;

IS_LOOPBACK
:
   'is_loopback'
;

GLOBAL
:
   'global'
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

REMOTE_IP
:
   'remote_ip'
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

PREFIX_SET
:
   'prefix-set'
;

SEMICOLON
:
   ';'
;

VARIABLE
:
   '$' F_VarChar+
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
   'A' .. 'Z'
   | 'a' .. 'z'
   | '0' .. '9'
   | '_'
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
