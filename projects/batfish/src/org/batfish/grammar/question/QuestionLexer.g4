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

ADMINISTRATIVE_COST
:
   'administrative_cost'
;

AND
:
   'and'
;

ASSERT
:
   'assert'
;

BGP
:
   'bgp'
;

BGP_NEIGHBOR
:
   'bgp_neighbor'
;

CLEAR_IPS
:
   'clear_ips'
;

CONFIGURED
:
   'configured'
;

CONTAINS_IP
:
   'contains_ip'
;

ELSE
:
   'else'
;

ENABLED
:
   'enabled'
;

FALSE
:
   'false'
;

FOREACH
:
   'foreach'
;

GENERATED_ROUTE
:
   'generated_route'
;

HAS_GENERATED_ROUTE
:
   'has_generated_route'
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

HAS_IP
:
   'has_ip'
;

HAS_NEXT_HOP_INTERFACE
:
   'has_next_hop_interface'
;

HAS_NEXT_HOP_IP
:
   'has_next_hop_ip'
;

LOCAL_AS
:
   'local_as'
;

LOCAL_IP
:
   'local_ip'
;

MULTIPATH
:
   'multipath'
;

NAME
:
   'name'
;

NEXT_HOP_INTERFACE
:
   'next_hop_interface'
;

NEXT_HOP_IP
:
   'next_hop_ip'
;

NODE
:
   'node'
;

NOT
:
   'not'
;

NUM_IPS
:
   'num_ips'
;

ONFAILURE
:
   'onfailure'
;

OR
:
   'or'
;

OSPF
:
   'ospf'
;

PASSIVE
:
   'passive'
;

PREFIX
:
   'prefix'
;

PREFIX_SET
:
   'prefix-set'
;

PRINTF
:
   'printf'
;

REMOTE_AS
:
   'remote_as'
;

REMOTE_IP
:
   'remote_ip'
;

STATIC
:
   'static'
;

STATIC_ROUTE
:
   'static_route'
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

ASTERISK
:
   '*'
;

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

COLON_EQUALS
:
   ':='
;

COMMA
:
   ','
;

DEC
:
   '0'
   |
   (
      '-'? F_PositiveDecimalDigit F_DecimalDigit*
   )
;

DOUBLE_EQUALS
:
   '=='
;

DOUBLE_PLUS
:
   '++'
;

DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , pushMode ( M_QuotedString )
;

FORWARD_SLASH
:
   '/'
;

GT
:
   '>'
;

MINUS
:
   '-'
;

NEWLINE
:
   F_NewlineChar+ -> channel ( HIDDEN )
;

NOT_EQUALS
:
   '!='
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

PLUS
:
   '+'
;

PRINTF_STRING
:
   '%s'
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
F_DecimalDigit
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

mode M_QuotedString;

M_QuotedString_TEXT
:
   ~'"'+ -> type ( STRING_LITERAL )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , popMode
;
