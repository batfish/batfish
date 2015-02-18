lexer grammar GNS3TopologyLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.topology;
}

INTERFACE
:
   (
      F_CiscoFastEthernet
      | F_JuniperEthernet
   ) -> pushMode ( M_Edge )
;

ROUTER_HEADING
:
   '[['
   (
      'JUNOS'
      | 'ROUTER'
   ) -> pushMode ( M_Router )
;

ROUTER_TAIL
:
   ']]'
;

WS
:
   F_WhitespaceChar+ -> channel (HIDDEN)
;

NEWLINE
:
   F_NewlineChar+
;

EQUALS
:
   '='
;

VARIABLE
:
   (F_VarChar | ']')+
;

fragment
F_CiscoFastEthernet
:
   'f' F_Digit+ '/' F_Digit+
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_JuniperEthernet
:
   'e' F_Digit+
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
   ~[\n\r \t\u000C=\]]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

mode M_Edge;

M_Edge_EQUALS
:
   '=' -> type(EQUALS)
;

M_Edge_NEWLINE
:
   F_NewlineChar+ -> type(NEWLINE), popMode
;

M_Edge_VARIABLE
:
   F_VarChar+ -> type ( VARIABLE ) , popMode
;

M_Edge_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Router;

M_Router_VARIABLE
:
   F_VarChar+ -> type ( VARIABLE ) , popMode
;

M_Router_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;
