lexer grammar QuestionParametersLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.question;
}

tokens {
   CLOSE_ANGLE_BRACKET,
   CLOSE_BRACE,
   COMMA,
   DEC,
   FALSE,
   IP_ADDRESS,
   IP_PREFIX,
   MINUS,
   OPEN_ANGLE_BRACKET,
   OPEN_BRACE,
   REGEX,
   SET,
   STRING,
   STRING_LITERAL,
   TRUE
}

// Simple tokens

// Complex tokens

HASH
:
   '#' -> channel ( HIDDEN ) , pushMode ( M_Comment )
;

EQUALS
:
   '=' -> pushMode ( M_Value )
;

NEWLINE
:
   F_NewlineChar+ -> channel ( HIDDEN )
;

VARIABLE
:
   F_LeadingVarChar F_VarChar*
;

WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

fragment
F_DecByte
:
   (
      F_PositiveDecimalDigit F_DecimalDigit F_DecimalDigit
   )
   |
   (
      F_PositiveDecimalDigit F_DecimalDigit
   )
   | F_DecimalDigit
;

fragment
F_DecimalDigit
:
   '0' .. '9'
;

fragment
F_LeadingVarChar
:
   'A' .. 'Z'
   | 'a' .. 'z'
   | '_'
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
F_PositiveDecimalDigit
:
   '1' .. '9'
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

mode M_Comment;

M_Comment_COMMENT
:
   F_NonNewlineChar+ -> channel ( HIDDEN )
;

M_Comment_NEWLINE
:
   F_NewlineChar+ -> channel ( HIDDEN ) , mode ( DEFAULT_MODE )
;

mode M_Negative;

M_Negative_DEC
:
   (
      '0'
      |
      (
         F_PositiveDecimalDigit F_DecimalDigit*
      )
   ) -> type ( DEC ) , mode ( DEFAULT_MODE )
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

mode M_SetType;

M_SetType_CLOSE_ANGLE_BRACKET
:
   '>' -> type(CLOSE_ANGLE_BRACKET), popMode
;

M_SetType_OPEN_ANGLE_BRACKET
:
   '<' -> type(OPEN_ANGLE_BRACKET)
;

M_SetType_STRING
:
   'string' -> type(STRING)
;

mode M_Value;

M_Value_CLOSE_BRACE
:
   '}' -> type(CLOSE_BRACE)
;

M_Value_COMMA
:
   ',' -> type(COMMA)
;

M_Value_DEC
:
   (
      '0'
      |
      (
         F_PositiveDecimalDigit F_DecimalDigit*
      )
   ) -> type ( DEC ) , popMode
;

M_Value_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , pushMode ( M_QuotedString )
;

M_Value_FALSE
:
   'false' -> type ( FALSE ) , popMode
;

M_Value_HASH
:
   '#' -> channel ( HIDDEN ) , pushMode ( M_Comment )
;

M_Value_IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte -> type ( IP_ADDRESS ) ,
   popMode
;

M_Value_IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_DecimalDigit+ ->
   type ( IP_PREFIX ) , popMode
;

M_Value_MINUS
:
   '-' -> type ( MINUS ) , pushMode ( M_Negative )
;

M_Value_OPEN_BRACE
:
   '{' -> type(OPEN_BRACE)
;

M_Value_REGEX
:
   'regex<' ~'>'* '>' -> type ( REGEX ) , popMode
;

M_Value_TRUE
:
   'true' -> type ( TRUE ) , popMode
;

M_Value_SET
:
   'set' -> type(SET), pushMode(M_SetType)
;

M_Value_STRING_LITERAL
:
   F_LeadingVarChar F_VarChar* -> type ( STRING_LITERAL ) , popMode
;

M_Value_WS
:
   (
      F_WhitespaceChar
      | F_NewlineChar
   )+ -> channel ( HIDDEN )
;

