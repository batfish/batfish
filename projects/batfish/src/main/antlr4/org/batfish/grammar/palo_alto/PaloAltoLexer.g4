lexer grammar PaloAltoLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in PaloAltoLexer.java goes here
}

// Keywords

DEVICECONFIG
:
    'deviceconfig'
;

HOSTNAME
:
    'hostname'
;

SET
:
    'set'
;

SYSTEM
:
    'system'
;

// Complex tokens

NEWLINE
:
    F_Newline+
;

VARIABLE
:
   F_Variable_VarChar+
;

WS
:
    F_Whitespace+ -> channel(HIDDEN) // parser never sees tokens on hidden channel
;

// Fragments

fragment
F_Newline
:
    [\r\n] // carriage return or line feed
;

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r;{}[\]&|()"']
;

// Modes
// Blank for now, not all lexers will require modes
