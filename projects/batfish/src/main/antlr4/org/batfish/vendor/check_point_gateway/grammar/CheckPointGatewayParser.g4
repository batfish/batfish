parser grammar CheckPointGatewayParser;

import
   CheckPointGateway_common;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = CheckPointGatewayLexer;
}

check_point_gateway_configuration: line EOF;

line: set_line;

set_line: SET set_line_tail NEWLINE+;

set_line_tail: s_hostname;

s_hostname: HOSTNAME hostname;
