lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
TRUNK: 'trunk';
TAG: 'tag';
TRANSIT_DELAY: 'transit-delay';
TYPE: 'type';
TYPE_1: 'type-1';
TYPE_2: 'type-2';
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
DISTRIBUTE_LIST: 'distribute-list';
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
PREFIX: 'prefix';
PREFIX_LIST: 'prefix-list';
IPV4: 'ipv4';
IPV6: 'ipv6';
LAG: 'lag';
LOOPBACK: 'loopback';
LOCAL_PREFERENCE: 'local-preference';
LOCAL: 'local';
LINK_LOCAL: 'link-local';
NO: 'no';
NO_ADVERTISE: 'no-advertise';
NO_SUMMARY: 'no-summary';
NSSA: 'nssa';
NETWORK: 'network';
MATCH: 'match';
MAXIMUM_PATHS: 'maximum-paths';
METRIC: 'metric';
METRIC_TYPE: 'metric-type';
MTU: 'mtu';
NEIGHBOR: 'neighbor';
NULLROUTE: 'nullroute';
REJECT: 'reject';
REDISTRIBUTE: 'redistribute';
RETRANSMIT_INTERVAL: 'retransmit-interval';
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
PRIORITY: 'priority';
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
SUMMARY_ADDRESS: 'summary-address';
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
