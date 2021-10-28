lexer grammar A10Lexer;

options {
   superClass = 'A10BaseLexer';
}

tokens {
  QUOTED_TEXT,
  RBA_LINE,
  RBA_TAIL,
  WORD,
  WORD_SEPARATOR
}

// A10 keywords
ACCESS_LIST: 'access-list';
ACTIVATE: 'activate';
ACTIVE: 'active';
ADDRESS: 'address';
AFLEX: 'aflex' -> pushMode(M_Word);
ARP_RETRY: 'arp-retry';
AUTO: 'auto';
BFD: 'bfd';
BGP: 'bgp';
BLADE_PARAMETERS: 'blade-parameters';
BOTH: 'both';
BUCKET_COUNT: 'bucket-count';
CACHE: 'cache';
CAPABILITY: 'capability';
CHECK: 'check';
CIPHER: 'cipher';
CLIENT_SSL: 'client-ssl';
COMMON: 'common';
COMPOUND
:
  'compound'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
CONN_LIMIT: 'conn-limit';
CONN_MIRROR: 'conn-mirror';
CONNECTED: 'connected';
CONNECTION_REUSE: 'connection-reuse';
COOKIE: 'cookie';
CPU_PROCESS: 'cpu-process';
DATABASE
:
  'database'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
DEAD_TIMER: 'dead-timer';
DEF_SELECTION_IF_PREF_FAILED: 'def-selection-if-pref-failed';
DEFAULT: 'default';
DESCRIPTION: 'description' -> pushMode(M_Word);
DESTINATION_IP: 'destination-ip';
DEVICE_ID: 'device-id';
DIAMETER: 'diameter';
DISABLE: 'disable';
DISABLE_DEFAULT_VRID: 'disable-default-vrid';
DNS: 'dns';
DO_AUTO_RECOVERY: 'do-auto-recovery';
DUPLEXITY: 'duplexity';
DYNAMIC: 'dynamic';
DYNAMIC_SERVICE: 'dynamic-service';
HA: 'ha';
ENABLE: 'enable';
ETHERNET: 'ethernet';
EXPECT: 'expect';
EXTENDED: 'extended';
EXTERNAL
:
  'external'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
FAIL_OVER_POLICY_TEMPLATE: 'fail-over-policy-template' -> pushMode(M_Word);
FALL_OVER: 'fall-over';
FAST_EXTERNAL_FAILOVER: 'fast-external-failover';
FLOATING_IP: 'floating-ip';
FTP: 'ftp';
GATEWAY: 'gateway';
GET_READY_TIME: 'get-ready-time';
GET_READY_TIME_ACOS2: 'get_ready_time';
GLOBAL: 'global';
GRACEFUL_RESTART: 'graceful-restart';
GROUP: 'group';
HELLO_INTERVAL: 'hello-interval';
HA_GROUP: 'ha-group';
HA_GROUP_ID: 'ha-group-id';
HALFOPEN: 'halfopen';
HEALTH: 'health';
HEALTH_CHECK: 'health-check' -> pushMode(M_Word);
HEALTH_CHECK_DISABLE: 'health-check-disable';
HOSTNAME: 'hostname' -> pushMode(M_Word);
HTTP
:
  'http'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
HTTPS
:
  'https'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
ICMP: 'icmp';
ID: 'id';
INTERVAL: 'interval';
IMAP: 'imap';
IP: 'ip';
IP_NAT: 'ip-nat';
IP_RR: 'ip-rr';
IPG_BIT_TIME: 'ipg-bit-time';
INBOUND: 'inbound';
INTERFACE: 'interface';
KERBEROS_KDC: 'kerberos-kdc';
LACP: 'lacp';
LACP_TRUNK: 'lacp-trunk';
LACP_UDLD: 'lacp-udld';
LDAP
:
  'ldap'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
LEAST_CONNECTION: 'least-connection';
LEAST_REQUEST: 'least-request';
LLDP: 'lldp';
LOAD_INTERVAL: 'load-interval';
LOCAL_PREFERENCE: 'local-preference';
LOG_NEIGHBOR_CHANGES: 'log-neighbor-changes';
LONG: 'long';
LOOPBACK: 'loopback';
MAXIMUM_PATHS: 'maximum-paths';
MAXIMUM_PREFIX: 'maximum-prefix';
MEMBER: 'member' -> pushMode(M_Word);
METHOD: 'method';
MIN_ACTIVE_MEMBER: 'min-active-member';
MODE: 'mode';
MONITOR
:
  'monitor'
  {
    if (lastTokenType() == HEALTH) {
      pushMode(M_Word);
    }
  }
