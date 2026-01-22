lexer grammar F5BigipStructuredLexer;

options {
  superClass = 'org.batfish.grammar.f5_bigip_structured.parsing.F5BigipStructuredBaseLexer';
}

tokens {
  BACKSLASH_CARRIAGE_RETURN,
  BACKSLASH_CHAR,
  BACKSLASH_NEWLINE,
  BACKSLASH_NEWLINE_WS,
  CHARS,
  COMMENT,
  DOLLAR,
  DOUBLE_QUOTED_STRING,
  EVENT,
  PAREN_LEFT,
  PAREN_RIGHT,
  PROC,
  RULE_SPECIAL,
  WHEN
}

// Keywords

ACTION: 'action';

ACTIVATE: 'activate';

ACTIVE_BONUS: 'active-bonus';

ACTIVE_MODULES: 'active-modules';

ADAPTIVE: 'adaptive';

ADDRESS: 'address';

ADDRESS_FAMILY: 'address-family';

ALERT_TIMEOUT: 'alert-timeout';

ALL: 'all';

ALLOW_DYNAMIC_RECORD_SIZING: 'allow-dynamic-record-sizing';

ALLOW_NON_SSL: 'allow-non-ssl';

ALLOW_SERVICE: 'allow-service';

ALWAYS: 'always';

ANALYTICS: 'analytics';

AND: 'and';

ANY: 'any';

APP_SERVICE: 'app-service';

ARP: 'arp';

AUTO_SYNC: 'auto-sync';

BASE_MAC: 'base-mac';

BGP: 'bgp';

BUILD: 'build';

BUNDLE: 'bundle';

BUNDLE_SPEED: 'bundle-speed';

CA_CERT: 'ca-cert';

CA_CERT_BUNDLE: 'ca-cert-bundle';

CA_DEVICES: 'ca-devices';

CA_KEY: 'ca-key';

CACHE_SIZE: 'cache-size';

CACHE_TIMEOUT: 'cache-timeout';

CAPABILITY: 'capability';

CERT: 'cert';

CERT_EXTENSION_INCLUDES: 'cert-extension-includes';

CERT_KEY_CHAIN: 'cert-key-chain';

CERT_LIFESPAN: 'cert-lifespan';

CERT_LOOKUP_BY_IPADDR_PORT: 'cert-lookup-by-ipaddr-port';

CERTIFICATE_AUTHORITY: 'certificate-authority';

CHAIN: 'chain';

CHASSIS_ID: 'chassis-id';

CIPHER_GROUP: 'cipher-group';

CIPHERLIST: 'cipherlist';

CIPHERS: 'ciphers';

CLASSIFICATION: 'classification';

CLIENT_LDAP: 'client-ldap';

CLIENT_SSL: 'client-ssl';

CM: 'cm';

COMMUNITY: 'community';

COMPATIBILITY: 'compatibility';

CONSOLE_INACTIVITY_TIMEOUT: 'console-inactivity-timeout';

CENT_REPORT_DESTINATION_TYPE: 'cent-report-destination-type';

CREATION_TIME: 'creation-time';

CONFIGSYNC_IP: 'configsync-ip';

COOKIE: 'cookie';

DATA_GROUP: 'data-group';

DEFAULT: 'default';

DEFAULT_NODE_MONITOR: 'default-node-monitor';

DEFAULTS_FROM: 'defaults-from';

DENY: 'deny';

DESCRIPTION: 'description';

DRILLDOWN_ENTITIES: 'drilldown-entities';

DRILLDOWN_VALUES: 'drilldown-values';

DESTINATION: 'destination';

DEVICE: 'device';

DEVICE_GROUP: 'device-group';

DEVICES: 'devices';

DHCPV4: 'dhcpv4';

DHCPV6: 'dhcpv6';

DIAMETER: 'diameter';

DISABLED: 'disabled';

DNS: 'dns';

DNS_RESOLVER: 'dns-resolver';

DYNAD: 'dynad';

