lexer grammar PaloAltoLexer;

options {
  superClass = 'org.batfish.grammar.palo_alto.parsing.PaloAltoBaseLexer';
}

tokens {
  BODY,
  DOUBLE_QUOTE,
  IGNORED_CONFIG_BLOCK,
  SINGLE_QUOTE,
  WORD
}

// Keywords

TWO_BYTE: '2-byte';

FOUR_BYTE: '4-byte';

ACCEPT_SUMMARY: 'accept-summary';

ACTION: 'action';

ACTIVE_ACTIVE: 'active-active';

ACTIVE_ACTIVE_DEVICE_BINDING: 'active-active-device-binding';

ADDRESS: 'address';

ADDRESS_GROUP: 'address-group';

ADDRESS_PREFIX: 'address-prefix';

ADDRESS_FAMILY_IDENTIFIER: 'address-family-identifier';

ADJUST_TCP_MSS: 'adjust-tcp-mss';

ADMIN_DIST: 'admin-dist';

ADMIN_DISTS: 'admin-dists';

ADMIN_ROLE: 'admin-role';

ADVERTISE: 'advertise';

AES_128_CBC: 'aes-128-cbc';

AES_128_GCM: 'aes-128-gcm';

AES_192_CBC: 'aes-192-cbc';

AES_256_CBC: 'aes-256-cbc';

AES_256_GCM: 'aes-256-gcm';

AFTER: 'after';

AGGREGATE: 'aggregate';

AGGREGATE_ETHERNET: 'aggregate-ethernet';

AGGREGATE_GROUP: 'aggregate-group';

AGGREGATE_MED: 'aggregate-med';

ALGORITHM: 'algorithm';

ALL: 'all';

ALLOW: 'allow';

ALWAYS_COMPARE_MED: 'always-compare-med';

ANY: 'any';

APPLICATION: 'application';

APPLICATION_FILTER: 'application-filter';

APPLICATION_GROUP: 'application-group';

APPLICATION_OVERRIDE: 'application-override';

AREA: 'area';

AS_FORMAT: 'as-format';

AS_PATH: 'as-path';

AUTH: 'auth';

AUTH_PROFILE: 'auth-profile';

AUTHENTICATION: 'authentication';

AUTHENTICATION_PROFILE: 'authentication-profile';

AUTHENTICATION_TYPE: 'authentication-type';

AUTO: 'auto';

BEFORE: 'before';

BFD: 'bfd';

BGP: 'bgp';

BOTH: 'both';

BOTNET: 'botnet' -> pushMode(M_IgnoredConfigBlock);

BOTTOM: 'bottom';

BROADCAST: 'broadcast';

BSD
:
    [Bb][Ss][Dd]
;

CATEGORY: 'category';

CERTIFICATE: 'certificate';

CERTIFICATE_PROFILE: 'certificate-profile';

CLIENT: 'client';

COLOR: 'color';

COMMENT: 'comment';

COMMENTS: 'comments';

COMMUNITY: 'community';

CONNECT: 'connect';

CONFIG: 'config';

CONNECTION_OPTIONS: 'connection-options';

CONTENT_PREVIEW: 'content-preview';

CRYPTO_PROFILES: 'crypto-profiles';

CUSTOM_URL_CATEGORY: 'custom-url-category';

DAMPENING_PROFILE: 'dampening-profile';

DATA_FILTERING: 'data-filtering';

DATA_OBJECTS: 'data-objects';

DAYS: 'days';

DEAD_COUNTS: 'dead-counts';

DECRYPTION: 'decryption';

DEFAULT: 'default';

DEFAULT_GATEWAY: 'default-gateway';

DEFAULT_LOCAL_PREFERENCE: 'default-local-preference';

DEFAULT_ROUTE: 'default-route';

DELETE: 'delete';

DENY: 'deny';

DES: 'des';

DESCRIPTION
:
    'description' -> pushMode(M_Value)
;

DESTINATION: 'destination';

DESTINATION_HIP: 'destination-hip';

DESTINATION_TRANSLATION: 'destination-translation';

DETERMINISTIC_MED_COMPARISON: 'deterministic-med-comparison';

DEVICES: 'devices';

