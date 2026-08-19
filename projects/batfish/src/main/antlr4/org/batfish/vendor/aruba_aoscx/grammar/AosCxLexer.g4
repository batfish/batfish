lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
AREA: 'area';
HOSTNAME: 'hostname';
INTERFACE: 'interface';
IP: 'ip';
LOOPBACK: 'loopback';
NO: 'no';
NETWORK: 'network';
NULLROUTE: 'nullroute';
REJECT: 'reject';
ROUTE: 'route';
ROUTER: 'router';
ROUTER_ID: 'router-id';
OSPF: 'ospf';
POINT_TO_POINT: 'point-to-point';
SHUTDOWN: 'shutdown';
SPEED: 'speed';
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
