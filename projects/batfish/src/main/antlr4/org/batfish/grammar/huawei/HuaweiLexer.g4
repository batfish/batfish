lexer grammar HuaweiLexer;

options {
   superClass = 'org.batfish.grammar.huawei.parsing.HuaweiBaseLexer';
}

tokens {
   IPV6_ADDRESS,
   IPV6_PREFIX
}

// Huawei VRP Keywords

ACL: 'acl';

ACL_NUMBER: 'acl-number';

ACL_NAME: 'acl-name';

ACL_ADVANCED: 'advanced';

ACL_BASIC: 'basic';

ACL_IPV6: 'ipv6';

ADDRESS: 'address';

ADDRESS_GROUP: 'address-group';

MASK: 'mask';

AREA: 'area';

ARP: 'arp';

AS_NUMBER: 'as-number';

AS_PATH_LIMIT: 'as-path-limit';

AUTHENTICATION_MODE: 'authentication-mode';

AUTO: 'auto';

AVAILABLE: 'available';

ANY: 'any';

BANDWIDTH: 'bandwidth';

BOTH: 'both';

BATCH: 'batch';

BECOME: 'become';

BGP: 'bgp';

BOOTROM: 'bootrom';

BRIDGE_DOMAIN: 'bridge-domain';

CANCEL: 'cancel';

CAPABILITY: 'capability';

CFM: 'cfm';

CHECK_ENABLE: 'check-enable';

CIR: 'cir';

CLASS: 'class';

CLASS_MAP: 'class-map';

CLEAN: 'clean';

COMMAND: 'command';

COMMIT: 'commit';

COMMUNITY: 'community';

CONFIG: 'config';

CONNECT_INTERFACE: 'connect-interface';

CONTROL: 'control';

COST: 'cost';

CRAWL: 'crawl';

CREATE: 'create';

CRYPTO: 'crypto';

CROSS_CONNECT: 'cross-connect';

CUSTOMER: 'customer';

DAR: 'dar';

DATA: 'data';

DELETABLE: 'deletable';

DENY: 'deny';

DESCRIPTION: 'description';

DEST_ADDRESS: 'dest-address';

DESTINATION: 'destination';

DEVICE: 'device';

DHCP: 'dhcp';

DIFF_SERV: 'diff-serv';

DISTINGUISHER: 'distinguisher';

DISABLE: 'disable';

DISCARD: 'discard';

DISP: 'disp';

DISPLAY: 'display';

DOT1Q: 'dot1q';

DUAL: 'dual';

DYNAMIC: 'dynamic';

EAP: 'eap';

EAPOL: 'eapol';

EGRESS: 'egress';

ENABLE: 'enable';

ENCAP: 'encap';

ENCAPULATION: 'encapsulation';

EQ: 'eq';

ESI: 'esi';

ETH_TRUNK: 'Eth-Trunk';

ETHERNET: 'Ethernet';

EVC: 'evc';

EVPN: 'evpn';

EVI: 'evi';

EXPORT: 'export';

EXTERNAL: 'external';

EXTENDED: 'extended';

FAILOVER: 'failover';

FIRMWARE: 'firmware';

FLEX_ALGO: 'flex-algo';

FLOW: 'flow';

FLOW_ACTION: 'flow-action';

FLOOD: 'flood';

FRAGMENT: 'fragment';

FORCED: 'forced';

FR: 'fr';

FR_ID: 'fr-id';

FREE: 'free';

FTP: 'ftp';

GEMPORT: 'gemport';

GIGABITETHERNET: 'GigabitEthernet';

GLOBAL: 'global';

GROUP: 'group';

GT: 'gt';

HA: 'ha';

HEADING: 'heading';

HEX: 'hex';

HOT_STANDBY: 'hot-standby';

HRP: 'hrp';

HW_DLD: 'hw.dld';

ICMP: 'icmp';

ID: 'id';

IF_INDEX: 'if-index';

IGMP: 'igmp';

IGMP_SNOOPING: 'igmp-snooping';

IMPORT: 'import';

IMPORT_ROUTE: 'import-route';

INBOUND: 'inbound';

INFO_CENTER: 'info-center';

INTERNAL: 'internal';

INTERFACE: 'interface';

