lexer grammar AssertionLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

tokens {
   QUOTED_TEXT
}

// Assertion Keywords

AND
:
   'and'
;

FALSE
:
   'false'
;

EMPTYPATH
:
   'emptypath'
;

EQ
:
   'eq'
;

GE
:
   'ge'
;

GT
:
   'gt'
;

IF
:
   'if'
;

LE
:
   'le'
;

LT
:
   'lt'
;

NOT
:
   'not'
;

OR
:
   'or'
;

PATHSIZE
:
   'pathsize'
;

TRUE
:
   'true'
;

// Other tokens

DOUBLE
:
   (
      (
         F_Digit+ '.' F_Digit*
      )
      |
      (
         '.' F_Digit+
      )
   ) [Dd]
;

FLOAT
:
   (
      (
         F_Digit+ '.' F_Digit*
      )
      |
      (
         '.' F_Digit+
      )
   ) [Ff]
;

LONG
:
   F_Digit+ [Ll]
;

INT
:
   F_Digit+
;

PAREN_LEFT
:
   '('
;

PAREN_RIGHT
:
   ')'
;

SINGLE_QUOTE
:
   '\'' -> pushMode ( M_QuotedString )
;

WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_Whitespace
:
   [ \r\n\t\u000C]
;

mode M_QuotedString;

M_QuotedString_QUOTED_TEXT
:
   ~'\''+ -> type ( QUOTED_TEXT )
;

M_QuotedString_SINGLE_QUOTE
:
   '\'' -> type ( SINGLE_QUOTE ) , popMode
;
