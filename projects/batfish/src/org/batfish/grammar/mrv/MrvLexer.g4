lexer grammar MrvLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.mrv;
}

tokens {
   QUOTED_TEXT
}

// Mrv Keywords

ASYNC
:
   'Async'
;

AUTOHANG
:
   'AutoHang'
;

BOOL
:
   'BOOL'
;

IFNAME
:
   'ifName'
;

INTERFACE
:
   'Interface'
;

SYSTEM
:
   'System'
;

SYSTEMNAME
:
   'SystemName'
;

TYPE
:
   'TYPE'
;

// Other tokens

DOUBLE_QUOTE
:
   '"' -> pushMode ( M_QuotedString )
;

LINE_COMMENT
:
   '!' -> pushMode(M_LineComment), channel(HIDDEN)
;

PERIOD
:
   '.'
;

fragment
F_Newline
:
   [\r\n]+
;

fragment
F_NonNewline
:
   ~[\r\n]
;

mode M_LineComment;

M_LineComment_FILLER
:
   F_NonNewline* F_Newline -> channel(HIDDEN), popMode
;

mode M_QuotedString;

M_QuotedString_QUOTED_TEXT
:
   ~'"'+ -> type ( QUOTED_TEXT )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> type ( DOUBLE_QUOTE ) , popMode
;
