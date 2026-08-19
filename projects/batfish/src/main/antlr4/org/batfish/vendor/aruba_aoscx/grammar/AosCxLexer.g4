lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
ADDRESS_FAMILY: 'address-family';
BGP: 'bgp';
ACTIVATE: 'activate';
AREA: 'area';
HOSTNAME: 'hostname';
INTERFACE: 'interface';
IP: 'ip';
IPV4: 'ipv4';
LOOPBACK: 'loopback';
NO: 'no';
NETWORK: 'network';
NEIGHBOR: 'neighbor';
NULLROUTE: 'nullroute';
REJECT: 'reject';
REMOTE_AS: 'remote-as';
ROUTE: 'route';
ROUTER: 'router';
ROUTER_ID: 'router-id';
OSPF: 'ospf';
POINT_TO_POINT: 'point-to-point';
SHUTDOWN: 'shutdown';
UNICAST: 'unicast';
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
