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
CAPABILITY: 'capability';
COMMON: 'common';
CONN_LIMIT: 'conn-limit';
CONNECTED: 'connected';
DEAD_TIMER: 'dead-timer';
DEF_SELECTION_IF_PREF_FAILED: 'def-selection-if-pref-failed';
DEFAULT: 'default';
DESCRIPTION: 'description' -> pushMode(M_Word);
DEVICE_ID: 'device-id';
DISABLE: 'disable';
DISABLE_DEFAULT_VRID: 'disable-default-vrid';
DO_AUTO_RECOVERY: 'do-auto-recovery';
DUPLEXITY: 'duplexity';
DYNAMIC: 'dynamic';
ENABLE: 'enable';
ETHERNET: 'ethernet';
EXTENDED: 'extended';
FAIL_OVER_POLICY_TEMPLATE: 'fail-over-policy-template' -> pushMode(M_Word);
FALL_OVER: 'fall-over';
FAST_EXTERNAL_FAILOVER: 'fast-external-failover';
FLOATING_IP: 'floating-ip';
GATEWAY: 'gateway';
GET_READY_TIME: 'get-ready-time';
GET_READY_TIME_ACOS2: 'get_ready_time';
GRACEFUL_RESTART: 'graceful-restart';
HELLO_INTERVAL: 'hello-interval';
HA_GROUP_ID: 'ha-group-id';
HEALTH_CHECK: 'health-check' -> pushMode(M_Word);
HEALTH_CHECK_DISABLE: 'health-check-disable';
HOSTNAME: 'hostname' -> pushMode(M_Word);
HTTP: 'http';
HTTPS: 'https';
IP: 'ip';
IP_NAT: 'ip-nat';
IP_RR: 'ip-rr';
IPG_BIT_TIME: 'ipg-bit-time';
INBOUND: 'inbound';
INTERFACE: 'interface';
LACP: 'lacp';
LACP_TRUNK: 'lacp-trunk';
LACP_UDLD: 'lacp-udld';
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
MTU: 'mtu';
NAME: 'name' -> pushMode(M_Word);
NAT: 'nat';
NEIGHBOR: 'neighbor';
NETMASK: 'netmask';
NEXTHOP_TRIGGER_COUNT: 'nexthop-trigger-count';
NO: 'no';
NO_AUTO_UP_ON_AFLEX: 'no-auto-up-on-aflex';
NONE: 'none';
NOTIFICATION: 'notification';
ONLY_FLAGGED: 'only-flagged';
ONLY_NOT_FLAGGED: 'only-not-flagged';
OPTIMIZATION_LEVEL: 'optimization-level';
ORF: 'orf';
PASSIVE: 'passive';
PEER: 'peer';
PEER_GROUP: 'peer-group';
POOL: 'pool' -> pushMode(M_Word);
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
PRIORITY: 'priority';
RANGE: 'range';
RBA: 'rba' -> pushMode(M_Rba);
RESTART_TIME: 'restart-time';
REDISTRIBUTE: 'redistribute';
REDISTRIBUTION_FLAGGED: 'redistribution-flagged';
REMOTE_AS: 'remote-as';
ROLE: 'role';
ROUND_ROBIN: 'round-robin';
ROUTE: 'route';
ROUTE_REFRESH: 'route-refresh';
ROUTER: 'router';
ROUTER_ID: 'router-id';
ROUTER_INTERFACE: 'router-interface';
RX: 'rx';
SCALEOUT_DEVICE_ID: 'scaleout-device-id';
SCAN_TIME: 'scan-time';
SEND_COMMUNITY: 'send-community';
SESSION_SYNC: 'session-sync';
SET_ID: 'set-id';
SERVER
:
  'server'
  {
    if (lastTokenType() == SLB || lastTokenType() == TEMPLATE) {
      pushMode(M_Word);
    }
  }
;
SERVICE_GROUP: 'service-group' -> pushMode(M_Word);
SERVICE_LEAST_CONNECTION: 'service-least-connection';
SHORT: 'short';
SLB: 'slb';
SOFT_RECONFIGURATION: 'soft-reconfiguration';
SOURCE_NAT: 'source-nat';
SPEED: 'speed';
STANDARD: 'standard';
STATIC: 'static';
STATS_DATA_DISABLE: 'stats-data-disable';
STATS_DATA_ENABLE: 'stats-data-enable';
SYNCHRONIZATION: 'synchronization';
TAGGED: 'tagged';
TCP: 'tcp';
TCP_PROXY: 'tcp-proxy';
TEMPLATE: 'template';
THRESHOLD: 'threshold';
TIMEOUT: 'timeout';
TIMER: 'timer';
TIMERS: 'timers';
TO: 'to';
TRACK_EVENT_DELAY: 'track-event-delay';
TRUNK: 'trunk';
TRUNK_GROUP: 'trunk-group';
TX: 'tx';
UDP: 'udp';
UNTAGGED: 'untagged';
UPDATE_SOURCE: 'update-source';
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
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/]
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