EBGP_MULTIHOP: 'ebgp-multihop';

EDITION: 'edition';

EFFECTIVE_IP: 'effective-ip';

EFFECTIVE_PORT: 'effective-port';

ENABLED: 'enabled';

ENTRIES: 'entries';

EXPIRATION: 'expiration';

EXTERNAL: 'external';

FALL_OVER: 'fall-over';

FALSE: 'false';

FASTHTTP: 'fasthttp';

FASTL4: 'fastl4';

FDB: 'fdb';

FEATURE_MODULE: 'feature-module';

FIX: 'fix';

FOLDER: 'folder';

FORTY_G: '40G';

FORWARD_ERROR_CORRECTION: 'forward-error-correction';

FPGA: 'fpga';

FTP: 'ftp';

GATEWAY_ICMP: 'gateway-icmp';

GENERIC_ALERT: 'generic-alert';

GLOBAL_SETTINGS: 'global-settings';

GTP: 'gtp';

GUI_SECURITY_BANNER_TEXT: 'gui-security-banner-text';

GUI_PAGECODE: 'gui-pagecode';

GUI_WIDGET: 'gui-widget';

GUI_SETUP: 'gui-setup';

GUID: 'guid';

GW: 'gw';

HA_GROUP: 'ha-group';

HANDSHAKE_TIMEOUT: 'handshake-timeout';

HIDDEN_LITERAL: 'hidden';

HOSTNAME: 'hostname';

HTML: 'html';

HTTP: 'http';

HTTP_COMPRESSION: 'http-compression';

HTTP_PROXY_CONNECT: 'http-proxy-connect';

HTTP2: 'http2';

HTTPD: 'httpd';

HTTPS: 'https';

ICAP: 'icap';

ICMP_ECHO: 'icmp-echo';

IDLE_TIMEOUT_OVERRIDE: 'idle-timeout-override';

IFILE: 'ifile';

INHERIT_CERTKEYCHAIN: 'inherit-certkeychain';

ILX: 'ilx';

INTERFACE: 'interface';

INTERFACES: 'interfaces';

INTERNAL: 'internal';

INTERVAL: 'interval';

IP: 'ip';

IP_DSCP: 'ip-dscp';

IP_FORWARD: 'ip-forward';

IP_PROTOCOL: 'ip-protocol';

IPOTHER: 'ipother';

IPSECALG: 'ipsecalg';

IPV4: 'ipv4';

IPV6: 'ipv6';

KERNEL: 'kernel';

KEY: 'key';

LACP: 'lacp';

LAST_MODIFIED_TIME: 'last-modified-time';

LDAP: 'ldap';

LIMIT_TYPE: 'limit-type';

LLDP_ADMIN: 'lldp-admin';

LLDP_GLOBALS: 'lldp-globals';

LLDP_TLVMAP: 'lldp-tlvmap';

LOAD_BALANCING_MODE: 'load-balancing-mode';

LOCAL_AS: 'local-as';

LTM: 'ltm';

MAC: 'mac';

MANAGEMENT_DHCP: 'management-dhcp';

MANAGEMENT_IP: 'management-ip';

MANAGEMENT_ROUTE: 'management-route';

MAP_T: 'map-t';

MARKETING_NAME: 'marketing-name';

MASK: 'mask';

MATCH: 'match';

MATCH_ACROSS_POOLS: 'match-across-pools';

MATCH_ACROSS_SERVICES: 'match-across-services';

MATCH_ACROSS_VIRTUALS: 'match-across-virtuals';

MAX_ACTIVE_HANDSHAKES: 'max-active-handshakes';

MAX_AGE: 'max-age';

MAX_AGGREGATE_RENEGOTIATION_PER_MINUTE: 'max-aggregate-renegotiation-per-minute';

MAX_RENEGOTIATIONS_PER_MINUTE: 'max-renegotiations-per-minute';

MAX_REUSE: 'max-reuse';

MAX_SIZE: 'max-size';

MAXIMUM_PREFIX: 'maximum-prefix';

