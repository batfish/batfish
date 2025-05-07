lexer grammar FlatJuniperLexer;

options {
   superClass = 'org.batfish.grammar.flatjuniper.parsing.FlatJuniperBaseLexer';
}

// Wildcard is from the first pass lexing. Aka, it appears in the configs.
// Wildcard artifact is generated when we generate text and run a parser on it (say, apply-path strings).
@members {
  private void setWildcard() {
    setType(_markWildcards ? WILDCARD_ARTIFACT : WILDCARD);
  }
}

tokens {
   ACK,
   AS_PATH_REGEX,
   BANG,
   BGP_RIB_NAME,
   CERTIFICATE_STRING,
   DOUBLE_QUOTED_NAME,
   DYNAMIC_DB,
   SECRET_STRING,
   FIN,
   IGNORED_WORD,
   INTERFACE_ID,
   INTERFACE_WILDCARD,
   INET_RIB_NAME,
   INET6_RIB_NAME,
   ISO_RIB_NAME,
   ISO_ADDRESS,
   LAST_AS,
   LITERAL_OR_REGEX_COMMUNITY,
   MPLS_RIB_NAME,
   NAME,
   PIPE,
   POLICY_EXPRESSION,
   RST,
   SUB_RANGE,
   SYN,
   UINT32L,
   VERSION_STRING,
   VXLAN_RIB_NAME,
   WILDCARD_ARTIFACT
}

// Juniper Keywords

ACCEPT: 'accept';

ACCEPT_DATA: 'accept-data';

ACCEPTED_PREFIX_LIMIT: 'accepted-prefix-limit';

ACCESS: 'access';

ACCESS_INTERNAL: 'access-internal';

ACCESS_PROFILE: 'access-profile' -> pushMode(M_Name);

ACCOUNTING: 'accounting';
ACTIVATE: 'activate';
ACTIVE: 'active';

ACTIVE_SERVER_GROUP: 'active-server-group' -> pushMode(M_Name);

ADAPTIVE: 'adaptive';

ADAPTIVE_SHAPERS: 'adaptive-shapers';

ADD
:
  'add'
  {
    if (lastTokenType() == COMMUNITY) {
      pushMode(M_Name);
    }
  }
;

ADD_PATH: 'add-path';

ADDRESS
:
  'address'
  {
    if (lastTokenType() == ADDRESS_BOOK
        || secondToLastTokenType() == ADDRESS_BOOK
        || secondToLastTokenType() == ADDRESS_SET) {
      pushMode(M_Name);
    }
  }
;

ADDRESS_BOOK
:
  'address-book'
  {
    if (lastTokenType() == SECURITY) {
      pushMode(M_Name);
    }
  }
;

ADDRESS_FAMILY: 'address-family';

ADDRESS_MASK: 'address-mask';

ADDRESS_SET: 'address-set' -> pushMode(M_Name);

ADJUST_INTERVAL: 'adjust-interval';

ADJUST_THRESHOLD: 'adjust-threshold';

ADJUST_THRESHOLD_ABSOLUTE: 'adjust-threshold-absolute';

ADJUST_THRESHOLD_ACTIVATE_BANDWIDTH: 'adjust-threshold-activate-bandwidth';

ADJUST_THRESHOLD_OVERFLOW_LIMIT: 'adjust-threshold-overflow-limit';

ADJUST_THRESHOLD_UNDERFLOW_LIMIT: 'adjust-threshold-underflow-limit';

ADMIN_GROUP: 'admin-group' -> pushMode(M_AdminGroup);

ADMIN_GROUPS: 'admin-groups' -> pushMode(M_Name);

ADVERTISE_EXTERNAL: 'advertise-external';
ADVERTISE_FROM_MAIN_VPN_TABLES: 'advertise-from-main-vpn-tables';
ADVERTISE_HIGH_METRICS: 'advertise-high-metrics';

ADVERTISE_INACTIVE: 'advertise-inactive';

ADVERTISE_INTERVAL: 'advertise-interval';

ADVERTISE_PEER_AS: 'advertise-peer-as';

AFS: 'afs';

AFTER: 'after';

AGGREGATE: 'aggregate';
AGGREGATED_DEVICES: 'aggregated-devices';
AGGREGATED_ETHER_OPTIONS: 'aggregated-ether-options';

AGGREGATOR: 'aggregator';

AGGRESSIVE: 'aggressive';
AE_IRB: 'ae-irb';
AES_128_CBC: 'aes-128-cbc';

AES_128_CMAC_96: 'aes-128-cmac-96';

AES_128_GCM: 'aes-128-gcm';


AES_192_CBC: 'aes-192-cbc';

AES_192_GCM: 'aes-192-gcm';

AES_256_CBC: 'aes-256-cbc';

AES_256_GCM: 'aes-256-gcm';

AH: 'ah';

AH_HEADER: 'AH-header';
AGING_TIMER: 'aging-timer';
ALARM_WITHOUT_DROP: 'alarm-without-drop';

ALARM_THRESHOLD: 'alarm-threshold';

ALG: 'alg';

ALGORITHM: 'algorithm';

ALIAS: 'alias';

ALIASES: 'aliases';

ALL: 'all';
ALL_PATHS: 'all-paths';
ALLOW: 'allow';

ALLOW_DUPLICATES: 'allow-duplicates';

ALLOW_SNOOPED_CLIENTS: 'allow-snooped-clients';

ALLOW_V4MAPPED_PACKETS: 'allow-v4mapped-packets';

ALWAYS_COMPARE_MED: 'always-compare-med';

ALWAYS_SEND: 'always-send';

ALWAYS_WRITE_GIADDR: 'always-write-giaddr';

ANALYZER: 'analyzer';

ANY: 'any';

ANY_IPV4: 'any-ipv4';

ANY_IPV6: 'any-ipv6';

ANY_REMOTE_HOST: 'any-remote-host';

ANY_SERVICE: 'any-service';

APPLICATION: 'application' -> pushMode(M_Application);

APPLICATION_PROTOCOL: 'application-protocol';
APPLICATION_SERVICES: 'application-services';
APPLICATION_SET: 'application-set' -> pushMode(M_ApplicationSet);

APPLICATION_TRACKING: 'application-tracking';

APPLICATION_TRAFFIC_CONTROL: 'application-traffic-control';

APPLICATIONS: 'applications';

APPLY_GROUPS: 'apply-groups' -> pushMode(M_ApplyGroups);

APPLY_GROUPS_EXCEPT: 'apply-groups-except' -> pushMode(M_Name);

APPLY_MACRO: 'apply-macro';

APPLY_PATH: 'apply-path';

ARCHIVE: 'archive';

AREA: 'area';

AREA_RANGE: 'area-range';

ARP: 'arp';

ARP_RESP: 'arp-resp';

AS_OVERRIDE: 'as-override';

AS_PATH
:
  'as-path'
  {
    switch(lastTokenType()) {
      case POLICY_OPTIONS:
        // set policy-options as-path
        pushMode(M_AsPathDefinitionName);
        break;
      case FROM:
        // set policy-options policy-statement from as-path
        pushMode(M_Name);
        break;
      default:
        switch(secondToLastTokenType()) {
          case AS_PATH_GROUP:
            // set policy-options as-path-group foo as-path
            pushMode(M_AsPathDefinitionName);
            break;
          default:
            break;
        }
        break;
    }
  }
;

AS_PATH_EXPAND: 'as-path-expand' -> pushMode(M_AsPathExpand);

AS_PATH_GROUP: 'as-path-group' -> pushMode (M_Name);

AS_PATH_PREPEND
:
   'as-path-prepend' -> pushMode ( M_AsPathPrepend )
;

ASCII_TEXT: 'ascii-text';

ASDOT_NOTATION: 'asdot-notation';

ATTACK_THRESHOLD: 'attack-threshold';

ATTACH: 'attach';

AUTHENTICATION: 'authentication';

AUTHENTICATION_ALGORITHM: 'authentication-algorithm';

AUTHENTICATION_KEY: 'authentication-key' -> pushMode(M_SecretString);

AUTHENTICATION_KEY_CHAIN: 'authentication-key-chain' -> pushMode(M_Name);

AUTHENTICATION_KEY_CHAINS: 'authentication-key-chains';

AUTHENTICATION_METHOD: 'authentication-method';

AUTHENTICATION_ORDER: 'authentication-order';

AUTHENTICATION_TYPE: 'authentication-type';

AUTHORIZATION: 'authorization';

AUTHORIZED_KEYS_COMMAND: 'authorized-keys-command';

AUTHORIZED_KEYS_COMMAND_USER: 'authorized-keys-command-user';

AUTO: 'auto';

AUTO_BANDWIDTH: 'auto-bandwidth';

AUTO_EXPORT: 'auto-export';

AUTO_NEGOTIATION: 'auto-negotiation';

AUTO_SNAPSHOT: 'auto-snapshot';

AUTONOMOUS_SYSTEM: 'autonomous-system' -> pushMode(M_BgpAsn);

AUXILIARY: 'auxiliary';

BAD_INNER_HEADER: 'bad-inner-header';

BAD_OPTION: 'bad-option';

BACKUP_ROUTER: 'backup-router';

BANDWIDTH
:
   'bandwidth' -> pushMode ( M_Bandwidth )
;

BASIC: 'basic';

BEFORE: 'before';

BFD: 'bfd';

BFD_LIVENESS_DETECTION: 'bfd-liveness-detection';

BGP: 'bgp';
BGP_ERROR_TOLERANCE: 'bgp-error-tolerance';
BGP_OUTPUT_QUEUE_PRIORITY: 'bgp-output-queue-priority';

BIFF: 'biff';

BIND_INTERFACE
:
   'bind-interface' -> pushMode ( M_Interface )
;

BLOCK_FRAG: 'block-frag';

BMP: 'bmp';

BOOT_SERVER: 'boot-server';

BOOTP: 'bootp';

BOOTP_SUPPORT: 'bootp-support';

BOOTPC: 'bootpc';

BOOTPS: 'bootps';

BRIDGE: 'bridge';

BRIDGE_DOMAINS: 'bridge-domains' -> pushMode(M_Name);

BROADCAST_CLIENT: 'broadcast-client';

BUNDLE: 'bundle';

C: 'c';

CALIPSO_OPTION: 'CALIPSO-option';

CATEGORIES: 'categories';

CCC: 'ccc';

CERTIFICATES: 'certificates';

CHANGE_LOG: 'change-log';

CHASSIS: 'chassis';

CIPHERS: 'ciphers';

CLASS: 'class';

CLASS_OF_SERVICE: 'class-of-service';

CLASSIFIERS: 'classifiers';

CLEAR: 'clear';

CLIENT: 'client';

CLIENT_ALIVE_COUNT_MAX: 'client-alive-count-max';

CLIENT_ALIVE_INTERVAL: 'client-alive-interval';

CLIENT_LIST: 'client-list' -> pushMode(M_Name);

CLIENT_LIST_NAME: 'client-list-name' -> pushMode(M_Name);

CLIENTS: 'clients';

CLUSTER: 'cluster';

CMD: 'cmd';
CODE_POINT: 'code-point' -> pushMode(M_Name);
CODE_POINT_ALIASES: 'code-point-aliases';
CODE_POINTS: 'code-points' -> pushMode(M_Name);
COLOR: 'color';

COLOR2: 'color2';

COMMIT: 'commit';

COMMUNICATION_PROHIBITED_BY_FILTERING: 'communication-prohibited-by-filtering';

COMMUNITY
:
  'community'
  {
    switch (lastTokenType()) {
      case FROM:
      case POLICY_OPTIONS:
      case SNMP:
        pushMode(M_Name);
        break;
      default:
        break;
    }
  }
;

COMMUNITY_COUNT: 'community-count';

COMPATIBLE: 'compatible';

COMPRESS_CONFIGURATION_FILES: 'compress-configuration-files';

CONDITION
:
   'condition' -> pushMode(M_Name)
;

CONDITIONAL_METRIC: 'conditional-metric';

CONFEDERATION: 'confederation';

CONNECTIONS: 'connections';

CONNECTION_LIMIT: 'connection-limit';

CONNECTIONS_LIMIT: 'connections-limit';

CONNECTION_MODE: 'connection-mode';

CONNECTIVITY_FAULT_MANAGEMENT: 'connectivity-fault-management';
CONSOLE: 'console';

CONTACT: 'contact';
COPY: 'copy';
COS_NEXT_HOP_MAP: 'cos-next-hop-map' -> pushMode(M_Name);

COUNT: 'count' -> pushMode(M_Name);

CREDIBILITY_PROTOCOL_PREFERENCE: 'credibility-protocol-preference';

CVSPSERVER: 'cvspserver';

CWR: 'cwr';

DAEMON: 'daemon';

DAMPING: 'damping';

DATABASE_REPLICATION: 'database-replication';
DEVICE_COUNT: 'device-count';
DESTINATION_HEADER: 'destination-header';

DESTINATION_THRESHOLD: 'destination-threshold';

DCBX: 'dcbx';

DDOS_PROTECTION: 'ddos-protection';

DEACTIVATE: 'deactivate';

DEAD_INTERVAL: 'dead-interval';

DEAD_PEER_DETECTION: 'dead-peer-detection';

DECAPSULATE: 'decapsulate';

DEFAULT: 'default';

DEFAULT_ACTION: 'default-action';

DEFAULT_ADDRESS_SELECTION: 'default-address-selection';

DEFAULT_GATEWAY: 'default-gateway';

DEFAULT_LSA: 'default-lsa';

DEFAULT_METRIC: 'default-metric';

DEFAULT_POLICY: 'default-policy';

DEFAULTS: 'defaults';
DELEGATE_PROCESSING: 'delegate-processing';
DELETE
:
  'delete'
  {
    if (lastTokenType() == COMMUNITY) {
      pushMode(M_Name);
    }
  }
;

DELETE_BINDING_ON_RENEGOTIATION: 'delete-binding-on-renegotiation';

DENY: 'deny';

DENY_ALL: 'deny-all';

DES_CBC: 'des-cbc';

DESCRIPTION
:
   'description' -> pushMode ( M_Description )
;

DESIGNATED_FORWARDER_ELECTION_HOLD_TIME: 'designated-forwarder-election-hold-time';

DESTINATION: 'destination';

DESTINATION_ADDRESS
:
  'destination-address'
   {
     if (lastTokenType() == MATCH) {
       pushMode(M_AddressSpecifier);
     }
   }
;

DESTINATION_ADDRESS_EXCLUDED: 'destination-address-excluded';

DESTINATION_ADDRESS_NAME: 'destination-address-name' -> pushMode(M_Name);

DESTINATION_HOST_PROHIBITED: 'destination-host-prohibited';

DESTINATION_HOST_UNKNOWN: 'destination-host-unknown';

DESTINATION_IP: 'destination-ip';

DESTINATION_IP_BASED: 'destination-ip-based';

DESTINATION_NAT: 'destination-nat';

DESTINATION_NETWORK_PROHIBITED: 'destination-network-prohibited';

DESTINATION_NETWORK_UNKNOWN: 'destination-network-unknown';

DESTINATION_PORT: 'destination-port' -> pushMode(M_Port);

DESTINATION_PORT_EXCEPT: 'destination-port-except' -> pushMode(M_Port);
DESTINATION_PORT_RANGE_OPTIMIZE: 'destination-port-range-optimize';

DESTINATION_PREFIX_LIST: 'destination-prefix-list' -> pushMode(M_Name);
DESTINATION_UDP_PORT: 'destination-udp-port';
DESTINATION_UNREACHABLE: 'destination-unreachable';

DF_BIT: 'df-bit';

DH_GROUP: 'dh-group';

DHCP: 'dhcp';

DHCP_LOCAL_SERVER: 'dhcp-local-server';

DHCP_RELAY: 'dhcp-relay';

DHCP_SECURITY: 'dhcp-security';

DIRECT: 'direct';

DISABLE: 'disable';

DISABLE_4BYTE_AS: 'disable-4byte-as';

DISCARD: 'discard';

DNS: 'dns';

DOMAIN: 'domain';

DOMAIN_NAME: 'domain-name' -> pushMode(M_Name);

DOMAIN_SEARCH: 'domain-search';
DOMAIN_TYPE: 'domain-type';
DROP_PATH_ATTRIBUTES: 'drop-path-attributes';

DROP_PROFILES: 'drop-profiles' -> pushMode(M_Name);

DSA_SIGNATURES: 'dsa-signatures';

DSCP: 'dscp' -> pushMode(M_Name);

DSLITE: 'dslite';

DSTOPTS: 'dstopts';

DTCP_ONLY: 'dtcp-only';

DUMP_ON_PANIC: 'dump-on-panic';

DUPLICATE_MAC_DETECTION: 'duplicate-mac-detection';

DVMRP: 'dvmrp';

DYNAMIC: 'dynamic';

DYNAMIC_DNS: 'dynamic-dns';

ECE: 'ece';

ECHO_REPLY: 'echo-reply';

ECHO_REQUEST: 'echo-request';

EGP: 'egp';

EGRESS: 'egress';

EIGHT02_3AD: '802.3ad' -> pushMode(M_Interface);

EKLOGIN: 'eklogin';

EKSHELL: 'ekshell';

ELIGIBLE: 'eligible';

ENABLE: 'enable';

ENCAPSULATION: 'encapsulation';

ENCRYPTED_PASSWORD: 'encrypted-password' -> pushMode(M_SecretString);

ENCRYPTION_ALGORITHM: 'encryption-algorithm';

ENFORCE_FIRST_AS: 'enforce-first-as';

ENGINE_ID: 'engine-id';

ENHANCED_HASH_KEY: 'enhanced-hash-key';

EQUAL_COST_PATHS: 'equal-cost-paths';

ESP: 'esp';

ESP_HEADER: 'ESP-header';

ESTABLISH_TUNNELS: 'establish-tunnels';

ETHER_OPTIONS: 'ether-options';

ETHER_TYPE: 'ether-type';
ETHERNET: 'ethernet';
ETHERNET_SWITCHING: 'ethernet-switching';

ETHERNET_SWITCHING_OPTIONS: 'ethernet-switching-options';

EVENT_OPTIONS: 'event-options';

EVPN: 'evpn';

EXACT: 'exact';

EXCEPT: 'except';

EXCLUDE: 'exclude';

EXCLUDE_NON_ELIGIBLE: 'exclude-non-eligible';

EXCLUDE_NON_FEASIBLE: 'exclude-non-feasible';

EXEC: 'exec';

EXP: 'exp';

EXPEDITED: 'expedited';

EXPLICIT_PRIORITY: 'explicit-priority';

EXPORT
:
  'export'
  {
    // set routing-options interface-routes family inet export
    if (secondToLastTokenType() != FAMILY) {
      pushMode(M_PolicyExpression);
    }
  }
;

EXPORT_RIB: 'export-rib' -> pushMode(M_Name);

EXPRESSION: 'expression';

EXTENDED_NEXTHOP_TUNNEL: 'extended-nexthop-tunnel';
EXTENDED_VNI_LIST: 'extended-vni-list' -> pushMode(M_ExtendedVniList);

EXTENSIBLE_SUBSCRIBER: 'extensible-subscriber';

EXTENSION_SERVICE: 'extension-service';

EXTERNAL: 'external';

EXTERNAL_INTERFACE
:
   'external-interface' -> pushMode ( M_Interface )
;

EXTERNAL_PREFERENCE: 'external-preference';

EXTERNAL_ROUTER_ID: 'external-router-id';

EXTENSION_HEADER: 'extension-header';

EXTENSIONS: 'extensions';

FABRIC: 'fabric';

FABRIC_OPTIONS: 'fabric-options';

FACILITY_OVERRIDE: 'facility-override';

FAIL_FILTER: 'fail-filter' -> pushMode(M_Name);

FAMILY: 'family';

FAST_INTERVAL: 'fast-interval';

FASTETHER_OPTIONS: 'fastether-options';

FILE: 'file';

