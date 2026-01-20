lexer grammar PaloAltoLexer;

options {
  superClass = 'org.batfish.grammar.palo_alto.parsing.PaloAltoBaseLexer';
  caseInsensitive = true;
}

tokens {
  BODY,
  DOUBLE_QUOTE,
  IGNORED_CONFIG_BLOCK,
  SINGLE_QUOTE
}

SET: 'set';

// Keywords

ANTI_REPLAY: 'anti-replay';

AUTO_KEY: 'auto-key';

IPSEC: 'ipsec';

TUNNEL_INTERFACE: 'tunnel-interface';

TUNNEL_MONITOR: 'tunnel-monitor';

DEVICE_TELEMETRY: 'device-telemetry';

SNMP_SETTING: 'snmp-setting';

PERMITTED_IP: 'permitted-ip';

ROUTE: 'route';

AUTHENTICATION_TYPE: 'authentication-type';

PEER_IP_BACKUP: 'peer-ip-backup';

PEER_IP: 'peer-ip';

ELECTION_OPTION: 'election-option';

STATE_SYNCHRONIZATION: 'state-synchronization';

MONITORING: 'monitoring';

ACTIVE_PASSIVE: 'active-passive';

PROFILE_SETTING: 'profile-setting';

GROUP_TAG: 'group-tag';

SCHEDULE: 'schedule';

TWO_BYTE: '2-byte';

FOUR_BYTE: '4-byte';

ACCEPT_SUMMARY: 'accept-summary';

ACTION: 'action';

FORWARD: 'forward';

EGRESS_INTERFACE: 'egress-interface';

ENFORCE_SYMMETRIC_RETURN: 'enforce-symmetric-return';

MONITOR: 'monitor';

ACTIVE: 'active';

DEFAULT_SECURITY_RULES: 'default-security-rules';

PBF: 'pbf';

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

AGGREGATED_CONFED_AS_PATH: 'aggregated-confed-as-path';

AGGREGATE_ETHERNET: 'aggregate-ethernet';

AGGREGATE_GROUP: 'aggregate-group';

AGGREGATE_MED: 'aggregate-med';

ALG: 'alg';

ALGORITHM: 'algorithm';

ALL: 'all';

ALLOW: 'allow';

ALLOWAS_IN: 'allowas-in';

AND_ALSO_TO: 'and-also-to';

ALTERNATE_USER_NAME_1: 'alternate-user-name-1';

ALTERNATE_USER_NAME_2: 'alternate-user-name-2';

ALTERNATE_USER_NAME_3: 'alternate-user-name-3';

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

AUTHENTICATION_SEQUENCE: 'authentication-sequence';

AUTH_ALGO_SHA1: 'auth-algo-sha1';

AUTH_ALGO_SHA256: 'auth-algo-sha256';

AUTH_ALGO_SHA384: 'auth-algo-sha384';

AUTO: 'auto';

BEFORE: 'before';

BFD: 'bfd';

BGP: 'bgp';

BI_DIRECTIONAL: 'bi-directional';

BOTH: 'both';

BOTNET: 'botnet' -> pushMode(M_IgnoredConfigBlock);

BOTTOM: 'bottom';

BROADCAST: 'broadcast';

BSD
:
    'bsd'
;

CATEGORY: 'category';

CERTIFICATE: 'certificate';

CERTIFICATE_PROFILE: 'certificate-profile';

CUTOFF: 'cutoff';

CLIENT: 'client';

COLOR: 'color';

COMMENT: 'comment';

COMMENTS: 'comments';

COMMUNITY: 'community';

CONNECT: 'connect';

CONFIG: 'config';

CONNECTION_OPTIONS: 'connection-options';

CONTENT_PREVIEW: 'content-preview';

COPY_TOS: 'copy-tos';

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

DEVICE_GROUP: 'device-group';

DOMAIN: 'domain';

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

DECAY_HALF_LIFE_REACHABLE: 'decay-half-life-reachable';

DECAY_HALF_LIFE_UNREACHABLE: 'decay-half-life-unreachable';

DETERMINISTIC_MED_COMPARISON: 'deterministic-med-comparison';

DEVICES: 'devices';

DEVICE_ID: 'device-id';