MAXIMUM_RECORD_SIZE: 'maximum-record-size';

MEMBERS: 'members';

METRICS: 'metrics';

MIN_ACTIVE_MEMBERS: 'min-active-members';

MOD_SSL_METHODS: 'mod-ssl-methods';

MODE: 'mode';

MQTT: 'mqtt';

MODULE: 'module';

MONITOR: 'monitor';

NEIGHBOR: 'neighbor';

NET: 'net';

NETFLOW: 'netflow';

NETWORK: 'network';

NETWORK_FAILOVER: 'network-failover';

NODE: 'node';

NTP: 'ntp';

OCSP_STAPLING: 'ocsp-stapling';

OCSP_STAPLING_PARAMS: 'ocsp-stapling-params';

ONE_CONNECT: 'one-connect';

ONE_HUNDRED_G: '100G';

OPTIONAL_MODULES
: 
  'optional-modules'
;

OPTIONS: 'options';

ORDER_ON_PAGE: 'order-on-page';

ORIGINS: 'origins';

OUT: 'out';

OVERRIDE_CONNECTION_LIMIT: 'override-connection-limit';

OUTBOUND_SMTP: 'outbound-smtp';

PCP: 'pcp';

PASSPHRASE: 'passphrase';

PEER_NO_RENEGOTIATE_TIMEOUT: 'peer-no-renegotiate-timeout';

PERIOD: 'period';

PERMIT: 'permit';

PERSIST: 'persist';

PERSISTENCE: 'persistence';

PLATFORM_ID: 'platform-id';

POOL: 'pool';

POOLS: 'pools';

PORT: 'port';

PPTP: 'pptp';

PREFIX: 'prefix';

PREFIX_LEN_RANGE: 'prefix-len-range';

PREFIX_LIST: 'prefix-list';

PRIORITY_GROUP: 'priority-group';

PRODUCT: 'product';

PROFILE: 'profile';

PROFILES: 'profiles';

PROVISION: 'provision';

PROXY_CA_CERT: 'proxy-ca-cert';

PROXY_CA_KEY: 'proxy-ca-key';

PROXY_SSL: 'proxy-ssl';

PROXY_SSL_PASSTHROUGH: 'proxy-ssl-passthrough';

QOE: 'qoe';

RADIUS: 'radius';

RECV: 'recv';

RECV_DISABLE: 'recv-disable';

REDISTRIBUTE: 'redistribute';

REJECT: 'reject';

REMOTE_AS: 'remote-as';

RENEGOTIATE_MAX_RECORD_DELAY: 'renegotiate-max-record-delay';

RENEGOTIATE_PERIOD: 'renegotiate-period';

RENEGOTIATE_SIZE: 'renegotiate-size';

RENEGOTIATION: 'renegotiation';

REQUEST_ADAPT: 'request-adapt';

REQUEST_LOG: 'request-log';

RESPONDER_URL: 'responder-url';

RESPONSE_ADAPT: 'response-adapt';

REWRITE: 'rewrite';

ROUTE: 'route';

ROUTE_ADVERTISEMENT: 'route-advertisement';

ROUTE_DOMAIN: 'route-domain';

ROUTE_MAP: 'route-map';

ROUTER_ID: 'router-id';

ROUTING: 'routing';

RTSP: 'rtsp';

RULE
:
  'rule'
  {
    if (lastTokenType() == LTM && secondToLastTokenType() == NEWLINE) {
      setLtmRuleDeclaration();
      setType(RULE_SPECIAL);
    }
  }
;

RULES: 'rules';

SCTP: 'sctp';

SECURE_RENEGOTIATION: 'secure-renegotiation';

SECURITY: 'security';

SELECTIVE: 'selective';

SELF: 'self';

SELF_ALLOW: 'self-allow';

SELF_DEVICE: 'self-device';

SEND: 'send';

SERVER_LDAP: 'server-ldap';

SERVER_NAME: 'server-name';