;
MTU: 'mtu';
NAME: 'name' -> pushMode(M_Word);
NAT: 'nat';
NEIGHBOR: 'neighbor';
NETMASK: 'netmask';
NEXTHOP_TRIGGER_COUNT: 'nexthop-trigger-count';
NO: 'no';
NO_AUTO_UP_ON_AFLEX: 'no-auto-up-on-aflex';
NO_HEARTBEAT: 'no-heartbeat';
NONE: 'none';
NOTIFICATION: 'notification';
NTP: 'ntp';
ONLY_FLAGGED: 'only-flagged';
ONLY_NOT_FLAGGED: 'only-not-flagged';
OPTIMIZATION_LEVEL: 'optimization-level';
ORF: 'orf';
OVERRIDE_PORT: 'override-port';
PARTITION: 'partition' -> pushMode(M_Word);
PASSIVE: 'passive';
PEER: 'peer';
PEER_GROUP: 'peer-group';
PERSIST: 'persist';
PERSIST_SCORING: 'persist-scoring';
POLICY: 'policy';
POOL: 'pool' -> pushMode(M_Word);
POP3: 'pop3';
PORT
:
  'port'
  {
    if (lastTokenType() == TEMPLATE) {
      pushMode(M_Word);
    }
  }
;
PORT_OVERLOAD: 'port-overload';
PORTS_THRESHOLD: 'ports-threshold';
PREEMPT_MODE: 'preempt-mode';
PREEMPTION_DELAY: 'preemption-delay';
PREEMPTION_ENABLE: 'preemption-enable';
PRIORITY: 'priority';
RADIUS
:
  'radius'
  {
    if (lastTokenType() == METHOD) {
      pushMode(M_Words);
    }
  }
;
RANGE: 'range';
RBA: 'rba' -> pushMode(M_Rba);
RESTART_TIME: 'restart-time';
REDISTRIBUTE: 'redistribute';
REDISTRIBUTION_FLAGGED: 'redistribution-flagged';
REMOTE_AS: 'remote-as';
RETRY: 'retry';
ROLE: 'role';
ROUND_ROBIN: 'round-robin';
ROUTE: 'route';
ROUTE_REFRESH: 'route-refresh';
ROUTER: 'router';
ROUTER_ID: 'router-id';
ROUTER_INTERFACE: 'router-interface';
RTSP: 'rtsp';
RX: 'rx';
SCALEOUT_DEVICE_ID: 'scaleout-device-id';
SCAN_TIME: 'scan-time';
SEND_COMMUNITY: 'send-community';
SERVER
:
  'server'
  {
    if (lastTokenType() == SLB) {
      pushMode(M_Word);
    }
  }
;
SERVER_INTERFACE: 'server-interface';
SERVER_SSL: 'server-ssl';
SERVICE_GROUP: 'service-group' -> pushMode(M_Word);
SERVICE_LEAST_CONNECTION: 'service-least-connection';
SESSION_SYNC: 'session-sync';
SET_ID: 'set-id';
SHORT: 'short';
SIP: 'sip';
SLB: 'slb';
SMTP: 'smtp';
SNMP: 'snmp';
SOFT_RECONFIGURATION: 'soft-reconfiguration';
SOURCE_IP: 'source-ip';
SOURCE_NAT: 'source-nat';
SPEED: 'speed';
SSL_CIPHERS: 'ssl-ciphers' -> pushMode(M_Word);
STANDARD: 'standard';
STATIC: 'static';
STATS_DATA_DISABLE: 'stats-data-disable';
STATS_DATA_ENABLE: 'stats-data-enable';
SYNCHRONIZATION: 'synchronization';
TACPLUS: 'tacplus';
TAGGED: 'tagged';
TCP: 'tcp';
TCP_PROXY: 'tcp-proxy';
TEMPLATE: 'template' -> pushMode(M_Template);
THRESHOLD: 'threshold';
TIME_INTERVAL: 'time-interval';
TIMEOUT: 'timeout';
TIMEOUT_RETRY_COUNT: 'timeout-retry-count';
TIMER: 'timer';
TIMERS: 'timers';
TO: 'to';
TRACK_EVENT_DELAY: 'track-event-delay';
TRUNK: 'trunk';
TRUNK_GROUP: 'trunk-group';
TX: 'tx';
UDP: 'udp';
UNTAGGED: 'untagged';
UP_RETRY: 'up-retry';
UPDATE_SOURCE: 'update-source';
URL: 'url';
USE_RCV_HOP_FOR_RESP: 'use-rcv-hop-for-resp';
USER_TAG: 'user-tag' -> pushMode(M_Word);
VE: 've';
VIP: 'vip';
VIRTUAL_PORT: 'virtual-port';
VIRTUAL_SERVER: 'virtual-server' -> pushMode(M_Word);
VLAN: 'vlan';
VRID: 'vrid';
VRID_LEAD: 'vrid-lead' -> pushMode(M_Word);
VRRP_A: 'vrrp-a';
WAF: 'waf';
WEIGHT: 'weight';

