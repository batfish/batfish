lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
HOSTNAME: 'hostname';
INTERFACE: 'interface';
IP: 'ip';
LOOPBACK: 'loopback';
NO: 'no';
NULLROUTE: 'nullroute';
REJECT: 'reject';
ROUTE: 'route';
SHUTDOWN: 'shutdown';
VLAN: 'vlan';

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
