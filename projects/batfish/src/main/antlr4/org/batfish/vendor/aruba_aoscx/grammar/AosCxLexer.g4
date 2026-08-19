lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

HOSTNAME: 'hostname';

NEWLINE: '\r'? '\n';

LINE_COMMENT
:
  '!' ~[\r\n]* -> channel(HIDDEN)
;

WORD
:
  ~[ \t\r\n!]+
;

WS
:
  [ \t]+ -> channel(HIDDEN)
;
