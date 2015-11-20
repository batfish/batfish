lexer grammar QuestionLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.question;
}

tokens {
   INT,
   STRING,
   STRING_LITERAL
}

// Simple tokens

ACCEPT
:
   'accept'
;

ACTION
:
   'action'
;

ACTIVE
:
   'active'
;

ADD
:
   'add'
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

CLAUSE
:
   'clause'
;

CLEAR
:
   'clear'
;

COMPATIBLE_IKE_PROPOSALS
:
   'compatible_ike_proposals'
;

COMPATIBLE_IPSEC_PROPOSALS
:
   'compatible_ipsec_proposals'
;

CONFIGURED
:
   'configured'
;

CONTAINS
:
   'contains'
;

DEFAULTS
:
   'defaults'
;

DROP
:
   'drop'
;

DST_IP
:
   'dst_ip'
;

DST_PORT
:
   'dst_port'
;

ELSE
:
   'else'
;

ENABLED
:
   'enabled'
;

FAILURE
:
   'failure'
;

FALSE
:
   'false'
;

FINAL_NODE
:
   'final_node'
;

FLOW
:
   'flow'
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

IKE_GATEWAY_NAME
:
   'ike_gateway_name'
;

IKE_POLICY_NAME
:
   'ike_policy_name'
;

INGRESS_NODE
:
   'ingress_node'
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

IP_PROTOCOL
:
   'ip_protocol'
;

IPSEC_POLICY_NAME
:
   'ipsec_policy_name'
;

IPSEC_VPN
:
   'ipsec_vpn'
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

HAS_REMOTE_IPSEC_VPN
:
   'has_remote_ipsec_vpn'
;

INGRESS_PATH
:
   'ingress-path'
;

L1_ACTIVE
:
   'l1_active'
;

L1_PASSIVE
:
   'l1_passive'
;

L2_ACTIVE
:
   'l2_active'
;

L2_PASSIVE
:
   'l2_passive'
;

LINE
:
   'line'
;

LOCAL_AS
:
   'local_as'
;

LOCAL_IP
:
   'local_ip'
;

LOCAL_PATH
:
   'local-path'
;

MATCH_PROTOCOL
:
   'match_protocol'
;

MATCH_ROUTE_FILTER
:
   'match_route_filter'
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

OSPF_OUTBOUND_POLICY
:
   'ospf_outbound_policy'
;

OWNER_NAME
:
   'owner_name'
;

PASSIVE
:
   'passive'
;

PRE_SHARED_KEY
:
   'pre_shared_key'
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

PROTOCOL
:
   'protocol'
;

REACHABILITY
:
   'reachability'
;

REMOTE_AS
:
   'remote_as'
;

REMOTE_IP
:
   'remote_ip'
;

REMOTE_IPSEC_VPN
:
   'remote_ipsec_vpn'
;

ROUTE_FILTER
:
   'route_filter'
;

SET
:
   'set' -> pushMode(M_Set)
;

SIZE
:
   'size'
;

SRC_IP
:
   'src_ip'
;

SRC_PORT
:
   'src_port'
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

TRACEROUTE
:
   'traceroute'
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
      F_PositiveDecimalDigit F_DecimalDigit*
   )
;

DOUBLE_EQUALS
:
   '=='
;

DOUBLE_FORWARD_SLASH
:
   '//' -> channel ( HIDDEN ) , pushMode ( M_Comment )
;

DOUBLE_PLUS
:
   '++'
;

DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , pushMode ( M_QuotedString )
;

EQUALS
:
   '='
;

FORWARD_SLASH
:
   '/'
;

GT
:
   '>'
;

IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_DecimalDigit+
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

REGEX
:
   'regex<' ~'>'* '>'
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
   F_NewlineChar+ -> channel ( HIDDEN ) , popMode
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

mode M_Set;

M_Set_INT
:
   'int' -> type(INT)
;

M_Set_IP
:
   'ip' -> type(IP)
;

M_Set_OPEN_ANGLE_BRACKET
:
   '<' -> channel(HIDDEN)
;

M_Set_ROUTE_FILTER
:
   'route_filter' -> type(ROUTE_FILTER)
;

M_Set_STRING
:
   'string' -> type(STRING)
;

M_Set_CLOSE_ANGLE_BRACKET
:
   '>' -> channel(HIDDEN), popMode
;