DEVICECONFIG: 'deviceconfig';

DH_GROUP: 'dh-group';

DISABLE: 'disable';

DISABLE_IF_UNREACHABLE: 'disable-if-unreachable';

DISABLE_SERVER_RESPONSE_INSPECTION: 'disable-server-response-inspection';

DISABLED: 'disabled';

DISCARD: 'discard';

DISPLAY_NAME: 'display-name';

DNS: 'dns';

DNS_SETTING: 'dns-setting';

DYNAMIC_USER_GROUP: 'dynamic-user-group';

DYNAMIC_IP: 'dynamic-ip';

DYNAMIC_IP_AND_PORT: 'dynamic-ip-and-port';

DOS_PROTECTION: 'dos-protection';

DOWN: 'down';

DROP: 'drop';

DYNAMIC: 'dynamic';

EBGP: 'ebgp';

EMAIL: 'email';

EMAIL_SCHEDULER: 'email-scheduler';

ENABLE_MP_BGP: 'enable-mp-bgp';

ENABLE_PACKET_BUFFER_PROTECTION: 'enable-packet-buffer-protection';

ECMP: 'ecmp';

EGP: 'egp';

ENABLE: 'enable';

ENABLE_SENDER_SIDE_LOOP_DETECTION: 'enable-sender-side-loop-detection';

ENABLE_USER_IDENTIFICATION: 'enable-user-identification';

ENABLED: 'enabled';

ENCRYPTION: 'encryption';

ENC_ALGO_AES_128_CBC: 'enc-algo-aes-128-cbc';

ENC_ALGO_AES_128_GCM: 'enc-algo-aes-128-gcm';

ENC_ALGO_AES_256_CBC: 'enc-algo-aes-256-cbc';

ENC_ALGO_AES_256_GCM: 'enc-algo-aes-256-gcm';

ENC_ALGO_AES_CHACHA20_POLY1305: 'enc-algo-aes-chacha20-poly1305';

ESP: 'esp';

EVASIVE: 'evasive';

EXCLUDE_LIST: 'exclude-list';

EXCEPTION_LIST: 'exception-list';

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

FAST: 'fast';

FAILURE_CONDITION: 'failure-condition';

FILE_BLOCKING: 'file-blocking';

FIVE_MINUTE: 'five-minute';

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

GROUP_EMAIL: 'group-email';

GROUP_ID: 'group-id';

GROUP_INCLUDE_LIST: 'group-include-list';

GROUP_MAPPING: 'group-mapping';

GROUP_MEMBER: 'group-member';

GROUP_NAME: 'group-name';

GROUP_OBJECT: 'group-object';

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
    'ietf'
;

IGP: 'igp';

IKE: 'ike';

IKE_CRYPTO_PROFILES: 'ike-crypto-profiles';

IMPORT: 'import';

IMPORT_NEXTHOP: 'import-nexthop';

INCLUDE_LIST: 'include-list';

INCOMING_BGP_CONNECTION: 'incoming-bgp-connection';

INCOMPLETE: 'incomplete';

INSTALL_ROUTE: 'install-route';

INTERFACE: 'interface';

INTERFACE1: 'interface1';

INTERFACE2: 'interface2';

INTERFACE_MANAGEMENT_PROFILE: 'interface-management-profile';

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

LACP: 'lacp';

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

LINK_STATE_PASS_THROUGH: 'link-state-pass-through';

LINK_TYPE: 'link-type';

LIST: 'list';

LLDP: 'lldp';

LOCAL_ADDRESS: 'local-address';

LOCAL_AS: 'local-as';

LOCAL_PORT: 'local-port';

LOCAL_USER_DATABASE: 'local-user-database';

LOG_COLLECTOR: 'log-collector';

LOG_COLLECTOR_GROUP: 'log-collector-group';

LOG_END: 'log-end';

LOG_SETTING: 'log-setting';

LOG_SETTINGS: 'log-settings';

LOG_START: 'log-start';

LOOPBACK: 'loopback';

MATCH: 'match';

MANAGER: 'manager';

MAX_HOLD_TIME: 'max-hold-time';

MAX_PATH: 'max-path';

MAX_PREFIXES: 'max-prefixes';

