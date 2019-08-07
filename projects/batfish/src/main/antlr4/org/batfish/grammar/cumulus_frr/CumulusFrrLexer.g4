lexer grammar CumulusFrrLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseLexer';
}

COMMENT_LINE
:
  (
    F_Whitespace
  )* [!]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;

DEC
:
  F_Digit+
;

EXIT_VRF
:
  'exit-vrf'
;

FRR_VERSION_LINE
:
  'frr version' F_NonNewline*
;

NEWLINE
:
  F_Newline+
;

VNI
:
  'vni'
;

VRF
:
  'vrf' -> pushMode(M_Word)
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

WORD
:
  F_Word
;

// Fragments
fragment
F_Digit
:
  [0-9]
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$^*_=+.;:{}]
  | '-'
;

fragment
F_Newline
:
  [\n\r] // carriage return or line feed

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

// modes
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