DEVICE_GROUP: 'device-group';

DEVICE_ID: 'device-id';

DEVICECONFIG: 'deviceconfig';

DH_GROUP: 'dh-group';

DISABLE: 'disable';

DISABLED: 'disabled';

DISCARD: 'discard';

DISPLAY_NAME: 'display-name';

DNS: 'dns';

DNS_SETTING: 'dns-setting';

DOMAIN: 'domain';

DOS_PROTECTION: 'dos-protection';

DOWN: 'down';

DROP: 'drop';

DYNAMIC: 'dynamic';

DYNAMIC_IP_AND_PORT: 'dynamic-ip-and-port';

EBGP: 'ebgp';

ECMP: 'ecmp';

EGP: 'egp';

ENABLE: 'enable';

ENABLE_SENDER_SIDE_LOOP_DETECTION: 'enable-sender-side-loop-detection';

ENCRYPTION: 'encryption';

ESP: 'esp';

EVASIVE: 'evasive';

EXACT: 'exact';

EXCESSIVE_BANDWIDTH_USE: 'excessive-bandwidth-use';

EXPORT: 'export';

EXPORT_NEXTHOP: 'export-nexthop';

EXT_1: 'ext-1';

EXT_2: 'ext-2';

EXTENDED_COMMUNITY: 'extended-community';

EXTERNAL: 'external';

EXTERNAL_LIST: 'external-list';

ETHERNET: 'ethernet';

FACILITY: 'facility';

FAILURE_CONDITION: 'failure-condition';

FILE_BLOCKING: 'file-blocking';

FILTER: 'filter';

FORMAT: 'format';

FQDN: 'fqdn';

FROM: 'from';

FROM_PEER: 'from-peer';

FULL: 'full';

GATEWAY: 'gateway';

GLOBAL_BFD: 'global-bfd';

GLOBAL_PROTECT_APP_CRYPTO_PROFILES: 'global-protect-app-crypto-profiles';

GR_DELAY: 'gr-delay';

GRACEFUL_RESTART: 'graceful-restart';

GROUP: 'group';

GROUP1: 'group1';

GROUP2: 'group2';

GROUP5: 'group5';

GROUP14: 'group14';

GROUP19: 'group19';

GROUP20: 'group20';

GROUP_ID: 'group-id';

GTP: 'gtp';

HALF: 'half';

HA: 'ha';

HAS_KNOWN_VULNERABILITIES: 'has-known-vulnerabilities';

HASH: 'hash';

HELLO_INTERVAL: 'hello-interval';

HELPER_ENABLE: 'helper-enable';

HIGH_AVAILABILITY: 'high-availability';

HIP_OBJECTS: 'hip-objects';

HIP_PROFILES: 'hip-profiles';

HOLD_TIME:
    'hold-time'
;

HOSTNAME: 'hostname';

HOURLY: 'hourly';

HOURS: 'hours';

IBGP: 'ibgp';

ICMP: 'icmp';

IDLE_HOLD_TIME: 'idle-hold-time';

IETF
:
    [Ii][Ee][Tt][Ff]
;

IGP: 'igp';

IKE: 'ike';

IKE_CRYPTO_PROFILES: 'ike-crypto-profiles';

IMPORT: 'import';

IMPORT_NEXTHOP: 'import-nexthop';

INCOMING_BGP_CONNECTION: 'incoming-bgp-connection';

INCOMPLETE: 'incomplete';

INSTALL_ROUTE: 'install-route';

INTERFACE: 'interface';

INTERZONE: 'interzone';

INTRAZONE: 'intrazone';

IP: 'ip';

IP_ADDRESS_LITERAL: 'ip-address';

IP_NETMASK: 'ip-netmask';

IP_RANGE_LITERAL: 'ip-range';

IPSEC_CRYPTO_PROFILES: 'ipsec-crypto-profiles';

IPV4: 'ipv4';

IPV6: 'ipv6';

KEEP_ALIVE_INTERVAL: 'keep-alive-interval';

LAYER2: 'layer2';

LAYER3: 'layer3';

LIFETIME: 'lifetime';

LINK
:
    'link' -> pushMode ( M_Url )
;

LINK_DUPLEX: 'link-duplex';

