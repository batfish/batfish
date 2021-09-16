parser grammar A10Parser;

import A10_common;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = A10Lexer;
}

a10_configuration: line EOF;

line: set_line;

set_line: SET set_line_tail NEWLINE+;

set_line_tail: s_hostname;

//s_hostname: HOSTNAME;
s_hostname: HOSTNAME hostname;