// Complex tokens
COMMENT_LINE
:
  F_Whitespace* '!'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_Newline | EOF) -> skip
;

DOUBLE_QUOTE
:
  '"' -> pushMode ( M_DoubleQuote )
;

SINGLE_QUOTE
:
  ['] -> pushMode ( M_SingleQuote )
;

SUBNET_MASK: F_SubnetMask;

IP_ADDRESS: F_IpAddress;

IP_SLASH_PREFIX: F_IpSlashPrefix;

NEWLINE: F_Newline;

UINT8: F_Uint8;

UINT16: F_Uint16;

UINT32: F_Uint32;

WS: F_Whitespace+ -> skip;

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
F_Digit: [0-9];

fragment
F_IpAddress: F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte;

fragment
F_IpSlashPrefix: '/' F_IpPrefixLength;

fragment
F_IpPrefixLength
:
    F_Digit
    | [12] F_Digit
    | [3] [012]
;

// Any number of newlines, allowing whitespace in between
fragment
F_Newline
:
  F_NewlineChar (F_Whitespace* F_NewlineChar+)*
;

// A single newline character [sequence - allowing \r, \r\n, or \n]
fragment
F_NewlineChar
:
  '\r' '\n'?
  | '\n'
;

fragment
F_NonNewlineChar
:
    ~[\r\n] // carriage return or line feed
;

fragment
F_PositiveDigit: [1-9];

fragment
F_SubnetMask
:
  F_SubnetMaskOctet '.0.0.0'
  | '255.' F_SubnetMaskOctet '.0.0'
  | '255.255.' F_SubnetMaskOctet '.0'
  | '255.255.255.' F_SubnetMaskOctet
;

fragment
F_SubnetMaskOctet
:
  '0'
  | '128'
  | '192'
  | '224'
  | '240'
  | '248'
  | '252'
  | '254'
  | '255'
;

fragment
F_Uint8
:
    F_Digit
    | F_PositiveDigit F_Digit
    | '1' F_Digit F_Digit
    | '2' [0-4] F_Digit
    | '25' [0-5]
;

fragment
F_Uint16
:
    F_Digit
    | F_PositiveDigit F_Digit F_Digit? F_Digit?
    | [1-5] F_Digit F_Digit F_Digit F_Digit
    | '6' [0-4] F_Digit F_Digit F_Digit
    | '65' [0-4] F_Digit F_Digit
    | '655' [0-2] F_Digit
    | '6553' [0-5]
;

fragment
F_Uint32
:
// 0-4294967295
    F_Digit
    | F_PositiveDigit F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
    F_Digit? F_Digit?
    | [1-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
    F_Digit
    | '4' [0-1] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
    | '42' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
    | '429' [0-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
    | '4294' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit
    | '42949' [0-5] F_Digit F_Digit F_Digit F_Digit
    | '429496' [0-6] F_Digit F_Digit F_Digit
    | '4294967' [0-1] F_Digit F_Digit
    | '42949672' [0-8] F_Digit
    | '429496729' [0-5]
;

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_NonWhitespace
:
  ~[ \t\u000C\u00A0\n\r]
;

fragment
F_Word: F_WordChar+;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/<>]
  | '-'
;

fragment
F_StrChar: ~( [ \t\u000C\u00A0\n\r(),!$'"*#] | '[' | ']' );
fragment
F_Str: F_StrChar+;

fragment
F_EscapedDoubleQuote: '\\"';

fragment
F_EscapedSingleQuote: '\\' ['];

// Modes
mode M_DoubleQuote;
M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;
M_DoubleQuote_QUOTED_TEXT: (F_EscapedDoubleQuote | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;
M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;
M_SingleQuote_QUOTED_TEXT: (F_EscapedSingleQuote | ~['])+ -> type(QUOTED_TEXT);

mode M_Word;
M_Word_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_WordValue);
M_Word_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_WordValue;
M_WordValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_WordValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_WordValue_WORD: F_Word -> type(WORD);
M_WordValue_WS: F_Whitespace+ -> skip, popMode;
M_WordValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Words;
M_Words_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_WordsValue);
M_Words_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_WordsValue;
M_WordsValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_WordsValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_WordsValue_WORD: F_Word -> type(WORD);
M_WordsValue_WS: F_Whitespace+ -> type(WORD_SEPARATOR);
M_WordsValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Rba;
M_Rba_WS: F_Whitespace+ -> skip;
M_Rba_ROLE: ROLE -> type(ROLE), mode(M_RbaRoleName);
M_Rba_NEWLINE: F_Newline -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaRoleName;
M_RbaRoleName_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_RbaRoleNameValue);
M_RbaRoleName_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_RbaRoleNameValue;
M_RbaRoleNameValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_RbaRoleNameValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_RbaRoleNameValue_WORD: F_Word -> type(WORD);
M_RbaRoleNameValue_WS: F_Whitespace+ -> skip, mode(M_RbaTail);
M_RbaRoleNameValue_NEWLINE: F_Newline -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaTail;
M_RbaTail_RBA_TAIL: F_NonNewlineChar+ -> type(RBA_TAIL);
M_RbaTail_NEWLINE: F_Newline -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaLine;
M_RbaLine_WS: F_Whitespace+ -> skip;
M_RbaLine_RBA_LINE: F_Word F_Whitespace+ ('no-access'|'read'|'partition-only'|'oper'|'write') -> type(RBA_LINE);
M_RbaLine_NEWLINE: F_Newline -> type(NEWLINE);
M_RbaLine_COMMENT_LINE: F_Whitespace* '!' {lastTokenType() == NEWLINE}? F_NonNewlineChar* (F_Newline | EOF) -> skip;
M_RbaLine_END: F_NonWhitespace+ {less();} -> popMode;

mode M_Template;
M_Template_WS: F_Whitespace+ -> skip;
M_Template_CACHE: 'cache' -> type(CACHE), mode(M_Word);
M_Template_CIPHER: 'cipher' -> type(CIPHER), mode(M_Word);
M_Template_CLIENT_SSL: 'client-ssl' -> type(CLIENT_SSL), mode(M_Word);
M_Template_CONNECTION_REUSE: 'connection-reuse' -> type(CONNECTION_REUSE), mode(M_Word);
M_Template_DIAMETER: 'diameter' -> type(DIAMETER), mode(M_Word);
M_Template_DNS: 'dns' -> type(DNS), mode(M_Word);
M_Template_DYNAMIC_SERVICE: 'dynamic-service' -> type(DYNAMIC_SERVICE), mode(M_Word);
M_Template_HTTP: 'http' -> type(HTTP), mode(M_Word);
M_Template_PERSIST: 'persist' -> type(PERSIST), mode(M_TemplatePersist);
M_Template_POLICY: 'policy' -> type(POLICY), mode(M_Word);
M_Template_PORT: 'port' -> type(PORT), mode(M_Word);
M_Template_SERVER: 'server' -> type(SERVER), mode(M_Word);
M_Template_SERVER_SSL: 'server-ssl' -> type(SERVER_SSL), mode(M_Word);
M_Template_SIP: 'sip' -> type(SIP), mode(M_Word);
M_Template_TCP: 'tcp' -> type(TCP), mode(M_Word);
M_Template_TCP_PROXY: 'tcp-proxy' -> type(TCP_PROXY), mode(M_Word);
M_Template_UDP: 'udp' -> type(UDP), mode(M_Word);
M_Template_VIRTUAL_PORT: 'virtual-port' -> type(VIRTUAL_PORT), mode(M_Word);
M_Template_WAF: 'waf' -> type(WAF), mode(M_Word);
M_Template_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_TemplatePersist;
M_TemplatePersist_WS: F_Whitespace+ -> skip;
M_TemplatePersist_COOKIE: 'cookie' -> type(COOKIE), mode(M_Word);
M_TemplatePersist_DESTINATION_IP: 'destination-ip' -> type(DESTINATION_IP), mode(M_Word);
M_TemplatePersist_SOURCE_IP: 'source-ip' -> type(SOURCE_IP), mode(M_Word);
M_TemplatePersist_NEWLINE: F_Newline -> type(NEWLINE), popMode;