FILTER: 'filter' -> pushMode(M_Filter);

FILTER_DUPLICATES: 'filter-duplicates';

FILTER_INTERFACES: 'filter-interfaces';
FILL_LEVEL: 'fill-level';
FIN_NO_ACK: 'fin-no-ack';

FINGER: 'finger';

FINGERPRINT_HASH: 'fingerprint-hash';

FIREWALL: 'firewall';

FIRST_FRAGMENT: 'first-fragment';

FLAP_PERIOD: 'period';

FLAPS: 'flaps';

FLEXIBLE_VLAN_TAGGING: 'flexible-vlan-tagging';

FLOOD: 'flood';

FLOW: 'flow';

FLOW_CONTROL: 'flow-control';

FLOW_GATE: 'flow-gate';

FLOW_SESSION: 'flow-session';

FORCE_UP: 'force-up';

FOREVER: 'forever';

FORWARD_SNOOPED_CLIENTS: 'forward-snooped-clients';

FORWARDING: 'forwarding';

FORWARDING_CLASS: 'forwarding-class' -> pushMode(M_Name);

FORWARDING_CLASS_ACCOUNTING: 'forwarding-class-accounting';

FORWARDING_CLASS_SET: 'forwarding-class-set' -> pushMode(M_Name);

FORWARDING_CLASSES: 'forwarding-classes';
FORWARDING_CONTEXT: 'forwarding-context' -> pushMode(M_Name);
FORWARDING_OPTIONS: 'forwarding-options';

FORWARDING_POLICY: 'forwarding-policy';

FORWARDING_TABLE: 'forwarding-table';

FRAGMENT: 'fragment';

FRAGMENT_HEADER: 'fragment-header';

FRAGMENTATION_MAPS: 'fragmentation-maps';

FRAGMENTATION_NEEDED: 'fragmentation-needed';

FRAGMENT_OFFSET: 'fragment-offset' -> pushMode(M_SubRange);

FRAGMENT_OFFSET_EXCEPT: 'fragment-offset-except' -> pushMode(M_SubRange);

FRAMING: 'framing';

FROM: 'from';

FROM_ZONE: 'from-zone' -> pushMode(M_Zone);

FTP: 'ftp';

FTP_DATA: 'ftp-data';

FULL_DUPLEX: 'full-duplex';

G: 'g';

GATEWAY: 'gateway' -> pushMode(M_Name);

GENERATE: 'generate';

GIGETHER_OPTIONS: 'gigether-options';

GLOBAL: 'global';

GRACEFUL_RESTART: 'graceful-restart';

GRE: 'gre';

GRE_4IN4: 'gre-4in4';

GRE_4IN6: 'gre-4in6';

GRE_6IN4: 'gre-6in4';

GRE_6IN6: 'gre-6in6';

GROUP: 'group' -> pushMode(M_Name);

GROUP_IKE_ID: 'group-ike-id';

GROUP1: 'group1';

GROUP14: 'group14';

GROUP15: 'group15';

GROUP16: 'group16';

GROUP19: 'group19';

GROUP2: 'group2';

GROUP20: 'group20';

GROUP24: 'group24';

GROUP5: 'group5';

GROUPS: 'groups' -> pushMode(M_Name);

HALF_LIFE: 'half-life';

HASH_KEY: 'hash-key';

HELLO_AUTHENTICATION_KEY: 'hello-authentication-key' -> pushMode(M_SecretString);

HELLO_AUTHENTICATION_TYPE: 'hello-authentication-type';

HELLO_INTERVAL: 'hello-interval';

HELLO_PADDING: 'hello-padding';

HELPERS: 'helpers';
HEXADECIMAL: 'hexadecimal';

HIGH: 'high';

HIP_HEADER: 'HIP-header';

HOME_ADDRESS_OPTION: 'home-address-option';

HOP_BY_HOP_HEADER: 'hop-by-hop-header';

HOP_LIMIT: 'hop-limit';

HOST_OUTBOUND_TRAFFIC: 'host-outbound-traffic';

HMAC_MD5_96: 'hmac-md5-96';

HMAC_SHA1: 'hmac-sha-1';

HMAC_SHA1_96: 'hmac-sha1-96';
HMAC_SHA_1_96: 'hmac-sha-1-96';
HMAC_SHA_256_128: 'hmac-sha-256-128';

HOLD_DOWN: 'hold-down';

HOLD_TIME: 'hold-time';

HOP_BY_HOP: 'hop-by-hop';

HOST: 'host' -> pushMode(M_Name);

HOST_INBOUND_TRAFFIC: 'host-inbound-traffic';

HOST_NAME: 'host-name' -> pushMode(M_Name);

HOST_PRECEDENCE_VIOLATION: 'host-precedence-violation';

HOST_UNREACHABLE: 'host-unreachable';

HOST_UNREACHABLE_FOR_TOS: 'host-unreachable-for-tos';

HOSTKEY_ALGORITHM: 'hostkey-algorithm';

HOSTNAME: 'hostname' -> pushMode(M_Name);

HTTP: 'http';

HTTPS: 'https';

ICCP: 'iccp';

ICMP: 'icmp';

ICMP_CODE: 'icmp-code' -> pushMode(M_IcmpCodeOrType);
ICMP_CODE_EXCEPT: 'icmp-code-except' -> pushMode(M_IcmpCodeOrType);

ICMP_TYPE: 'icmp-type' -> pushMode(M_IcmpCodeOrType);
ICMP_TYPE_EXCEPT: 'icmp-type-except' -> pushMode(M_IcmpCodeOrType);

ICMP6: 'icmp6';

ICMP6_CODE: 'icmp6-code';

ICMPV6_MALFORMED: 'icmpv6-malformed';

ICMP6_TYPE: 'icmp6-type';

ICMPV6: 'icmpv6';

IDENT: 'ident';

IDENT_RESET: 'ident-reset';

IDLE_TIMEOUT: 'idle-timeout';

IDS_OPTION: 'ids-option' -> pushMode(M_Name);

IF_ROUTE_EXISTS: 'if-route-exists';

IGMP: 'igmp';

IGMP_SNOOPING: 'igmp-snooping';

IGNORE: 'ignore';

IGNORE_ATTACHED_BIT: 'ignore-attached-bit';

IGNORE_L3_INCOMPLETES: 'ignore-l3-incompletes';

IGP: 'igp';

IGP_METRIC: 'igp-metric';

IGP_METRIC_THRESHOLD: 'igp-metric-threshold';

IKE: 'ike';

IKE_ESP_NAT: 'ike-esp-nat';

IKE_POLICY: 'ike-policy' -> pushMode(M_Name);

IKE_USER_TYPE: 'ike-user-type';

ILNP_NONCE_OPTION: 'ILNP-nonce-option';

IMAP: 'imap';

IMMEDIATELY: 'immediately';

IMPORT
:
  'import'
  {
    if (secondToLastTokenType() != FAMILY) {
      // set routing-options interface-routes family inet import
      pushMode(M_PolicyExpression);
    }
  }
;

IMPORT_POLICY: 'import-policy' -> pushMode(M_PolicyExpression);

IMPORT_RIB: 'import-rib' -> pushMode(M_Name);

IN_PLACE_LSP_BANDWIDTH_UPDATE: 'in-place-lsp-bandwidth-update';

INACTIVE: 'inactive';

INACTIVITY_TIMEOUT: 'inactivity-timeout';

INCLUDE_ALL: 'include-all';

INCLUDE_ANY: 'include-any';

INCLUDE_MP_NEXT_HOP: 'include-mp-next-hop';

INCOMPLETE: 'incomplete';

INDIRECT_NEXT_HOP: 'indirect-next-hop';

INDIRECT_NEXT_HOP_CHANGE_ACKNOWLEDGEMENTS: 'indirect-next-hop-change-acknowledgements';

INET
:
  'inet'
  {
    if (lastTokenType() == LOCAL_IDENTITY) {
      pushMode(M_Name);
    }
  }
;

INET6: 'inet6';

INET_MDT: 'inet-mdt';

INET_MVPN: 'inet-mvpn';

INET6_MVPN: 'inet6-mvpn';

INET_VPN: 'inet-vpn';

INET6_VPN: 'inet6-vpn';

INFO_REPLY: 'info-reply';

INFO_REQUEST: 'info-request';

INGRESS: 'ingress';

INGRESS_REPLICATION: 'ingress-replication';

INNER: 'inner';

INPUT: 'input' -> pushMode(M_Name);

INPUT_LIST: 'input-list' -> pushMode(M_Name);

INPUT_VLAN_MAP: 'input-vlan-map';

INSECURE: 'insecure';

INSERT: 'insert';

INSTALL: 'install';

INSTALL_NEXTHOP: 'install-nexthop';

INSTANCE: 'instance' -> pushMode(M_Name);

INSTANCE_IMPORT: 'instance-import' -> pushMode(M_Name);

INSTANCE_TYPE: 'instance-type';

INTERACTIVE_COMMANDS: 'interactive-commands';

INTERCONNECT_DEVICE: 'interconnect-device' -> pushMode(M_FabricDevice);

INTERFACE
:
   'interface' -> pushMode ( M_Interface )
;

INTERFACE_MODE: 'interface-mode';

INTERFACE_RANGE: 'interface-range' -> pushMode(M_Name);

INTERFACE_SET: 'interface-set' -> pushMode(M_InterfaceSet);

INTERFACE_SPECIFIC: 'interface-specific';

INTERFACE_SWITCH: 'interface-switch' -> pushMode(M_Name);

INTERFACE_TRANSMIT_STATISTICS: 'interface-transmit-statistics';

INTERFACES
:
  'interfaces'
  {
    if (lastTokenType() == CLASS_OF_SERVICE) {
      pushMode(M_InterfaceWildcard);
    } else {
      pushMode(M_Interface);
    }
  }
;

INTERFACE_ROUTES: 'interface-routes';

INTERFACE_TYPE: 'interface-type';

INTERNAL: 'internal';

INTERNET_OPTIONS: 'internet-options';
INTERPOLATE: 'interpolate';
INTERVAL: 'interval';
INVERT_MATCH: 'invert-match';

IP: 'ip';

IP_DESTINATION_ADDRESS: 'ip-destination-address';

IP_HEADER_BAD: 'ip-header-bad';

IP_IN_UDP: 'ip-in-udp';

IP_OPTIONS: 'ip-options';

IP_PROTOCOL: 'ip-protocol';

IP_SOURCE_ADDRESS: 'ip-source-address';

IP_SWEEP: 'ip-sweep';

IPIP: 'ipip';

IPIP_4IN4: 'ipip-4in4';

IPIP_4IN6: 'ipip-4in6';

IPIP_6IN4: 'ipip-6in4';

IPIP_6IN6: 'ipip-6in6';

IPIP_6OVER4: 'ipip-6over4';

IPIP_6TO4RELAY: 'ipip-6to4relay';

IPSEC: 'ipsec';

IPSEC_POLICY: 'ipsec-policy' -> pushMode(M_Name);

IPSEC_VPN: 'ipsec-vpn' -> pushMode(M_Name);

IPV6: 'ipv6';

IPV6_EXTENSION_HEADER: 'ipv6-extension-header';

IPV6_EXTENSION_HEADER_LIMIT: 'ipv6-extension-header-limit';

IPV6_MALFORMED_HEADER: 'ipv6-malformed-header';

IS_FRAGMENT: 'is-fragment';

ISATAP: 'isatap';

ISIS: 'isis';

ISIS_ENHANCED: 'isis-enhanced';

ISO
:
   'iso' -> pushMode ( M_ISO )
;

ISO_VPN: 'iso-vpn';

JUMBO_PAYLOAD_OPTION: 'jumbo-payload-option';

JUNOS_AOL: 'junos-aol';

JUNOS_BGP: 'junos-bgp';

JUNOS_BIFF: 'junos-biff';

JUNOS_BOOTPC: 'junos-bootpc';

JUNOS_BOOTPS: 'junos-bootps';

JUNOS_CHARGEN: 'junos-chargen';

JUNOS_CIFS: 'junos-cifs';

JUNOS_CVSPSERVER: 'junos-cvspserver';

JUNOS_DHCP_CLIENT: 'junos-dhcp-client';

JUNOS_DHCP_RELAY: 'junos-dhcp-relay';

JUNOS_DHCP_SERVER: 'junos-dhcp-server';

JUNOS_DISCARD: 'junos-discard';

JUNOS_DNS_TCP: 'junos-dns-tcp';

JUNOS_DNS_UDP: 'junos-dns-udp';

JUNOS_ECHO: 'junos-echo';

JUNOS_FINGER: 'junos-finger';

JUNOS_FTP: 'junos-ftp';

JUNOS_FTP_DATA: 'junos-ftp-data';

JUNOS_GNUTELLA: 'junos-gnutella';

JUNOS_GOPHER: 'junos-gopher';

JUNOS_GPRS_GTP_C: 'junos-gprs-gtp-c';


JUNOS_GPRS_GTP_U: 'junos-gprs-gtp-u';


JUNOS_GPRS_GTP_V0: 'junos-gprs-gtp-v0';

JUNOS_GPRS_SCTP: 'junos-gprs-sctp';

JUNOS_GRE: 'junos-gre';

JUNOS_GTP: 'junos-gtp';

JUNOS_H323: 'junos-h323';

JUNOS_HOST: 'junos-host';

JUNOS_HTTP: 'junos-http';

JUNOS_HTTP_EXT: 'junos-http-ext';

JUNOS_HTTPS: 'junos-https';

JUNOS_ICMP_ALL: 'junos-icmp-all';

JUNOS_ICMP_PING: 'junos-icmp-ping';

JUNOS_ICMP6_ALL: 'junos-icmp6-all';

JUNOS_ICMP6_DST_UNREACH_ADDR: 'junos-icmp6-dst-unreach-addr';

JUNOS_ICMP6_DST_UNREACH_ADMIN: 'junos-icmp6-dst-unreach-admin';

JUNOS_ICMP6_DST_UNREACH_BEYOND: 'junos-icmp6-dst-unreach-beyond';

JUNOS_ICMP6_DST_UNREACH_PORT: 'junos-icmp6-dst-unreach-port';

JUNOS_ICMP6_DST_UNREACH_ROUTE: 'junos-icmp6-dst-unreach-route';

JUNOS_ICMP6_ECHO_REPLY: 'junos-icmp6-echo-reply';

JUNOS_ICMP6_ECHO_REQUEST: 'junos-icmp6-echo-request';

JUNOS_ICMP6_PACKET_TOO_BIG: 'junos-icmp6-packet-too-big';

JUNOS_ICMP6_PARAM_PROB_HEADER: 'junos-icmp6-param-prob-header';

JUNOS_ICMP6_PARAM_PROB_NEXTHDR: 'junos-icmp6-param-prob-nexthdr';

JUNOS_ICMP6_PARAM_PROB_OPTION: 'junos-icmp6-param-prob-option';

JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY: 'junos-icmp6-time-exceed-reassembly';

JUNOS_ICMP6_TIME_EXCEED_TRANSIT: 'junos-icmp6-time-exceed-transit';

JUNOS_IDENT: 'junos-ident';

JUNOS_IKE: 'junos-ike';

JUNOS_IKE_NAT: 'junos-ike-nat';

JUNOS_IMAP: 'junos-imap';

JUNOS_IMAPS: 'junos-imaps';

JUNOS_INTERNET_LOCATOR_SERVICE: 'junos-internet-locator-service';

JUNOS_IRC: 'junos-irc';

JUNOS_L2TP: 'junos-l2tp';

JUNOS_LDAP: 'junos-ldap';

JUNOS_LDP_TCP: 'junos-ldp-tcp';

JUNOS_LDP_UDP: 'junos-ldp-udp';

JUNOS_LPR: 'junos-lpr';

JUNOS_MAIL: 'junos-mail';

JUNOS_MGCP: 'junos-mgcp';

JUNOS_MGCP_CA: 'junos-mgcp-ca';

JUNOS_MGCP_UA: 'junos-mgcp-ua';

JUNOS_MS_RPC: 'junos-ms-rpc';

JUNOS_MS_RPC_ANY: 'junos-ms-rpc-any';

JUNOS_MS_RPC_EPM: 'junos-ms-rpc-epm';

JUNOS_MS_RPC_IIS_COM: 'junos-ms-rpc-iis-com';

JUNOS_MS_RPC_IIS_COM_1: 'junos-ms-rpc-iis-com-1';

JUNOS_MS_RPC_IIS_COM_ADMINBASE: 'junos-ms-rpc-iis-com-adminbase';

JUNOS_MS_RPC_MSEXCHANGE: 'junos-ms-rpc-msexchange';

JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP: 'junos-ms-rpc-msexchange-directory-nsp';

JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR: 'junos-ms-rpc-msexchange-directory-rfr';

JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE: 'junos-ms-rpc-msexchange-info-store';

JUNOS_MS_RPC_TCP: 'junos-ms-rpc-tcp';

JUNOS_MS_RPC_UDP: 'junos-ms-rpc-udp';

JUNOS_MS_RPC_UUID_ANY_TCP: 'junos-ms-rpc-uuid-any-tcp';

JUNOS_MS_RPC_UUID_ANY_UDP: 'junos-ms-rpc-uuid-any-udp';

JUNOS_MS_RPC_WMIC: 'junos-ms-rpc-wmic';

JUNOS_MS_RPC_WMIC_ADMIN: 'junos-ms-rpc-wmic-admin';

JUNOS_MS_RPC_WMIC_ADMIN2: 'junos-ms-rpc-wmic-admin2';

JUNOS_MS_RPC_WMIC_MGMT: 'junos-ms-rpc-wmic-mgmt';

JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT: 'junos-ms-rpc-wmic-webm-callresult';

JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT: 'junos-ms-rpc-wmic-webm-classobject';

JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN: 'junos-ms-rpc-wmic-webm-level1login';

JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID: 'junos-ms-rpc-wmic-webm-login-clientid';

JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER: 'junos-ms-rpc-wmic-webm-login-helper';

JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK: 'junos-ms-rpc-wmic-webm-objectsink';

JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES: 'junos-ms-rpc-wmic-webm-refreshing-services';

JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER: 'junos-ms-rpc-wmic-webm-remote-refresher';

JUNOS_MS_RPC_WMIC_WEBM_SERVICES: 'junos-ms-rpc-wmic-webm-services';

JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN: 'junos-ms-rpc-wmic-webm-shutdown';

JUNOS_MS_SQL: 'junos-ms-sql';

JUNOS_MSN: 'junos-msn';

JUNOS_NBDS: 'junos-nbds';

JUNOS_NBNAME: 'junos-nbname';

JUNOS_NETBIOS_SESSION: 'junos-netbios-session';

JUNOS_NFS: 'junos-nfs';

JUNOS_NFSD_TCP: 'junos-nfsd-tcp';

JUNOS_NFSD_UDP: 'junos-nfsd-udp';

JUNOS_NNTP: 'junos-nntp';

JUNOS_NS_GLOBAL: 'junos-ns-global';

JUNOS_NS_GLOBAL_PRO: 'junos-ns-global-pro';

JUNOS_NSM: 'junos-nsm';

JUNOS_NTALK: 'junos-ntalk';

JUNOS_NTP: 'junos-ntp';

JUNOS_OSPF: 'junos-ospf';

JUNOS_PC_ANYWHERE: 'junos-pc-anywhere';

JUNOS_PERSISTENT_NAT: 'junos-persistent-nat';

JUNOS_PING: 'junos-ping';

JUNOS_PINGV6: 'junos-pingv6';

JUNOS_POP3: 'junos-pop3';

JUNOS_PPTP: 'junos-pptp';

JUNOS_PRINTER: 'junos-printer';

JUNOS_R2CP: 'junos-r2cp';

JUNOS_RADACCT: 'junos-radacct';

JUNOS_RADIUS: 'junos-radius';

JUNOS_RDP: 'junos-rdp';

JUNOS_REALAUDIO: 'junos-realaudio';

