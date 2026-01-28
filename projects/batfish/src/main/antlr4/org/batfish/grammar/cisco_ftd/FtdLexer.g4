lexer grammar FtdLexer;

options {
   superClass = 'org.batfish.grammar.cisco_ftd.parsing.FtdBaseLexer';
}

tokens {
   QUOTED_TEXT,
   RAW_TEXT
}

// Cisco FTD Keywords

AAA: 'aaa';

ABSOLUTE: 'absolute';

ACCESS_CLASS: 'access-class';

ACCESS_CONTROL: 'access-control';

ACCESS_GROUP: 'access-group';

ACCESS_LIST: 'access-list';

ACTIVE: 'active';

ACTIVATE: 'activate';

ADDRESS_FAMILY: 'address-family';

ADDRESS: 'address';

ADVANCED: 'advanced';

ALERTS: 'alerts';

ALWAYS: 'always';

ANY: 'any';

ANY4: 'any4';

ANY6: 'any6';

AREA: 'area';

ARP: 'arp';

AUTO: 'auto';

AFTER_AUTO: 'after-auto';

BEFORE_AUTO: 'before-auto';

BOOT: 'boot';

BGP: 'bgp';

CA: 'ca';

CISCO: 'cisco';

CLASS: 'class';

CLASS_MAP: 'class-map';

CONNECTED: 'connected';

COMMUNITY_LIST: 'community-list';

CONN_MATCH: 'conn-match';

CONNECTION: 'connection';

COUNTER: 'counter';

CRITICAL: 'critical';

CRYPTO: 'crypto';

CRYPTOCHECKSUM: 'Cryptochecksum';

CTS: 'cts';

DEBUGGING: 'debugging';

DEFAULT_INFORMATION: 'default-information';

DEFAULT: 'default';

DENY: 'deny';

DESCRIPTION: 'description' -> pushMode(M_Description);

DESTINATION: 'destination';

DIAGNOSTIC: 'diagnostic';

DISABLED: 'disabled';

DOMAIN: 'domain';

DOMAIN_LOOKUP: 'domain-lookup';

DP: 'dp';

DYNAMIC: 'dynamic';

EMERGENCIES: 'emergencies';

ERRORS: 'errors';

EXTENDED: 'extended';

DNS: 'dns';

DNS_GROUP: 'dns-group';

EQ: 'eq';

ENABLE: 'enable';

ENCRYPTION: 'encryption';
ESP_3DES: 'esp-3des';
ESP_AES: 'esp-aes';
ESP_AES_192: 'esp-aes-192';
ESP_AES_256: 'esp-aes-256';
ESP_DES: 'esp-des';
ESP_MD5_HMAC: 'esp-md5-hmac';
ESP_SHA_HMAC: 'esp-sha-hmac';
ESP_NONE: 'esp-none';

END: 'end';

ETHERNET: 'Ethernet';

EVENT_LOG: 'event-log';

EXIT_ADDRESS_FAMILY: 'exit-address-family';

EXIT: 'exit';

FAILOVER: 'failover';

FILTER_LIST: 'filter-list';

FLASH: 'flash';

FLOW_END: 'flow-end';

FLOW_NSEL: 'flow-nsel';

FLOW_OFFLOAD: 'flow-offload';

FLOW_START: 'flow-start';

FQDN: 'fqdn';

FTP: 'ftp';

GLOBAL: 'global';

GIGABIT_ETHERNET: 'GigabitEthernet';

GROUP: 'group';

GROUP_OBJECT: 'group-object';

GT: 'gt';

HARDWARE: 'hardware';

HOLDTIME: 'holdtime';

HOST: 'host';

HOSTNAME: 'hostname';

HTTPS: 'https';

ICMP: 'icmp';

ID: 'id';

IKEV2: 'ikev2';

IFC: 'ifc';

IN: 'in';

INTEGRITY: 'integrity';

INACTIVE: 'inactive';

INFINITE: 'infinite';

INFORMATIONAL: 'informational';

INTERFACE: 'interface';

IPSEC: 'ipsec';

IPSEC_ATTRIBUTES: 'ipsec-attributes';

IPSEC_L2L: 'ipsec-l2l';

IP: 'ip';

IPV4: 'ipv4';

IPV6: 'ipv6';

KEEPALIVE: 'keepalive';

KEEPALIVE_COUNTER: 'keepalive-counter';

KEEPALIVE_TIMEOUT: 'keepalive-timeout';

