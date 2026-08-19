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
GE: 'ge';
LE: 'le';
INTERFACE: 'interface';
IN: 'in';
IP: 'ip';
PREFIX_LIST: 'prefix-list';
IPV4: 'ipv4';
LOOPBACK: 'loopback';
LOCAL_PREFERENCE: 'local-preference';
NO: 'no';
NETWORK: 'network';
MATCH: 'match';
NEIGHBOR: 'neighbor';
NULLROUTE: 'nullroute';
REJECT: 'reject';
SEQ: 'seq';
SET: 'set';
REMOTE_AS: 'remote-as';
ROUTE: 'route';
ROUTE_MAP: 'route-map';
ROUTER: 'router';
ROUTER_ID: 'router-id';
OSPF: 'ospf';
OUT: 'out';
POINT_TO_POINT: 'point-to-point';
PERMIT: 'permit';
DENY: 'deny';
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