JUNOS_RIP: 'junos-rip';

JUNOS_ROUTING_INBOUND: 'junos-routing-inbound';

JUNOS_RSH: 'junos-rsh';

JUNOS_RTSP: 'junos-rtsp';

JUNOS_SCCP: 'junos-sccp';

JUNOS_SCTP_ANY: 'junos-sctp-any';

JUNOS_SIP: 'junos-sip';

JUNOS_SMB: 'junos-smb';

JUNOS_SMB_SESSION: 'junos-smb-session';

JUNOS_SMTP: 'junos-smtp';

JUNOS_SMTPS: 'junos-smtps';

JUNOS_SNMP_AGENTX: 'junos-snmp-agentx';

JUNOS_SNPP: 'junos-snpp';

JUNOS_SQL_MONITOR: 'junos-sql-monitor';

JUNOS_SQLNET_V1: 'junos-sqlnet-v1';

JUNOS_SQLNET_V2: 'junos-sqlnet-v2';

JUNOS_SSH: 'junos-ssh';

JUNOS_STUN: 'junos-stun';

JUNOS_STUN_TCP: 'junos-stun-tcp';

JUNOS_STUN_UDP: 'junos-stun-udp';

JUNOS_SUN_RPC: 'junos-sun-rpc';

JUNOS_SUN_RPC_ANY: 'junos-sun-rpc-any';

JUNOS_SUN_RPC_ANY_TCP: 'junos-sun-rpc-any-tcp';

JUNOS_SUN_RPC_ANY_UDP: 'junos-sun-rpc-any-udp';

JUNOS_SUN_RPC_MOUNTD: 'junos-sun-rpc-mountd';

JUNOS_SUN_RPC_MOUNTD_TCP: 'junos-sun-rpc-mountd-tcp';

JUNOS_SUN_RPC_MOUNTD_UDP: 'junos-sun-rpc-mountd-udp';

JUNOS_SUN_RPC_NFS: 'junos-sun-rpc-nfs';

JUNOS_SUN_RPC_NFS_ACCESS: 'junos-sun-rpc-nfs-access';

JUNOS_SUN_RPC_NFS_TCP: 'junos-sun-rpc-nfs-tcp';

JUNOS_SUN_RPC_NFS_UDP: 'junos-sun-rpc-nfs-udp';

JUNOS_SUN_RPC_NLOCKMGR: 'junos-sun-rpc-nlockmgr';

JUNOS_SUN_RPC_NLOCKMGR_TCP: 'junos-sun-rpc-nlockmgr-tcp';

JUNOS_SUN_RPC_NLOCKMGR_UDP: 'junos-sun-rpc-nlockmgr-udp';

JUNOS_SUN_RPC_PORTMAP: 'junos-sun-rpc-portmap';

JUNOS_SUN_RPC_PORTMAP_TCP: 'junos-sun-rpc-portmap-tcp';

JUNOS_SUN_RPC_PORTMAP_UDP: 'junos-sun-rpc-portmap-udp';

JUNOS_SUN_RPC_RQUOTAD: 'junos-sun-rpc-rquotad';

JUNOS_SUN_RPC_RQUOTAD_TCP: 'junos-sun-rpc-rquotad-tcp';

JUNOS_SUN_RPC_RQUOTAD_UDP: 'junos-sun-rpc-rquotad-udp';

JUNOS_SUN_RPC_RUSERD: 'junos-sun-rpc-ruserd';

JUNOS_SUN_RPC_RUSERD_TCP: 'junos-sun-rpc-ruserd-tcp';

JUNOS_SUN_RPC_RUSERD_UDP: 'junos-sun-rpc-ruserd-udp';

JUNOS_SUN_RPC_SADMIND: 'junos-sun-rpc-sadmind';

JUNOS_SUN_RPC_SADMIND_TCP: 'junos-sun-rpc-sadmind-tcp';

JUNOS_SUN_RPC_SADMIND_UDP: 'junos-sun-rpc-sadmind-udp';

JUNOS_SUN_RPC_SPRAYD: 'junos-sun-rpc-sprayd';

JUNOS_SUN_RPC_SPRAYD_TCP: 'junos-sun-rpc-sprayd-tcp';

JUNOS_SUN_RPC_SPRAYD_UDP: 'junos-sun-rpc-sprayd-udp';

JUNOS_SUN_RPC_STATUS: 'junos-sun-rpc-status';

JUNOS_SUN_RPC_STATUS_TCP: 'junos-sun-rpc-status-tcp';

JUNOS_SUN_RPC_STATUS_UDP: 'junos-sun-rpc-status-udp';

JUNOS_SUN_RPC_TCP: 'junos-sun-rpc-tcp';

JUNOS_SUN_RPC_UDP: 'junos-sun-rpc-udp';

JUNOS_SUN_RPC_WALLD: 'junos-sun-rpc-walld';

JUNOS_SUN_RPC_WALLD_TCP: 'junos-sun-rpc-walld-tcp';

JUNOS_SUN_RPC_WALLD_UDP: 'junos-sun-rpc-walld-udp';

JUNOS_SUN_RPC_YPBIND: 'junos-sun-rpc-ypbind';

JUNOS_SUN_RPC_YPBIND_TCP: 'junos-sun-rpc-ypbind-tcp';

JUNOS_SUN_RPC_YPBIND_UDP: 'junos-sun-rpc-ypbind-udp';

JUNOS_SUN_RPC_YPSERV: 'junos-sun-rpc-ypserv';

JUNOS_SUN_RPC_YPSERV_TCP: 'junos-sun-rpc-ypserv-tcp';

JUNOS_SUN_RPC_YPSERV_UDP: 'junos-sun-rpc-ypserv-udp';

JUNOS_SYSLOG: 'junos-syslog';

JUNOS_TACACS: 'junos-tacacs';

JUNOS_TACACS_DS: 'junos-tacacs-ds';

JUNOS_TALK: 'junos-talk';

JUNOS_TCP_ANY: 'junos-tcp-any';

JUNOS_TELNET: 'junos-telnet';

JUNOS_TFTP: 'junos-tftp';

JUNOS_UDP_ANY: 'junos-udp-any';

JUNOS_UUCP: 'junos-uucp';

JUNOS_VDO_LIVE: 'junos-vdo-live';

JUNOS_VNC: 'junos-vnc';

JUNOS_WAIS: 'junos-wais';

JUNOS_WHO: 'junos-who';

JUNOS_WHOIS: 'junos-whois';

JUNOS_WINFRAME: 'junos-winframe';

JUNOS_WXCONTROL: 'junos-wxcontrol';

JUNOS_X_WINDOWS: 'junos-x-windows';

JUNOS_XNM_CLEAR_TEXT: 'junos-xnm-clear-text';

JUNOS_XNM_SSL: 'junos-xnm-ssl';

JUNOS_YMSG: 'junos-ymsg';

K: 'k';

KEEP: 'keep';

KERBEROS_SEC: 'kerberos-sec';

KERNEL: 'kernel';

KEY
:
  'key'
  {
    switch(secondToLastTokenType()) {
      case KEY_CHAIN:
        // set security authentication-key-chains key-chain foo key 0 ...
        pushMode(M_Name);
        break;
      case SERVER:
        // set system ntp server 2.3.4.7 key 33333
        pushMode(M_SecretString);
        break;
      default:
        break;
    }
  }
;

KEYS: 'keys';

KEY_CHAIN: 'key-chain' -> pushMode(M_Name);

KEY_EXCHANGE: 'key-exchange';

KLOGIN: 'klogin';

KPASSWD: 'kpasswd';

KRB_PROP: 'krb-prop';

KRBUPDATE: 'krbupdate';

KSHELL: 'kshell';

L: 'L';

L2CIRCUIT: 'l2circuit';

L2VPN: 'l2vpn';

L2_INTERFACE
:
  'l2-interface' -> pushMode(M_Interface)
;

L2_LEARNING: 'l2-learning';

L3VPN: 'l3vpn';

L3VPN_INET6: 'l3vpn-inet6';

L3_INTERFACE
:
   'l3-interface' -> pushMode(M_Interface)
;

LABEL_SWITCHED_PATH: 'label-switched-path' -> pushMode(M_Name);

LABELED_UNICAST: 'labeled-unicast';

LACP: 'lacp';

LAN: 'lan';

LAND: 'land';

LARGE: 'large';

LAYER2_CONTROL: 'layer2-control';

LDP_SYNCHRONIZATION: 'ldp-synchronization';

LICENSE: 'license';

LINE_IDENTIFICATION_OPTION: 'line-identification-option';

LINK_MODE: 'link-mode';
LINK_SPEED: 'link-speed';
LDAP: 'ldap';

LDP: 'ldp';

LEARN_VLAN_1P_PRIORITY: 'learn-vlan-1p-priority';

LEVEL: 'level';

LIFETIME_KILOBYTES: 'lifetime-kilobytes';

LIFETIME_SECONDS: 'lifetime-seconds';

LIMIT_SESSION: 'limit-session';

LINK_PROTECTION: 'link-protection';

LLDP: 'lldp';

LLDP_MED: 'lldp-med';

LOAD_BALANCE: 'load-balance';

LOC_RIB: 'loc-rib';

LOCAL
:
  'local'
  {
    if (lastTokenType() == CERTIFICATES) {
      pushMode(M_CertificatesLocal);
    }
  }
;

LOCAL_ADDRESS: 'local-address';

LOCAL_AS: 'local-as' -> pushMode(M_BgpAsn);

LOCAL_IDENTITY: 'local-identity';

LOCAL_PORT: 'local-port';

LOCAL_PREFERENCE: 'local-preference';

LOCATION: 'location';

LOG: 'log';

LOG_OUT_ON_DISCONNECT: 'log-out-on-disconnect';

LOG_PREFIX: 'log-prefix';

LOG_UPDOWN: 'log-updown';

LOGICAL_SYSTEM: 'logical-system' -> pushMode(M_Name);

LOGICAL_SYSTEMS: 'logical-systems' -> pushMode(M_Name);

LOGIN: 'login';

LONGER: 'longer';

LOOPBACK: 'loopback';

LOOPS: 'loops';

LOOSE_SOURCE_ROUTE: 'loose-source-route';

LOOSE_SOURCE_ROUTE_OPTION: 'loose-source-route-option';

LOSS_PRIORITY: 'loss-priority';

LOSS_PRIORITY_MAPS: 'loss-priority-maps';

LOW: 'low';

LSP: 'lsp';

LSP_EQUAL_COST: 'lsp-equal-cost';

LSP_INTERVAL: 'lsp-interval';

LSP_LIFETIME: 'lsp-lifetime';

LSP_TELEMETRY: 'lsp-telemetry';

LSPING: 'lsping';

M: 'm';

MAC
:
   'mac' -> pushMode ( M_MacAddress )
;

MACS: 'macs';

MAIN: 'main';
MAINTENANCE_DOMAIN: 'maintenance-domain' -> pushMode(M_Name);
MAPPED_PORT: 'mapped-port';

MARTIANS: 'martians';

MASK_REPLY: 'mask-reply';

MASK_REQUEST: 'mask-request';

MASTER_ONLY: 'master-only';

MATCH: 'match';

MAX_CONFIGURATIONS_ON_FLASH: 'max-configurations-on-flash';

MAX_CONFIGURATION_ROLLBACKS: 'max-configuration-rollbacks';

MAX_PRE_AUTHENTICATION_PACKETS: 'max-pre-authentication-packets';

MAX_SESSION_NUMBER: 'max-session-number';

MAX_SESSIONS: 'max-sessions';
MAX_SESSIONS_PER_CONNECTION: 'max-sessions-per-connection';

MAX_SUPPRESS: 'max-suppress';

MAXIMUM: 'maximum';

MAXIMUM_BANDWIDTH: 'maximum-bandwidth';

MAXIMUM_LABELS: 'maximum-labels';

MAXIMUM_PREFIXES: 'maximum-prefixes';

MD5: 'md5';

MEDIUM: 'medium';

MEDIUM_HIGH: 'medium-high';

MEDIUM_LOW: 'medium-low';

MEMBER
:
  'member'
  {
    if (secondToLastTokenType() == INTERFACE_RANGE) {
      pushMode(M_Interface);
    }
  }
;

MEMBER_RANGE
:
   'member-range' -> pushMode(M_MemberRange)
;

MEMBERS
:
  'members'
  {
    if (secondToLastTokenType() == COMMUNITY) {
      pushMode(M_Members);
    } else if (lastTokenType() == VLAN) {
      pushMode(M_VlanMembers);
    }
  }
;

MEMBERSHIP_REPORT: 'membership-report';

MEMBERSHIP_QUERY: 'membership-query';

METRIC: 'metric';

METRIC2: 'metric2';

METRIC_OUT: 'metric-out';

METRIC_TYPE
:
   'metric-type' -> pushMode ( M_MetricType )
;

MGCP_CA: 'mgcp-ca';

MGCP_UA: 'mgcp-ua';

MINIMUM_BANDWIDTH: 'minimum-bandwidth';

MINIMUM_INTERVAL: 'minimum-interval';

MS_RPC: 'ms-rpc';

MLD: 'mld';

MOBILEIP_AGENT: 'mobileip-agent';

MOBILIP_MN: 'mobilip-mn';

MOBILITY_HEADER: 'mobility-header';

MODE: 'mode';

MPLS: 'mpls';

MSDP: 'msdp';

MSTP: 'mstp';

MTU: 'mtu';

MTU_DISCOVERY: 'mtu-discovery';

MULTI_CHASSIS: 'multi-chassis';

MULTICAST: 'multicast';

MULTICAST_MAC
:
   'multicast-mac' -> pushMode ( M_MacAddress )
;

MULTICAST_MODE: 'multicast-mode';

MULTIHOP: 'multihop';

MULTIPATH: 'multipath';

MULTIPATH_RESOLVE: 'multipath-resolve';

MULTIPLE_AS: 'multiple-as';

MULTIPLIER: 'multiplier';

MULTISERVICE_OPTIONS: 'multiservice-options';

MVPN: 'mvpn';

NAME_LITERALLY: 'name' -> pushMode(M_Name);

NAME_RESOLUTION: 'name-resolution';

NAME_SERVER: 'name-server';

NAT: 'nat';

NATIVE_VLAN_ID: 'native-vlan-id';

NBMA: 'nbma';

NEAREST: 'nearest';

NEIGHBOR: 'neighbor';

NEIGHBOR_ADVERTISEMENT: 'neighbor-advertisement';

NEIGHBOR_DISCOVERY: 'neighbor-discovery';

NEIGHBOR_SOLICIT: 'neighbor-solicit';

NETBIOS_DGM: 'netbios-dgm';

NETBIOS_NS: 'netbios-ns';

NETBIOS_SSN: 'netbios-ssn';

NETCONF: 'netconf';

NETWORK_DOMAIN: 'network-domain';

NETWORK_SUMMARY_EXPORT: 'network-summary-export';

NETWORK_UNREACHABLE_FOR_TOS: 'network-unreachable-for-tos';

NETWORK_UNREACHABLE: 'network-unreachable';

NEVER: 'never';

NEXT: 'next';

NEXT_HEADER: 'next-header';

NEXT_HOP
:
   'next-hop'
   {
     if (lastTokenType() != THEN) {
       pushMode(M_Interface);
     }
   }
;

NEXT_IP: 'next-ip';

NEXT_IP6: 'next-ip6';

NEXT_TABLE: 'next-table' -> pushMode(M_Name);

NFSD: 'nfsd';

NHRP: 'nhrp';

NNTP: 'nntp';

NTALK: 'ntalk';

NO_ACTIVE_BACKBONE: 'no-active-backbone';

NO_ADJACENCY_DOWN_NOTIFICATION: 'no-adjacency-down-notification';

NO_ADVERTISE: 'no-advertise';

NO_ANTI_REPLAY: 'no-anti-replay';

NO_ARP: 'no-arp';
NO_AUTO_NEGOTIATION: 'no-auto-negotiation';
NO_CLIENT_REFLECT: 'no-client-reflect';
NO_DECREMENT_TTL: 'no-decrement-ttl';
NO_ECMP_FAST_REROUTE: 'no-ecmp-fast-reroute';
NO_EXPORT: 'no-export';
NO_EXPORT_SUBCONFED: 'no-export-subconfed';
NO_FLOW_CONTROL: 'no-flow-control';
NO_GATEWAY_COMMUNITY: 'no-gateway-community';
NO_INSTALL: 'no-install';
NO_IPV4_ROUTING: 'no-ipv4-routing';
NO_NAT_TRAVERSAL: 'no-nat-traversal';


NO_NEIGHBOR_DOWN_NOTIFICATION: 'no-neighbor-down-notification';
NO_NEIGHBOR_LEARN: 'no-neighbor-learn';

NO_NEXT_HEADER: 'no-next-header';

NO_NEXTHOP_CHANGE: 'no-nexthop-change';

NO_PASSWORDS: 'no-passwords';

NO_PEER_LOOP_CHECK: 'no-peer-loop-check';

NO_PING_RECORD_ROUTE: 'no-ping-record-route';

NO_PING_TIME_STAMP: 'no-ping-time-stamp';

NO_PREEMPT: 'no-preempt';

NO_PREPEND_GLOBAL_AS: 'no-prepend-global-as';

NO_READVERTISE: 'no-readvertise';

NO_REDIRECTS: 'no-redirects';

NO_REDIRECTS_IPV6: 'no-redirects-ipv6';

NO_RESOLVE: 'no-resolve';

NO_RETAIN: 'no-retain';

NO_RFC_1583: 'no-rfc-1583';
NO_SELF_PING: 'no-self-ping';

NO_SUMMARIES: 'no-summaries';

NO_TCP_FORWARDING: 'no-tcp-forwarding';

NO_TRANSLATION: 'no-translation';

NO_TRAPS: 'no-traps';

NODE_DEVICE: 'node-device' -> pushMode(M_FabricDevice);

NODE_GROUP: 'node-group' -> pushMode(M_Name);

NODE_LINK_PROTECTION: 'node-link-protection';
NONE: 'none';
NONSTOP_ROUTING: 'nonstop-routing';
NON_STRICT_PRIORITY_SCHEDULING: 'non-strict-priority-scheduling';
NOTIFICATION_RIB: 'notification-rib' -> pushMode(M_Name);

NSSA: 'nssa';

NTP: 'ntp';

OAM: 'oam';

OFF: 'off';

OFFSET: 'offset';
OPTIMIZE_HOLD_DEAD_DELAY: 'optimize-hold-dead-delay';
OPTIMIZED: 'optimized';
OPTIONS: 'options';
ORIGIN: 'origin';
ORHIGHER: 'orhigher';
ORLONGER: 'orlonger';
ORLOWER: 'orlower';
OSPF: 'ospf';
OSPF3: 'ospf3';
OUT_DELAY: 'out-delay';
OUTBOUND_SSH: 'outbound-ssh';
OUTER: 'outer';
OUTPUT: 'output' -> pushMode(M_Name);
OUTPUT_LIST: 'output-list' -> pushMode(M_Name);
OUTPUT_QUEUE_PRIORITY: 'output-queue-priority';
OUTPUT_TRAFFIC_CONTROL_PROFILE: 'output-traffic-control-profile' -> pushMode(M_Name);
OUTPUT_VLAN_MAP: 'output-vlan-map';
OVERLAY_ECMP: 'overlay-ecmp';
OVERLOAD: 'overload';
OVERRIDE_METRIC: 'override-metric';
OVERRIDES: 'overrides';
P2MP: 'p2mp';
P2MP_OVER_LAN: 'p2mp-over-lan';
P2P: 'p2p';
PACKET_LENGTH: 'packet-length' -> pushMode(M_SubRange);
PACKET_LENGTH_EXCEPT: 'packet-length-except' -> pushMode(M_SubRange);

PACKET_TOO_BIG: 'packet-too-big';

PARAMETER_PROBLEM: 'parameter-problem';

PASSIVE: 'passive';

