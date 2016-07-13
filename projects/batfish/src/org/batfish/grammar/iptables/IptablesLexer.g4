lexer grammar IptablesLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.iptables;
}

//TODO: what are tokens?
tokens {
	QUOTED_TEXT
}

// Iptables Keywords

ACCEPT
:
	'ACCEPT'
;

FLAG_APPEND
:
	'-A' 
	| '--append'
;

FLAG_CHECK
:
	'-C' 
	| '--check'
;

FLAG_DELETE
:
	'-D' 
	| '--delete'
;

FLAG_INSERT
:
	'-I' 
	| '--insert'
;

FLAG_REPLACE
:
	'-R' 
	| '--replace'
;

FLAG_LIST
:
	'-L' 
	| '--list'
;

FLAG_LIST_RULES
:
	'-S' 
	| '--list-rules'
;

FLAG_FLUSH
:
	'-F' 
	| '--flush'
;

FLAG_ZERO
:
	'-Z' 
	| '--zero'
;

FLAG_NEW_CHAIN
:
	'-N' 
	| '--new-chain'
;

FLAG_DELETE_CHAIN
:
	'-X' 
	| '--delete-chain'
;

FLAG_POLICY
:
	'-P' 
	| '--policy'
;

FLAG_RENAME_CHAIN
:
	'-E' 
	| '--rename-chain'
;

FLAG_HELP
:
	'-h' 
;

FORWARD
:
	'FORWARD'
;

INPUT
:
	'INPUT'
;

OUTPUT
:
	'OUTPUT'
;

// Other tokens

DASH
:
   '-'
;

NEWLINE
:
   F_Newline+
;

WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

fragment
F_Newline
:
   [\r\n]+
;

fragment
F_Whitespace
:
   ' '
   | '\t'
   | '\u000C'
;