LAN: 'lan';

LB: 'lb';

LINE: 'line';

LINK: 'link';

LIFETIME: 'lifetime';

LOG: 'log';

LOG_ADJACENCY_CHANGES: 'log-adj-changes';

LOG_NEIGHBOR_CHANGES: 'log-neighbor-changes';

LOCAL_AUTHENTICATION: 'local-authentication';

MAP: 'map';

MATCH: 'match';

LOGGING: 'logging';

LOOKUP: 'lookup';

LT: 'lt';

MAC_ADDRESS: 'mac-address';

MANAGEMENT: 'Management';

MANAGEMENT_ONLY: 'management-only';

MANUAL: 'manual';

MASK: 'mask';

MODE: 'mode';

MONITOR_INTERFACE: 'monitor-interface';

MULTICHANNEL: 'multichannel';

MSEC: 'msec';

METRIC: 'metric';

METRIC_TYPE: 'metric-type';

MTU: 'mtu';

NAMEIF: 'nameif';

NAMES: 'names';

REMOTE_ACCESS: 'remote-access';

REMOTE_AUTHENTICATION: 'remote-authentication';

SECONDS: 'seconds';

TUNNEL: 'tunnel';
TUNNEL_GROUP: 'tunnel-group';

TYPE: 'type';

NAME_SERVER: 'name-server';

NAT: 'nat';

NEIGHBOR: 'neighbor';

NEQ: 'neq';

NETWORK: 'network';

NETWORK_OBJECT: 'network-object';

NEWLINE: ('\r'? '\n')+;

NGFW: 'NGFW';

NGIPS: 'ngips';

NO: 'no';

NOTIFICATIONS: 'notifications';

OBJECT: 'object';

OBJECT_GROUP: 'object-group';

OBJECT_GROUP_SEARCH: 'object-group-search';

OFFLOAD: 'offload';

ORIGINATE: 'originate';

OSPF: 'ospf';

OUT: 'out';

PARAMETERS: 'parameters';

PAGER: 'pager';

PASSIVE: 'passive';

PASSIVE_INTERFACE: 'passive-interface';

PEER: 'peer';

PASSWORD: 'password';

PBKDF2: 'pbkdf2';

PERMIT: 'permit';

PFS: 'pfs';

PMTU_AGING: 'pmtu-aging';

POLICY: 'policy';

POLICY_MAP: 'policy-map';

POLLTIME: 'polltime';

PORT_CHANNEL: 'Port-channel';

PORT_OBJECT: 'port-object';

PREFIX: 'prefix';

PRE_SHARED_KEY: 'pre-shared-key';

PRIMARY: 'primary';

PRESERVE: 'preserve';

PRESERVE_UNTAG: 'preserve-untag';

PROFILE: 'profile';

PROPAGATE: 'propagate';

PRF: 'prf';

PROXY: 'proxy';

RANGE: 'range';
TRANSPORT: 'transport';

REDISTRIBUTE: 'redistribute';

REMOTE_AS: 'remote-as';

REMARK: 'remark' -> pushMode(M_REMARK);

REVOCATION_CHECK: 'revocation-check';

ROUTE: 'route';

ROUTE_MAP: 'route-map';

ROUTER: 'router';

ROUTER_ID: 'router-id';

RULE_ID: 'rule-id';

SEARCH: 'search';

SECONDARY: 'secondary';

SECURITY_ASSOCIATION: 'security-association';

SECURITY_LEVEL: 'security-level';

SERVER_GROUP: 'server-group';

SERVICE: 'service';

SERVICE_MODULE: 'service-module';

SERVICE_OBJECT: 'service-object';

SERVICE_POLICY: 'service-policy';

SET: 'set';

SETUP: 'setup';

SGT: 'sgt';

SHUTDOWN: 'shutdown';

SNMP: 'snmp';

SNMP_SERVER: 'snmp-server';

SOURCE: 'source';

SSH: 'ssh';

STANDBY: 'standby';

STANDARD: 'standard';

STATIC: 'static';

SNORT: 'snort';

SYSTEM: 'system';

TAG: 'tag';

SUBNET: 'subnet';

SUBNETS: 'subnets';

TCP: 'tcp';

TELNET: 'telnet';

THREAT_DETECTION: 'threat-detection';

TIMERS: 'timers';

TIME_RANGE: 'time-range';

TIMEOUT: 'timeout';

TRAFFIC: 'traffic';