LINK_SPEED: 'link-speed';

LINK_STATE: 'link-state';

LINK_TYPE: 'link-type';

LIST: 'list';

LLDP: 'lldp';

LOCAL_ADDRESS: 'local-address';

LOCAL_AS: 'local-as';

LOCAL_PORT: 'local-port';

LOG_COLLECTOR: 'log-collector';

LOG_COLLECTOR_GROUP: 'log-collector-group';

LOG_END: 'log-end';

LOG_SETTING: 'log-setting';

LOG_SETTINGS: 'log-settings';

LOG_START: 'log-start';

LOOPBACK: 'loopback';

MATCH: 'match';

MAX_PATH: 'max-path';

MAX_PREFIXES: 'max-prefixes';

MD5: 'md5';

MED: 'med';

MESHED_CLIENT: 'meshed-client';

MIN_ROUTE_ADV_INTERVAL: 'min-route-adv-interval';

MINUTES: 'minutes';

MEMBERS: 'members';

METRIC: 'metric';

MGT_CONFIG: 'mgt-config';

MODE: 'mode';

MOVE: 'move';

MTU: 'mtu';

MULTICAST: 'multicast';

MULTIHOP: 'multihop';

NAT: 'nat';

NDP_PROXY: 'ndp-proxy';

NEGATE_DESTINATION: 'negate-destination';

NEGATE_SOURCE: 'negate-source';

NETMASK: 'netmask';

NETWORK: 'network';

NEXT_VR: 'next-vr';

NEXTHOP: 'nexthop';

NO: 'no';

NO_REDIST: 'no-redist';

NON_CLIENT: 'non-client';

NONE: 'none';

NORMAL: 'normal';

NSSA: 'nssa';

NTP_SERVER_ADDRESS: 'ntp-server-address';

NTP_SERVERS: 'ntp-servers';

NULL: 'null';

OPEN_DELAY_TIME: 'open-delay-time';

ORIGIN: 'origin';

ORIGINAL: 'original';

OSPF: 'ospf';

OSPF_EXT: 'ospf-ext';

OSPF_INT: 'ospf-int';

OSPFV3: 'ospfv3';

OSPFV3_EXT: 'ospfv3-ext';

OSPFV3_INT: 'ospfv3-int';

OUTGOING_BGP_CONNECTION: 'outgoing-bgp-connection';

P2P: 'p2p';

P2MP: 'p2mp';

PANORAMA: 'panorama';

PANORAMA_SERVER: 'panorama-server';

PARENT_DG: 'parent-dg';

PASSIVE: 'passive';

PASSWORD: 'password';

PATH_MONITOR: 'path-monitor';

PEER: 'peer';

PEER_ADDRESS: 'peer-address';

PEER_AS: 'peer-as';

PEER_GROUP: 'peer-group';

PERVASIVE: 'pervasive';

POLICY: 'policy';

PORT: 'port';

POST_RULEBASE: 'post-rulebase';

PRE_RULEBASE: 'pre-rulebase';

PRIMARY: 'primary';

PRIMARY_NTP_SERVER: 'primary-ntp-server';

PRIORITY: 'priority';

PROFILE: 'profile';

PROFILE_GROUP: 'profile-group';

PROFILES: 'profiles';

PRONE_TO_MISUSE: 'prone-to-misuse';

PROTOCOL: 'protocol';

QOS: 'qos';

READONLY: 'readonly';

RECURRING: 'recurring';

REDIST: 'redist';

REDIST_PROFILE: 'redist-profile';

REDIST_RULES: 'redist-rules';

REFLECTOR_CLIENT: 'reflector-client';

REFLECTOR_CLUSTER_ID: 'reflector-cluster-id';

REJECT_DEFAULT_ROUTE: 'reject-default-route';

REMOTE_PORT: 'remote-port';

REMOVE_PRIVATE_AS: 'remove-private-as';

RESET_BOTH: 'reset-both';

RESET_CLIENT: 'reset-client';

RESET_SERVER: 'reset-server';

RESOLVE: 'resolve';

RESPONSE: 'response';

RESULT: 'result';

RETRANSMIT_INTERVAL: 'retransmit-interval';

