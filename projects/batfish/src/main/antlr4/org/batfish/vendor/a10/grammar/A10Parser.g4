parser grammar A10Parser;

import
  A10_common,
  A10_interface,
  A10_rba,
  A10_vlan;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = A10Lexer;
}

a10_configuration: NEWLINE? statement+ NEWLINE* EOF;

statement: s_hostname | s_interface | s_rba | s_vlan;

s_hostname: HOSTNAME hostname NEWLINE;
