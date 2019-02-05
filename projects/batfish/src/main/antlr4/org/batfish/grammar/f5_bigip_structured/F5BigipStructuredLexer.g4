lexer grammar F5BigipStructuredLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in F5BigipStructuredLexer.java goes here

private int lastTokenType = -1;

@Override
public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
       lastTokenType = token.getType();
    }
}

}

// Keywords

ADDRESS
:
  'address'
;

FORTY_G
:
  '40G'
;

BUNDLE_SPEED
:
  'bundle-speed'
;

GLOBAL_SETTINGS
:
  'global-settings'
;

HOSTNAME
:
  'hostname'
;

IF
:
  'if'
;

INTERFACE
:
  'interface'
;

INTERFACES
:
  'interfaces'
;

NET
:
  'net'
;

ONE_HUNDRED_G
:
  '100G'
;

SELF
:
  'self'
;

SYS
:
  'sys'
;

TAG
:
  'tag'
;

VLAN
:
  'vlan'
;

// Complex tokens

BRACE_LEFT
:
  '{'
;

BRACE_RIGHT
:
  '}'
;

BRACKET_LEFT
:
  '['
;

BRACKET_RIGHT
:
  ']'
;

DOUBLE_QUOTED_STRING
:
  '"' ~'"'* '"'
;

LINE_COMMENT
:
  '#' F_NonNewlineChar* F_Newline+ -> channel ( HIDDEN )
;

NEWLINE
:
  F_Newline+
;

SEMICOLON
:
  ';' -> channel ( HIDDEN )
;

WORD
:
  F_WordChar+
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

// Fragments

fragment
F_Newline
:
  [\r\n] // carriage return or line feed

;

fragment
F_NonNewlineChar
:
  ~[\r\n] // carriage return or line feed

;

fragment
F_Whitespace
:
  [ \t\u000C] // tab or space or unicode 0x000C

;

fragment
F_WordChar
:
  ~[ \t\n\r{}[\]]
;