MAX_VERSION: 'max-version';

MD5: 'md5';

MED: 'med';

MESHED_CLIENT: 'meshed-client';

MINIMUM_RECEIVE_INTERVAL: 'minimum-receive-interval';

MINIMUM_TRANSMIT_INTERVAL: 'minimum-transmit-interval';

MIN_ROUTE_ADV_INTERVAL: 'min-route-adv-interval';

MINUTES: 'minutes';

MODE: 'mode';

MEMBERS: 'members';

MULTICAST_FIREWALLING: 'multicast-firewalling';

METRIC: 'metric';

MIN_VERSION: 'min-version';

MGT_CONFIG: 'mgt-config';



MOVE: 'move';

MTU: 'mtu';

MULTICAST: 'multicast';

MULTIHOP: 'multihop';

MULTIPATH: 'multipath';

MULTIPLIER: 'multiplier';

NAT: 'nat';

NDP_PROXY: 'ndp-proxy';

NEGATE_DESTINATION: 'negate-destination';

NEGATE_SOURCE: 'negate-source';

NETMASK: 'netmask';

NETWORK: 'network';

NEXT_VR: 'next-vr';

NEXTHOP: 'nexthop';

NEXT_HOP_SELF: 'next-hop-self';

NO: 'no';

NO_REDIST: 'no-redist';

NON_CLIENT: 'non-client';

NONE: 'none';

NORMAL: 'normal';

NSSA: 'nssa';

NTP_SERVER_ADDRESS: 'ntp-server-address';

NTP_SERVERS: 'ntp-servers';

NULL: 'null';

OPTION: 'option';

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

OVERRIDE: 'override';

P2P: 'p2p';

P2MP: 'p2mp';

PANORAMA: 'panorama';

PANORAMA_SERVER: 'panorama-server';

PARENT_DG: 'parent-dg';

PASSIVE: 'passive';

PASSIVE_PRE_NEGOTIATION: 'passive-pre-negotiation';

PASSWORD: 'password';

PATH_MONITOR: 'path-monitor';

PEER: 'peer';

PEER_ADDRESS: 'peer-address';

PEER_AS: 'peer-as';

PEER_GROUP: 'peer-group';

PEERING_TYPE: 'peering-type';

PERVASIVE: 'pervasive';

POLICY: 'policy';

PORT: 'port';

PORT_PRIORITY: 'port-priority';

POST_RULEBASE: 'post-rulebase';

PRE_RULEBASE: 'pre-rulebase';

PREPEND: 'prepend';

PRIMARY: 'primary';

PRIMARY_NTP_SERVER: 'primary-ntp-server';

PRIORITY: 'priority';

PROFILE: 'profile';

PROFILE_GROUP: 'profile-group';

PROFILES: 'profiles';

PRONE_TO_MISUSE: 'prone-to-misuse';

PROTOCOL: 'protocol';

PROTOCOL_SETTINGS: 'protocol-settings';

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

REPORTS: 'reports';

REGION: 'region';

RESET_BOTH: 'reset-both';

RESET_CLIENT: 'reset-client';

RESET_SERVER: 'reset-server';

SMTP: 'smtp';

SOFT_RESET_WITH_STORED_INFO: 'soft-reset-with-stored-info';

SOFTEN_INBOUND: 'soften-inbound';

SNMPTRAP: 'snmptrap';

RESOLVE: 'resolve';

RESPONSE: 'response';

RESULT: 'result';

REUSE: 'reuse';

SCHEDULE_TYPE: 'schedule-type';

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
    'ssl'
;

SUBSEQUENT_ADDRESS_FAMILY_IDENTIFIER: 'subsequent-address-family-identifier';

STATIC: 'static';

STATIC_IP: 'static-ip';

STATIC_IPV6: 'static-ipv6';

STATIC_ROUTE: 'static-route';

STRICT_LSA_CHECKING: 'strict-LSA-checking';

STUB: 'stub';

SUBCATEGORY: 'subcategory';

SYSLOG: 'syslog';

SSL_TLS_SERVICE_PROFILE: 'ssl-tls-service-profile';

SYSTEM: 'system';

TAG: 'tag';

TAP: 'tap';

