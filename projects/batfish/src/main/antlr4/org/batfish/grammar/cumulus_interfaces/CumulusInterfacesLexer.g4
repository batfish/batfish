lexer grammar CumulusInterfacesLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_interfaces.parsing.CumulusInterfacesBaseLexer';
}
tokens {
  WORD
}

// Keyword tokens

ADDRESS
:
  'address'
;

AUTO
:
  'auto' -> pushMode (M_Word)
;

IFACE
:
  'iface' -> pushMode(M_Word)
;

// Complex tokens
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

IP_PREFIX
:
  F_IpPrefix
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
F_Digit
:
  [0-9]
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_IpPrefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_IpPrefixLength
:
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

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
F_PositiveDigit
:
  [1-9]
;

fragment
F_Uint8
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
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