PASSWORD: 'password';

PATH: 'path' -> pushMode(M_AsPathExpr);

PATH_COUNT: 'path-count';

PATH_SELECTION: 'path-selection';
PATH_SELECTION_MODE: 'path-selection-mode';
PAYLOAD_PROTOCOL: 'payload-protocol';

PEER_ADDRESS: 'peer-address';

PEER_AS: 'peer-as' -> pushMode(M_BgpAsn);

PEER_UNIT: 'peer-unit';

PER_PACKET: 'per-packet';

PER_UNIT_SCHEDULER: 'per-unit-scheduler';

PERFECT_FORWARD_SECRECY: 'perfect-forward-secrecy';

PERMIT: 'permit';

PERMIT_ALL: 'permit-all';

PERSISTENT_NAT: 'persistent-nat';

PGM: 'pgm';

PIM: 'pim';

PING: 'ping';

PING_DEATH: 'ping-death';

POE: 'poe';

POINT_TO_POINT: 'point-to-point';

POLICER: 'policer';

POLICIES: 'policies';

POLICY
:
  'policy'
  {
    switch(lastTokenType()) {
      case GLOBAL:
      case IKE:
      case IPSEC:
        pushMode(M_Name);
        break;
      default:
        switch(secondToLastTokenType()) {
          case FROM_ZONE:
          case SECURITY_PROFILE:
          case TO_ZONE:
            pushMode(M_Name);
            break;
          default:
            pushMode(M_PolicyExpression);
            break;
        }
        break;
    }
  }
;

POLICY_OPTIONS: 'policy-options';

POLICY_STATEMENT: 'policy-statement' -> pushMode(M_Name);

POLL_INTERVAL: 'poll-interval';

POOL: 'pool' -> pushMode(M_Name);

POOL_DEFAULT_PORT_RANGE: 'pool-default-port-range';

POOL_UTILIZATION_ALARM: 'pool-utilization-alarm';

POP: 'pop';

POP_POP: 'pop-pop';

POP_SWAP: 'pop-swap';

POP3: 'pop3';

PORT: 'port' -> pushMode(M_Port);
PORT_EXCEPT: 'port-except' -> pushMode(M_Port);

PORT_MIRROR: 'port-mirror';

PORT_MIRRORING: 'port-mirroring';

PORT_MODE: 'port-mode';

PORT_OVERLOADING: 'port-overloading';

PORT_OVERLOADING_FACTOR: 'port-overloading-factor';

PORT_RANDOMIZATION: 'port-randomization';

PORT_SCAN: 'port-scan';

PORT_UNREACHABLE: 'port-unreachable';

PORTS: 'ports';

POST_POLICY: 'post-policy';

PPM: 'ppm';

PPTP: 'pptp';

PRE_POLICY: 'pre-policy';

PRE_SHARED_KEY: 'pre-shared-key';

PRE_SHARED_KEYS: 'pre-shared-keys';

PRECEDENCE: 'precedence';

PRECEDENCE_CUTOFF_IN_EFFECT: 'precedence-cutoff-in-effect';

PRECISION_TIMERS: 'precision-timers';

PREEMPT: 'preempt';

PREFER: 'prefer';

PREFERENCE: 'preference';

PREFERRED: 'preferred';

PREFIX: 'prefix';

PREFIX_NAME: 'prefix-name' -> pushMode(M_PrefixName);

PREFIX_EXPORT_LIMIT: 'prefix-export-limit';

PREFIX_LENGTH_RANGE: 'prefix-length-range' -> pushMode(M_PrefixLengthRange);

PREFIX_LIMIT: 'prefix-limit';

PREFIX_LIST: 'prefix-list' -> pushMode(M_Name);

PREFIX_LIST_FILTER: 'prefix-list-filter' -> pushMode(M_Name);

PREFIX_POLICY: 'prefix-policy' -> pushMode(M_Name);

PRIMARY: 'primary';

PRINTER: 'printer';

PRIORITY: 'priority';

PRIORITY_COST: 'priority-cost';

PRIVATE: 'private';

PROBE_IDLE_TUNNEL: 'probe-idle-tunnel';
PROCESSES: 'processes';

PROPOSAL: 'proposal' -> pushMode(M_Name);

PROPOSAL_SET: 'proposal-set';

PROPOSALS: 'proposals' -> pushMode(M_NameList);

PROTECT: 'protect';

PROTOCOL: 'protocol';

PROTOCOL_UNREACHABLE: 'protocol-unreachable';

PROTOCOL_VERSION: 'protocol-version';

PROTOCOLS: 'protocols';

PROVIDER_TUNNEL: 'provider-tunnel';

PROXY_ARP: 'proxy-arp';

PROXY_IDENTITY: 'proxy-identity';

PROXY_MACIP_ADVERTISEMENT: 'proxy-macip-advertisement';

PSH: 'psh';

PUSH: 'push';

PUSH_PUSH: 'push-push';

Q931: 'q931';

QUALIFIED_NEXT_HOP
:
   'qualified-next-hop' -> pushMode(M_Interface)
;

QUEUE: 'queue' -> pushMode(M_Queue);
QUICK_START_OPTION: 'quick-start-option';

R2CP: 'r2cp';

RADACCT: 'radacct';

RADIUS: 'radius';

RADIUS_OPTIONS: 'radius-options';

RADIUS_SERVER: 'radius-server';

RANGE: 'range';

RANGE_ADDRESS: 'range-address';

RANDOM: 'random';

RAS: 'ras';

RATE_LIMIT: 'rate-limit';

REALAUDIO: 'realaudio';

READ_ONLY: 'read-only';

READ_WRITE: 'read-write';

READVERTISE: 'readvertise';

RECEIVE: 'receive';

RECORD_LIFETIME: 'record-lifetime';
RECORD_ROUTE_OPTION: 'record-route-option';

RECOVERY_TIMEOUT: 'recovery-timeout';

REDIRECT: 'redirect';

REDIRECT_FOR_HOST: 'redirect-for-host';

REDIRECT_FOR_NETWORK: 'redirect-for-network';

REDIRECT_FOR_TOS_AND_HOST: 'redirect-for-tos-and-host';

REDIRECT_FOR_TOS_AND_NET: 'redirect-for-tos-and-net';

// TODO: should this just allow a number afterward?
REDUNDANCY_GROUP: 'redundancy-group' -> pushMode(M_Name);

REDUNDANT_ETHER_OPTIONS: 'redundant-ether-options';

REDUNDANT_PARENT: 'redundant-parent' -> pushMode(M_Interface);

REFERENCE_BANDWIDTH
:
   'reference-bandwidth' -> pushMode ( M_Bandwidth )
;

REFRESH_TIME: 'refresh-time';
REJECT: 'reject';

REKEY: 'rekey';

RELAY_AGENT_OPTION: 'relay-agent-option';

REMOTE: 'remote';

REMOTE_END_POINT: 'remote-end-point';

REMOVE: 'remove' -> pushMode(M_Remove);

REMOVE_PRIVATE: 'remove-private';

REMOVED: 'Removed';

REPLACE: 'replace';

REQUIRED_OPTION_MISSING: 'required-option-missing';

RESOLUTION: 'resolution';

RESOLVE: 'resolve';

RESOURCES: 'resources';

REST: 'rest';

RESTRICT: 'restrict';

RESTRICTED_QUEUES: 'restricted-queues';

RETAIN: 'retain';
RETRANSMIT_INTERVAL: 'retransmit-interval';
RETRY_TIMER: 'retry-timer';
REUSE: 'reuse';

REVERSE: 'reverse';

REVERSE_SSH: 'reverse-ssh';

REVERSE_TELNET: 'reverse-telnet';

REWRITE_RULES: 'rewrite-rules';

RIB: 'rib' -> pushMode(M_RibName);

RIB_GROUP
:
  'rib-group'
   {
     switch(lastTokenType()) {
       case INTERFACE_ROUTES:
         pushMode(M_InterfaceRoutesRibGroup);
         break;
       case ISIS:
         pushMode(M_IsisRibGroup);
         break;
       default:
         pushMode(M_Name);
         break;
     }
   }
;

RIB_GROUPS: 'rib-groups' -> pushMode(M_Name);

RIB_OUT: 'rib-out';

RIP: 'rip';

RIPNG: 'ripng';

RKINIT: 'rkinit';

RLOGIN: 'rlogin';

ROOT_AUTHENTICATION: 'root-authentication';

ROOT_LOGIN: 'root-login';

ROUTE: 'route';

ROUTE_DISTINGUISHER
:
   'route-distinguisher' -> pushMode ( M_RouteDistinguisher )
;

ROUTE_DISTINGUISHER_ID: 'route-distinguisher-id';

ROUTE_FILTER: 'route-filter';

ROUTE_MONITORING: 'route-monitoring';

ROUTE_RECORD: 'route-record';

ROUTE_TARGET: 'route-target';

ROUTE_TYPE: 'route-type';

ROUTER_ADVERTISEMENT: 'router-advertisement';

ROUTER_ALERT: 'router-alert';

ROUTER_ALERT_OPTION: 'router-alert-option';

ROUTER_DISCOVERY: 'router-discovery';

ROUTER_ID: 'router-id';

ROUTER_SOLICIT: 'router-solicit';

ROUTING: 'routing';

ROUTING_HEADER: 'routing-header';

ROUTING_INSTANCE: 'routing-instance' -> pushMode(M_RoutingInstanceName);
ROUTING_INSTANCE_ACCESS: 'routing-instance-access';
ROUTING_INSTANCES: 'routing-instances' -> pushMode(M_Routing_Instances);
ROUTING_INTERFACE: 'routing-interface' -> pushMode(M_Interface);
ROUTING_OPTIONS: 'routing-options';

RPC_PROGRAM_NUMBER: 'rpc-program-number';

RPF_CHECK: 'rpf-check';

RPL_OPTION: 'RPL-option';

RPM: 'rpm';

RSA_SIGNATURES: 'rsa-signatures';

RSH: 'rsh';

RSTP: 'rstp';

RSVP: 'rsvp';

RTSP: 'rtsp';

RULE: 'rule' -> pushMode(M_Name);

RULE_SET: 'rule-set' -> pushMode(M_Name);

SAMPLE: 'sample';

SAMPLING: 'sampling';

SAP: 'sap';

SAVED_CORE_CONTEXT: 'saved-core-context';

SAVED_CORE_FILES: 'saved-core-files';

SCCP: 'sccp';

SCHEDULER: 'scheduler';

SCHEDULER_MAP: 'scheduler-map' -> pushMode(M_Name);

SCHEDULER_MAPS: 'scheduler-maps';

SCHEDULERS: 'schedulers';

SCREEN
:
  'screen'
   {
     if (secondToLastTokenType() == SECURITY_ZONE) {
       // set security zones security-zone ZONENAME screen
       pushMode(M_Screen);
     }
   }
;

SCRIPTS: 'scripts';

SCTP: 'sctp';

SCRUBBED: F_Scrubbed;

SECRET: 'secret' -> pushMode(M_SecretString);

SECONDARY: 'secondary' -> pushMode(M_Name);

SECURITY: 'security';

SECURITY_OPTION: 'security-option';

SECURITY_PROFILE: 'security-profile' -> pushMode(M_Name);

SECURITY_ZONE: 'security-zone' -> pushMode(M_Zone);

SELF: 'self';

SELF_PING_DURATION: 'self-ping-duration';

SEND: 'send';

SERVER: 'server' -> pushMode(M_NameOrIp);

SERVER_GROUP: 'server-group' -> pushMode(M_Name);

SERVICE
:
  'service' 
  {
    if (lastTokenType() == PROXY_IDENTITY) {
      pushMode(M_ProxyIdentityService);
    }
  }
;

SERVICE_DEPLOYMENT: 'service-deployment';

SERVICE_FILTER: 'service-filter';

SERVICES: 'services';

SERVICES_OFFLOAD: 'services-offload';
SESSION: 'session';

SET
:
  'set'
  {
    if (lastTokenType() == COMMUNITY || lastTokenType() == TUNNEL_ATTRIBUTE) {
      pushMode(M_Name);
    }
  }
;

SFLOW: 'sflow';

SFM_DPD_OPTION: 'SFM-DPD-option';

SHA_256: 'sha-256';

SHA_384: 'sha-384';

SHA1: 'sha1';

SHARED_BUFFER: 'shared-buffer';

SHARED_IKE_ID: 'shared-ike-id';

SHIM6_HEADER: 'shim6-header';

SHORTCUTS: 'shortcuts';

SIGNALING: 'signaling';

SIMPLE: 'simple';

SINGLE_CONNECTION: 'single-connection';

SIP: 'sip';

SMTP: 'smtp';

SNMP: 'snmp';

SNMP_TRAP: 'snmp-trap';

SNMPTRAP: 'snmptrap';

SNPP: 'snpp';

SOCKS: 'socks';

SOFT_PREEMPTION: 'soft-preemption';

SONET_OPTIONS: 'sonet-options';

SOURCE: 'source';

SOURCE_ADDRESS
:
  'source-address'
   {
     if (lastTokenType() == MATCH) {
       pushMode(M_AddressSpecifier);
     }
   }
;

SOURCE_ADDRESS_EXCLUDED: 'source-address-excluded';

SOURCE_ADDRESS_FILTER: 'source-address-filter';

SOURCE_ADDRESS_NAME: 'source-address-name' -> pushMode(M_Name);

SOURCE_HOST_ISOLATED: 'source-host-isolated';

SOURCE_IDENTITY: 'source-identity' -> pushMode(M_SourceIdentity);

SOURCE_INTERFACE
:
   'source-interface' -> pushMode(M_Interface)
;

SOURCE_IP_BASED: 'source-ip-based';

SOURCE_MAC_ADDRESS: 'source-mac-address' -> pushMode(M_MacAddressAndLength);

SOURCE_NAT: 'source-nat';

SOURCE_PORT: 'source-port' -> pushMode(M_Port);
SOURCE_PORT_EXCEPT: 'source-port-except' -> pushMode(M_Port);
SOURCE_PORT_RANGE_OPTIMIZE: 'source-port-range-optimize';

SOURCE_PREFIX_LIST: 'source-prefix-list' -> pushMode(M_Name);

SOURCE_ROUTE_FAILED: 'source-route-failed';

SOURCE_ROUTE_OPTION: 'source-route-option';

SOURCE_THRESHOLD: 'source-threshold';

SOURCE_QUENCH: 'source-quench';

SPEED
:
   'speed' -> pushMode ( M_Speed )
;

SPF_OPTIONS: 'spf-options';

SPOOFING: 'spoofing';

SQLNET_V2: 'sqlnet-v2';

SRLG: 'srlg' -> pushMode(M_Name);

SRLG_COST: 'srlg-cost';

SRLG_VALUE: 'srlg-value';

SSH: 'ssh';

STANDARD: 'standard';

START_TIME: 'start-time' -> pushMode(M_RestOfLine);

STATIC: 'static';

STATIC_HOST_MAPPING: 'static-host-mapping' -> pushMode(M_RestOfLine);

STATIC_NAT: 'static-nat';

STATION: 'station' -> pushMode(M_Name);

STATION_ADDRESS: 'station-address';

STATION_PORT: 'station-port';

STATISTICS_TIMEOUT: 'statistics-timeout';

STATS_CACHE_LIFETIME: 'stats-cache-lifetime';

STORM_CONTROL: 'storm-control' -> pushMode(M_Name);

STORM_CONTROL_PROFILES: 'storm-control-profiles';

STP: 'stp';

STREAM_ID: 'stream-id';

STREAM_OPTION: 'stream-option';

STRICT_SOURCE_ROUTE: 'strict-source-route';

STRICT_SOURCE_ROUTE_OPTION: 'strict-source-route-option';

STRUCTURED_DATA: 'structured-data';

STUB: 'stub';

SUBSCRIBER_MANAGEMENT: 'subscriber-management';

SUBTRACT: 'subtract';

SUN_RPC: 'sun-rpc';

SUNRPC: 'sunrpc';

SUPPRESS: 'suppress';

SWAP: 'swap';

SWAP_PUSH: 'swap-push';

SWAP_SWAP: 'swap-swap';

SWITCH_OPTIONS: 'switch-options';

SWITCHOVER_ON_ROUTING_CRASH: 'switchover-on-routing-crash';

SYN_ACK_ACK_PROXY: 'syn-ack-ack-proxy';

SYN_FIN: 'syn-fin';

SYN_FLOOD: 'syn-flood';

SYN_FRAG: 'syn-frag';

SYSLOG: 'syslog';

SYSTEM: 'system';

SYSTEM_SERVICES: 'system-services';

TABLE: 'table' -> pushMode(M_Name);

TACACS: 'tacacs';

TACACS_DS: 'tacacs-ds';

TACPLUS: 'tacplus';

TACPLUS_SERVER: 'tacplus-server';

TAG: 'tag';

TAG2: 'tag2';

TALK: 'talk';

TARGET: 'target';

TARGET_HOST: 'target-host';

TARGET_HOST_PORT: 'target-host-port';

TARGETED_BROADCAST: 'targeted-broadcast';

TARGETS: 'targets';

TCP: 'tcp';

TCP_ESTABLISHED: 'tcp-established';

TCP_FLAGS
:
   'tcp-flags' -> pushMode ( M_TcpFlags )
;

TCP_FORWARDING: 'tcp-forwarding';

TCP_INITIAL: 'tcp-initial';

TCP_MSS: 'tcp-mss';

TCP_NO_FLAG: 'tcp-no-flag';

TCP_RST: 'tcp-rst';

TCP_SWEEP: 'tcp-sweep';

TE_METRIC: 'te-metric';

TEARDOWN: 'teardown';

TEAR_DROP: 'tear-drop';

TEREDO: 'teredo';

TELNET: 'telnet';

TERM: 'term' -> pushMode(M_Name);

TFTP: 'tftp';

TFTP_SERVER: 'tftp-server';

THEN: 'then';

THREEDES_CBC: '3des-cbc';

THRESHOLD: 'threshold';

THROUGH: 'through';

TIME_FORMAT: 'time-format';

TIME_EXCEEDED: 'time-exceeded';

TIME_ZONE: 'time-zone' -> pushMode(M_RestOfLine);

TIMED: 'timed';

TIMEOUT: 'timeout';

TIMESTAMP: 'timestamp';

TIMESTAMP_OPTION: 'timestamp-option';

TIMESTAMP_REPLY: 'timestamp-reply';

TO: 'to';

TOLERANCE: 'tolerance';
TO_ZONE: 'to-zone' -> pushMode(M_Zone);

TRACE: 'trace';

TRACE_OPTIONS: 'trace-options';

TRACEOPTIONS: 'traceoptions';

TRACEROUTE: 'traceroute';

TRACK: 'track';

TRAFFIC_CONTROL_PROFILES: 'traffic-control-profiles' -> pushMode(M_Name);

TRAFFIC_ENGINEERING: 'traffic-engineering';

TRANSLATION_TABLE: 'translation-table';

TRAP_DESTINATIONS: 'trap-destinations';

TRAP: 'trap';

TRAP_GROUP: 'trap-group' -> pushMode(M_Name);

TRAP_OPTIONS: 'trap-options';

TRAPS: 'traps';

TRI_COLOR: 'tri-color';

TRUNK: 'trunk';

TRUST: 'trust';

TTL: 'ttl';

TTL_EQ_ZERO_DURING_REASSEMBLY: 'ttl-eq-zero-during-reassembly';

TTL_EQ_ZERO_DURING_TRANSIT: 'ttl-eq-zero-during-transit';

TUNNEL: 'tunnel';

TUNNEL_ATTRIBUTE
:
  'tunnel-attribute'
  {
    if (lastTokenType() == POLICY_OPTIONS) {
      pushMode(M_Name);
    }
  }
;

TUNNEL_ENCAPSULATION_LIMIT_OPTION: 'tunnel-encapsulation-limit-option';

TUNNEL_TYPE: 'tunnel-type';