SERVER_SSL: 'server-ssl';

SERVERS: 'servers';

SERVICE_DOWN_ACTION: 'service-down-action';

SESSION_MIRRORING: 'session-mirroring';

SESSION_TICKET: 'session-ticket';

SESSION_TICKET_TIMEOUT: 'session-ticket-timeout';

SET: 'set';

SET_SYNC_LEADER: 'set-sync-leader';

SFLOW: 'sflow';

SIGN_HASH: 'sign-hash';

SIP: 'sip';

SLOW_RAMP_TIME: 'slow-ramp-time';

SMTPS: 'smtps';

SNAT: 'snat';

SNAT_TRANSLATION: 'snat-translation';

SNATPOOL: 'snatpool';

SNI_DEFAULT: 'sni-default';

SNI_REQUIRE: 'sni-require';

SNMP: 'snmp';

SSHD: 'sshd';

SYSLOG: 'syslog';

SOCKS: 'socks';

SOURCE: 'source';

SOURCE_ADDR: 'source-addr';

SOURCE_ADDRESS_TRANSLATION: 'source-address-translation';

SOURCE_MASK: 'source-mask';

SERVERSSL_USE_SNI: 'serverssl-use-sni';

SPLITSESSIONCLIENT: 'splitsessionclient';

SPLITSESSIONSERVER: 'splitsessionserver';

SSL: 'ssl';

SSL_FORWARD_PROXY: 'ssl-forward-proxy';

SSL_FORWARD_PROXY_BYPASS: 'ssl-forward-proxy-bypass';

SSL_PROFILE: 'ssl-profile';

SSL_SIGN_HASH: 'ssl-sign-hash';

STATISTICS: 'statistics';

STATUS: 'status';

STATUS_AGE: 'status-age';

STP: 'stp';

STP_GLOBALS: 'stp-globals';

STREAM: 'stream';

STRICT_RESUME: 'strict-resume';

SYNC_FAILOVER: 'sync-failover';

SYNC_ONLY: 'sync-only';

SYS: 'sys';

TAG: 'tag';

TCP: 'tcp';

TCP_ANALYTICS: 'tcp-analytics';

TFTP: 'tftp';

TIME_UNTIL_UP: 'time-until-up';

TIME_ZONE: 'time-zone';

TIMEOUT: 'timeout';

TIMEZONE: 'timezone';

TRAFFIC_ACCELERATION: 'traffic-acceleration';

TRAFFIC_GROUP: 'traffic-group';

TRANSLATE_ADDRESS: 'translate-address';

TRANSLATE_PORT: 'translate-port';

TRUE: 'true';

TRUNK: 'trunk';

TRUNKS: 'trunks';

TRUST_DOMAIN: 'trust-domain';

TRUST_GROUP: 'trust-group';

TRUSTED_RESPONDERS: 'trusted-responders';

TUNNELS: 'tunnels';

TURBOFLEX: 'turboflex';

TYPE: 'type';

UDP: 'udp';

UNCLEAN_SHUTDOWN: 'unclean-shutdown';

UNICAST_ADDRESS: 'unicast-address';

UNIT_ID: 'unit-id';

UPDATE_SOURCE: 'update-source';

USERNAME: 'username';

VALUE: 'value';

VERSION: 'version';

VIEW_BY: 'view-by';

VIRTUAL: 'virtual';

VIRTUAL_ADDRESS: 'virtual-address';

VLAN: 'vlan';

VLANS: 'vlans';

VLANS_DISABLED: 'vlans-disabled';

VLANS_ENABLED: 'vlans-enabled';

WEB_ACCELERATION: 'web-acceleration';

WEB_SECURITY: 'web-security';

WEBSOCKET: 'websocket';

WEIGHT: 'weight';

WIDGET_TYPE: 'widget-type';

XML: 'xml';

// Complex tokens

BRACE_LEFT
:
  '{'
  {
    if (isLtmRuleDeclaration()) {
      pushMode(M_Irule);
      unsetLtmRuleDeclaration();
    }
  }
