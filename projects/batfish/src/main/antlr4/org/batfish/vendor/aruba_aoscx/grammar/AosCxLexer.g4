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
ALWAYS: 'always';
ADDRESS_FAMILY: 'address-family';
ACCESS_LIST: 'access-list';
APPLY: 'apply';
ANY: 'any';
BGP: 'bgp';
BROADCAST: 'broadcast';
COST: 'cost';
CONNECTED: 'connected';
COUNT: 'count';
DEFAULT_GATEWAY: 'default-gateway';
DEFAULT_INFORMATION: 'default-information';
DEFAULT_METRIC: 'default-metric';
DEFAULT: 'default';
DESCRIPTION: 'description';
DEAD_INTERVAL: 'dead-interval';
DISTANCE: 'distance';
ACTIVATE: 'activate';
AREA: 'area';
ATTACH: 'attach';
HOSTNAME: 'hostname';
HELLO_INTERVAL: 'hello-interval';
GE: 'ge';
LE: 'le';
INTERFACE: 'interface';
INTER_AREA: 'inter-area';
INTRA_AREA: 'intra-area';
IN: 'in';
IP: 'ip';
PREFIX_LIST: 'prefix-list';
IPV4: 'ipv4';
IPV6: 'ipv6';
LAG: 'lag';
LOOPBACK: 'loopback';
LOCAL_PREFERENCE: 'local-preference';
LINK_LOCAL: 'link-local';
NO: 'no';
NO_SUMMARY: 'no-summary';
NETWORK: 'network';
MATCH: 'match';
METRIC: 'metric';
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
OSPFV3: 'ospfv3';
ORIGINATE: 'originate';
OUT: 'out';
POINT_TO_POINT: 'point-to-point';
PASSIVE: 'passive';
PASSIVE_INTERFACE: 'passive-interface';
REFERENCE_BANDWIDTH: 'reference-bandwidth';
PERMIT: 'permit';
DENY: 'deny';
DISABLE: 'disable';
ENABLE: 'enable';
EQ: 'eq';
EXTERNAL: 'external';
GT: 'gt';
LT: 'lt';
RANGE: 'range';
SHUTDOWN: 'shutdown';
UNICAST: 'unicast';
SPEED: 'speed';
STATIC: 'static';
STUB: 'stub';
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
