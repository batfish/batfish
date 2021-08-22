parser grammar CheckPointGatewayParser;

import
   CheckPointGateway_bonding_group,
   CheckPointGateway_common,
   CheckPointGateway_interface,
   CheckPointGateway_static_route;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = CheckPointGatewayLexer;
}

check_point_gateway_configuration: line+ EOF;

line: add_line | set_line;

add_line: ADD add_line_tail NEWLINE+;

add_line_tail
:
   a_bonding_group
   | a_interface
;

set_line: SET set_line_tail NEWLINE+;

set_line_tail
:
   s_bonding_group
   | s_hostname
   | s_interface
   | s_static_route
;

s_hostname: HOSTNAME hostname;
