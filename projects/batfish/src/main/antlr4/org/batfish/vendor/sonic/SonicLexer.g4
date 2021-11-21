lexer grammar SonicLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ANYTHING
:
  . | EOF
;