TRANSFORM: 'transform';

TRANSFORM_SET: 'transform-set';

TRUST: 'trust';

TRUSTED: 'trusted';

TRUSTPOOL: 'trustpool';

TZNAME: 'tzname';

UDP: 'udp';
UNICAST: 'unicast';

UNIT: 'unit';

VARIABLE: 'variable';

VERSION: 'version';

WHITELIST: 'whitelist';

VLAN: 'vlan';

VLAN_ID: 'vlan-id';

VRF: 'vrf';

VTY: 'vty';

WARNINGS: 'warnings';

WWW: 'www';

// Operators

AMPERSAND: '&';
ASTERISK: '*';
CARET: '^';
COLON: ':';
COMMA: ',';
DASH: '-';
DOLLAR: '$';
DOUBLE_QUOTE: '"';
FORWARD_SLASH: '/';
LEFT_BRACE: '{';
LEFT_BRACKET: '[';
LEFT_PAREN: '(';
PERIOD: '.';
PIPE: '|';
PLUS: '+';
RIGHT_BRACE: '}';
RIGHT_BRACKET: ']';
RIGHT_PAREN: ')';
SEMICOLON: ';';
UNDERSCORE: '_';

// Numbers

UINT8
:
   '0'
   | F_Digit_1_9
;

UINT16
:
   F_Digit_1_9 F_Digit
   | F_Digit_1_9 F_Digit F_Digit
   | F_Digit_1_9 F_Digit F_Digit F_Digit
   | [1-5] F_Digit F_Digit F_Digit F_Digit
   | '6' [0-4] F_Digit F_Digit F_Digit
   | '65' [0-4] F_Digit F_Digit
   | '655' [0-2] F_Digit
   | '6553' [0-5]
;

UINT32
:
   F_Uint32
;

DEC
:
   F_Digit+
;

IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit?
;

// Other

COMMENT_LINE
:
   (
      '!'
      | '#'
      | ':'
   )
   {lastTokenType() == NEWLINE || lastTokenType() == -1}?

   F_NonNewlineChar* F_Newline+ -> channel(HIDDEN)
;

VARIABLE_NAME
:
   '$' F_Variable_Name_Char+
;

// RAW_TEXT is now only used in specific lexer modes (M_REMARK, M_Description)
// See M_REMARK and M_Description modes at the end of this file.

// NAME matches identifiers and names that aren't recognized keywords
// It requires at least one non-alphanumeric character (underscore, hyphen, etc.)
// to avoid conflicting with keywords + numbers like "Ethernet1"
NAME
:
   F_Variable_Char* F_Variable_Special_Char F_Variable_Char*
;

// WORD matches alphabetic sequences that aren't recognized keywords
// Must be defined AFTER all keywords - when same length, first rule wins
WORD
:
   [a-zA-Z]+
;

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

// Fragments

fragment
F_DecByte
:
   F_Digit
   | F_PositiveDigit F_Digit
   | '1' F_Digit F_Digit
   | '2' [0-4] F_Digit
   | '25' [0-5]
;

fragment
F_Digit
:
   [0-9]
;

fragment
F_Digit_1_9
:
   [1-9]
;

fragment
F_HexDigit
:
   [0-9A-Fa-f]
;

fragment
F_Newline
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_PositiveDigit
:
   [1-9]
;

fragment
F_Uint32
:
   '0'
   | F_Digit_1_9 F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
;

fragment
F_Variable_Name_Char
:
   [0-9A-Za-z_]
;

fragment
F_WhitespaceChar
:
   [ \t\u000C]
;

// F_Variable_Char matches all characters allowed in identifiers/names
// Note: period (.) is NOT included so interface subinterfaces like Port-channel1.320
// can be tokenized as NAME PERIOD DEC
fragment
F_Variable_Char
:
   [a-zA-Z0-9_\-:@#]
;

// F_Variable_Special_Char - characters that distinguish names from keywords
// These are characters that can appear in names but NOT in keywords
fragment
F_Variable_Special_Char
:
   [_\-@#]
;

// Lexer modes for RAW_TEXT handling

mode M_REMARK;

M_REMARK_RAW_TEXT
:
   F_NonNewlineChar+ -> type(RAW_TEXT)
;

M_REMARK_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_REMARK_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Description;

M_Description_RAW_TEXT
:
   F_NonNewlineChar+ -> type(RAW_TEXT)
;

M_Description_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_Description_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;
