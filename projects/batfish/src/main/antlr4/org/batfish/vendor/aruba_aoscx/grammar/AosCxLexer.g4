lexer grammar AosCxLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

ADDRESS: 'address';
TRUNK: 'trunk';
TAG: 'tag';
THREE_DES: '3des';
TRANSIT_DELAY: 'transit-delay';
TYPE: 'type';
TYPE_1: 'type-1';
TYPE_2: 'type-2';
ROUTING: 'routing';
NATIVE: 'native';
ALLOWED: 'allowed';
ALL_INTERFACES: 'all-interfaces';
ALWAYS: 'always';
ADDRESS_FAMILY: 'address-family';
ACCESS_LIST: 'access-list';
APPLY: 'apply';
ANY: 'any';
AES: 'aes';
AUTHENTICATION: 'authentication';
BFD: 'bfd';
BGP: 'bgp';
BROADCAST: 'broadcast';
COST: 'cost';
CONNECTED: 'connected';
CIPHERTEXT: 'ciphertext';
COUNT: 'count';
DEFAULT_GATEWAY: 'default-gateway';
DEFAULT_INFORMATION: 'default-information';
DEFAULT_METRIC: 'default-metric';
DEFAULT: 'default';
DESCRIPTION: 'description';
DES: 'des';
DEAD_INTERVAL: 'dead-interval';
DISTANCE: 'distance';
DISTRIBUTE_LIST: 'distribute-list';
ACTIVATE: 'activate';
ACTIVE_BACKBONE: 'active-backbone';
AREA: 'area';
ATTACH: 'attach';
HOSTNAME: 'hostname';
HELLO_INTERVAL: 'hello-interval';
HELPER: 'helper';
HEX_STRING: 'hex-string';
GE: 'ge';
GRACEFUL_RESTART: 'graceful-restart';
LE: 'le';
INTERFACE: 'interface';
IGNORE_LOST_INTERFACE: 'ignore-lost-interface';
INTER_AREA: 'inter-area';
INTRA_AREA: 'intra-area';
IN: 'in';
IP: 'ip';
PREFIX: 'prefix';
PREFIX_LIST: 'prefix-list';
IPV4: 'ipv4';
IPV6: 'ipv6';
IPSEC: 'ipsec';
LAG: 'lag';
LOOPBACK: 'loopback';
LOCAL_PREFERENCE: 'local-preference';
LOCAL: 'local';
LINK_LOCAL: 'link-local';
NO: 'no';
NO_ADVERTISE: 'no-advertise';
NO_SUMMARY: 'no-summary';
NULL: 'null';
NSSA: 'nssa';
NETWORK: 'network';
MATCH: 'match';
MD5: 'md5';
MAX_METRIC: 'max-metric';
MAXIMUM_PATHS: 'maximum-paths';
METRIC: 'metric';
METRIC_TYPE: 'metric-type';
MTU: 'mtu';
NEIGHBOR: 'neighbor';
NULLROUTE: 'nullroute';
REJECT: 'reject';
REDISTRIBUTE: 'redistribute';
RESTART_INTERVAL: 'restart-interval';
RETRANSMIT_INTERVAL: 'retransmit-interval';
SEQ: 'seq';
SET: 'set';
REMOTE_AS: 'remote-as';
ROUTE: 'route';
ROUTE_MAP: 'route-map';
ROUTE_TYPE: 'route-type';
ROUTED_IN: 'routed-in';
ROUTED_OUT: 'routed-out';
ROUTER: 'router';
ROUTER_ID: 'router-id';
ROUTER_LSA: 'router-lsa';
OSPF: 'ospf';
OSPFV3: 'ospfv3';
ORIGINATE: 'originate';
ON_STARTUP: 'on-startup';
OUT: 'out';
POINT_TO_POINT: 'point-to-point';
PLAINTEXT: 'plaintext';
PASSIVE: 'passive';
PRIORITY: 'priority';
PASSIVE_INTERFACE: 'passive-interface';
REFERENCE_BANDWIDTH: 'reference-bandwidth';
PERMIT: 'permit';
DENY: 'deny';
DISABLE: 'disable';
ENABLE: 'enable';
ENCRYPTION: 'encryption';
EQ: 'eq';
EXTERNAL: 'external';
GT: 'gt';
LT: 'lt';
RANGE: 'range';
SHUTDOWN: 'shutdown';
SHA1: 'sha1';
SPI: 'spi';
UNICAST: 'unicast';
SPEED: 'speed';
SOURCE_PROTOCOL: 'source-protocol';
STATIC: 'static';
STRICT_LSA_CHECK: 'strict-lsa-check';
SUMMARY_ADDRESS: 'summary-address';
STUB: 'stub';
STUB_DEFAULT_ROUTE: 'stub-default-route';
VIRTUAL_LINK: 'virtual-link';
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
