lexer grammar CumulusInterfacesLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_interfaces.parsing.CumulusInterfacesBaseLexer';
}
tokens {
  WORD
}

AUTO
:
  'auto' -> pushMode (M_Word)
;

IFACE
:
  'iface' -> pushMode(M_Word)
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* [#]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;

NEWLINE
:
  F_Newline+
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel
;

// Fragments

fragment
F_Newline
:
  [\n\r]
;

fragment
F_NonNewline
:
  ~[\n\r]
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z_.:] | '-'
;

mode M_Word;

M_Word_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_Word_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Words;

M_Words_NEWLINE
:
  F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Words_WORD
:
  F_Word -> type ( WORD )
;

M_Words_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;