TYPE: 'type';

TYPE_7: 'type-7';

UDP: 'udp';

UDP_SWEEP: 'udp-sweep';

UNICAST: 'unicast';

UNIT: 'unit';

UNKNOWN_PROTOCOL: 'unknown-protocol';

UNREACHABLE: 'unreachable';

UNTRUST: 'untrust';

UNTRUST_SCREEN: 'untrust-screen';

UPLINK_FAILURE_DETECTION: 'uplink-failure-detection';

UPTO: 'upto' -> pushMode(M_PrefixLength);

URG: 'urg';

URPF_LOGGING: 'urpf-logging';

USER: 'user';

USER_DEFINED_OPTION_TYPE: 'user-defined-option-type';

UUID: 'uuid';

V1_ONLY: 'v1-only';
V2_ONLY: 'v2-only';
VALIDATION: 'validation';

VERSION
:
   'version' -> pushMode ( M_Version )
;

VIEW: 'view';

VIRTUAL_ADDRESS: 'virtual-address';

VIRTUAL_CHANNEL: 'virtual-channel';

VIRTUAL_CHASSIS: 'virtual-chassis';

VIRTUAL_GATEWAY_ADDRESS: 'virtual-gateway-address';

VIRTUAL_ROUTER: 'virtual-router';

VIRTUAL_SWITCH: 'virtual-switch';

VLAN
:
  'vlan'
  {
    if (lastTokenType() != ETHERNET_SWITCHING) {
      pushMode(M_Name);
    }
  }
;

VLANS: 'vlans' -> pushMode(M_Name);

VLAN_ID: 'vlan-id';

VLAN_ID_LIST: 'vlan-id-list' -> pushMode(M_VlanIdList);

VLAN_TAGS: 'vlan-tags';

VLAN_TAGGING: 'vlan-tagging';

VNI: 'vni';

VNI_OPTIONS: 'vni-options';

VPLS: 'vpls';

VPN: 'vpn' -> pushMode(M_Name);

VPN_MONITOR: 'vpn-monitor';

VRF: 'vrf';

VRF_EXPORT: 'vrf-export' -> pushMode(M_Name);

VRF_IMPORT: 'vrf-import' -> pushMode(M_Name);

VRF_TABLE_LABEL: 'vrf-table-label';

VRF_TARGET
:
   'vrf-target' -> pushMode ( M_VrfTarget )
;

VRRP: 'vrrp';

VRRP_GROUP: 'vrrp-group';

VSTP: 'vstp';

VTEP_SOURCE_INTERFACE
:
   'vtep-source-interface' -> pushMode(M_Interface)
;

VXLAN: 'vxlan';

VXLAN_ROUTING: 'vxlan-routing';

WEB_MANAGEMENT: 'web-management';

WEBAPI: 'webapi';

WHITE_LIST: 'white-list' -> pushMode(M_Name);

WHO: 'who';

WIDE_METRICS_ONLY: 'wide-metrics-only';

WILDCARD_ADDRESS: 'wildcard-address';

WINNUKE: 'winnuke';
WITHDRAW_PRIORITY: 'withdraw-priority';
XAUTH: 'xauth';

XDMCP: 'xdmcp';

XNM_CLEAR_TEXT: 'xnm-clear-text';

XNM_SSL: 'xnm-ssl';

ZONE: 'zone' -> pushMode(M_Name);

ZONES: 'zones';

// End of Juniper keywords

// Other tokens