;

BRACE_RIGHT
:
  '}'
;

BRACKET_LEFT
:
  '['
;

BRACKET_RIGHT
:
  ']'
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewlineChar* F_Newline+ -> channel ( HIDDEN )
;

COMMENT_TAIL
:
  '#' F_NonNewlineChar* -> channel ( HIDDEN )
;

MAC_ADDRESS
:
  F_MacAddress
;

VLAN_ID
:
  F_VlanId
;

UINT16
:
  F_Uint16
;

UINT32
:
  F_Uint32
;

DEC
:
  F_Digit+
;

DOUBLE_QUOTE
:
  '"' -> more, pushMode(M_DoubleQuote)
;

IMISH_CHUNK
:
  '!'
  {lastTokenType() == NEWLINE}?

  F_NonNewlineChar* F_Newline+ F_Anything*
;

IP_ADDRESS
:
  F_IpAddress
;

IP_ADDRESS_PORT
:
  F_IpAddressPort
;

IP_PREFIX
:
  F_IpPrefix
;

IPV6_ADDRESS
:
  F_Ipv6Address
;

IPV6_ADDRESS_PORT
:
  F_Ipv6AddressPort
;

IPV6_PREFIX
:
  F_Ipv6Prefix
;

NEWLINE
:
  F_Newline+
;

PARTITION
:
  F_Partition
;

SEMICOLON
:
  ';' -> channel ( HIDDEN )
;

STANDARD_COMMUNITY
:
  F_StandardCommunity
;

WORD_PORT
:
  F_WordPort
;

WORD_ID
:
  F_WordId
;

WORD
:
  F_Word
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

// Fragments

fragment
F_Anything
:
  .
;

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
F_BackslashChar
:
  ~[0-9abfnrtvxuU\r\n]
;

fragment
F_BackslashNewlineWhitespace
:
  '\\' F_Newline
  (
    F_Newline
    | F_Whitespace
  )*
;

fragment
F_HexDigit
:
  [0-9A-Fa-f]
;

fragment
F_HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_HexWord2
:
  F_HexWord ':' F_HexWord
;

fragment
F_HexWord3
:
  F_HexWord2 ':' F_HexWord
;

fragment
F_HexWord4
:
  F_HexWord3 ':' F_HexWord
;

fragment
F_HexWord5
:
  F_HexWord4 ':' F_HexWord
;

fragment
F_HexWord6
:
  F_HexWord5 ':' F_HexWord
;

fragment
F_HexWord7
:
  F_HexWord6 ':' F_HexWord
;

fragment
F_HexWord8
:
  F_HexWord6 ':' F_HexWordFinal2
;

fragment
F_HexWordFinal2
:
  F_HexWord2
  | F_IpAddress
;

fragment
F_HexWordFinal3
:
  F_HexWord ':' F_HexWordFinal2
;

fragment
F_HexWordFinal4
:
  F_HexWord ':' F_HexWordFinal3
;

fragment
F_HexWordFinal5
:
  F_HexWord ':' F_HexWordFinal4
;

fragment
F_HexWordFinal6
:
  F_HexWord ':' F_HexWordFinal5
;

fragment
F_HexWordFinal7
:
  F_HexWord ':' F_HexWordFinal6
;

fragment
F_HexWordLE1
:
  F_HexWord?
;

fragment
F_HexWordLE2
:
  F_HexWordLE1
  | F_HexWordFinal2
;

fragment
F_HexWordLE3
:
  F_HexWordLE2
  | F_HexWordFinal3
;

fragment
F_HexWordLE4
:
  F_HexWordLE3
  | F_HexWordFinal4
;

fragment
F_HexWordLE5
:
  F_HexWordLE4
  | F_HexWordFinal5
;

fragment
F_HexWordLE6
:
  F_HexWordLE5
  | F_HexWordFinal6
;

fragment
F_HexWordLE7
:
  F_HexWordLE6
  | F_HexWordFinal7
;

