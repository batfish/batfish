lexer grammar LogiQLLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.logicblox;
}

PREDICATE_SEMANTICS_COMMENT_HEADER
:
   '///' -> pushMode(M_PredicateSemantics)
;

ALIAS_ALL
:
   'alias_all'
;

BLOCK
:
   'block'
;

CLAUSES
:
   'clauses'
;

COLON
:
   ':'
;

COMMA
:
   ','
;

CONSTRUCTOR
:
   'lang:constructor'
;

DOUBLE_LEFT_ARROW
:
   '<--'
;

EQUALS
:
   '='
;

EXPORT
:
   'export'
;

GRAVE
:
   '`'
;

LEFT_BRACE
:
   '{'
;

LEFT_BRACKET
:
   '['
;

LEFT_PAREN
:
   '('
;

LINE_COMMENT
:
   '//' -> pushMode(M_LineComment), channel(HIDDEN)
;

PERIOD
:
   '.'
;

RIGHT_ARROW
:
   '->'
;

RIGHT_BRACE
:
   '}'
;

RIGHT_BRACKET
:
   ']'
;

RIGHT_PAREN
:
   ')'
;

VARIABLE
:
   F_LeadingVarChar F_BodyVarChar*
;

WS
:
   F_WhitespaceChar+ -> channel (HIDDEN)
;

fragment
F_BodyVarChar
:
   [_]
   | F_Letter
   | F_Digit
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_LeadingVarChar
:
   '_'
   | F_Letter
;

fragment
F_Letter
:
   (
      'a' .. 'z'
   )
   |
   (
      'A' .. 'Z'
   )
;

fragment
F_NewlineChar
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C\r\n]
;

mode M_PredicateSemantics;

M_PredicateSemantics_LINE
:
   F_NonNewlineChar+
;

M_PredicatSemantics_NEWLINE
:
   F_NewlineChar+ -> popMode
;

mode M_LineComment;

M_LineComment_LINE
:
   F_NonNewlineChar* F_NewlineChar+ -> channel (HIDDEN), popMode
;
