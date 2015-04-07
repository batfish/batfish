lexer grammar QuestionLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.question;
}

// Simple tokens

MULTIPATH
:
   'multipath'
;

// Complex tokens

NEWLINE
:
   F_NewlineChar+ -> channel(HIDDEN)
;

VARIABLE
:
   F_VarChar+
;

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
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