INSIDE: 'inside';

IP: 'ip';

IP_ADDRESS: 'ip-address';

IP_PREFIX: 'ip-prefix';

IP_POOL: 'ip-pool';

IP_SOURCE: 'ip-source';

IPV4: 'ipv4';

ISOLATE: 'isolate';

ISPID: 'ispid';

KEEPALIVE: 'keepalive';

L2: 'l2';

L2VPN: 'l2vpn';

L3: 'l3';

LAG: 'lag';

LACP: 'lacp';

LDRA: 'ldra';

LEAK: 'leak';

LIMIT: 'limit';

LINE: 'line';

LINK_AGGR: 'link-aggr';

LOAD_BALANCE: 'load-balance';

LOCAL: 'local';

LOG: 'log';

LOGGING: 'logging';

LT: 'lt';

LOOPBACK: 'LoopBack';

MAC_ADDRESS: 'mac-address';

MAC_LEARNING: 'mac-learning';

MAIN: 'main';

MASTER: 'master';

MATCH: 'match';

MAX: 'max';

MDI: 'mdi';

MGMT: 'mgmt';

MPLS: 'mpls';

MPLS_TE: 'mpls-te';

MRU: 'mru';

MTU: 'mtu';

MULTICAST: 'multicast';

NAME: 'name';

NAT: 'nat';

NEGOTIATION: 'negotiation';

NEIGHBOR: 'neighbor';

NET_STREAM: 'net-stream';

NETWORK: 'network';

NEXT_HOP: 'next-hop';

NO: 'no';

NON_STOP_ROUTING: 'non-stop-routing';

NTP: 'ntp';

NULL: 'null';

OAM: 'oam';

OFF: 'off';

ON: 'on';

OPERATION: 'operation';

OPTION: 'option';

OSPF: 'ospf';

OUTBOUND: 'outbound';

OVERWRITE: 'overwrite';

PACKET: 'packet';

PARENT: 'parent';

PASSWORD: 'password';

PEER: 'peer';

PERMIT: 'permit';

PHYSICAL: 'physical';

PIRT: 'pirt';

POLICY: 'policy';

POLICY_MAP: 'policy-map';

PORT: 'port';

PORTAL: 'portal';

PRECEDENCE: 'precedence';

PREFER: 'prefer';

PREFERENCE: 'preference';

PREFIX: 'prefix';

PREFIX_PRIORITY: 'prefix-priority';

PRE_SHARED_KEY: 'pre-shared-key';

PRIMARY: 'primary';

PRIO: 'prio';

PRIORITY: 'priority';

PROFILE: 'profile';

PROTECT: 'protect';

PROTOCOL: 'protocol';

PRUNE: 'prune';

PVID: 'pvid';

QINQ: 'qinq';

QOS: 'qos';

QUERY: 'query';

QUEUE: 'queue';

RADIUS: 'radius';

RANGE: 'range';

RATE: 'rate';

RATE_LIMIT: 'rate-limit';

RAW: 'raw';

RECOVER: 'recover';

RECOVERY: 'recovery';

REDIRECT: 'redirect';

REFLECT: 'reflect';

REGISTRATION: 'registration';

REJECT: 'reject';

REMOTE: 'remote';

RESET: 'reset';

RESTRICT: 'restrict';

RETURN: 'return';

RIP: 'rip';

RIPNG: 'ripng';

RMON: 'rmon';

ROLE: 'role';

ROUTE: 'route';

ROUTE_DISTINGUISHER: 'route-distinguisher';

ROUTE_POLICY: 'route-policy';

ROUTE_STATIC: 'route-static';

ROUTER_ID: 'router-id';

ROUTING: 'routing';

RULE: 'rule';

RPC: 'rpc';

RSVP: 'rsvp';

RSVP_TE: 'rsvp-te';

RUN: 'run';

SA: 'sa';

SAC: 'sac';

SCHEDULE: 'schedule';

SECONDARY: 'secondary';

SECTION: 'section';

SECURE: 'secure';

SECURITY: 'security';

SERVER: 'server';

SERVICE: 'service';

SESSION: 'session';

SET: 'set';

SHARE: 'share';

SHUTDOWN: 'shutdown';

SID: 'sid';

