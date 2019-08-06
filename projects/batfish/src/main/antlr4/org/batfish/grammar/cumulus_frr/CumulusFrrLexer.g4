lexer grammar CumulusFrrLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseLexer';
}

// Keywords
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

VRF
:
  'vrf'
;

// Fragments
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