fragment
F_IpAddress
:
  F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

fragment
F_IpAddressPort
:
  F_IpAddress ':' F_Uint16
;

fragment
F_IpPrefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_IpPrefixLength
:
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

fragment
F_Ipv6Address
:
  '::' F_HexWordLE7
  | F_HexWord '::' F_HexWordLE6
  | F_HexWord2 '::' F_HexWordLE5
  | F_HexWord3 '::' F_HexWordLE4
  | F_HexWord4 '::' F_HexWordLE3
  | F_HexWord5 '::' F_HexWordLE2
  | F_HexWord6 '::' F_HexWordLE1
  | F_HexWord7 '::'
  | F_HexWord8
;

fragment
F_Ipv6AddressPort
:
  F_Ipv6Address '.' F_Uint16
;

fragment
F_Ipv6Prefix
:
  F_Ipv6Address '/' F_Ipv6PrefixLength
;

fragment
F_Ipv6PrefixLength
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' [01] F_Digit
  | '12' [0-8]
;

fragment
F_IruleVarName
:
  [0-9A-Za-z_]+
  (
    '::' [0-9A-Za-z_]+
  )*
;

fragment
F_MacAddress
:
  F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit
;

fragment
F_Newline
:
  [\r\n] // carriage return or line feed

;

fragment
F_NonNewlineChar
:
  ~[\r\n] // carriage return or line feed

;

fragment
F_Partition
:
  '/'
  (
    F_PartitionChar+ '/'
  )*
;

fragment
F_PartitionChar
:
  F_WordCharCommon
  | [:]
;

fragment
F_PositiveDigit
:
  '1' .. '9'
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_TclIdentifier
:
  [a-zA-Z_] [a-zA-Z0-9_]*
;

fragment
F_Uint16
:
// 0-65535
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
F_VlanId
:
// 1-4094
  F_PositiveDigit F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit
  | '40' [0-8] F_Digit
  | '409' [0-4]
;

fragment
F_Whitespace
:
  [ \t\u000C] // tab or space or unicode 0x000C

;

fragment
F_Word
:
  F_WordCharCommon
  (
    F_WordChar* F_WordCharCommon
  )?
;

fragment
F_WordCharCommon
:
  ~[ \t\n\r{}[\]/:"]
;

fragment
F_WordChar
:
  F_WordCharCommon
  | [:/]
;

fragment
F_WordPort
:
  F_WordId ':' F_Uint16
;

fragment
F_WordId
:
  F_WordCharCommon+
;

mode M_DoubleQuote;

M_DoubleQuote_BODY_CHAR
:
  ~'"' -> more
;

M_DoubleQuote_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTED_STRING), popMode
;

M_DoubleQuote_ESCAPED_DOUBLE_QUOTE
:
  '\\"' -> more
;

mode M_Irule;

M_Irule_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_Irule_COMMENT
:
  '#' F_NonNewlineChar* F_Newline+ -> channel(HIDDEN)
;

M_Irule_NEWLINE
:
  F_Newline+ -> type(NEWLINE)
;

M_Irule_PROC
:
  'proc' -> type(PROC), pushMode(M_Proc)
;

M_Irule_WHEN
:
  'when' -> type(WHEN), pushMode(M_Event)
;

M_Irule_WS
:
  F_Whitespace+ -> channel(HIDDEN)
;

mode M_Proc;

M_Proc_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), mode(M_ProcArgs)
;

M_Proc_CHARS
:
  F_IruleVarName -> type(CHARS)
;

M_Proc_WS
:
  F_Whitespace+ -> channel(HIDDEN)
;

mode M_ProcArgs;

M_ProcArgs_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), mode(M_ProcPostArgs)
;

M_ProcArgs_CHARS
:
  F_IruleVarName -> type(CHARS)
;

M_ProcArgs_WS
:
  F_Whitespace+ -> channel(HIDDEN)
;

mode M_ProcPostArgs;

M_ProcPostArgs_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), mode(M_Command)
;