SNMP: 'snmp';

SNMP_AGENT: 'snmp-agent';

SOURCE: 'source';

SOURCE_ADDRESS: 'source-address';

SSH: 'ssh';

SSL: 'ssl';

STANDBY: 'standby';

STATIC: 'static';

STP: 'stp';

SUBMIT: 'submit';

SUPER: 'super';

SUPPRESS: 'suppress';

SVC: 'svc';

SVLAN: 'svlan';

SWITCH: 'switch';

SYSNAME: 'sysname';

SYSTEM: 'system';

TCS: 'tcs';

TCP: 'tcp';

TELNET: 'telnet';

TERMINATION: 'termination';

TFTP: 'tftp';

TIMEOUT: 'timeout';

TIMER: 'timer';

TIMES: 'times';

TLS: 'tls';

TOPOLOGY: 'topology';

TRACK: 'track';

TRACEROUTE: 'traceroute';

TRANSACTION: 'transaction';

TRANSIT: 'transit';

TRAP: 'trap';

TRUNK: 'trunk';

TUNNEL: 'tunnel';

TYPE: 'type';

UCL: 'ucl';

UDP: 'udp';

UNDO: 'undo';

UNICAST: 'unicast';

UNITS: 'units';

UNREACHABLE: 'unreachable';

UPDATE: 'update';

USER: 'user';

USER_GROUP: 'user-group';

USER_INTERFACE: 'user-interface';

VLAN: 'vlan';

VLANIF: 'Vlanif';

VLAN_BATCH: 'vlan batch';

VID: 'vid';

VLL: 'vll';

VPLS: 'vpls';

VPN: 'vpn';

VPN_INSTANCE: 'vpn-instance';

VPN_TARGET: 'vpn-target';

VRRP: 'vrrp';

VS: 'vs';

VXLAN: 'vxlan';

WINDOW: 'window';

XPL: 'xpl';

Y1731: 'y1731';

ZONE: 'zone';

// Literals

NEWLINE: '\r'? '\n' -> skip;

WS: [ \t]+ -> skip;

// Fragments for numeric patterns
fragment DIGIT: [0-9];
fragment NONZERO_DIGIT: [1-9];

// Numbers - simplified definitions (exact range validation happens in extraction)
UINT8: DIGIT+;
UINT16: DIGIT+;
UINT32: DIGIT+;

DEC: '-'? [0-9]+ ('.' [0-9]+)?;

FLOAT: [0-9]+ '.' [0-9]+;

// IP Address and Prefix patterns (NOT keywords)
IPV4_ADDRESS_PATTERN
:
   UINT8 '.' UINT8 '.' UINT8 '.' UINT8
;

IPV6_ADDRESS_PATTERN
:
   // Must have at least 2 colons to distinguish from route distinguisher format (e.g., 100:1)
   [0-9a-fA-F:]* ':' [0-9a-fA-F:]* ':' [0-9a-fA-F:]*
   -> type(IPV6_ADDRESS)
;

IPV4_PREFIX_PATTERN
:
   IPV4_ADDRESS_PATTERN '/' UINT8
;

IPV6_PREFIX_PATTERN
:
   IPV6_ADDRESS_PATTERN '/' UINT8 -> type(IPV6_PREFIX)
;

// Variables

VARIABLE: (~[ \t\r\n])+;

// RAW_TEXT moved to fragments only - use in parser modes if needed
fragment RAW_TEXT: (~[\r\n])+;

DOUBLE_QUOTE: '"';

QUIT: 'quit';

EXIT: 'exit';

SUB: 'sub';

SUB_INTERFACE: 'sub-interface';

SHORTER: 'shorter';

COMMA: ',';

COLON: ':';

PERIOD: '.';

FORWARD_SLASH: '/';

DASH: '-';

PLUS: '+';

EQUALS: '=';

BRACKET_LEFT: '[';

BRACKET_RIGHT: ']';

CARET: '^';

TILDE: '~';

EXCLAMATION: '!';

AT: '@';

HASH: '#';

DOLLAR: '$';

PERCENT: '%';

AMPERSAND: '&';

ASTERISK: '*';

UNDERSCORE: '_';

PIPE: '|';

BACKSLASH: '\\';