TARGET: 'target';

THREATS: 'threats';

TCP
:
    'tcp'
;

TCP_HALF_CLOSED_TIMEOUT: 'tcp-half-closed-timeout';

TCP_TIMEOUT: 'tcp-timeout';

TCP_TIME_WAIT_TIMEOUT: 'tcp-time-wait-timeout';

TECHNOLOGY: 'technology';

TEMPLATE: 'template';

TEMPLATE_STACK: 'template-stack';

TEMPLATES: 'templates';

THREE_DES: '3des';

TIMEZONE: 'timezone';

TO: 'to';

TO_INTERFACE: 'to-interface';

TOP: 'top';

TRANSFERS_FILES: 'transfers-files';

TRANSIT_DELAY: 'transit-delay';

TRANSLATED_ADDRESS: 'translated-address';

TRANSLATED_PORT: 'translated-port';

TRANSPORT: 'transport';

TRANSMISSION_RATE: 'transmission-rate';

TUNNEL: 'tunnel';

TUNNELS_OTHER_APPS: 'tunnels-other-apps';

TYPE: 'type';

UDP
:
    'udp'
;

UDP_TIMEOUT: 'udp-timeout';

UNSPECIFIED: 'unspecified';

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

USE_SAME_SYSTEM_MAC: 'use-same-system-mac';

USE_SELF: 'use-self';

USED_BY: 'used-by';



USED_BY_MALWARE: 'used-by-malware';

USERID: 'userid';

USER_ACL: 'user-acl';

USER_ID_COLLECTOR: 'user-id-collector';

USER_EMAIL: 'user-email';

USER_ID_AGENT: 'user-id-agent';

USER_NAME: 'user-name';

USER_OBJECT: 'user-object';

USERNAME: 'username';

VERSION: 'version';

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

UUID: [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] '-' [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] '-' [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] '-' [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] '-' [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9] [a-f0-9];

// Ignored config blocks
REDISTRIBUTION_AGENT: 'redistribution-agent' -> pushMode(M_IgnoredConfigBlock);

SERVER_PROFILE: 'server-profile';

// Complex tokens

CLOSE_BRACKET
:
    ']'
;



UINT8
:
    F_Uint8
;

UINT16
:
    F_Uint16
;

COMMA
:
    ','
;

DASH: '-';

DOT: '.';

UNDERSCORE: '_';

DOUBLE_QUOTED_STRING
:
    '"' ~'"'* '"'
;

IP_ADDRESS
:
    F_IpAddress
;

IP_ADDRESS_V6
:
    F_Ipv6Address
;

IP_PREFIX
:
    F_IpPrefix
;

IP_RANGE
:
    F_IpAddress '-' F_IpAddress
    | F_Ipv6Address '-' F_Ipv6Address
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



UINT32
:
    F_Uint32
;

WORD
:
    [a-z_][a-z0-9_]*
;

fragment
F_Variable_VarChar
:
    ~[ \t\n\r;,{}[\]&|()"']
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
    | F_Ipv6Address '/' F_Ipv6PrefixLength
;

fragment
F_IpPrefixLength
:
    F_Digit
    | [12] F_Digit
    | [3] [012]
;

fragment
F_HexDigit
:
  [0-9a-f]
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
F_Ipv6Address
:
  (
    '::' F_HexWordLE7
    | F_HexWord '::' F_HexWordLE6
    | F_HexWord2 '::' F_HexWordLE5
    | F_HexWord3 '::' F_HexWordLE4
    | F_HexWord4 '::' F_HexWordLE3
    | F_HexWord5 '::' F_HexWordLE2
    | F_HexWord6 '::' F_HexWordLE1
    | F_HexWord7 '::'
    | F_HexWord8
  )
  ( '%' [0-9a-z]+ )?
;

fragment
F_Ipv6PrefixLength
:
    F_Digit
    | F_Digit F_Digit
    | '1' [01] F_Digit
    | '12' [0-8]
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
    [a-z]
;

F_UrlInner
:
    F_UrlInnerAlphaNum
    | F_UrlInnerReserved
    | F_UrlInnerUnreserved
;

F_UrlInnerAlphaNum
:
    [a-z0-9]
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