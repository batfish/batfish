lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
TRUNK: 'trunk';
TAG: 'tag';
ROUTING: 'routing';
NATIVE: 'native';
ALLOWED: 'allowed';
ADDRESS_FAMILY: 'address-family';
ACCESS_LIST: 'access-list';
APPLY: 'apply';
ANY: 'any';
BGP: 'bgp';
COST: 'cost';
CONNECTED: 'connected';
DEFAULT_GATEWAY: 'default-gateway';
DESCRIPTION: 'description';
ACTIVATE: 'activate';
AREA: 'area';
ATTACH: 'attach';
HOSTNAME: 'hostname';
GE: 'ge';
LE: 'le';
INTERFACE: 'interface';
IN: 'in';
IP: 'ip';
PREFIX_LIST: 'prefix-list';
IPV4: 'ipv4';
LAG: 'lag';
LOOPBACK: 'loopback';
LOCAL_PREFERENCE: 'local-preference';
NO: 'no';
NETWORK: 'network';
MATCH: 'match';
MTU: 'mtu';
NEIGHBOR: 'neighbor';
NULLROUTE: 'nullroute';
REJECT: 'reject';
REDISTRIBUTE: 'redistribute';
SEQ: 'seq';
SET: 'set';
REMOTE_AS: 'remote-as';
ROUTE: 'route';
ROUTE_MAP: 'route-map';
ROUTED_IN: 'routed-in';
ROUTED_OUT: 'routed-out';
ROUTER: 'router';
ROUTER_ID: 'router-id';
OSPF: 'ospf';
OUT: 'out';
POINT_TO_POINT: 'point-to-point';
PERMIT: 'permit';
DENY: 'deny';
EQ: 'eq';
GT: 'gt';
LT: 'lt';
RANGE: 'range';
SHUTDOWN: 'shutdown';
UNICAST: 'unicast';
SPEED: 'speed';
STATIC: 'static';
VLAN: 'vlan';
VRF: 'vrf';

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
