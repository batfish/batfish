lexer grammar BatfishTopologyLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.topology;
}

HEADER
:
   'BATFISH_TOPOLOGY'
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
   '"' -> pushMode ( M_DoubleQuote ) , channel ( HIDDEN )
;

HASH
:
   '#' -> pushMode ( M_COMMENT ) , channel ( HIDDEN )
;

NEWLINE
:
   F_NewlineChar+
;

SINGLE_QUOTE
:
   '\'' -> pushMode ( M_SingleQuote ) , channel ( HIDDEN )
;

VARIABLE
:
   F_VarChar+
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

mode M_COMMENT;

M_COMMENT_COMMENT
:
   F_NonNewlineChar+ -> channel ( HIDDEN )
;

M_COMMENT_NEWLINE
:
   F_NewlineChar+ -> type ( NEWLINE ) , popMode
;

mode M_DoubleQuote;

M_DoubleQuote_VARIABLE
:
   ~'"'+ -> type ( VARIABLE )
;

M_DoubleQuote_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , popMode
;

mode M_SingleQuote;

M_SingleQuote_VARIABLE
:
   ~'\''+ -> type ( VARIABLE )
;

M_SingleQuote_SINGLE_QUOTE
:
   '\'' -> channel ( HIDDEN ) , popMode
;
