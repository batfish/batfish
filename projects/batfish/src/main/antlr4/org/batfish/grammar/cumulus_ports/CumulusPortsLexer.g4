lexer grammar CumulusPortsLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_ports.parsing.CumulusPortsBaseLexer';
}

// Keywords

DISABLED: 'disabled';

EQUALSIGN
:
  '='
;

// Other tokens
BLANK_LINE
:
  (
    F_Whitespace
  )* F_Newline
  {lastTokenType() == NEWLINE|| lastTokenType() == -1}?

  F_Newline* -> channel ( HIDDEN )
;

BREAKOUT
:
  F_Digit+ 'x' F_Digit+ 'G'
;

PORT
:
  F_Digit+
;

SPEED
:
  F_Digit+ 'G'
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
F_Digit
:
  [0-9]
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
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

