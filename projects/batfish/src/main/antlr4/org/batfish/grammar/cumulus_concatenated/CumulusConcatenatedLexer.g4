lexer grammar CumulusConcatenatedLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ANYTHING
:
  . | EOF
;

NEWLINE
:
  F_Newline+
;

// Fragments

fragment
F_Newline
:
  [\n\r]
;