lexer grammar CumulusFrrLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

// Keywords
VRF
:
  'vrf'
;

EXIT_VRF
:
  'exit-vrf'
;

NEWLINE
:
  F_Newline+
;

fragment
F_Newline
:
  [\r\n] // carriage return or line feed
;

