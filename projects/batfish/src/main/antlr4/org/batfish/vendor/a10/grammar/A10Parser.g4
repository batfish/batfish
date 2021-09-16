parser grammar A10Parser;

import A10_common;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = A10Lexer;
}

a10_configuration: statement+ EOF;

statement: s_hostname;

s_hostname: HOSTNAME hostname NEWLINE;
