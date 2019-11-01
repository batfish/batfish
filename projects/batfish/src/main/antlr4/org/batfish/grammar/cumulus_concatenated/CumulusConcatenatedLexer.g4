lexer grammar CumulusConcatenatedLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ANYTHING
:
  . | EOF
;