COMMENT_LINE
:
  F_WhitespaceChar* [!#] {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_NewlineChar+ | EOF) -> channel(HIDDEN)
;

WILDCARD: F_Wildcard {setWildcard();};

STANDARD_COMMUNITY: F_StandardCommunity;

AMPERSAND
:
   '&'
;

ASTERISK
:
   '*'
;

BACKSLASH
:
   '\\'
;

CARAT
:
   '^'
;

CLOSE_BRACE
:
   '}'
;

CLOSE_BRACKET
:
   ']'
;

CLOSE_PAREN
:
   ')'
;

COLON
:
   ':'
;

COMMA
:
   ','
;

DASH: '-';

DOLLAR
:
   '$'
;

DOUBLE_AMPERSAND
:
   '&&'
;

DOUBLE_PIPE
:
   '||'
;

DOUBLE_QUOTED_STRING
:
   '"' ~'"'* '"'
;

/*
FLOAT
:
   F_PositiveDigit F_Digit* '.'
   (
      '0'
      | F_PositiveDigit F_Digit*
   )
;
*/

FORWARD_SLASH
:
   '/'
;

GREATER_THAN
:
   '>'
;

IP_ADDRESS: F_IpAddress;

IP_ADDRESS_AND_MASK: F_IpAddressAndMask;

IP_PREFIX: F_IpPrefix;

IPV6_ADDRESS: F_Ipv6Address;

IPV6_PREFIX: F_Ipv6Prefix;

NEWLINE: F_Newline;

OPEN_BRACE
:
   '{'
;

OPEN_BRACKET
:
   '['
;

OPEN_PAREN
:
   '('
;

PERIOD
:
   '.'
;

PLUS
:
   '+'
;

QUESTION_MARK
:
   '?'
;

SEMICOLON
:
   ';'
;

SINGLE_QUOTE
:
   '\''
;

UNDERSCORE: '_';

V3: 'v3';

WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

// Numbers. Order matters!

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

DEC
:
   F_Digit+
;

// Any contiguous sequence of non-whitespace tokens in the default mode not matching any previous
// token will be lexed as UNRECOGNIZED_WORD. The UNRECOGNIZED_WORD token should only be valid in the
// null_filler parser rule. Since the UNRECOGNIZED_WORD token will be a preferred match over any
// shorter token in the default mode, recognition of shorter tokens that do not take up an entire
// contiguous sequence of non-whitespace tokens must happen in a non-default mode.
UNRECOGNIZED_WORD: F_NonWhitespaceChar+;

// Fragments

fragment
F_Digit
:
  [0-9]
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
F_InterfaceMediaType
:
   ('ae' |
   'as' |
   'at' |
   'bcm' |
   'cau4' |
   'ca1' |
   'ci' |
   'coc1' |
   'coc3' |
   'coc12' |
   'coc48' |
   'cp' |
   'cstm1' |
   'cstm4' |
   'cstm16' |
   'ct1' |
   'ct3' |
   'demux' |
   'dfc' |
   'ds' |
   'dsc' |
   'e1' |
   'e3' |
   'em' |
   'es' |
   'et' |
   'fab' |
   'fe' |
   'fti' |
   'fxp' |
   'ge' |
   'gr' |
   'gre' |
   'ip' |
   'ipip' |
   'ixgbe' |
   'iw' |
   'lc' |
   'lm' |
   'lo' |
   'ls' |
   'lsi' |
   'lt' |
   'me' |
   'mo' |
   'ms' |
   'mt' |
   'mtun' |
   'oc3' |
   'pd' |
   'pe' |
   'pimd' |
   'pime' |
   'reth' |
   'rlsq' |
   'rms' |
   'rsp' |
   'se' |
   'si' |
   'so' |
   'sp' |
   'st' |
   'stm1' |
   'stm4' |
   'stm16' |
   't1' |
   't3' |
   'tap' |
   'umd' |
   'vc4' |
   'vme' |
   'vsp' |
   'vt' |
   'vtnet' |
   'xe' |
   'xt')
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_IpAddressAndMask
:
  F_IpAddress '/' F_IpAddress
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
F_Letter
:
   [A-Za-z]
;

fragment
F_DoubleQuotedString
:
  '"' F_NonNewlineChar* '"'
;

fragment
F_Wildcard
:
  F_WildcardInner
  | '"' F_WildcardInner '"'
;

fragment
F_WildcardInner
:
  '<' ~'>'* '>'
;

fragment
F_SecretString
:
  '"' ~'"'+ '"'
;

fragment
F_CertificateString
:
  '"' ~'"'+ '"'
;

fragment
F_Scrubbed
:
  '<SCRUBBED>' | '%CENSORED%'
;

fragment
F_Name
:
  F_NameChar+
  | F_NameQuoted
;

fragment
F_NameQuoted
:
  '"' (F_NameChar | ' ')* '"'
;

fragment
F_Alpha
:
  [A-Za-z]
;

fragment
F_NameChar
:
  [0-9A-Za-z_]
  | '-'
  | '+'
  | '/'
  | ','
  | '.'
  | ':'
;

// Any number of newlines, allowing whitespace in between
fragment
F_Newline
:
  F_NewlineChar (F_WhitespaceChar* F_NewlineChar+)*
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
   ~[\r\n]
;

fragment
F_NonWhitespaceChar
:
   ~[ \t\u000C\r\n]
;

fragment
F_PositiveDigit
:
   [1-9]
;

fragment
F_RoutingInstanceNameChar
:
   // Letters, numbers, and hyphens
   // https://www.juniper.net/documentation/us/en/software/junos/vpn-l3/topics/topic-map/l3-vpns-routing-instances.html#id-configuring-routing-instances-on-pe-routers-in-vpns__d57160e331
   // Underscores are also allowed in practice, e.g., https://www.juniper.net/documentation/us/en/software/junos/vpn-l3/topics/example/mpls-qfx-series-vpn-layer3.html
   // has "set routing-instances CE1_L3vpn protocols bgp group CE1 type external"
   [A-Za-z0-9_] | '-'
;

fragment
F_RoutingInstanceName
:
   F_RoutingInstanceNameChar+
;

fragment
F_Variable_RequiredVarChar
:
   ~[ 0-9\t\n\r/.,\-;{}<>[\]&|()"']
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_Variable_RequiredVarChar_Ipv6
:
   ~[ 0-9\t\n\r/.,\-:;{}<>[\]&|()"']
;

fragment
F_Variable_InterfaceVarChar
:
   ~[ \t\n\r.,:;{}<>[\]&|()"']
;

fragment
F_Variable_LeadingVarChar
:
   ~[ \t\n\r:;{}<>[\]&|()"']
;

fragment
F_Variable_LeadingVarChar_Ipv6
:
   ~[ \t\n\r:;{}<>[\]&|()"']
;

fragment
F_Uint8
:
  // juniper allows leading zeros in general
  '0'*
  (
    F_Digit
    | F_PositiveDigit F_Digit
    | '1' F_Digit F_Digit
    | '2' [0-4] F_Digit
    | '25' [0-5]
  )
;

fragment
F_Uint16
:
  // juniper allows leading zeros in general
  '0'*
  (
    F_Digit
    | F_PositiveDigit F_Digit F_Digit? F_Digit?
    | [1-5] F_Digit F_Digit F_Digit F_Digit
    | '6' [0-4] F_Digit F_Digit F_Digit
    | '65' [0-4] F_Digit F_Digit
    | '655' [0-2] F_Digit
    | '6553' [0-5]
  )
;

fragment
F_Uint32
:
// 0-4294967295
  // juniper allows leading zeros in general
  '0'*
  (
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
  )
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r;{}[\]&|()"']
;

fragment
F_Variable_VarChar_Ipv6
:
   ~[ \t\n\r:;{}[\]&|()"']
;

fragment
F_WhitespaceChar
:
   [ \t\u000C\u00A0]
;

fragment
F_InterfaceId
:
  F_InterfaceIdBase
  | '"' F_InterfaceIdBase '"'
;

fragment
F_InterfaceIdBase
:
  // optional node
  (F_InterfaceHostname ':')?
  
  // mandatory parent interface name
  F_InterfaceName
  
  // optional unit
  ('.' F_Digit+)? 
;

fragment
F_InterfaceHostname
:
  F_InterfaceHostname_LeadingChar F_InterfaceHostname_TrailingChar*
;

fragment
F_InterfaceHostname_LeadingChar
:
   [A-Za-z]
;

fragment
F_InterfaceHostname_TrailingChar
:
   [A-Za-z0-9_]|'-'
;

fragment
F_InterfaceName
:
  (
    F_InterfaceMediaType '-'? F_Digit+ ('/' F_Digit+)*
    // optional channel  
    (':' F_Digit+)?
  )
  | 'irb'
  | 'vlan'
  | 'vme'
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
F_UnquotedAsPathRegex
:
  F_UnquotedAsPathRegexChar+
;

fragment
F_UnquotedAsPathRegexChar
:
  [0-9,$*.{}+_]
  | '['
  | ']'
  | '-'
  | '^'
;

fragment
F_QuotedAsPathRegex
:
  '"' F_QuotedAsPathRegexChar+ '"'
;

fragment
F_QuotedAsPathRegexChar
:
  F_UnquotedAsPathRegexChar
  | [|()?! ]
;

mode M_AsPathDefinitionName;

M_AsPathDefinition_WS: F_WhitespaceChar+ -> skip;
M_AsPathDefinition_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathDefinition_WILDCARD: F_Wildcard {setWildcard();} -> mode(M_AsPathDefinitionRegex);
M_AsPathDefinition_NAME: F_Name -> type(NAME), mode(M_AsPathDefinitionRegex);

mode M_AsPathDefinitionRegex;

M_AsPathDefinitionRegex_WS: F_WhitespaceChar+ -> channel(HIDDEN), mode(M_AsPathDefinitionRegex2);
M_AsPathDefinitionRegex_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AsPathDefinitionRegex2;
M_AsPathDefinitionRegex2_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathDefinitionRegex2_QUOTED_AS_PATH_REGEX: F_QuotedAsPathRegex -> type(AS_PATH_REGEX), popMode;
M_AsPathDefinitionRegex2_AS_PATH_REGEX: F_UnquotedAsPathRegex -> type(AS_PATH_REGEX), popMode;

mode M_AsPathPrepend;

M_AsPathPrepend_WS: F_WhitespaceChar+ -> skip, mode(M_AsPathPrepend2);
M_AsPathPrepend_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AsPathPrepend2;

M_AsPathPrepend2_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathPrepend2_UINT8: F_Uint8 -> type(UINT8);
M_AsPathPrepend2_UINT16: F_Uint16 -> type(UINT16);
M_AsPathPrepend2_UINT32: F_Uint32 -> type(UINT32);
M_AsPathPrepend2_PERIOD: '.' -> type(PERIOD);
M_AsPathPrepend2_DOUBLE_QUOTE: '"' -> skip, mode(M_AsPathPrepend_Inner);
M_AsPathPrepend2_WS: F_WhitespaceChar+ -> skip, popMode;

mode M_AsPathPrepend_Inner;

M_AsPathPrepend_Inner_UINT8: F_Uint8 -> type (UINT8);
M_AsPathPrepend_Inner_UINT16: F_Uint16 -> type(UINT16);
M_AsPathPrepend_Inner_UINT32: F_Uint32 -> type(UINT32);
M_AsPathPrepend_Inner_DOUBLE_QUOTE: '"' -> skip, popMode;
M_AsPathPrepend_Inner_PERIOD: '.' -> type(PERIOD);
M_AsPathPrepend_Inner_WS: F_WhitespaceChar+ -> skip;

mode M_AsPathExpand;

M_AsPathExpand_WS: F_WhitespaceChar+ -> skip, mode(M_AsPathExpand2);
M_AsPathExpand_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AsPathExpand2;

M_AsPathExpand2_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathExpand2_UINT8: F_Uint8 -> type(UINT8);
M_AsPathExpand2_UINT16: F_Uint16 -> type(UINT16);
M_AsPathExpand2_UINT32: F_Uint32 -> type(UINT32);
M_AsPathExpand2_PERIOD: '.' -> type(PERIOD);
M_AsPathExpand2_DOUBLE_QUOTE: '"' -> skip, mode(M_AsPathExpand_Inner);
M_AsPathExpand2_LAST_AS: 'last-as' -> type(LAST_AS), mode(M_AsPathExpandLastAs);
M_AsPathExpand2_WS: F_WhitespaceChar+ -> skip, popMode;

mode M_AsPathExpand_Inner;

M_AsPathExpand_Inner_UINT8: F_Uint8 -> type (UINT8);
M_AsPathExpand_Inner_UINT16: F_Uint16 -> type(UINT16);
M_AsPathExpand_Inner_UINT32: F_Uint32 -> type(UINT32);
M_AsPathExpand_Inner_DOUBLE_QUOTE: '"' -> skip, popMode;
M_AsPathExpand_Inner_PERIOD: '.' -> type(PERIOD);
M_AsPathExpand_Inner_WS: F_WhitespaceChar+ -> skip;

mode M_AsPathExpandLastAs;
M_AsPathExpandLastAs_WS: F_WhitespaceChar+ -> skip;
M_AsPathExpandLastAs_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathExpandLastAs_UINT8: F_Uint8 -> type (UINT8);
M_AsPathExpandLastAs_UINT16: F_Uint16 -> type(UINT16);
M_AsPathExpandLastAs_COUNT: 'count' -> type(COUNT);

mode M_Description;

M_Description_DESCRIPTION
:
   F_NonWhitespaceChar F_NonNewlineChar*
;

M_Description_NEWLINE: F_Newline -> type(NEWLINE), popMode;

M_Description_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_Interface;

M_Interface_ALL: 'all' -> type(ALL), popMode;

M_Interface_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);

M_Interface_APPLY_GROUPS_EXCEPT: 'apply-groups-except' -> type(APPLY_GROUPS_EXCEPT), popMode;

M_Interface_INTERFACE_RANGE: 'interface-range' -> type(INTERFACE_RANGE), mode(M_Name);

M_Interface_PORT_OVERLOADING: 'port-overloading' -> type(PORT_OVERLOADING), popMode;

M_Interface_PORT_OVERLOADING_FACTOR:
  'port-overloading-factor' -> type(PORT_OVERLOADING_FACTOR), popMode
;

M_Interface_TRACEOPTIONS: 'traceoptions' -> type(TRACEOPTIONS), popMode;

M_Interface_IP_ADDRESS: F_IpAddress -> type(IP_ADDRESS), popMode;

M_Interface_IPV6_ADDRESS: F_Ipv6Address -> type(IPV6_ADDRESS), popMode;

M_Interface_NEWLINE: F_Newline -> type(NEWLINE), popMode;

M_Interface_WILDCARD: F_Wildcard {setWildcard();} -> popMode;

// TODO: probably better to adjust modes so we don't need this
M_Interface_INTERFACE: 'interface' -> type(INTERFACE);

M_Interface_WS: F_WhitespaceChar+ -> skip;

M_Interface_INTERFACE_ID: F_InterfaceId -> type(INTERFACE_ID), popMode;

// for interface-range member
M_Interface_DOUBLE_QUOTED_STRING: F_DoubleQuotedString -> type(DOUBLE_QUOTED_STRING), popMode;

mode M_InterfaceWildcard;

M_InterfaceWildcard_APPLY_GROUPS
:
  'apply-groups' -> type(APPLY_GROUPS), popMode
;

M_InterfaceWildcard_APPLY_GROUPS_EXCEPT
:
  'apply-groups-except' -> type(APPLY_GROUPS_EXCEPT), popMode
;

M_InterfaceWildcard_NEWLINE
:
  F_NewlineChar+ -> type(NEWLINE), popMode
;

M_InterfaceWildcard_INTERFACE_WILDCARD
:
  (
    [A-Za-z]+ [-A-Za-z0-9]+ '*'?
    | '*'
  ) -> type(INTERFACE_WILDCARD), popMode
;

M_InterfaceWildcard_WILDCARD
:
  F_Wildcard
  {setWildcard();}
    -> popMode
;

M_InterfaceWildcard_WS
:
  F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_ISO;

M_ISO_ADDRESS
:
   'address' -> type ( ADDRESS ) , mode ( M_ISO_Address )
;

M_ISO_DESTINATION_UDP_PORT
:
   'destination-udp-port' -> type ( DESTINATION_UDP_PORT ) , popMode
;

M_ISO_MTU
:
   'mtu' -> type ( MTU ) , popMode
;

M_ISO_Newline
:
  F_Newline -> type(NEWLINE), popMode
;

M_ISO_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_ISO_Address;

M_ISO_Address_ISO_ADDRESS
:
   F_HexDigit+
   (
      '.' F_HexDigit+
   )+ -> type ( ISO_ADDRESS ) , popMode
;

M_ISO_Address_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_MacAddress;

MAC_ADDRESS: F_MacAddress -> popMode;

M_MacAddress_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_MemberRange;

M_MemberRange_INTERFACE_ID: F_InterfaceName -> type(INTERFACE_ID), mode(M_MemberRange2);

M_MemberRange_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_MemberRange2;

M_MemberRange2_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

M_MemberRange2_TO:
   'to' -> type(TO), mode(M_Interface)
;

mode M_Members;

M_Members_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN )
;

M_Members_NEWLINE
:
   F_Newline -> type(NEWLINE), popMode
;

M_Members_NO_ADVERTISE
:
   'no-advertise' -> type ( NO_ADVERTISE )
;

M_Members_NO_EXPORT
:
   'no-export' -> type ( NO_EXPORT )
;

M_Members_NO_EXPORT_SUBCONFED
:
   'no-export-subconfed' -> type ( NO_EXPORT_SUBCONFED )
;

M_Members_LITERAL_OR_REGEX_COMMUNITY
:
  ~[ \t\n\r"]+ -> type(LITERAL_OR_REGEX_COMMUNITY)
;

M_Members_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_Name;

M_Name_WS: F_WhitespaceChar+ -> skip;
M_Name_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Name_SCRUBBED: F_Scrubbed -> type(NAME), popMode;
M_Name_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_Name_NAME: F_Name -> type(NAME), popMode;

mode M_NameList;

M_NameList_WS: F_WhitespaceChar+ -> skip;
M_NameList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameList_SCRUBBED: F_Scrubbed -> type(NAME), popMode;
M_NameList_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_NameList_NAME: F_Name -> type(NAME), popMode;
M_NamList_OPEN_BRACKET: '[' -> type(OPEN_BRACKET), mode(M_NameListInner);

mode M_NameListInner;
M_NameListInner_WS: F_WhitespaceChar+ -> skip;
M_NameListInner_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameListInner_SCRUBBED: F_Scrubbed -> type(NAME);
M_NameListInner_NAME: F_Name -> type(NAME);
M_NamList_CLOSE_BRACKET: ']' -> type(CLOSE_BRACKET), popMode;

mode M_NameOrIp;

M_NameOrIp_WS: F_WhitespaceChar+ -> skip;
M_NameOrIp_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NameOrIp_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_NameOrIp_IP_ADDRESS: F_IpAddress -> type(IP_ADDRESS), popMode;
M_NameOrIp_IPV6_ADDRESS: F_Ipv6Address -> type(IPV6_ADDRESS), popMode;
M_NameOrIp_NAME: F_Name -> type(NAME), popMode;

mode M_Bandwidth;

M_Bandwidth_DEC
:
  F_Digit+ -> type ( DEC )
;

M_Bandwidth_C
:
  'c' -> type ( C ) , popMode
;

M_Bandwidth_G
:
  'g' -> type ( G ) , popMode
;

M_Bandwidth_K
:
  'k' -> type ( K ) , popMode
;

M_Bandwidth_M
:
  'm' -> type ( M ) , popMode
;

M_Bandwidth_NEWLINE
:
  F_NewlineChar+ -> type ( NEWLINE ) , popMode
;

M_Bandwidth_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_RoutingInstanceName;

M_RoutingInstanceName_NAME: F_RoutingInstanceName -> type ( NAME ) , popMode;
M_RoutingInstanceName_WS: F_WhitespaceChar+ -> channel ( HIDDEN );
M_RoutingInstanceName_NEWLINE: F_NewlineChar+ -> type ( NEWLINE ) , popMode;

mode M_TcpFlags;

M_TcpFlags_ACK
:
   'ack' -> type ( ACK )
;

M_TcpFlags_CWR
:
   'cwr' -> type ( CWR )
;

M_TcpFlags_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , pushMode ( M_TcpFlags2 )
;

M_TcpFlags_ECE
:
   'ece' -> type ( ECE )
;

M_TcpFlags_FIN
:
   'fin' -> type ( FIN )
;

M_TcpFlags_NEWLINE
:
   F_NewlineChar+ -> type ( NEWLINE ) , popMode
;

M_TcpFlags_RST
:
   'rst' -> type ( RST )
;

M_TcpFlags_SYN
:
   'syn' -> type ( SYN )
;

M_TcpFlags_URG
:
   'urg' -> type ( URG )
;

M_TcpFlags_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_TcpFlags2;

M_TcpFlags2_ACK
:
   'ack' -> type ( ACK )
;

M_TcpFlags2_AMPERSAND
:
   '&' -> type ( AMPERSAND )
;

M_TcpFlags2_BANG
:
   '!' -> type ( BANG )
;

M_TcpFlags2_CLOSE_PAREN
:
   ')' -> type ( CLOSE_PAREN )
;

M_TcpFlags2_CWR
:
   'cwr' -> type ( CWR )
;

M_TcpFlags2_ECE
:
   'ece' -> type ( ECE )
;

M_TcpFlags2_FIN
:
   'fin' -> type ( FIN )
;

M_TcpFlags2_OPEN_PAREN
:
   '(' -> type ( OPEN_PAREN )
;

M_TcpFlags2_PIPE
:
   '|' -> type ( PIPE )
;

M_TcpFlags2_RST
:
   'rst' -> type ( RST )
;

M_TcpFlags2_SYN
:
   'syn' -> type ( SYN )
;

M_TcpFlags2_URG
:
   'urg' -> type ( URG )
;

M_TcpFlags2_DOUBLE_QUOTE
:
   '"' -> channel ( HIDDEN ) , mode ( DEFAULT_MODE )
;

M_TcpFlags2_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_MetricType;

METRIC_TYPE_1
:
   '1' -> popMode
;

METRIC_TYPE_2
:
   '2' -> popMode
;

M_MetricType_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;



mode M_Remove;
M_Remove_ALL: 'all' -> type(ALL), popMode;
M_Remove_WS: F_WhitespaceChar+ -> skip;
M_Remove_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Remove_NAME: F_Name -> type(NAME), popMode;

mode M_RouteDistinguisher;
M_RouteDistinguisher_COLON: ':' -> type(COLON);
M_RouteDistinguisher_IP_ADDRESS: F_IpAddress -> type(IP_ADDRESS);
M_RouteDistinguisher_UINT16: F_Uint16 -> type(UINT16);
M_RouteDistinguisher_UINT32: F_Uint32 -> type(UINT32);
M_RouteDistinguisher_UINT32L: F_Uint32 'L' -> type(UINT32L);
M_RouteDistinguisher_NEWLINE :F_Newline -> type(NEWLINE), popMode;
M_RouteDistinguisher_WS: F_WhitespaceChar+ -> channel(HIDDEN);

mode M_Routing_Instances;
M_Routing_Instances_WS: F_WhitespaceChar+ -> skip;
M_Routing_Instances_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Routing_Instances_SCRUBBED: F_Scrubbed -> type(NAME), popMode;
M_Routing_Instances_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_Routing_Instances_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);
M_Routing_Instances_NAME: F_RoutingInstanceName -> type(NAME), popMode;

mode M_Speed;

M_Speed_DEC
:
   F_Digit+ -> type ( DEC )
;

M_Speed_G
:
   'g' -> type ( G ) , popMode
;

M_Speed_M
:
   'm' -> type ( M ) , popMode
;

M_Speed_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_Version;

M_Version_V1_ONLY
:
   'v1-only' -> type ( V1_ONLY ) , popMode
;

M_Version_V2_ONLY
:
   'v2-only' -> type ( V2_ONLY ) , popMode
;

M_Version_QUOTED_STRING
:
   '"' ~'"'* '"' -> type ( VERSION_STRING ) , popMode
;

M_Version_VERSION_STRING
:
   ~[ \t\u000C\r\n;]+ -> type ( VERSION_STRING ) , popMode
;

M_Version_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_VlanMembers;

M_VlanMembers_ALL: 'all' -> type(ALL), popMode;

M_VlanMembers_DEFAULT: 'default' -> type(DEFAULT), popMode;

M_VlanMembers_WS: F_WhitespaceChar+ -> skip;

M_VlanMembers_NEWLINE: F_Newline -> type(NEWLINE), popMode;

M_VlanMembers_RANGE
:
  (
    F_Digit
    | '-'
    | ','
  )+
  {less();} -> mode(M_Range2) // go to second mode since we already saw whitespace
;

M_VlanMembers_NAME: F_Name -> type(NAME), popMode;

mode M_VrfTarget;

M_VrfTarget_COLON
:
   ':' -> type ( COLON )
;

M_VrfTarget_DEC
:
   F_Digit+ -> type ( DEC )
;

M_VrfTarget_EXPORT
:
   'export' -> type ( EXPORT )
;

M_VrfTarget_IMPORT
:
   'import' -> type ( IMPORT )
;

M_VrfTarget_L
:
   'L' -> type ( L )
;

M_VrfTarget_NEWLINE
:
   F_NewlineChar+ -> type(NEWLINE), popMode
;

M_VrfTarget_PERIOD
:
   '.' -> type ( PERIOD )
;

M_VrfTarget_TARGET
:
   'target' -> type ( TARGET )
;

M_VrfTarget_WS
:
   F_WhitespaceChar+ -> channel ( HIDDEN )
;

mode M_PolicyExpression;

M_PolicyExpression_OPEN_PAREN: '(' -> type(OPEN_PAREN);
M_PolicyExpression_CLOSE_PAREN: ')' -> type(CLOSE_PAREN);
M_PolicyExpression_DOUBLE_AMPERSAND: '&&' -> type(DOUBLE_AMPERSAND);
M_PolicyExpression_DOUBLE_PIPE: '||' -> type(DOUBLE_PIPE);
M_PolicyExpression_BANG: '!' -> type(BANG);
M_PolicyExpression_NAME: F_Name -> type(NAME);

M_PolicyExpression_NEWLINE: F_Newline -> type(NEWLINE), popMode;

M_PolicyExpression_WS: F_WhitespaceChar+ -> skip;

mode M_Application;
M_Application_ANY: 'any' -> type(ANY), popMode;
M_Application_JUNOS_AOL: 'junos-aol' -> type(JUNOS_AOL), popMode;
M_Application_JUNOS_BGP: 'junos-bgp' -> type(JUNOS_BGP), popMode;
M_Application_JUNOS_BIFF: 'junos-biff' -> type(JUNOS_BIFF), popMode;
M_Application_JUNOS_BOOTPC: 'junos-bootpc' -> type(JUNOS_BOOTPC), popMode;
M_Application_JUNOS_BOOTPS: 'junos-bootps' -> type(JUNOS_BOOTPS), popMode;
M_Application_JUNOS_CHARGEN: 'junos-chargen' -> type(JUNOS_CHARGEN), popMode;
M_Application_JUNOS_CVSPSERVER: 'junos-cvspserver' -> type(JUNOS_CVSPSERVER), popMode;
M_Application_JUNOS_DHCP_CLIENT: 'junos-dhcp-client' -> type(JUNOS_DHCP_CLIENT), popMode;
M_Application_JUNOS_DHCP_RELAY: 'junos-dhcp-relay' -> type(JUNOS_DHCP_RELAY), popMode;
M_Application_JUNOS_DHCP_SERVER: 'junos-dhcp-server' -> type(JUNOS_DHCP_SERVER), popMode;
M_Application_JUNOS_DISCARD: 'junos-discard' -> type(JUNOS_DISCARD), popMode;
M_Application_JUNOS_DNS_TCP: 'junos-dns-tcp' -> type(JUNOS_DNS_TCP), popMode;
M_Application_JUNOS_DNS_UDP: 'junos-dns-udp' -> type(JUNOS_DNS_UDP), popMode;
M_Application_JUNOS_ECHO: 'junos-echo' -> type(JUNOS_ECHO), popMode;
M_Application_JUNOS_FINGER: 'junos-finger' -> type(JUNOS_FINGER), popMode;
M_Application_JUNOS_FTP: 'junos-ftp' -> type(JUNOS_FTP), popMode;
M_Application_JUNOS_FTP_DATA: 'junos-ftp-data' -> type(JUNOS_FTP_DATA), popMode;
M_Application_JUNOS_GNUTELLA: 'junos-gnutella' -> type(JUNOS_GNUTELLA), popMode;
M_Application_JUNOS_GOPHER: 'junos-gopher' -> type(JUNOS_GOPHER), popMode;
M_Application_JUNOS_GPRS_GTP_C: 'junos-gprs-gtp-c' -> type(JUNOS_GPRS_GTP_C), popMode;
M_Application_JUNOS_GPRS_GTP_U: 'junos-gprs-gtp-u' -> type(JUNOS_GPRS_GTP_U), popMode;
M_Application_JUNOS_GPRS_GTP_V0: 'junos-gprs-gtp-v0' -> type(JUNOS_GPRS_GTP_V0), popMode;
M_Application_JUNOS_GPRS_SCTP: 'junos-gprs-sctp' -> type(JUNOS_GPRS_SCTP), popMode;
M_Application_JUNOS_GRE: 'junos-gre' -> type(JUNOS_GRE), popMode;
M_Application_JUNOS_GTP: 'junos-gtp' -> type(JUNOS_GTP), popMode;
M_Application_JUNOS_H323: 'junos-h323' -> type(JUNOS_H323), popMode;
M_Application_JUNOS_HTTP: 'junos-http' -> type(JUNOS_HTTP), popMode;
M_Application_JUNOS_HTTP_EXT: 'junos-http-ext' -> type(JUNOS_HTTP_EXT), popMode;
M_Application_JUNOS_HTTPS: 'junos-https' -> type(JUNOS_HTTPS), popMode;
M_Application_JUNOS_ICMP_ALL: 'junos-icmp-all' -> type(JUNOS_ICMP_ALL), popMode;
M_Application_JUNOS_ICMP_PING: 'junos-icmp-ping' -> type(JUNOS_ICMP_PING), popMode;
M_Application_JUNOS_ICMP6_ALL: 'junos-icmp6-all' -> type(JUNOS_ICMP6_ALL), popMode;
M_Application_JUNOS_ICMP6_DST_UNREACH_ADDR: 'junos-icmp6-dst-unreach-addr' -> type(JUNOS_ICMP6_DST_UNREACH_ADDR), popMode;
M_Application_JUNOS_ICMP6_DST_UNREACH_ADMIN: 'junos-icmp6-dst-unreach-admin' -> type(JUNOS_ICMP6_DST_UNREACH_ADMIN), popMode;
M_Application_JUNOS_ICMP6_DST_UNREACH_BEYOND: 'junos-icmp6-dst-unreach-beyond' -> type(JUNOS_ICMP6_DST_UNREACH_BEYOND), popMode;
M_Application_JUNOS_ICMP6_DST_UNREACH_PORT: 'junos-icmp6-dst-unreach-port' -> type(JUNOS_ICMP6_DST_UNREACH_PORT), popMode;
M_Application_JUNOS_ICMP6_DST_UNREACH_ROUTE: 'junos-icmp6-dst-unreach-route' -> type(JUNOS_ICMP6_DST_UNREACH_ROUTE), popMode;
M_Application_JUNOS_ICMP6_ECHO_REPLY: 'junos-icmp6-echo-reply' -> type(JUNOS_ICMP6_ECHO_REPLY), popMode;
M_Application_JUNOS_ICMP6_ECHO_REQUEST: 'junos-icmp6-echo-request' -> type(JUNOS_ICMP6_ECHO_REQUEST), popMode;
M_Application_JUNOS_ICMP6_PACKET_TOO_BIG: 'junos-icmp6-packet-too-big' -> type(JUNOS_ICMP6_PACKET_TOO_BIG), popMode;
M_Application_JUNOS_ICMP6_PARAM_PROB_HEADER: 'junos-icmp6-param-prob-header' -> type(JUNOS_ICMP6_PARAM_PROB_HEADER), popMode;
M_Application_JUNOS_ICMP6_PARAM_PROB_NEXTHDR: 'junos-icmp6-param-prob-nexthdr' -> type(JUNOS_ICMP6_PARAM_PROB_NEXTHDR), popMode;
M_Application_JUNOS_ICMP6_PARAM_PROB_OPTION: 'junos-icmp6-param-prob-option' -> type(JUNOS_ICMP6_PARAM_PROB_OPTION), popMode;
M_Application_JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY: 'junos-icmp6-time-exceed-reassembly' -> type(JUNOS_ICMP6_TIME_EXCEED_REASSEMBLY), popMode;
M_Application_JUNOS_ICMP6_TIME_EXCEED_TRANSIT: 'junos-icmp6-time-exceed-transit' -> type(JUNOS_ICMP6_TIME_EXCEED_TRANSIT), popMode;
M_Application_JUNOS_IDENT: 'junos-ident' -> type(JUNOS_IDENT), popMode;
M_Application_JUNOS_IKE: 'junos-ike' -> type(JUNOS_IKE), popMode;
M_Application_JUNOS_IKE_NAT: 'junos-ike-nat' -> type(JUNOS_IKE_NAT), popMode;
M_Application_JUNOS_IMAP: 'junos-imap' -> type(JUNOS_IMAP), popMode;
M_Application_JUNOS_IMAPS: 'junos-imaps' -> type(JUNOS_IMAPS), popMode;
M_Application_JUNOS_INTERNET_LOCATOR_SERVICE: 'junos-internet-locator-service' -> type(JUNOS_INTERNET_LOCATOR_SERVICE), popMode;
M_Application_JUNOS_IRC: 'junos-irc' -> type(JUNOS_IRC), popMode;
M_Application_JUNOS_L2TP: 'junos-l2tp' -> type(JUNOS_L2TP), popMode;
M_Application_JUNOS_LDAP: 'junos-ldap' -> type(JUNOS_LDAP), popMode;
M_Application_JUNOS_LDP_TCP: 'junos-ldp-tcp' -> type(JUNOS_LDP_TCP), popMode;
M_Application_JUNOS_LDP_UDP: 'junos-ldp-udp' -> type(JUNOS_LDP_UDP), popMode;
M_Application_JUNOS_LPR: 'junos-lpr' -> type(JUNOS_LPR), popMode;
M_Application_JUNOS_MAIL: 'junos-mail' -> type(JUNOS_MAIL), popMode;
M_Application_JUNOS_MGCP_CA: 'junos-mgcp-ca' -> type(JUNOS_MGCP_CA), popMode;
M_Application_JUNOS_MGCP_UA: 'junos-mgcp-ua' -> type(JUNOS_MGCP_UA), popMode;
M_Application_JUNOS_MS_RPC_EPM: 'junos-ms-rpc-epm' -> type(JUNOS_MS_RPC_EPM), popMode;
M_Application_JUNOS_MS_RPC_IIS_COM_1: 'junos-ms-rpc-iis-com-1' -> type(JUNOS_MS_RPC_IIS_COM_1), popMode;
M_Application_JUNOS_MS_RPC_IIS_COM_ADMINBASE: 'junos-ms-rpc-iis-com-adminbase' -> type(JUNOS_MS_RPC_IIS_COM_ADMINBASE), popMode;
M_Application_JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP: 'junos-ms-rpc-msexchange-directory-nsp' -> type(JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_NSP), popMode;
M_Application_JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR: 'junos-ms-rpc-msexchange-directory-rfr' -> type(JUNOS_MS_RPC_MSEXCHANGE_DIRECTORY_RFR), popMode;
M_Application_JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE: 'junos-ms-rpc-msexchange-info-store' -> type(JUNOS_MS_RPC_MSEXCHANGE_INFO_STORE), popMode;
M_Application_JUNOS_MS_RPC_TCP: 'junos-ms-rpc-tcp' -> type(JUNOS_MS_RPC_TCP), popMode;
M_Application_JUNOS_MS_RPC_UDP: 'junos-ms-rpc-udp' -> type(JUNOS_MS_RPC_UDP), popMode;
M_Application_JUNOS_MS_RPC_UUID_ANY_TCP: 'junos-ms-rpc-uuid-any-tcp' -> type(JUNOS_MS_RPC_UUID_ANY_TCP), popMode;
M_Application_JUNOS_MS_RPC_UUID_ANY_UDP: 'junos-ms-rpc-uuid-any-udp' -> type(JUNOS_MS_RPC_UUID_ANY_UDP), popMode;
M_Application_JUNOS_MS_RPC_WMIC_ADMIN: 'junos-ms-rpc-wmic-admin' -> type(JUNOS_MS_RPC_WMIC_ADMIN), popMode;
M_Application_JUNOS_MS_RPC_WMIC_ADMIN2: 'junos-ms-rpc-wmic-admin2' -> type(JUNOS_MS_RPC_WMIC_ADMIN2), popMode;
M_Application_JUNOS_MS_RPC_WMIC_MGMT: 'junos-ms-rpc-wmic-mgmt' -> type(JUNOS_MS_RPC_WMIC_MGMT), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT: 'junos-ms-rpc-wmic-webm-callresult' -> type(JUNOS_MS_RPC_WMIC_WEBM_CALLRESULT), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT: 'junos-ms-rpc-wmic-webm-classobject' -> type(JUNOS_MS_RPC_WMIC_WEBM_CLASSOBJECT), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN: 'junos-ms-rpc-wmic-webm-level1login' -> type(JUNOS_MS_RPC_WMIC_WEBM_LEVEL1LOGIN), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID: 'junos-ms-rpc-wmic-webm-login-clientid' -> type(JUNOS_MS_RPC_WMIC_WEBM_LOGIN_CLIENTID), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER: 'junos-ms-rpc-wmic-webm-login-helper' -> type(JUNOS_MS_RPC_WMIC_WEBM_LOGIN_HELPER), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK: 'junos-ms-rpc-wmic-webm-objectsink' -> type(JUNOS_MS_RPC_WMIC_WEBM_OBJECTSINK), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES: 'junos-ms-rpc-wmic-webm-refreshing-services' -> type(JUNOS_MS_RPC_WMIC_WEBM_REFRESHING_SERVICES), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER: 'junos-ms-rpc-wmic-webm-remote-refresher' -> type(JUNOS_MS_RPC_WMIC_WEBM_REMOTE_REFRESHER), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_SERVICES: 'junos-ms-rpc-wmic-webm-services' -> type(JUNOS_MS_RPC_WMIC_WEBM_SERVICES), popMode;
M_Application_JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN: 'junos-ms-rpc-wmic-webm-shutdown' -> type(JUNOS_MS_RPC_WMIC_WEBM_SHUTDOWN), popMode;
M_Application_JUNOS_MS_SQL: 'junos-ms-sql' -> type(JUNOS_MS_SQL), popMode;
M_Application_JUNOS_MSN: 'junos-msn' -> type(JUNOS_MSN), popMode;
M_Application_JUNOS_NBDS: 'junos-nbds' -> type(JUNOS_NBDS), popMode;
M_Application_JUNOS_NBNAME: 'junos-nbname' -> type(JUNOS_NBNAME), popMode;
M_Application_JUNOS_NETBIOS_SESSION: 'junos-netbios-session' -> type(JUNOS_NETBIOS_SESSION), popMode;
M_Application_JUNOS_NFS: 'junos-nfs' -> type(JUNOS_NFS), popMode;
M_Application_JUNOS_NFSD_TCP: 'junos-nfsd-tcp' -> type(JUNOS_NFSD_TCP), popMode;
M_Application_JUNOS_NFSD_UDP: 'junos-nfsd-udp' -> type(JUNOS_NFSD_UDP), popMode;
M_Application_JUNOS_NNTP: 'junos-nntp' -> type(JUNOS_NNTP), popMode;
M_Application_JUNOS_NS_GLOBAL: 'junos-ns-global' -> type(JUNOS_NS_GLOBAL), popMode;
M_Application_JUNOS_NS_GLOBAL_PRO: 'junos-ns-global-pro' -> type(JUNOS_NS_GLOBAL_PRO), popMode;
M_Application_JUNOS_NSM: 'junos-nsm' -> type(JUNOS_NSM), popMode;
M_Application_JUNOS_NTALK: 'junos-ntalk' -> type(JUNOS_NTALK), popMode;
M_Application_JUNOS_NTP: 'junos-ntp' -> type(JUNOS_NTP), popMode;
M_Application_JUNOS_OSPF: 'junos-ospf' -> type(JUNOS_OSPF), popMode;
M_Application_JUNOS_PC_ANYWHERE: 'junos-pc-anywhere' -> type(JUNOS_PC_ANYWHERE), popMode;
M_Application_JUNOS_PERSISTENT_NAT: 'junos-persistent-nat' -> type(JUNOS_PERSISTENT_NAT), popMode;
M_Application_JUNOS_PING: 'junos-ping' -> type(JUNOS_PING), popMode;
M_Application_JUNOS_PINGV6: 'junos-pingv6' -> type(JUNOS_PINGV6), popMode;
M_Application_JUNOS_POP3: 'junos-pop3' -> type(JUNOS_POP3), popMode;
M_Application_JUNOS_PPTP: 'junos-pptp' -> type(JUNOS_PPTP), popMode;
M_Application_JUNOS_PRINTER: 'junos-printer' -> type(JUNOS_PRINTER), popMode;
M_Application_JUNOS_R2CP: 'junos-r2cp' -> type(JUNOS_R2CP), popMode;
M_Application_JUNOS_RADACCT: 'junos-radacct' -> type(JUNOS_RADACCT), popMode;
M_Application_JUNOS_RADIUS: 'junos-radius' -> type(JUNOS_RADIUS), popMode;
M_Application_JUNOS_RDP: 'junos-rdp' -> type(JUNOS_RDP), popMode;
M_Application_JUNOS_REALAUDIO: 'junos-realaudio' -> type(JUNOS_REALAUDIO), popMode;
M_Application_JUNOS_RIP: 'junos-rip' -> type(JUNOS_RIP), popMode;
M_Application_JUNOS_RSH: 'junos-rsh' -> type(JUNOS_RSH), popMode;
M_Application_JUNOS_RTSP: 'junos-rtsp' -> type(JUNOS_RTSP), popMode;
M_Application_JUNOS_SCCP: 'junos-sccp' -> type(JUNOS_SCCP), popMode;
M_Application_JUNOS_SCTP_ANY: 'junos-sctp-any' -> type(JUNOS_SCTP_ANY), popMode;
M_Application_JUNOS_SIP: 'junos-sip' -> type(JUNOS_SIP), popMode;
M_Application_JUNOS_SMB: 'junos-smb' -> type(JUNOS_SMB), popMode;
M_Application_JUNOS_SMB_SESSION: 'junos-smb-session' -> type(JUNOS_SMB_SESSION), popMode;
M_Application_JUNOS_SMTP: 'junos-smtp' -> type(JUNOS_SMTP), popMode;
M_Application_JUNOS_SMTPS: 'junos-smtps' -> type(JUNOS_SMTPS), popMode;
M_Application_JUNOS_SNMP_AGENTX: 'junos-snmp-agentx' -> type(JUNOS_SNMP_AGENTX), popMode;
M_Application_JUNOS_SNPP: 'junos-snpp' -> type(JUNOS_SNPP), popMode;
M_Application_JUNOS_SQL_MONITOR: 'junos-sql-monitor' -> type(JUNOS_SQL_MONITOR), popMode;
M_Application_JUNOS_SQLNET_V1: 'junos-sqlnet-v1' -> type(JUNOS_SQLNET_V1), popMode;
M_Application_JUNOS_SQLNET_V2: 'junos-sqlnet-v2' -> type(JUNOS_SQLNET_V2), popMode;
M_Application_JUNOS_SSH: 'junos-ssh' -> type(JUNOS_SSH), popMode;
M_Application_JUNOS_STUN_TCP: 'junos-stun-tcp' -> type(JUNOS_STUN_TCP), popMode;
M_Application_JUNOS_STUN_UDP: 'junos-stun-udp' -> type(JUNOS_STUN_UDP), popMode;
M_Application_JUNOS_SUN_RPC_ANY_TCP: 'junos-sun-rpc-any-tcp' -> type(JUNOS_SUN_RPC_ANY_TCP), popMode;
M_Application_JUNOS_SUN_RPC_ANY_UDP: 'junos-sun-rpc-any-udp' -> type(JUNOS_SUN_RPC_ANY_UDP), popMode;
M_Application_JUNOS_SUN_RPC_MOUNTD_TCP: 'junos-sun-rpc-mountd-tcp' -> type(JUNOS_SUN_RPC_MOUNTD_TCP), popMode;
M_Application_JUNOS_SUN_RPC_MOUNTD_UDP: 'junos-sun-rpc-mountd-udp' -> type(JUNOS_SUN_RPC_MOUNTD_UDP), popMode;
M_Application_JUNOS_SUN_RPC_NFS_TCP: 'junos-sun-rpc-nfs-tcp' -> type(JUNOS_SUN_RPC_NFS_TCP), popMode;
M_Application_JUNOS_SUN_RPC_NFS_UDP: 'junos-sun-rpc-nfs-udp' -> type(JUNOS_SUN_RPC_NFS_UDP), popMode;
M_Application_JUNOS_SUN_RPC_NLOCKMGR_TCP: 'junos-sun-rpc-nlockmgr-tcp' -> type(JUNOS_SUN_RPC_NLOCKMGR_TCP), popMode;
M_Application_JUNOS_SUN_RPC_NLOCKMGR_UDP: 'junos-sun-rpc-nlockmgr-udp' -> type(JUNOS_SUN_RPC_NLOCKMGR_UDP), popMode;
M_Application_JUNOS_SUN_RPC_PORTMAP_TCP: 'junos-sun-rpc-portmap-tcp' -> type(JUNOS_SUN_RPC_PORTMAP_TCP), popMode;
M_Application_JUNOS_SUN_RPC_PORTMAP_UDP: 'junos-sun-rpc-portmap-udp' -> type(JUNOS_SUN_RPC_PORTMAP_UDP), popMode;
M_Application_JUNOS_SUN_RPC_RQUOTAD_TCP: 'junos-sun-rpc-rquotad-tcp' -> type(JUNOS_SUN_RPC_RQUOTAD_TCP), popMode;
M_Application_JUNOS_SUN_RPC_RQUOTAD_UDP: 'junos-sun-rpc-rquotad-udp' -> type(JUNOS_SUN_RPC_RQUOTAD_UDP), popMode;
M_Application_JUNOS_SUN_RPC_RUSERD_TCP: 'junos-sun-rpc-ruserd-tcp' -> type(JUNOS_SUN_RPC_RUSERD_TCP), popMode;
M_Application_JUNOS_SUN_RPC_RUSERD_UDP: 'junos-sun-rpc-ruserd-udp' -> type(JUNOS_SUN_RPC_RUSERD_UDP), popMode;
M_Application_JUNOS_SUN_RPC_SADMIND_TCP: 'junos-sun-rpc-sadmind-tcp' -> type(JUNOS_SUN_RPC_SADMIND_TCP), popMode;
M_Application_JUNOS_SUN_RPC_SADMIND_UDP: 'junos-sun-rpc-sadmind-udp' -> type(JUNOS_SUN_RPC_SADMIND_UDP), popMode;
M_Application_JUNOS_SUN_RPC_SPRAYD_TCP: 'junos-sun-rpc-sprayd-tcp' -> type(JUNOS_SUN_RPC_SPRAYD_TCP), popMode;
M_Application_JUNOS_SUN_RPC_SPRAYD_UDP: 'junos-sun-rpc-sprayd-udp' -> type(JUNOS_SUN_RPC_SPRAYD_UDP), popMode;
M_Application_JUNOS_SUN_RPC_STATUS_TCP: 'junos-sun-rpc-status-tcp' -> type(JUNOS_SUN_RPC_STATUS_TCP), popMode;
M_Application_JUNOS_SUN_RPC_STATUS_UDP: 'junos-sun-rpc-status-udp' -> type(JUNOS_SUN_RPC_STATUS_UDP), popMode;
M_Application_JUNOS_SUN_RPC_TCP: 'junos-sun-rpc-tcp' -> type(JUNOS_SUN_RPC_TCP), popMode;
M_Application_JUNOS_SUN_RPC_UDP: 'junos-sun-rpc-udp' -> type(JUNOS_SUN_RPC_UDP), popMode;
M_Application_JUNOS_SUN_RPC_WALLD_TCP: 'junos-sun-rpc-walld-tcp' -> type(JUNOS_SUN_RPC_WALLD_TCP), popMode;
M_Application_JUNOS_SUN_RPC_WALLD_UDP: 'junos-sun-rpc-walld-udp' -> type(JUNOS_SUN_RPC_WALLD_UDP), popMode;
M_Application_JUNOS_SUN_RPC_YPBIND_TCP: 'junos-sun-rpc-ypbind-tcp' -> type(JUNOS_SUN_RPC_YPBIND_TCP), popMode;
M_Application_JUNOS_SUN_RPC_YPBIND_UDP: 'junos-sun-rpc-ypbind-udp' -> type(JUNOS_SUN_RPC_YPBIND_UDP), popMode;
M_Application_JUNOS_SUN_RPC_YPSERV_TCP: 'junos-sun-rpc-ypserv-tcp' -> type(JUNOS_SUN_RPC_YPSERV_TCP), popMode;
M_Application_JUNOS_SUN_RPC_YPSERV_UDP: 'junos-sun-rpc-ypserv-udp' -> type(JUNOS_SUN_RPC_YPSERV_UDP), popMode;
M_Application_JUNOS_SYSLOG: 'junos-syslog' -> type(JUNOS_SYSLOG), popMode;
M_Application_JUNOS_TACACS: 'junos-tacacs' -> type(JUNOS_TACACS), popMode;
M_Application_JUNOS_TACACS_DS: 'junos-tacacs-ds' -> type(JUNOS_TACACS_DS), popMode;
M_Application_JUNOS_TALK: 'junos-talk' -> type(JUNOS_TALK), popMode;
M_Application_JUNOS_TCP_ANY: 'junos-tcp-any' -> type(JUNOS_TCP_ANY), popMode;
M_Application_JUNOS_TELNET: 'junos-telnet' -> type(JUNOS_TELNET), popMode;
M_Application_JUNOS_TFTP: 'junos-tftp' -> type(JUNOS_TFTP), popMode;
M_Application_JUNOS_UDP_ANY: 'junos-udp-any' -> type(JUNOS_UDP_ANY), popMode;
M_Application_JUNOS_UUCP: 'junos-uucp' -> type(JUNOS_UUCP), popMode;
M_Application_JUNOS_VDO_LIVE: 'junos-vdo-live' -> type(JUNOS_VDO_LIVE), popMode;
M_Application_JUNOS_VNC: 'junos-vnc' -> type(JUNOS_VNC), popMode;
M_Application_JUNOS_WAIS: 'junos-wais' -> type(JUNOS_WAIS), popMode;
M_Application_JUNOS_WHO: 'junos-who' -> type(JUNOS_WHO), popMode;
M_Application_JUNOS_WHOIS: 'junos-whois' -> type(JUNOS_WHOIS), popMode;
M_Application_JUNOS_WINFRAME: 'junos-winframe' -> type(JUNOS_WINFRAME), popMode;
M_Application_JUNOS_WXCONTROL: 'junos-wxcontrol' -> type(JUNOS_WXCONTROL), popMode;
M_Application_JUNOS_X_WINDOWS: 'junos-x-windows' -> type(JUNOS_X_WINDOWS), popMode;
M_Application_JUNOS_XNM_CLEAR_TEXT: 'junos-xnm-clear-text' -> type(JUNOS_XNM_CLEAR_TEXT), popMode;
M_Application_JUNOS_XNM_SSL: 'junos-xnm-ssl' -> type(JUNOS_XNM_SSL), popMode;
M_Application_JUNOS_YMSG: 'junos-ymsg' -> type(JUNOS_YMSG), popMode;

// application-sets also included
M_Application_JUNOS_CIFS: 'junos-cifs' -> type(JUNOS_CIFS), popMode;
M_Application_JUNOS_MGCP: 'junos-mgcp' -> type(JUNOS_MGCP), popMode;
M_Application_JUNOS_MS_RPC: 'junos-ms-rpc' -> type(JUNOS_MS_RPC), popMode;
M_Application_JUNOS_MS_RPC_ANY: 'junos-ms-rpc-any' -> type(JUNOS_MS_RPC_ANY), popMode;
M_Application_JUNOS_MS_RPC_IIS_COM: 'junos-ms-rpc-iis-com' -> type(JUNOS_MS_RPC_IIS_COM), popMode;
M_Application_JUNOS_MS_RPC_MSEXCHANGE: 'junos-ms-rpc-msexchange' -> type(JUNOS_MS_RPC_MSEXCHANGE), popMode;
M_Application_JUNOS_MS_RPC_WMIC: 'junos-ms-rpc-wmic' -> type(JUNOS_MS_RPC_WMIC), popMode;
M_Application_JUNOS_ROUTING_INBOUND: 'junos-routing-inbound' -> type(JUNOS_ROUTING_INBOUND), popMode;
M_Application_JUNOS_STUN: 'junos-stun' -> type(JUNOS_STUN), popMode;
M_Application_JUNOS_SUN_RPC: 'junos-sun-rpc' -> type(JUNOS_SUN_RPC), popMode;
M_Application_JUNOS_SUN_RPC_ANY: 'junos-sun-rpc-any' -> type(JUNOS_SUN_RPC_ANY), popMode;
M_Application_JUNOS_SUN_RPC_MOUNTD: 'junos-sun-rpc-mountd' -> type(JUNOS_SUN_RPC_MOUNTD), popMode;
M_Application_JUNOS_SUN_RPC_NFS: 'junos-sun-rpc-nfs' -> type(JUNOS_SUN_RPC_NFS), popMode;
M_Application_JUNOS_SUN_RPC_NFS_ACCESS: 'junos-sun-rpc-nfs-access' -> type(JUNOS_SUN_RPC_NFS_ACCESS), popMode;
M_Application_JUNOS_SUN_RPC_NLOCKMGR: 'junos-sun-rpc-nlockmgr' -> type(JUNOS_SUN_RPC_NLOCKMGR), popMode;
M_Application_JUNOS_SUN_RPC_PORTMAP: 'junos-sun-rpc-portmap' -> type(JUNOS_SUN_RPC_PORTMAP), popMode;
M_Application_JUNOS_SUN_RPC_RQUOTAD: 'junos-sun-rpc-rquotad' -> type(JUNOS_SUN_RPC_RQUOTAD), popMode;
M_Application_JUNOS_SUN_RPC_RUSERD: 'junos-sun-rpc-ruserd' -> type(JUNOS_SUN_RPC_RUSERD), popMode;
M_Application_JUNOS_SUN_RPC_SADMIND: 'junos-sun-rpc-sadmind' -> type(JUNOS_SUN_RPC_SADMIND), popMode;
M_Application_JUNOS_SUN_RPC_SPRAYD: 'junos-sun-rpc-sprayd' -> type(JUNOS_SUN_RPC_SPRAYD), popMode;
M_Application_JUNOS_SUN_RPC_STATUS: 'junos-sun-rpc-status' -> type(JUNOS_SUN_RPC_STATUS), popMode;
M_Application_JUNOS_SUN_RPC_WALLD: 'junos-sun-rpc-walld' -> type(JUNOS_SUN_RPC_WALLD), popMode;
M_Application_JUNOS_SUN_RPC_YPBIND: 'junos-sun-rpc-ypbind' -> type(JUNOS_SUN_RPC_YPBIND), popMode;
M_Application_JUNOS_SUN_RPC_YPSERV: 'junos-sun-rpc-ypserv' -> type(JUNOS_SUN_RPC_YPSERV), popMode;

M_Application_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_Application_NAME: F_Name -> type(NAME), popMode;
M_Application_WS: F_WhitespaceChar+ -> skip;
M_Application_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_ApplicationSet;
M_ApplicationSet_JUNOS_CIFS: 'junos-cifs' -> type(JUNOS_CIFS), popMode;
M_ApplicationSet_JUNOS_MGCP: 'junos-mgcp' -> type(JUNOS_MGCP), popMode;
M_ApplicationSet_JUNOS_MS_RPC: 'junos-ms-rpc' -> type(JUNOS_MS_RPC), popMode;
M_ApplicationSet_JUNOS_MS_RPC_ANY: 'junos-ms-rpc-any' -> type(JUNOS_MS_RPC_ANY), popMode;
M_ApplicationSet_JUNOS_MS_RPC_IIS_COM: 'junos-ms-rpc-iis-com' -> type(JUNOS_MS_RPC_IIS_COM), popMode;
M_ApplicationSet_JUNOS_MS_RPC_MSEXCHANGE: 'junos-ms-rpc-msexchange' -> type(JUNOS_MS_RPC_MSEXCHANGE), popMode;
M_ApplicationSet_JUNOS_MS_RPC_WMIC: 'junos-ms-rpc-wmic' -> type(JUNOS_MS_RPC_WMIC), popMode;
M_ApplicationSet_JUNOS_ROUTING_INBOUND: 'junos-routing-inbound' -> type(JUNOS_ROUTING_INBOUND), popMode;
M_ApplicationSet_JUNOS_STUN: 'junos-stun' -> type(JUNOS_STUN), popMode;
M_ApplicationSet_JUNOS_SUN_RPC: 'junos-sun-rpc' -> type(JUNOS_SUN_RPC), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_ANY: 'junos-sun-rpc-any' -> type(JUNOS_SUN_RPC_ANY), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_MOUNTD: 'junos-sun-rpc-mountd' -> type(JUNOS_SUN_RPC_MOUNTD), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_NFS: 'junos-sun-rpc-nfs' -> type(JUNOS_SUN_RPC_NFS), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_NFS_ACCESS: 'junos-sun-rpc-nfs-access' -> type(JUNOS_SUN_RPC_NFS_ACCESS), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_NLOCKMGR: 'junos-sun-rpc-nlockmgr' -> type(JUNOS_SUN_RPC_NLOCKMGR), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_PORTMAP: 'junos-sun-rpc-portmap' -> type(JUNOS_SUN_RPC_PORTMAP), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_RQUOTAD: 'junos-sun-rpc-rquotad' -> type(JUNOS_SUN_RPC_RQUOTAD), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_RUSERD: 'junos-sun-rpc-ruserd' -> type(JUNOS_SUN_RPC_RUSERD), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_SADMIND: 'junos-sun-rpc-sadmind' -> type(JUNOS_SUN_RPC_SADMIND), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_SPRAYD: 'junos-sun-rpc-sprayd' -> type(JUNOS_SUN_RPC_SPRAYD), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_STATUS: 'junos-sun-rpc-status' -> type(JUNOS_SUN_RPC_STATUS), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_WALLD: 'junos-sun-rpc-walld' -> type(JUNOS_SUN_RPC_WALLD), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_YPBIND: 'junos-sun-rpc-ypbind' -> type(JUNOS_SUN_RPC_YPBIND), popMode;
M_ApplicationSet_JUNOS_SUN_RPC_YPSERV: 'junos-sun-rpc-ypserv' -> type(JUNOS_SUN_RPC_YPSERV), popMode;

M_ApplicationSet_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_ApplicationSet_NAME: F_Name -> type(NAME), popMode;
M_ApplicationSet_WS: F_WhitespaceChar+ -> skip;
M_ApplicationSet_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SecretString;

M_SecretString_QUOTED_STRING: F_DoubleQuotedString -> type(SECRET_STRING), popMode;
M_SecretString_WORD: F_NonWhitespaceChar+ -> type(SECRET_STRING), popMode;

M_SecretString_WS: F_WhitespaceChar+ -> skip;
M_SecretString_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AddressSpecifier;
M_AddressSpecifier_ANY: 'any' -> type(ANY), popMode;
M_AddressSpecifier_ANY_IPV4: 'any-ipv4' -> type(ANY_IPV4), popMode;
M_AddressSpecifier_ANY_IPV6: 'any-ipv6' -> type(ANY_IPV6), popMode;
M_AddressSpecifier_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_AddressSpecifier_IP_PREFIX: F_IpPrefix -> type(IP_PREFIX), popMode;
M_AddressSpecifier_IPV6_PREFIX: F_Ipv6Prefix -> type(IPV6_PREFIX), popMode;
M_AddressSpecifier_NAME: F_Name -> type(NAME), popMode;
M_AddressSpecifier_WS: F_WhitespaceChar+ -> skip;
M_AddressSpecifier_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SourceIdentity;
M_SourceIdentity_ANY: 'any' -> type(ANY), popMode;
M_SourceIdentity_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_SourceIdentity_NAME: F_Name -> type(NAME), popMode;
M_SourceIdentity_WS: F_WhitespaceChar+ -> skip;
M_SourceIdentity_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_PrefixName;
M_PrefixName_ROUTING_INSTANCE: 'routing-instance' -> type(ROUTING_INSTANCE), mode(M_RoutingInstanceName);
M_PrefixName_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_PrefixName_NAME: F_Name -> type(NAME), popMode;
M_PrefixName_WS: F_WhitespaceChar+ -> skip;
M_PrefixName_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_CertificatesLocal;
M_CertificatesLocal_NAME: F_Name -> type(NAME), mode(M_Certificate);
M_CertificatesLocal_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_CertificatesLocal_WS: F_WhitespaceChar+ -> skip;
M_CertificatesLocal_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Certificate;
M_Certificate_CERTIFICATE_STRING: F_CertificateString -> type(CERTIFICATE_STRING), popMode;
M_Certificate_SCRUBBED: F_Scrubbed -> type(SCRUBBED), popMode;
M_Certificate_WS: F_WhitespaceChar+ -> skip;
M_Certificate_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_RestOfLine;
M_RestOfLine_WS: F_WhitespaceChar+ -> skip;
M_RestOfLine_QUOTED_STRING: F_DoubleQuotedString -> type(IGNORED_WORD);
M_RestOfLine_WORD: F_NonWhitespaceChar+ -> type(IGNORED_WORD);
M_RestOfLine_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_ProxyIdentityService;
M_ProxyIdentityService_ANY: 'any' -> type(ANY), popMode;
M_ProxyIdentityService_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_ProxyIdentityService_NAME: F_Name -> type(NAME), popMode;
M_ProxyIdentityService_WS: F_WhitespaceChar+ -> skip;
M_ProxyIdentityService_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Screen;
M_Screen_UNTRUST_SCREEN: 'untrust-screen' -> type(UNTRUST_SCREEN), popMode;
M_Screen_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_Screen_NAME: F_Name -> type(NAME), popMode;
M_Screen_WS: F_WhitespaceChar+ -> skip;
M_Screen_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Zone;
M_Zone_JUNOS_HOST: 'junos-host' -> type(JUNOS_HOST), popMode;
M_Zone_TRUST: 'trust' -> type(TRUST), popMode;
M_Zone_UNTRUST: 'untrust' -> type(UNTRUST), popMode;
M_Zone_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_Zone_NAME: F_Name -> type(NAME), popMode;
M_Zone_WS: F_WhitespaceChar+ -> skip;
M_Zone_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_FabricDevice;
M_FabricDevice_WILDCARD: F_Wildcard {setWildcard();};
M_FabricDevice_NAME: F_Name -> type(NAME);
M_FabricDevice_WS: F_WhitespaceChar+ -> skip;
M_FabricDevice_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_InterfaceRoutesRibGroup;
M_InterfaceRoutesRibGroup_INET: 'inet' -> type(INET), mode(M_Name);
M_InterfaceRoutesRibGroup_INET6: 'inet6' -> type(INET6), mode(M_Name);
M_InterfaceRoutesRibGroup_WS: F_WhitespaceChar+ -> skip;
M_InterfaceRoutesRibGroup_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_InterfaceSet;
M_InterfaceSet_NAME: F_Name -> type(NAME), mode(M_InterfaceIdOrInterfaceWildcard);
M_InterfaceSet_WS: F_WhitespaceChar+ -> skip;
M_InterfaceSet_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_InterfaceIdOrInterfaceWildcard;
M_InterfaceIdOrInterfaceWildcard_INTERFACE_ID: F_InterfaceId -> type(INTERFACE_ID), popMode;
M_InterfaceIdOrInterfaceWildcard_INTERFACE_WILDCARD: F_NonWhitespaceChar+ -> type(INTERFACE_WILDCARD), popMode;
M_InterfaceIdOrInterfaceWildcard_WS: F_WhitespaceChar+ -> skip;
M_InterfaceIdOrInterfaceWildcard_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_IsisRibGroup;
M_IsisRibGroup_INET: 'inet' -> type(INET), mode(M_Name);
M_IsisRibGroup_WS: F_WhitespaceChar+ -> skip;
M_IsisRibGroup_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Filter;
M_Filter_WILDCARD: F_Wildcard {setWildcard();};
M_Filter_INPUT: 'input' -> type(INPUT);
M_Filter_INPUT_LIST: 'input-list' -> type(INPUT_LIST);
M_Filter_OUTPUT: 'output' -> type(OUTPUT);
M_Filter_OUTPUT_LIST: 'output-list' -> type(OUTPUT_LIST);
M_Filter_NAME: F_Name -> type(NAME), popMode;
M_Filter_WS: F_WhitespaceChar+ -> skip;
M_Filter_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_ApplyGroups;
M_ApplyGroups_WS: F_WhitespaceChar+ -> skip;
M_ApplyGroups_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_ApplyGroups_NODE: '"${node}"' -> type(NAME), popMode;
M_ApplyGroups_NAME: F_Name -> type(NAME), popMode;

mode M_Queue;
M_Queue_DEC: F_Digit+ -> type(DEC), mode(M_QueueNumber);
M_Queue_WS: F_WhitespaceChar+ -> skip;
M_Queue_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_QueueNumber;
M_QueueNumber_PRIORITY: 'priority' -> type(PRIORITY), mode(M_QueueNumber2);
M_QueueNumber_NAME: F_Name -> type(NAME), popMode;
M_QueueNumber_WS: F_WhitespaceChar+ -> skip;
M_QueueNumber_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_QueueNumber2;
M_QueueNumber2_HIGH: 'high' -> type(HIGH), mode(M_Name);
M_QueueNumber2_LOW: 'low' -> type(LOW), mode(M_Name);
M_QueueNumber2_WS: F_WhitespaceChar+ -> skip;
M_QueueNumber2_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SubRange;
M_SubRange_WS: F_WhitespaceChar+ -> skip;
M_SubRange_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_SubRange_UINT8: F_Uint8 -> type(UINT8);
M_SubRange_UINT16: F_Uint16 -> type(UINT16);
M_SubRange_DASH: '-' -> type(DASH), mode(M_SubRangeDash);
M_SubRange_OTHER: F_Alpha F_NonWhitespaceChar* { less(); } -> popMode;

mode M_SubRangeDash;
M_SubRangeDash_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_SubRangeDash_UINT8: F_Uint8 -> type(UINT8), popMode;
M_SubRangeDash_UINT16: F_Uint16 -> type(UINT16), popMode;

mode M_Range;
M_Range_WS: F_WhitespaceChar+ -> mode(M_Range2);
M_Range_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_Range2;
M_Range2_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);
M_Range2_DEC: F_Digit+ -> type(DEC);
M_Range2_DASH: '-' -> type(DASH);
M_Range2_COMMA: ',' -> type(COMMA);
M_Range2_WS: F_WhitespaceChar+ -> skip, popMode;
M_Range2_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_PrefixLengthRange;
M_PrefixLengthRange_WS: F_WhitespaceChar+ -> skip, mode(M_PrefixLengthRange2);
M_PrefixLengthRange_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_PrefixLengthRange2;
M_PrefixLengthRange2_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);
M_PrefixLengthRange2_DEC: F_Digit+ -> type(DEC);
M_PrefixLengthRange2_DASH: '-' -> type(DASH);
M_PrefixLengthRange2_FORWARD_SLASH: '/' -> type(FORWARD_SLASH);
M_PrefixLengthRange2_WS: F_WhitespaceChar+ -> skip, popMode;
M_PrefixLengthRange2_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_PrefixLength;
M_PrefixLength_WS: F_WhitespaceChar+ -> skip, mode(M_PrefixLength2);
M_PrefixLength_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_PrefixLength2;
M_PrefixLength2_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);
M_PrefixLength2_DEC: F_Digit+ -> type(DEC);
M_PrefixLength2_FORWARD_SLASH: '/' -> type(FORWARD_SLASH);
M_PrefixLength2_WS: F_WhitespaceChar+ -> skip, popMode;
M_PrefixLength2_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_MacAddressAndLength;
M_MacAddressAndLength_WS: F_WhitespaceChar+ -> skip, mode(M_MacAddressAndLength2);
M_MacAddressAndLength_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_MacAddressAndLength2;
M_MacAddressAndLength2_APPLY_GROUPS: 'apply-groups' -> type(APPLY_GROUPS), mode(M_ApplyGroups);
M_MacAddressAndLength2_DEC: F_Digit+ -> type(DEC);
M_MacAddressAndLength2_MAC_ADDESS: F_MacAddress -> type(MAC_ADDRESS);
M_MacAddressAndLength2_FORWARD_SLASH: '/' -> type(FORWARD_SLASH);
M_MacAddressAndLength2_WS: F_WhitespaceChar+ -> skip, popMode;
M_MacAddressAndLength2_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AsPathExpr;
M_AsPathExpr_WS: F_WhitespaceChar+ -> skip, mode(M_AsPathExpr2);
M_AsPathExpr_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AsPathExpr2;
M_AsPathExpr2_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathExpr2_WS: F_WhitespaceChar+ -> skip, popMode;
M_AsPathExpr2_UINT8: F_Uint8 -> type(UINT8);
M_AsPathExpr2_UINT16: F_Uint16 -> type(UINT16);
M_AsPathExpr2_UINT32: F_Uint32 -> type(UINT32);
M_AsPathExpr2_PERIOD: '.' -> type(PERIOD);
M_AsPathExpr2_DOUBLE_QUOTE: '"' -> skip, mode(M_AsPathExprQuoted);

mode M_AsPathExprQuoted;
M_AsPathExprQuoted_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_AsPathExprQuoted_WS: F_WhitespaceChar+ -> skip;
M_AsPathExprQuoted_DOUBLE_QUOTE: '"' -> skip, popMode;
M_AsPathExprQuoted_UINT8: F_Uint8 -> type(UINT8);
M_AsPathExprQuoted_UINT16: F_Uint16 -> type(UINT16);
M_AsPathExprQuoted_UINT32: F_Uint32 -> type(UINT32);
M_AsPathExprQuoted_PERIOD: '.' -> type(PERIOD);
M_AsPathExprQuoted_OPEN_BRACKET: '[' -> type(OPEN_BRACKET);
M_AsPathExprQuoted_CLOSE_BRACKET: ']' -> type(CLOSE_BRACKET);

mode M_Port;
M_Port_WS: F_WhitespaceChar+ -> skip;
M_Port_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Port_UINT8: F_Uint8 -> type(UINT8);
M_Port_UINT16: F_Uint16 -> type(UINT16);
M_Port_DASH: '-' -> type(DASH);

// Not a range. We can continue in default mode since words need not be broken up.
M_Port_NON_RANGE: [A-Za-z]+ {less();} -> popMode;

mode M_VlanIdList;
M_VlanIdList_WS: F_WhitespaceChar+ -> skip;
M_VlanIdList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_VlanIdList_UINT16: F_Uint16 -> type(UINT16);
M_VlanIdList_DASH: '-' -> type(DASH);

mode M_ExtendedVniList;
M_ExtendedVniList_WS: F_WhitespaceChar+ -> skip;
M_ExtendedVniList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_ExtendedVniList_ALL: 'all' -> type(ALL), popMode;
M_ExtendedVniList_OPEN_BRACKET: '[' -> type(OPEN_BRACKET);
M_ExtendedVniList_CLOSE_BRACKET: ']' -> type(CLOSE_BRACKET), popMode;
M_ExtendedVniList_UINT32: F_Uint32 -> type(UINT32), mode(M_ExtendedVniListNumber);

mode M_ExtendedVniListNumber;
M_ExtendedVniListNumber_WS: F_WhitespaceChar+ -> skip, mode(M_ExtendedVniList);
M_ExtendedVniListNumber_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_ExtendedVniListNumber_CLOSE_BRACKET: ']' -> type(CLOSE_BRACKET), popMode;
M_ExtendedVniListNumber_DASH: '-' -> type(DASH), mode(M_ExtendedVniListDash);

mode M_ExtendedVniListDash;
M_ExtendedVniListDash_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_ExtendedVniListDash_UINT32: F_Uint32 -> type(UINT32), mode(M_ExtendedVniList);

mode M_IcmpCodeOrType;
M_IcmpCodeOrType_WS: F_WhitespaceChar+ -> skip;
M_IcmpCodeOrType_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_IcmpCodeOrType_RANGE: F_Digit+ ('-' F_Digit+)? { less(); } -> mode(M_SubRange);
M_IcmpCodeOrType_NAMED: F_NonWhitespaceChar+ { less(); } -> popMode;

mode M_BgpAsn;
M_BgpAsn_WS: F_WhitespaceChar+ -> skip;
M_BgpAsn_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_BgpAsn_ASN: F_Digit+ ('.' F_Digit+)? { less(); } -> mode(M_BgpAsn2);
M_BgpAsn_OTHER: F_NonWhitespaceChar+ { less(); } -> popMode;

mode M_BgpAsn2;
M_BgpAsn2_WS: F_WhitespaceChar+ -> skip, popMode;
M_BgpAsn2_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_BgpAsn2_UINT8: F_Uint8 -> type(UINT8);
M_BgpAsn2_UINT16: F_Uint16 -> type(UINT16);
M_BgpAsn2_UINT32: F_Uint32 -> type(UINT32);
M_BgpAsn2_PERIOD: '.' -> type(PERIOD);

mode M_RibName;
M_RibName_INET: (F_RoutingInstanceName PERIOD)? INET PERIOD UINT8 -> type(INET_RIB_NAME), popMode;
M_RibName_INET6: (F_RoutingInstanceName PERIOD)? INET6 PERIOD UINT8 -> type(INET6_RIB_NAME), popMode;
M_RibName_MPLS: MPLS PERIOD UINT8 -> type(MPLS_RIB_NAME), popMode;
M_RibName_ISO: ISO PERIOD UINT8 -> type(ISO_RIB_NAME), popMode;
M_RibName_BGP: BGP PERIOD ( L2VPN | L3VPN | L3VPN_INET6 ) PERIOD UINT8 -> type(BGP_RIB_NAME), popMode;
M_RibName_VXLAN: COLON VXLAN PERIOD INET PERIOD UINT8 -> type(VXLAN_RIB_NAME), popMode;
M_RibName_WS: F_WhitespaceChar+ -> skip;
M_RibName_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_AdminGroup;
M_AdminGroup_EXCLUDE: 'exclude' -> type(EXCLUDE), mode(M_Name);
M_AdminGroup_INCLUDE_ALL: 'include-all' -> type(INCLUDE_ALL), mode(M_Name);
M_AdminGroup_INCLUDE_ANY: 'include-any' -> type(INCLUDE_ANY), mode(M_Name);
M_AdminGroup_WILDCARD: F_Wildcard {setWildcard();} -> popMode;
M_AdminGroup_NAME: F_Name -> type(NAME), popMode;
M_AdminGroup_WS: F_WhitespaceChar+ -> skip;
M_AdminGroup_NEWLINE: F_NewlineChar+ -> type(NEWLINE), popMode;