RIP: 'rip';

RISK: 'risk';

ROUTE_TABLE: 'route-table';

ROUTER_ID: 'router-id';

ROUTING_OPTIONS: 'routing-options';

ROUTING_TABLE: 'routing-table';

RULE_TYPE: 'rule-type';

RULEBASE: 'rulebase';

RULES: 'rules';

SCTP: 'sctp';

SECONDARY: 'secondary';

SECONDARY_NTP_SERVER: 'secondary-ntp-server';

SECONDS: 'seconds';

SECURITY: 'security';

SERVER: 'server';

SERVERS: 'servers';

SERVICE: 'service';

SERVICE_GROUP: 'service-group';

SET: 'set';

SET_ORIGIN: 'set-origin';

SETTING: 'setting';

SHA1: 'sha1';

SHA256: 'sha256';

SHA384: 'sha384';

SHA512: 'sha512';

SHARED: 'shared';

SHARED_GATEWAY: 'shared-gateway';

SOURCE: 'source';

SOURCE_HIP: 'source-hip';

SOURCE_PORT: 'source-port';

SOURCE_TRANSLATION: 'source-translation';

SOURCE_USER: 'source-user';

SPYWARE: 'spyware';

SSL
:
    [Ss][Ss][Ll]
;

STATIC: 'static';

STATIC_IPV6: 'static-ipv6';

STATIC_ROUTE: 'static-route';

STRICT_LSA_CHECKING: 'strict-LSA-checking';

STUB: 'stub';

SUBCATEGORY: 'subcategory';

SYSLOG: 'syslog';

SYSTEM: 'system';

TAG: 'tag';

TAP: 'tap';

TARGET: 'target';

TCP
:
    [Tt][Cc][Pp]
;

TECHNOLOGY: 'technology';

TEMPLATE: 'template';

TEMPLATES: 'templates';

TEMPLATE_STACK: 'template-stack';

THREE_DES: '3des';

TIMEZONE: 'timezone';

TO: 'to';

TOP: 'top';

TRANSFERS_FILES: 'transfers-files';

TRANSIT_DELAY: 'transit-delay';

TRANSLATED_ADDRESS: 'translated-address';

TRANSLATED_PORT: 'translated-port';

TRANSPORT: 'transport';

TUNNEL: 'tunnel';

TUNNELS_OTHER_APPS: 'tunnels-other-apps';

TYPE: 'type';

UDP
:
    [Uu][Dd][Pp]
;

UNICAST: 'unicast';

UNITS: 'units';

UNIVERSAL: 'universal';

UP: 'up';

UPDATE: 'update';

UPDATE_SCHEDULE: 'update-schedule';

UPDATE_SERVER: 'update-server';

URL: 'url';

URL_FILTERING: 'url-filtering';

USE_PEER: 'use-peer';

USE_SELF: 'use-self';

USED_BY: 'used-by';

USED_BY_MALWARE: 'used-by-malware';

USERID: 'userid';

USERNAME: 'username';

VIRTUAL_ROUTER: 'virtual-router';

VIRTUAL_WIRE: 'virtual-wire';

VIRUS: 'virus';

VISIBLE_VSYS: 'visible-vsys';

VLAN: 'vlan';

VSYS: 'vsys';

VULNERABILITY: 'vulnerability';

WEIGHT: 'weight';

WILDFIRE_ANALYSIS: 'wildfire-analysis';

YES: 'yes';

ZONE: 'zone';

// Ignored config blocks
REDISTRIBUTION_AGENT: 'redistribution-agent' -> pushMode(M_IgnoredConfigBlock);

SERVER_PROFILE: 'server-profile' -> pushMode(M_IgnoredConfigBlock);

// Complex tokens

CLOSE_BRACKET
:
    ']'
;

COMMA
:
    ','
;

DASH: '-';

DOUBLE_QUOTED_STRING
:
    '"' ~'"'* '"'
;

IP_ADDRESS
:
    F_IpAddress
;

IP_PREFIX
:
    F_IpPrefix
;

IP_RANGE
:
    F_IpAddress '-' F_IpAddress
;

