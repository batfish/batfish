parser grammar A10Parser;

import
  A10_common,
  A10_interface,
  A10_ip,
  A10_rba,
  A10_trunk,
  A10_vlan;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = A10Lexer;
}

a10_configuration: NEWLINE? statement+ EOF;

statement
:
   s_hostname
   | s_interface
   | s_ip
   | s_rba
   | s_trunk
   | s_vlan
;

s_hostname: HOSTNAME hostname NEWLINE;
