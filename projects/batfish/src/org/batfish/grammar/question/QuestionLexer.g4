lexer grammar QuestionLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.question;
}

tokens {
   BOOLEAN,
   INT,
   POLICY_MAP,
   POLICY_MAP_CLAUSE,
   PREFIX_SPACE,
   ROUTE_FILTER_LINE,
   STRING,
   STRING_LITERAL
}

// Simple tokens

ACCEPT
:
   'accept'
;

ACL_REACHABILITY
:
   'acl_reachability'
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

ADDRESS
:
   'address'
;

ALL_PREFIXES
:
   'all_prefixes'
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

BGP_ORIGINATION_SPACE_EXPLICIT
:
   'bgp_origination_space_explicit'
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

DESCRIPTION
:
   'description'
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

EBGP_MULTIHOP
:
   'ebgp_multihop'
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

FORMAT
:
   'format'
;

GENERATED_ROUTE
:
   'generated_route'
;

GET
:
   'get'
;

GET_MAP
:
   'get_map'
;

GLOBAL
:
   'global'
;

GROUP
:
   'group'
;

HAS_GENERATED_ROUTE
:
   'has_generated_route'
;

HAS_IP
:
   'has_ip'
;

HAS_LOCAL_IP
:
   'has_local_ip'
;

HAS_NEXT_HOP_INTERFACE
:
   'has_next_hop_interface'
;

HAS_NEXT_HOP_IP
:
   'has_next_hop_ip'
;

HAS_REMOTE_BGP_NEIGHBOR
:
   'has_remote_bgp_neighbor'
;

HAS_REMOTE_IP
:
   'has_remote_ip'
;

HAS_REMOTE_IPSEC_VPN
:
   'has_remote_ipsec_vpn'
;

HAS_SINGLE_REMOTE_BGP_NEIGHBOR
:
   'has_single_remote_bgp_neighbor'
;

HAS_SINGLE_REMOTE_IPSEC_VPN
:
   'has_single_remote_ipsec_vpn'
;

ICMP_CODE
:
   'icmp_code'
;

ICMP_TYPE
:
   'icmp_type'
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

INGRESS_PATH
:
   'ingress_path'
;

INPUT
:
   'input'
;

INTERFACE
:
   'interface'
;

INTERSECTION
:
   'intersection'
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

KEYS
:
   'keys'
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
   'local_path'
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

NEW_MAP
:
   'new_map'
;

NODE
:
   'node'
;

NODE_BGP_GENERATED_ROUTE
:
   'node_bgp_generated_route'
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

OVERLAPS
:
   'overlaps'
;

OWNER
:
   'owner'
;

PASSIVE
:
   'passive'
;

PERMIT
:
   'permit'
;

PRE_SHARED_KEY_HASH
:
   'pre_shared_key_hash'
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

PROTOCOL_DEPENDENCIES
:
   'protocol-dependencies'
;

QUERY
:
   'query'
;

REACHABILITY
:
   'reachability'
;

REDUCED_REACHABILITY
:
   'reduced_reachability'
;

REMOTE_AS
:
   'remote_as'
;

REMOTE_BGP_NEIGHBOR
:
   'remote_bgp_neighbor'
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
   'set' -> pushMode ( M_Set )
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

SUBNET
:
   'subnet'
;

TCP_FLAGS
:
   'tcp_flags'
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

UNLESS
:
   'unless'
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
   '"' -> pushMode ( M_QuotedString )
;

EQUALS
:
   '='
;

FORWARD_SLASH
:
   '/'
;

FORWARD_SLASH_STAR
:
   '/*' -> channel ( HIDDEN ) , pushMode ( M_MultilineComment )
;

GE
:
   '>='
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

LE
:
   '<='
;

LT
:
   '<'
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

VIEW
:
   'view'
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

mode M_MultilineComment;

M_MultilineComment_COMMENT
:
   (
      (
         ~'*'+
      )
      |
      (
         '*'+ ~[*/]
      )
   ) -> channel ( HIDDEN )
;

M_MultilineComment_TERMINATOR
:
   '*/' -> channel ( HIDDEN ) , popMode
;

mode M_QuotedString;

M_QuotedString_TEXT
:
   ~'"'+ -> type ( STRING_LITERAL )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> type ( DOUBLE_QUOTE ) , popMode
;

mode M_Set;

M_Set_BGP_NEIGHBOR
:
   'bgp_neighbor' -> type ( BGP_NEIGHBOR )
;

M_Set_INT
:
   'int' -> type ( INT )
;

M_Set_INTERFACE
:
   'interface' -> type ( INTERFACE )
;

M_Set_IP
:
   'ip' -> type ( IP )
;

M_Set_IPSEC_VPN
:
   'ipsec_vpn' -> type ( IPSEC_VPN )
;

M_Set_NODE
:
   'node' -> type ( NODE )
;

M_Set_OPEN_PAREN
:
   '(' -> type ( OPEN_PAREN ) , popMode
;

M_Set_POLICY_MAP
:
   'policy_map' -> type ( POLICY_MAP )
;

M_Set_POLICY_MAP_CLAUSE
:
   'policy_map_clause' -> type ( POLICY_MAP_CLAUSE )
;

M_Set_PREFIX
:
   'prefix' -> type ( PREFIX )
;

M_Set_PREFIX_SPACE
:
   'prefix_space' -> type ( PREFIX_SPACE )
;

M_Set_ROUTE_FILTER
:
   'route_filter' -> type ( ROUTE_FILTER )
;

M_Set_ROUTE_FILTER_LINE
:
   'route_filter_line' -> type ( ROUTE_FILTER_LINE )
;

M_Set_STATIC_ROUTE
:
   'static_route' -> type ( STATIC_ROUTE )
;

M_Set_STRING
:
   'string' -> type ( STRING )
;

M_Set_OPEN_ANGLE_BRACKET
:
   '<' -> channel ( HIDDEN )
;

M_Set_CLOSE_ANGLE_BRACKET
:
   '>' -> channel ( HIDDEN ) , popMode
;