// Handle developer and RANCID-header-style line comments
COMMENT_LINE
:
  F_Whitespace* [!#] {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_Newline+ | EOF) -> channel(HIDDEN)
;


NETFLOW_PROFILE: 'netflow-profile';

NEWLINE
:
    F_Newline+
;

OPEN_BRACKET
:
    '['
;

RANGE
:
    F_Digit+ '-' F_Digit+
;

SINGLE_QUOTED_STRING
:
    '\'' ~'\''* '\''
;

UINT8
:
    F_Uint8
;

UINT16
:
    F_Uint16
;

UINT32
:
    F_Uint32
;

VARIABLE
:
    F_Variable_VarChar+
;

WS
:
    F_Whitespace+ -> channel(HIDDEN) // parser never sees tokens on hidden channel
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
F_IpAddress
:
    F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
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
F_PositiveDigit
:
    [1-9]
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
F_Url
:
    F_UrlStart F_UrlInner+
;

F_UrlStart
:
    [a-zA-Z]
;

F_UrlInner
:
    F_UrlInnerAlphaNum
    | F_UrlInnerReserved
    | F_UrlInnerUnreserved
;

F_UrlInnerAlphaNum
:
    [a-zA-Z0-9]
;

F_UrlInnerReserved
:
    [!*'();:@&=+$,/?%#[\]]
;

F_UrlInnerUnreserved
:
    [-_.~]
;

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_Variable_VarChar
:
    ~[ \t\n\r;,{}[\]&|()"']
;

fragment
F_NotWhitespaceNewlineOrQuote
:
    ~["' \r\n\t\u000C] // quote, space, newline, tab, or unicode 0x000C
;

// Modes

// PaloAlto devices can produce set-line output where quotes enclose unescaped quotes.
//
// Terminate quoted values when encountering the expected closing quote followed by a newline
// to avoid accidentally consuming following lines.
mode M_Value;
M_Value_WS
:
  F_Whitespace+ -> skip
;

M_Value_BODY
:
  F_NotWhitespaceNewlineOrQuote+ -> type(BODY), popMode
;

M_Value_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTE), mode(M_ValueDoubleQuoted)
;

M_Value_NEWLINE
:
  F_Newline+ -> type(NEWLINE), popMode
;

M_Value_SINGLE_QUOTE
:
  '\'' -> type(SINGLE_QUOTE), mode(M_ValueSingleQuoted)
;

mode M_ValueDoubleQuoted;

M_ValueDoubleQuoted_DOUBLE_QUOTE
:
  '"' {followedByNewline()}? -> type(DOUBLE_QUOTE), popMode
;

M_ValueDoubleQuoted_BODY
:
  (~'"' | ('"' {!followedByNewline()}?))+ -> type(BODY)
;

mode M_ValueSingleQuoted;

M_ValueSingleQuoted_SINGLE_QUOTE
:
  '\'' {followedByNewline()}? -> type(SINGLE_QUOTE), popMode
;

M_ValueSingleQuoted_BODY
:
  (~'\'' | ('\'' {!followedByNewline()}?))+ -> type(BODY)
;

mode M_Url;

M_Url_NEWLINE
:
    F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Url_WORD
:
    F_Url -> type ( WORD ) , popMode
;

M_Url_WS
:
    F_Whitespace+ -> channel ( HIDDEN )
;

// Modes for ignored config blocks
mode M_IgnoredConfigBlock;

M_IgnoredConfigBlock_OPEN_BRACE: '{' -> skip, pushMode(M_IgnoredConfigBlockInner);

M_IgnoredConfigBlock_CONTENT: ~[\r\n]+ -> skip;

M_IgnoredConfigBlock_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;

// Inside an ignored block, count braces
mode M_IgnoredConfigBlockInner;

M_IgnoredConfigBlockInner_OPEN_BRACE: '{' -> skip, pushMode(M_IgnoredConfigBlockInner);

M_IgnoredConfigBlockInner_CLOSE_BRACE: '}' -> skip, popMode;

M_IgnoredConfigBlockInner_CONTENT: ~[{}]+ -> skip;

M_IgnoredConfigBlockInner_WS: F_Whitespace+ -> skip;

M_IgnoredConfigBlockInner_NEWLINE: F_Newline+ -> skip;