M_ProcPostArgs_WS
:
  F_Whitespace+ -> channel(HIDDEN)
;

mode M_Event;

M_Event_EVENT
:
  ~'{'+ -> type(EVENT) 
;

M_Event_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), mode(M_Command)
;

mode M_Command;

M_Command_BRACE_LEFT
:
  '{' -> pushMode(M_BracedSegment), type(BRACE_LEFT)
;

M_Command_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_Command_BRACKET_LEFT
:
  '[' -> type ( BRACKET_LEFT ) , pushMode ( M_Command )
;

M_Command_BRACKET_RIGHT
:
  ']' -> type ( BRACKET_RIGHT ) , popMode
;

M_Command_CHARS
:
  ~[ \t\r\n\\{}[\]$"]+ -> type(CHARS)
;

M_Command_COMMENT
:
  '#' F_NonNewlineChar* F_Newline -> type(COMMENT)
;

M_Command_DOLLAR
:
  '$' -> type(DOLLAR), pushMode(M_VariableSubstitution)
;

M_Command_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuotedSegment)
;

M_Command_NEWLINE
:
  F_Newline+ -> type(NEWLINE)
;

M_Command_WS
:
  [ \t]+ -> type(WS)
;

mode M_BracedSegment;

M_BracedSegment_CHARS
:
  ~[{}]+ -> type(CHARS)
;

M_BracedSegment_BACKSLASH_NEWLINE_WS
:
  F_BackslashNewlineWhitespace -> type(BACKSLASH_NEWLINE_WS)
;

M_BracedSegment_BRACE_LEFT
:
  '{' -> pushMode(M_BracedSegment), type(BRACE_LEFT)
;

M_BracedSegment_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

mode M_DoubleQuotedSegment;

M_DoubleQuotedSegment_CHARS
:
  ~[["$\\]+ -> type(CHARS)
;

M_DoubleQuotedSegment_BACKSLASH_CARRIAGE_RETURN
:
  '\\r' -> type(BACKSLASH_CARRIAGE_RETURN)
;

M_DoubleQuotedSegment_BACKSLASH_CHAR
:
  '\\' F_BackslashChar -> type(BACKSLASH_CHAR)
;

M_DoubleQuotedSegment_BACKSLASH_NEWLINE
:
  '\\n' -> type(BACKSLASH_NEWLINE)
;

M_DoubleQuotedSegment_BACKSLASH_NEWLINE_WS
:
  F_BackslashNewlineWhitespace -> type(BACKSLASH_NEWLINE_WS)
;

M_DoubleQuotedSegment_BRACKET_LEFT
:
  '[' -> type(BRACKET_LEFT), pushMode(M_Command)
;

M_DoubleQuotedSegment_DOLLAR
:
  '$' -> type(DOLLAR), pushMode(M_VariableSubstitution)
;

M_DoubleQuotedSegment_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTE), popMode
;

mode M_VariableSubstitution;

M_VariableSubstitution_BACKSLASH
:
  '\\'
  {
    less();
  } -> popMode
;

M_VariableSubstitution_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), pushMode(M_BracedVariableSubstitution)
;

M_VariableSubstitution_BRACKET_RIGHT
:
  ']'
  {
    less();
  } -> popMode
;

M_VariableSubstitution_CHARS
:
  F_IruleVarName -> type(CHARS)
;

M_VariableSubstitution_DOLLAR
:
// kinda screwed here
  '$' -> type(DOLLAR), popMode
;

M_VariableSubstitution_DOUBLE_QUOTE
:
  '"'
  {
    less();
  } -> popMode
;

M_VariableSubstitution_NEWLINE
:
  F_Newline+ -> type(NEWLINE), popMode
;

M_VariableSubstitution_WS
:
  F_Whitespace+ -> type(WS), popMode
;

mode M_BracedVariableSubstitution;

M_BracedVariableSubstitution_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_BracedVariableSubstitution_CHARS
:
  ~'}'+ -> type(CHARS)
;
