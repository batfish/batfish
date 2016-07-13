parser grammar IptablesParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = IptablesLexer;
}

@header {
package org.batfish.grammar.iptables;
}

command
:
   FLAG_APPEND
;

iptables_configuration
:
   command+
;