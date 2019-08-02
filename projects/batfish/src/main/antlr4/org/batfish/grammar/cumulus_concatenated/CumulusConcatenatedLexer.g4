lexer grammar CumulusConcatenatedLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

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