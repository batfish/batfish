lexer grammar CiscoLexer;

options {
   superClass = 'batfish.grammar.BatfishLexer';
}

@header {
package batfish.grammar.cisco;
}

@members {
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;
boolean enableACL_NUM = false;
boolean enableCOMMUNITY_LIST_NUM = false;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   sb.append("enableDEC: " + enableDEC + "\n");
   sb.append("enableACL_NUM: " + enableACL_NUM+ "\n");
   sb.append("enableCOMMUNITY_LIST_NUM: " + enableCOMMUNITY_LIST_NUM + "\n");
   return sb.toString();
}

}

tokens {
   ACL_NUM_APPLETALK,
   ACL_NUM_EXTENDED,
   ACL_NUM_EXTENDED_IPX,
   ACL_NUM_IPX,
   ACL_NUM_IPX_SAP,
   ACL_NUM_OTHER,
   ACL_NUM_PROTOCOL_TYPE_CODE,
   ACL_NUM_STANDARD,
   COMMUNITY_LIST_NUM_EXPANDED,
   COMMUNITY_LIST_NUM_STANDARD
}

// Cisco Keywords

AAA
:
   'aaa'
;

AAA_SERVER
:
   'aaa-server'
;

ABSOLUTE_TIMEOUT
:
   'absolute-timeout'
;

ACCEPT_DIALIN
:
   'accept-dialin'
;

ACCESS
:
   'access'
;

ACCESS_CLASS
:
   'access-class'
;

ACCESS_GROUP
:
   'access-group'
;

ACCESS_LIST
:
   'access-list'
   {enableACL_NUM = true; enableDEC = false;}

;

ACCESS_LOG
:
   'access-log'
;

ACCOUNTING
:
   'accounting'
;

ACCOUNTING_LIST
:
   'accounting-list'
;

ACCOUNTING_PORT
:
   'accounting-port'
;

ACCOUNTING_SERVER_GROUP
:
   'accounting-server-group'
;

ACTION
:
   'action'
;

ACTIVATE
:
   'activate'
;

ACTIVATION_CHARACTER
:
   'activation-character'
;

ACTIVE
:
   'active'
;

ADD
:
   'add'
;

ADDITIVE
:
   'additive'
;

ADDRESS
:
   'address'
;

ADDRESS_FAMILY
:
   'address-family'
;

ADDRESS_POOL
:
   'address-pool'
;

ADMINISTRATIVE_WEIGHT
:
   'administrative-weight'
;

ADMISSION
:
   'admission'
;

AES128_SHA1
:
   'aes128-sha1'
;

AES256_SHA1
:
   'aes256-sha1'
;

AESA
:
   'aesa'
;

AGGREGATE
:
   'aggregate'
;

AGGREGATE_ADDRESS
:
   'aggregate-address'
;

AHP
:
   'ahp'
;

ALERT_GROUP
:
   'alert-group'
;

ALIAS
:
   'alias'
;

ALLOWED
:
   'allowed'
;

ALLOWAS_IN
:
   'allowas-in'
;

ALWAYS
:
   'always'
;

ALWAYS_COMPARE_MED
:
   'always-compare-med'
;

ANY
:
   'any'
;

ANYCONNECT
:
   'anyconnect'
;

ANYCONNECT_ESSENTIALS
:
   'anyconnect-essentials'
;

AP
:
   'ap'
;

AQM_REGISTER_FNF
:
   'aqm-register-fnf'
;

ARCHIVE
:
   'archive'
;

AREA
:
   'area'
;

ARP
:
   'arp'
   { enableIPV6_ADDRESS = false; }

;

AS_PATH
:
   'as-path' -> pushMode(M_AsPath)
;

ASA
:
   'ASA'
;

ASDM
:
   'asdm'
;

ASSOCIATE
:
   'associate'
;

ASSOCIATION
:
   'association'
;

ASYNC
:
   'async'
;

ASYNC_BOOTP
:
   'async-bootp'
;

ATM
:
   'atm'
;

AUDIT
:
   'audit'
;

AUTH_PROXY
:
   'auth-proxy'
;

AUTHENTICATION
:
   'authentication'
;

AUTHENTICATION_PORT
:
   'authentication-port'
;

AUTHENTICATION_SERVER_GROUP
:
   'authentication-server-group'
;

AUTHORIZATION
:
   'authorization'
;

AUTHORIZATION_REQUIRED
:
   'authorization-required'
;

AUTHORIZATION_SERVER_GROUP
:
   'authorization-server-group'
;

AUTO
:
   'auto'
;

AUTO_COST
:
   'auto-cost'
;

AUTO_SUMMARY
:
   'auto-summary'
;

AUTO_SYNC
:
   'auto-sync'
;

AUTOSELECT
:
   'autoselect'
;

AUTOSTATE
:
   'autostate'
;

BACKGROUND_ROUTES_ENABLE
:
   'background-routes-enable'
;

BACKUPCRF
:
   'backupcrf'
;

BANDWIDTH
:
   'bandwidth'
;

BANNER
:
   'banner' ' '+
   (
      'exec'
      | 'login'
      | 'motd'
   ) -> pushMode(M_BANNER)
;

BASH
:
   'bash'
;

BESTPATH
:
   'bestpath'
;

BFD
:
   'bfd'
;

BGP
:
   'bgp'
;

BGP_COMMUNITY
:
   'bgp-community'
;

BIND
:
   'bind'
;

BOOT
:
   'boot'
;

BOOT_END_MARKER
:
   'boot-end-marker'
;

BOOT_START_MARKER
:
   'boot-start-marker'
;

BOOTP
:
   'bootp'
;

BOOTPC
:
   'bootpc'
;

BOOTPS
:
   'bootps'
;

BOTH
:
   'both'
;

BRIDGE
:
   'bridge'
;

BROADCAST
:
   'broadcast'
;

CA
:
   'ca'
;

CABLE_RANGE
:
   'cable-range'
;

CABLELENGTH
:
   'cablelength' -> pushMode(M_COMMENT)
;

CACHE
:
   'cache'
;

CACHE_TIMEOUT
:
   'cache-timeout'
;

CALL
:
   'call'
;

CALL_HOME
:
   'call-home'
;

CALLER_ID
:
   'caller-id'
;

CARD
:
   'card'
;

CAS_CUSTOM
:
   'cas-custom'
;

CCM
:
   'ccm'
;

CCM_GROUP
:
   'ccm-group'
;

CCM_MANAGER
:
   'ccm-manager'
;

CDP
:
   'cdp'
;

CEF
:
   'cef'
;

CERTIFICATE
:
   'certificate' -> pushMode(M_CERTIFICATE)
;

CFS
:
   'cfs'
;

CGMP
:
   'cgmp'
;

CHANNEL
:
   'channel'
;

CHANNEL_GROUP
:
   'channel-group'
;

CHANNEL_PROTOCOL
:
   'channel-protocol'
;

CHANNELIZED
:
   'channelized'
;

CIPC
:
   'cipc'
;

CLASS
:
   'class'
;

CLASSLESS
:
   'classless'
;

CLASS_MAP
:
   'class-map'
;

CLI
:
   'cli'
;

CLNS
:
   'clns'
;

CLOCK
:
   'clock'
;

CLUSTER
:
   'cluster'
;

CLUSTER_ID
:
   'cluster-id'
;

CMD
:
   'cmd'
;

CNS
:
   'cns'
;

CODEC
:
   'codec'
;

COLLECT
:
   'collect'
;

COMM_LIST
:
   'comm-list'
;

COMMANDER_ADDRESS
:
   'commander-address'
   { enableIPV6_ADDRESS = false; }

;

COMMUNITY
:
   'community'
   { enableIPV6_ADDRESS = false; }

;

COMMUNITY_LIST
:
   'community-list'
   {
      enableIPV6_ADDRESS = false;
      enableCOMMUNITY_LIST_NUM = true;
      enableDEC = false;
   }

;

CONFIG_REGISTER
:
   'config-register'
;

CONFIGURATION
:
   'configuration'
;

CONFORM_ACTION
:
   'conform-action'
;

CONGESTION_CONTROL
:
   'congestion-control'
;

CONNECTED
:
   'connected'
;

CONSOLE
:
   'console'
;

CONTACT_EMAIL_ADDR
:
   'contact-email-addr'
;

CONTEXT
:
   'context'
;

CONTROL_PLANE
:
   'control-plane'
;

CONTROLLER
:
   'controller'
;

COPY
:
   'copy'
;

COST
:
   'cost'
;

COUNTER
:
   'counter'
;

CPTONE
:
   'cptone'
;

CRYPTO
:
   'crypto'
;

CRL
:
   'crl'
;

CTL_FILE
:
   'ctl-file'
;

CTS
:
   'cts'
;

DAMPENING
:
   'dampening'
;

DATABITS
:
   'databits'
;

DBL
:
   'dbl'
;

DCBX
:
   'dcbx'
;

DEAD_INTERVAL
:
   'dead-interval'
;

DECAP_GROUP
:
   'decap-group'
;

DEFAULT
:
   'default'
;

DEFAULT_ACTION
:
   'default-action'
;

DEFAULT_DOMAIN
:
   'default-domain'
;

DEFAULT_GATEWAY
:
   'default-gateway'
;

DEFAULT_GROUP_POLICY
:
   'default-group-policy'
;

DEFAULT_INFORMATION
:
   'default-information'
;

DEFAULT_INFORMATION_ORIGINATE
:
   'default-information-originate'
;

DEFAULT_METRIC
:
   'default-metric'
;

DEFAULT_NETWORK
:
   'default-network'
;

DEFAULT_ORIGINATE
:
   'default-originate'
;

DEFAULT_ROUTER
:
   'default-router'
;

DEFINITION
:
   'definition'
;

DELAY
:
   'delay'
;

DELETE
:
   'delete'
;

DENY
:
   'deny'
;

DES
:
   'des' -> pushMode(M_DES)
;

DES_SHA1
:
   'des-sha1'
;

DESCRIPTION
:
   'description' -> pushMode(M_DESCRIPTION)
;

DESIRABLE
:
   'desirable'
;

DESTINATION
:
   'destination'
;

DETERMINISTIC_MED
:
   'deterministic-med'
;

DEVICE
:
   'device'
;

DEVICE_SENSOR
:
   'device-sensor'
;

DF
:
   'df'
;

DHCP
:
   'dhcp'
;

DHCPD
:
   'dhcpd'
;

DIAGNOSTIC
:
   'diagnostic'
;

DIAL_PEER
:
   'dial-peer'
;

DIALER_LIST
:
   'dialer-list'
;

DIRECT
:
   'direct'
;

DIRECTED_BROADCAST
:
   'directed-broadcast'
;

DISABLE
:
   'disable'
;

DISTANCE
:
   'distance'
;

DISTRIBUTE_LIST
:
   'distribute-list'
;

DNS
:
   'dns'
;

DNS_GUARD
:
   'dns-guard'
;

DNS_SERVER
:
   'dns-server'
;

DOMAIN
:
   'domain'
;

DOMAIN_ID
:
   'domain-id'
;

DOMAIN_LIST
:
   'domain-list'
;

DOMAIN_LOOKUP
:
   'domain-lookup'
;

DOMAIN_NAME
:
   'domain-name'
;

DONT_CAPABILITY_NEGOTIATE
:
   'dont-capability-negotiate'
;

DOT11
:
   'dot11'
;

DOT1Q
:
   'dot1q'
;

DROP
:
   'drop'
;

DS0_GROUP
:
   'ds0-group'
;

DSP
:
   'dsp'
;

DSPFARM
:
   'dspfarm'
;

DSS
:
   'dss'
;

DSU
:
   'dsu'
;

DUPLEX
:
   'duplex'
;

DVMRP
:
   'dvmrp'
;

DYNAMIC
:
   'dynamic'
;

DYNAMIC_ACCESS_POLICY_RECORD
:
   'dynamic-access-policy-record'
;

DYNAMIC_MAP
:
   'dynamic-map'
;

EBGP_MULTIHOP
:
   'ebgp-multihop'
;

ECHO
:
   'echo'
;

ECHO_REPLY
:
   'echo-reply'
;

EGP
:
   'egp'
;

EIGRP
:
   'eigrp'
;

EMPTY
:
   'empty'
;

ENABLE
:
   'enable'
;

ENCAPSULATION
:
   'encapsulation'
;

ENCR
:
   'encr'
;

ENCRYPTION
:
   'encryption'
;

END
:
   'end'
;

ENGINEID
:
   'engineid' -> pushMode(M_COMMENT)
;

ENROLLMENT
:
   'enrollment'
;

ENVIRONMENT
:
   'environment'
;

EOF_LITERAL
:
   'EOF'
;

EQ
:
   'eq'
;

ERRDISABLE
:
   'errdisable'
;

ESCAPE_CHARACTER
:
   'escape-character'
;

ESP
:
   'esp'
;

ESTABLISHED
:
   'established'
;

EVALUATE
:
   'evaluate'
;

EVENT
:
   'event'
;

EVENT_HANDLER
:
   'event-handler'
;

EXCEED_ACTION
:
   'exceed-action'
;

EXCEPTION
:
   'exception'
;

EXEC
:
   'exec'
;

EXEC_TIMEOUT
:
   'exec-timeout'
;

EXECUTE
:
   'execute'
;

EXIT
:
   'exit'
;

EXIT_ADDRESS_FAMILY
:
   'exit-address-family'
;

EXPORT
:
   'export'
;

EXPORT_PROTOCOL
:
   'export-protocol'
;

EXPORTER
:
   'exporter'
;

EXPANDED
:
   'expanded'
;

EXTCOMM_LIST
:
   'extcomm-list'
;

EXTCOMMUNITY_LIST
:
   'extcommunity-list'
;

EXTENDED
:
   'extended'
   { enableDEC = true; enableACL_NUM = false; }

;

FABRIC
:
   'fabric'
;

FACILITY_ALARM
:
   'facility-alarm'
;

FAILOVER
:
   'failover'
;

FAIR_QUEUE
:
   'fair-queue'
;

FALL_OVER
:
   'fall-over'
;

FALLBACK_DN
:
   'fallback-dn'
;

FEATURE
:
   'feature'
;

FILE
:
   'file'
;

FILE_BROWSING
:
   'file-browsing'
;

FILE_ENTRY
:
   'file-entry'
;

FILTER_LIST
:
   'filter-list'
;

FIREWALL
:
   'firewall'
   { enableIPV6_ADDRESS = false; }

;

FINGER
:
   'finger'
;

FIRMWARE
:
   'firmware'
;

FLOW
:
   'flow'
;

FLOW_CACHE
:
   'flow-cache'
;

FLOW_EXPORT
:
   'flow-export'
;

FLOW_TOP_TALKERS
:
   'flow-top-talkers'
;

FLOWCONTROL
:
   'flowcontrol'
;

FLUSH_AT_ACTIVATION
:
   'flush-at-activation'
;

FORWARD_PROTOCOL
:
   'forward-protocol'
;

FORWARDING
:
   'forwarding'
;

FQDN
:
   'fqdn'
;

FRAGMENTS
:
   'fragments'
;

FRAME_RELAY
:
   'frame-relay'
;

FRAMING
:
   'framing'
;

FREQUENCY
:
   'frequency'
;

FTP
:
   'ftp'
;

FTP_DATA
:
   'ftp-data'
;

FTP_SERVER
:
   'ftp-server'
;

FULL_DUPLEX
:
   'full-duplex'
;

GATEKEEPER
:
   'gatekeeper'
;

GATEWAY
:
   'gateway'
;

GE
:
   'ge'
;

GRACEFUL_RESTART
:
   'graceful-restart'
;

GRATUITOUS_ARPS
:
   'gratuitous-arps'
;

GRE
:
   'gre'
;

GROUP
:
   'group'
;

GROUP_ALIAS
:
   'group-alias'
;

GROUP_OBJECT
:
   'group-object'
;

GROUP_POLICY
:
   'group-policy'
;

GROUP_RANGE
:
   'group-range'
;

GROUP_URL
:
   'group-url'
;

GT
:
   'gt'
;

HALF_DUPLEX
:
   'half-duplex'
;

HARDWARE
:
   'hardware'
;

HASH
:
   'hash'
;

HELLO_MULTIPLIER
:
   'hello-multiplier'
;

HELPER_ADDRESS
:
   'helper-address'
;

HIDDEN
:
   'hidden'
;

HIDDEN_SHARES
:
   'hidden-shares'
;

HIDEKEYS
:
   'hidekeys'
;

HIGH_AVAILABILITY
:
   'high-availability'
;

HISTORY
:
   'history'
;

HOLD_QUEUE
:
   'hold-queue'
;

HOST
:
   'host'
;

HOST_ROUTING
:
   'host-routing'
;

HOST_UNKNOWN
:
   'host-unknown'
;

HOST_UNREACHABLE
:
   'host-unreachable'
;

HOSTNAME
:
   'hostname'
;

HSRP
:
   'hsrp'
;

HTTP
:
   'http'
;

HW_MODULE
:
   'hw-module'
;

ICMP
:
   'icmp'
;

ICMP_ECHO
:
   'icmp-echo'
;

ICMP_OBJECT
:
   'icmp-object'
;

IDENT
:
   'ident'
;

IDENTITY
:
   'identity'
;

IDLE_TIMEOUT
:
   'idle-timeout'
;

IGMP
:
   'igmp'
;

IGP
:
   'igp'
;

IKEV1
:
   'ikev1'
;

ILMI_KEEPALIVE
:
   'ilmi-keepalive'
;

IN
:
   'in'
;

INACTIVITY_TIMER
:
   'inactivity-timer'
;

INBOUND
:
   'inbound'
;

INCOMPLETE
:
   'incomplete'
;

INHERIT
:
   'inherit'
;

INSPECT
:
   'inspect'
;

INSTANCE
:
   'instance'
;

INTERFACE
:
   'interface'
   { enableIPV6_ADDRESS = false; }
   -> pushMode(M_Interface)
;

INTERNAL
:
   'internal'
;

INTERNET
:
   'internet'
;

INVALID_SPI_RECOVERY
:
   'invalid-spi-recovery'
;

IP
:
   'ip'
;

IP_ADDRESS_LITERAL
:
   'ip-address'
;

IPC
:
   'ipc'
;

IPINIP
:
   'ipinip'
;

IPSEC
:
   'ipsec'
;

IPSEC_UDP
:
   'ipsec-udp'
;

IPV4
:
   'ipv4'
;

IPV6
:
   'ipv6'
;

IPV6_ADDRESS_POOL
:
   'ipv6-address-pool'
;

IPX
:
   'ipx'
;

IRDP
:
   'irdp'
;

ISAKMP
:
   'isakmp'
;

ISDN
:
   'isdn'
;

ISL
:
   'isl'
;

KEEPALIVE
:
   'keepalive'
;

KEEPALIVE_ENABLE
:
   'keepalive-enable'
;

KEEPOUT
:
   'keepout'
;

KEY
:
   'key' -> pushMode(M_KEY)
;

KEYPAIR
:
   'keypair'
;

KEYRING
:
   'keyring'
;

LACP
:
   'lacp'
;

LANE
:
   'lane'
;

LAPB
:
   'lapb'
;

LAST_AS
:
   'last-as'
;

LE
:
   'le'
;

L2TP
:
   'l2tp'
;

L2TP_CLASS
:
   'l2tp-class'
;

LDAP_BASE_DN
:
   'ldap-base-dn'
;

LDAP_LOGIN
:
   'ldap-login'
;

LDAP_LOGIN_DN
:
   'ldap-login-dn'
;

LDAP_NAMING_ATTRIBUTE
:
   'ldap-naming-attribute'
;

LDAP_SCOPE
:
   'ldap-scope'
;

LENGTH
:
   'length'
;

LICENSE
:
   'license'
;

LIFETIME
:
   'lifetime'
;

LIMIT
:
   'limit'
;

LINE
:
   'line'
;

LINECODE
:
   'linecode'
;

LISTEN
:
   'listen'
;

LLDP
:
   'lldp'
;

LOAD_INTERVAL
:
   'load-interval'
;

LOAD_SHARING
:
   'load-sharing'
;

LOCAL
:
   'local'
;

LOCAL_AS
:
   'local-as'
;

LOCAL_INTERFACE
:
   'local-interface'
;

LOCAL_IP
:
   'local-ip'
;

LOCAL_PORT
:
   'local-port'
;

LOCAL_PREFERENCE
:
   'local-preference'
;

LOCATION
:
   'location' -> pushMode(M_COMMENT)
;

LOG
:
   'log'
;

LOG_ADJACENCY_CHANGES
:
   'log-adjacency-changes'
;

LOG_INPUT
:
   'log-input'
;

LOG_NEIGHBOR_CHANGES
:
   'log-neighbor-changes'
;

LOGGING
:
   'logging'
;

LOGIN
:
   'login'
;

LPD
:
   'lpd'
;

LRE
:
   'lre'
;

LT
:
   'lt'
;

MAC
:
   'mac'
;

MAC_ADDRESS
:
   'mac-address' -> pushMode(M_COMMENT)
;

MAC_ADDRESS_TABLE
:
   'mac-address-table'
;

MACRO
:
   'macro'
;

MAIL_SERVER
:
   'mail-server'
;

MAIN_CPU
:
   'main-cpu'
;

MANAGEMENT
:
   'management'
;

MANAGEMENT_ONLY
:
   'management-only'
;

MAP
:
   'map'
;

MAP_CLASS
:
   'map-class'
;

MAP_GROUP
:
   'map-group'
;

MAP_LIST
:
   'map-list'
;

MASK
:
   'mask'
;

MATCH
:
   'match'
;

MAXIMUM
:
   'maximum'
;

MAXIMUM_PATHS
:
   'maximum-paths'
;

MAXIMUM_PEERS
:
   'maximum-peers'
;

MAXIMUM_PREFIX
:
   'maximum-prefix'
;

MAXIMUM_ROUTES
:
   'maximum-routes'
;

MDIX
:
   'mdix'
;

MEDIA_TERMINATION
:
   'media-termination'
;

MEDIA_TYPE
:
   'media-type'
;

MEMBER
:
   'member'
;

MEMORY_SIZE
:
   'memory-size'
;

MESSAGE_DIGEST_KEY
:
   'message-digest-key'
;

MESSAGE_LENGTH
:
   'message-length'
;

METRIC
:
   'metric'
;

METRIC_TYPE
:
   'metric-type'
;

MFIB
:
   'mfib'
;

MFIB_MODE
:
   'mfib-mode'
;

MGCP
:
   'mgcp'
;

MICROCODE
:
   'microcode'
;

MINIMAL
:
   'minimal'
;

MLAG
:
   'mlag'
;

MLD
:
   'mld'
;

MLS
:
   'mls'
;

MOBILITY
:
   'mobility'
;

MODE
:
   'mode'
;

MODEM
:
   'modem'
;

MODULE
:
   'module'
;

MONITOR
:
   'monitor'
;

MOP
:
   'mop'
;

MOTD
:
   'motd'
;

MPLS
:
   'mpls'
;

MPLS_LABEL
:
   'mpls-label'
;

MROUTE
:
   'mroute'
;

MROUTE_CACHE
:
   'mroute-cache'
;

MSDP
:
   'msdp'
;

MTA
:
   'mta'
;

MTU
:
   'mtu'
;

MULTICAST
:
   'multicast'
;

MULTICAST_ROUTING
:
   'multicast-routing'
;

MULTILINK
:
   'multilink'
;

MULTIPOINT
:
   'multipoint'
;

MVR
:
   'mvr'
;

NAME_LOOKUP
:
   'name-lookup'
;

NAME_SERVER
:
   'name-server'
;

NAMEIF
:
   'nameif'
;

NAME
:
   'name' -> pushMode(M_NAME)
;

NAMES
:
   'names'
;

NAT
:
   'nat'
;

NAT_CONTROL
:
   'nat-control'
;

NATIVE
:
   'native'
;

ND
:
   'nd'
;

ND_NA
:
   'nd-na'
;

ND_NS
:
   'nd-ns'
;

NEGOTIATE
:
   'negotiate'
;

NEGOTIATION
:
   'negotiation'
;

NEIGHBOR
:
   'neighbor' -> pushMode(M_NEIGHBOR)
;

NEQ
:
   'neq'
;

NET_UNREACHABLE
:
   'net-unreachable'
;

NETBIOS_DGM
:
   'netbios-dgm'
;

NETBIOS_NS
:
   'netbios-ns'
;

NETBIOS_SS
:
   'netbios-ss'
;

NETCONF
:
   'netconf'
;

NETWORK
:
   'network'
;

NETWORK_CLOCK_PARTICIPATE
:
   'network-clock-participate'
;

NETWORK_CLOCK_SELECT
:
   'network-clock-select'
;

NETWORK_OBJECT
:
   'network-object'
;

NETWORK_UNKNOWN
:
   'network-unknown'
;

NEXT_HOP
:
   'next-hop'
;

NEXT_HOP_SELF
:
   'next-hop-self'
;

NNTP
:
   'nntp'
;

NO
:
   'no'
;

NO_ADVERTISE
:
   'no-advertise'
;

NO_BANNER
:
   'no' F_Whitespace+ 'banner'
;

NO_EXPORT
:
   'no-export'
;

NO_SUMMARY
:
   'no-summary'
;

NODE
:
   'node'
;

NON500_ISAKMP
:
   'non500-isakmp'
;

NONE
:
   'none'
;

NONEGOTIATE
:
   'nonegotiate'
;

NOTIFY
:
   'notify'
;

NSF
:
   'nsf'
;

NSSA
:
   'nssa'
;

NTP
:
   'ntp'
;

OBJECT
:
   'object'
;

OBJECT_GROUP
:
   'object-group'
;

OPENFLOW
:
   'openflow'
;

ORIGIN
:
   'origin'
;

ORIGINATE
:
   'originate'
;

OSPF
:
   'ospf'
;

OTHER_ACCESS
:
   'other-access'
;

OUI
:
   'oui' -> pushMode(M_COMMENT)
;

OUT
:
   'out'
;

OWNER
:
   'owner'
;

PACKET_TOO_BIG
:
   'packet-too-big'
;

PAGER
:
   'pager'
;

PARAMETER_PROBLEM
:
   'parameter-problem'
;

PARAMETERS
:
   'parameters'
;

PARENT
:
   'parent'
;

PARSER
:
   'parser'
;

PARTICIPATE
:
   'participate'
;

PASSIVE_INTERFACE
:
   'passive-interface'
;

PASSWORD
:
   'password' -> pushMode(M_COMMENT)
;

PASSWORD_STORAGE
:
   'password-storage'
;

PATH_JITTER
:
   'path-jitter'
;

PAUSE
:
   'pause'
;

PEER
:
   'peer'
;

PEER_GROUP
:
   'peer-group' -> pushMode(M_NEIGHBOR)
;

PERMANENT
:
   'permanent'
;

PEER_ADDRESS
:
   'peer-address'
;

PEER_GATEWAY
:
   'peer-gateway'
;

PEER_KEEPALIVE
:
   'peer-keepalive'
;

PEER_LINK
:
   'peer-link'
;

PERMIT
:
   'permit'
;

PHONE_PROXY
:
   'phone-proxy'
;

PHYSICAL_LAYER
:
   'physical-layer'
;

PICKUP
:
   'pickup'
;

PIM
:
   'pim'
;

PIM_AUTO_RP
:
   'pim-auto-rp'
;

PKI
:
   'pki'
;

PLAT
:
   'plat'
;

PLATFORM
:
   'platform'
;

POLICE
:
   'police'
;

POLICY
:
   'policy'
;

POLICY_LIST
:
   'policy-list'
;

POLICY_MAP
:
   'policy-map'
;

POP3
:
   'pop3'
;

PORT
:
   'port'
;

PORT_CHANNEL
:
   'port-channel'
;

PORT_OBJECT
:
   'port-object'
;

PORT_SECURITY
:
   'port-security'
;

PORT_UNREACHABLE
:
   'port-unreachable'
;

POWER
:
   'power'
;

PPP
:
   'ppp'
;

PRE_SHARED_KEY
:
   'pre-shared-key'
;

PREEMPT
:
   'preempt'
;

PREFIX
:
   'prefix'
;

PREFIX_LIST
:
   'prefix-list'
;

PREPEND
:
   'prepend'
;

PRI_GROUP
:
   'pri-group'
;

PRIORITY
:
   'priority'
;

PRIORITY_FLOW_CONTROL
:
   'priority-flow-control'
;

PRIORITY_QUEUE
:
   'priority-queue'
;

PRIVATE_VLAN
:
   'private-vlan'
;

PRIVILEGE
:
   'privilege'
;

PROCESS
:
   'process'
;

PROFILE
:
   'profile'
;

PROMPT
:
   'prompt'
;

PROTOCOL
:
   'protocol'
;

PROTOCOL_OBJECT
:
   'protocol-object'
;

PROXY_ARP
:
   'proxy-arp'
;

PSEUDOWIRE_CLASS
:
   'pseudowire-class'
;

QOS
:
   'qos'
;

QUEUE_BUFFERS
:
   'queue-buffers'
;

QUEUE_LIMIT
:
   'queue-limit'
;

QUEUE_SET
:
   'queue-set'
;

QUIT
:
   'quit'
;

RADIUS
:
   'radius'
;

RADIUS_COMMON_PW
:
   'radius-common-pw'
;

RADIUS_SERVER
:
   'radius-server'
;

RANDOM_DETECT
:
   'random-detect'
;

RANGE
:
   'range'
;

RATE_LIMIT
:
   'rate-limit'
;

RC4_SHA1
:
   'rc4-sha1'
;

RCMD
:
   'rcmd'
;

RCV_QUEUE
:
   'rcv-queue'
;

RD
:
   'rd'
;

RECORD
:
   'record'
;

RECORD_ENTRY
:
   'record-entry'
;

REDIRECT
:
   'redirect'
;

REDIRECT_FQDN
:
   'redirect-fqdn'
;

REDIRECTS
:
   'redirects'
;

REDISTRIBUTE
:
   'redistribute'
;

REDUNDANCY
:
   'redundancy'
;

REFLECT
:
   'reflect'
;

RELOAD_DELAY
:
   'reload-delay'
;

REMARK
:
   'remark' -> pushMode(M_REMARK)
;

REMOTE_AS
:
   'remote-as'
;

REMOTE_IP
:
   'remote-ip'
;

REMOTE_PORT
:
   'remote-port'
;

REMOVE_PRIVATE_AS
:
   'remove-private-as'
;

REMOTE_SPAN
:
   'remote-span'
;

REMOVED
:
   '<removed>'
;

RESOURCE
:
   'resource'
;

RESOURCE_POOL
:
   'resource-pool'
;

RETRANSMIT
:
   'retransmit'
;

REVERSE_ROUTE
:
   'reverse-route'
;

REVISION
:
   'revision'
;

REVOCATION_CHECK
:
   'revocation-check'
;

RING
:
   'ring'
;

RIP
:
   'rip'
;

ROLE
:
   'role'
;

ROTARY
:
   'rotary'
;

ROUTE
:
   'route'
;

ROUTE_CACHE
:
   'route-cache'
;

ROUTE_MAP
:
   'route-map'
;

ROUTE_REFLECTOR_CLIENT
:
   'route-reflector-client'
;

ROUTE_TARGET
:
   'route-target'
;

ROUTER
:
   'router'
;

ROUTER_ID
:
   'router-id'
;

ROUTING
:
   'routing'
;

RSAKEYPAIR
:
   'rsakeypair'
;

RTR
:
   'rtr'
;

RST
:
   'rst'
;

RULE
:
   'rule' -> pushMode(M_Rule)
;

SAME_SECURITY_TRAFFIC
:
   'same-security-traffic'
;

SAP
:
   'sap'
;

SCCP
:
   'sccp'
;

SCHEDULE
:
   'schedule'
;

SCHEDULER
:
   'scheduler'
;

SCHEME
:
   'scheme'
;

SCP
:
   'scp'
;

SCRIPTING
:
   'scripting'
;

SCTP
:
   'sctp'
;

SDM
:
   'sdm'
;

SECONDARY
:
   'secondary'
;

SECURITY
:
   'security'
;

SECURITY_LEVEL
:
   'security-level'
;

SEND_COMMUNITY
:
   'send-community'
;

SEND_LABEL
:
   'send-label'
;

SENDER
:
   'sender'
;

SEQ
:
   'seq'
;

SEQUENCE
:
   'sequence'
;

SERIAL
:
   'serial'
;

SERIAL_NUMBER
:
   'serial-number'
;

SERVER
:
   'server'
;

SERVER_PRIVATE
:
   'server-private'
;

SERVER_TYPE
:
   'server-type'
;

SERVICE
:
   'service'
;

SERVICE_MODULE
:
   'service-module'
;

SERVICE_POLICY
:
   'service-policy'
;

SERVICE_TYPE
:
   'service-type'
;

SESSION_DISCONNECT_WARNING
:
   'session-disconnect-warning' -> pushMode(M_COMMENT)
;

SESSION_LIMIT
:
   'session-limit'
;

SESSION_TIMEOUT
:
   'session-timeout'
;

SET
:
   'set'
;

SETUP
:
   'setup'
;

SFLOW
:
   'sflow'
;

SHA1
:
   'sha1' -> pushMode(M_SHA1)
;

SHAPE
:
   'shape'
;

SHELL
:
   'shell'
;

SHUTDOWN
:
   'shutdown'
;

SINGLE_ROUTER_MODE
:
   'single-router-mode'
;

SLA
:
   'sla'
;

SMTP
:
   'smtp'
;

SMTP_SERVER
:
   'smtp-server'
;

SNMP
:
   'snmp'
;

SNMP_SERVER
:
   'snmp-server'
;

SNMPTRAP
:
   'snmptrap'
;

SORT_BY
:
   'sort-by'
;

SPE
:
   'spe'
;

SOFT_RECONFIGURATION
:
   'soft-reconfiguration'
;

SONET
:
   'sonet'
;

SOURCE
:
   'source'
;

SOURCE_INTERFACE
:
   'source-interface'
;

SOURCE_IP_ADDRESS
:
   'source-ip-address'
;

SOURCE_ROUTE
:
   'source-route'
;

SOURCE_QUENCH
:
   'source-quench'
;

SPANNING_TREE
:
   'spanning-tree'
;

SPD
:
   'spd'
;

SPEED
:
   'speed'
;

SPLIT_TUNNEL_NETWORK_LIST
:
   'split-tunnel-network-list'
;

SPLIT_TUNNEL_POLICY
:
   'split-tunnel-policy'
;

SRR_QUEUE
:
   'srr-queue'
;

SSH
:
   'ssh'
;

SSL
:
   'ssl'
;

STACK_MIB
:
   'stack-mib'
;

STANDARD
:
   'standard'
   { enableDEC = true; enableACL_NUM = false; }

;

STANDBY
:
   'standby'
;

STATIC
:
   'static'
;

STCAPP
:
   'stcapp'
;

STOPBITS
:
   'stopbits'
;

STORM_CONTROL
:
   'storm-control'
;

STP
:
   'stp'
;

SUBJECT_NAME
:
   'subject-name'
;

SUBNET
:
   'subnet'
;

SUBNETS
:
   'subnets'
;

SUBNET_ZERO
:
   'subnet-zero'
;

SUBSCRIBE_TO
:
   'subscribe-to'
;

SUBSCRIBE_TO_ALERT_GROUP
:
   'subscribe-to-alert-group'
;

SUBSCRIBER
:
   'subscriber'
;

SUMMARY_ONLY
:
   'summary-only'
;

SUNRPC
:
   'sunrpc'
;

SUPPLEMENTARY_SERVICES
:
   'supplementary-services'
;

SWITCH
:
   'switch'
;

SWITCHBACK
:
   'switchback'
;

SWITCHING_MODE
:
   'switching-mode'
;

SWITCHPORT
:
   'switchport'
;

SYNC
:
   'sync'
;

SYNCHRONIZATION
:
   'synchronization'
;

SYSLOG
:
   'syslog'
;

SYSOPT
:
   'sysopt'
;

SYSTEM
:
   'system'
;

TABLE_MAP
:
   'table-map'
;

TACACS
:
   'tacacs'
;

TACACS_PLUS
:
   'tacacs+'
;

TACACS_SERVER
:
   'tacacs-server'
;

TAG
:
   'tag'
;

TAG_SWITCHING
:
   'tag-switching'
;

TB_VLAN1
:
   'tb-vlan1'
;

TB_VLAN2
:
   'tb-vlan2'
;

TCAM
:
   'tcam'
;

TCP
:
   'tcp'
;

TELNET
:
   'telnet'
;

TEMPLATE
:
   'template'
;

TERMINAL
:
   'terminal'
;

TERMINAL_TYPE
:
   'terminal-type'
;

TFTP
:
   'tftp'
;

TFTP_SERVER
:
   'tftp-server'
;

THREAT_DETECTION
:
   'threat-detection'
;

THREE_DES
:
   '3des'
;

THREE_DES_SHA1
:
   '3des-sha1'
;

TIME_EXCEEDED
:
   'time-exceeded'
;

TIMEOUT
:
   'timeout'
;

TIMEOUTS
:
   'timeouts'
;

TIMER
:
   'timer'
;

TIMERS
:
   'timers'
;

TIMING
:
   'timing'
;

TLS_PROXY
:
   'tls-proxy'
;

TOP
:
   'top'
;

TRACK
:
   'track'
;

TRACKED
:
   'tracked'
;

TRANSLATE
:
   'translate'
;

TRANSPORT
:
   'transport'
;

TRIGGER
:
   'trigger'
;

TRUNK
:
   'trunk'
;

TRUST
:
   'trust'
;

TRUSTPOINT
:
   'trustpoint'
;

TRUSTPOOL
:
   'trustpool'
;

TTL
:
   'ttl'
;

TTL_EXCEEDED
:
   'ttl-exceeded'
;

TUNNEL
:
   'tunnel'
;

TUNNEL_GROUP
:
   'tunnel-group'
;

TUNNEL_GROUP_LIST
:
   'tunnel-group-list'
;

TX_QUEUE
:
   'tx-queue'
;

TYPE
:
   'type'
;

UC_TX_QUEUE
:
   'uc-tx-queue'
;

UDLD
:
   'udld'
;

UDP
:
   'udp'
;

UNABLE
:
   'Unable'
;

UNICAST_ROUTING
:
   'unicast-routing'
;

UNNUMBERED
:
   'unnumbered'
;

UNREACHABLE
:
   'unreachable'
;

UNREACHABLES
:
   'unreachables'
;

UNICAST
:
   'unicast'
;

UPDATE_SOURCE
:
   'update-source' -> pushMode(M_Interface)
;

UPGRADE
:
   'upgrade'
;

USE_VRF
:
   'use-vrf'
;

USER_IDENTITY
:
   'user-identity'
;

USERNAME
:
   'username'
;

VALIDATION_USAGE
:
   'validation-usage'
;

VERIFY
:
   'verify'
;

VERSION
:
   'version'
;

VIOLATE_ACTION
:
   'violate-action'
;

VIRTUAL_REASSEMBLY
:
   'virtual-reassembly'
;

VIRTUAL_ROUTER
:
   'virtual-router'
;

VIRTUAL_TEMPLATE
:
   'virtual-template'
;

VLAN
:
   'vlan'
;

VMPS
:
   'vmps'
;

VOICE
:
   'voice'
;

VOICE_CARD
:
   'voice-card'
;

VOICE_PORT
:
   'voice-port'
;

VPC
:
   'vpc'
;

VPDN
:
   'vpdn'
;

VPDN_GROUP
:
   'vpdn-group'
;

VPN
:
   'vpn'
;

VPNV4
:
   'vpnv4'
;

VPNV6
:
   'vpnv6'
;

VPN_FILTER
:
   'vpn-filter'
;

VPN_IDLE_TIMEOUT
:
   'vpn-idle-timeout'
;

VPN_TUNNEL_PROTOCOL
:
   'vpn-tunnel-protocol'
;

VRF
:
   'vrf'
   {enableIPV6_ADDRESS = false;}

;

VRRP
:
   'vrrp'
;

VTP
:
   'vtp'
;

WEBVPN
:
   'webvpn'
;

WEIGHT
:
   'weight'
;

WINS_SERVER
:
   'wins-server'
;

WITHOUT_CSD
:
   'without-csd'
;

WLAN
:
   'wlan'
;

WRR_QUEUE
:
   'wrr-queue'
;

WSMA
:
   'wsma'
;

WWW
:
   'www'
;

X25
:
   'x25'
;

X29
:
   'x29'
;

XCONNECT
:
   'xconnect'
;

XLATE
:
   'xlate'
;

XML
:
   'xml'
;

// commonly used to hide password and keys

XX_HIDE
:
   'xx' 'x'+
;

// Other Tokens

COMMUNITY_NUMBER
:
   F_Digit
   {!enableIPV6_ADDRESS}?

   F_Digit* ':' F_Digit+
;

VARIABLE
:
   (
      (
         (
            F_Variable_RequiredVarChar
            {!enableIPV6_ADDRESS}?

            F_Variable_VarChar*
         )
         |
         (
            F_Variable_RequiredVarChar_Ipv6
            {enableIPV6_ADDRESS}?

            F_Variable_VarChar_Ipv6*
         )
      )
      |
      (
         (
            F_Variable_VarChar
            {!enableIPV6_ADDRESS}?

            F_Variable_VarChar* F_Variable_RequiredVarChar F_Variable_VarChar*
         )
         |
         (
            F_Variable_VarChar_Ipv6
            {enableIPV6_ADDRESS}?

            F_Variable_VarChar_Ipv6* F_Variable_RequiredVarChar_Ipv6
            F_Variable_VarChar_Ipv6*
         )
      )
   )
   {
      if (enableACL_NUM) {
         enableACL_NUM = false;
         enableDEC = true;
      }
      if (enableCOMMUNITY_LIST_NUM) {
         enableCOMMUNITY_LIST_NUM = false;
         enableDEC = true;
      }
   }

;

ACL_NUM
:
   F_Digit
   {enableACL_NUM}?

   F_Digit*
   {
	int val = Integer.parseInt(getText());
	if ((1 <= val && val <= 99) || (1300 <= val && val <= 1999)) {
		_type = ACL_NUM_STANDARD;
	}
	else if ((100 <= val && val <= 199) || (2000 <= val && val <= 2699)) {
		_type = ACL_NUM_EXTENDED;
	}
	else if (200 <= val && val <= 299) {
		_type = ACL_NUM_PROTOCOL_TYPE_CODE;
	}
	else if (600 <= val && val <= 699) {
		_type = ACL_NUM_APPLETALK;
	}
	else if (800 <= val && val <= 899) {
		_type = ACL_NUM_IPX;
	}
	else if (900 <= val && val <= 999) {
		_type = ACL_NUM_EXTENDED_IPX;
	}
	else if (1000 <= val && val <= 1099) {
		_type = ACL_NUM_IPX_SAP;
	}
	else {
		_type = ACL_NUM_OTHER;
	}
	enableDEC = true;
	enableACL_NUM = false;
}

;

AMPERSAND
:
   '&'
;

ANGLE_BRACKET_LEFT
:
   '<'
;

ANGLE_BRACKET_RIGHT
:
   '>'
;

ASTERISK
:
   '*'
;

AT
:
   '@'
;

BACKSLASH
:
   '\\'
;

BRACE_LEFT
:
   '{'
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

CARAT
:
   '^'
;

COLON
:
   ':'
;

COMMA
:
   ','
;

COMMUNITY_LIST_NUM
:
   F_Digit
   {enableCOMMUNITY_LIST_NUM}?

   F_Digit*
   {
		int val = Integer.parseInt(getText());
		if (1 <= val && val <= 99) {
			_type = COMMUNITY_LIST_NUM_STANDARD;
		}
		else if (100 <= val && val <= 500) {
			_type = COMMUNITY_LIST_NUM_EXPANDED;
		}
		enableCOMMUNITY_LIST_NUM = false;
		enableDEC = true;
	}

;

COMMENT_LINE
:
   '!' F_NonNewline* F_Newline+ -> channel(HIDDEN)
;

DASH
:
   '-'
;

DOLLAR
:
   '$'
;

DEC
:
   F_Digit
   {enableDEC}?

   F_Digit*
;

DOUBLE_QUOTE
:
   '"'
;

EQUALS
:
   '='
;

ESCAPE_C
:
   (
      '^C'
      | '\u0003'
      | '#'
   )
;

FLOAT
:
   (
      F_PositiveDigit* F_Digit '.' F_Digit+
   )
;

FORWARD_SLASH
:
   '/'
;

HEX
:
   '0x' F_HexDigit+
;

IP_ADDRESS
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit?
;

IPV6_ADDRESS
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
   )
   (
      F_HexDigit+
   )?
;

IPV6_PREFIX
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
      (
         F_HexDigit+
      )?
   ) '/' F_DecByte
;

NEWLINE
:
   F_Newline+
   {
   	enableIPV6_ADDRESS = true;
   	enableIP_ADDRESS = true;
  }

;

PAREN_LEFT
:
   '('
;

PAREN_RIGHT
:
   ')'
;

PERCENT
:
   '%'
;

PERIOD
:
   '.'
;

PLUS
:
   '+'
;

POUND
:
   '#'
;

SEMICOLON
:
   ';'
;

SINGLE_QUOTE
:
   '\''
;

UNDERSCORE
:
   '_'
;

WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

// Fragments

fragment
F_DecByte
:
   (
      F_PositiveDigit F_Digit F_Digit
   )
   |
   (
      F_PositiveDigit F_Digit
   )
   | F_Digit
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_HexDigit
:
   (
      '0' .. '9'
      | 'a' .. 'f'
      | 'A' .. 'F'
   )
;

fragment
F_HexWord
:
   F_HexDigit F_HexDigit F_HexDigit F_HexDigit
;

fragment
F_Letter
:
   F_LowerCaseLetter
   | F_UpperCaseLetter
;

fragment
F_LowerCaseLetter
:
   'a' .. 'z'
;

fragment
F_Newline
:
   [\n\r]
;

fragment
F_NonNewline
:
   ~[\n\r]
;

fragment
F_PositiveHexDigit
:
   (
      '1' .. '9'
      | 'a' .. 'f'
      | 'A' .. 'F'
   )
;

fragment
F_PositiveDigit
:
   '1' .. '9'
;

fragment
F_UpperCaseLetter
:
   'A' .. 'Z'
;

fragment
F_Variable_RequiredVarChar
:
   ~( '0' .. '9' | [ \t\n\r/.,-] )
;

fragment
F_Variable_RequiredVarChar_Ipv6
:
   ~( '0' .. '9' | [ \t\n\r/.,-:] )
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r]
;

fragment
F_Variable_VarChar_Ipv6
:
   ~[ \t\n\r:]
;

fragment
F_Whitespace
:
   ' '
   | '\t'
   | '\u000C'
;

mode M_AsPath;

M_AsPath_ACCESS_LIST
:
   'access-list' -> type(ACCESS_LIST), mode(M_AsPathAccessList)
;

M_AsPath_DEC
:
   F_Digit+ -> type(DEC), popMode
;

M_AsPath_PREPEND
:
   'prepend' -> type(PREPEND), popMode
;

M_AsPath_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_VarChar* -> type(VARIABLE), popMode
;

M_AsPath_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_AsPathAccessList;

M_AsPathAccessList_DEC
:
   F_Digit+ -> type(DEC)
;

M_AsPathAccessList_DENY
:
   'deny' -> type(DENY), mode(M_AsPathRegex)
;

M_AsPathAccessList_PERMIT
:
   'permit' -> type(PERMIT), mode(M_AsPathRegex)
;

M_AsPathAccessList_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_VarChar* -> type(VARIABLE)
;

M_AsPathAccessList_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_AsPathRegex;

M_AsPathRegex_ASTERISK
:
   '*' -> type(ASTERISK)
;

M_AsPathRegex_CARAT
:
   '^' -> type(CARAT)
;

M_AsPathRegex_DEC
:
   F_Digit+ -> type(DEC)
;

M_AsPathRegex_DOLLAR
:
   '$' -> type(DOLLAR)
;

M_AsPathRegex_DOUBLE_QUOTE
:
   '"' -> channel(HIDDEN)
;

M_AsPathRegex_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_AsPathRegex_PERIOD
:
   '.' -> type(PERIOD)
;

M_AsPathRegex_UNDERSCORE
:
   '_' -> channel(HIDDEN)
;

M_AsPathRegex_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_BANNER;

M_BANNER_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

M_BANNER_ESCAPE_C
:
   (
      '^C'
      | '\u0003'
   ) -> type(ESCAPE_C), mode(M_MOTD_C)
;

M_BANNER_HASH
:
   '#' -> type(POUND), mode(M_MOTD_HASH)
;

M_BANNER_NEWLINE
:
   F_Newline+ -> type(NEWLINE), mode(M_MOTD_EOF)
;

mode M_CERTIFICATE;

M_CERTIFICATE_QUIT
:
   'quit' -> type(QUIT), popMode
;

M_CERTIFICATE_TEXT
:
   (
      ~'q'
      |
      (
         'q' ~'u'
      )
      |
      (
         'qu' ~'i'
      )
      |
      (
         'qui' ~'t'
      )
   )+
;

mode M_COMMENT;

M_COMMENT_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_COMMENT_NON_NEWLINE
:
   F_NonNewline+
;

mode M_DES;

M_DES_DEC_PART
:
   F_Digit+
;

M_DES_HEX_PART
:
   F_HexDigit+ -> popMode
;

M_DES_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_DESCRIPTION;

M_DESCRIPTION_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_DESCRIPTION_NON_NEWLINE
:
   F_NonNewline+
;

mode M_Interface;

M_Interface_PREFIX
:
   F_Letter (F_Letter | '-')*
;

M_Interface_COLON
:
   ':' -> type(COLON)
;

M_Interface_COMMA
:
   ',' -> type(COMMA)
;

M_Interface_DASH
:
   '-' -> type(DASH)
;

M_Interface_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_Interface_NUMBER
:
   DEC -> type(DEC)
;

M_Interface_PERIOD
:
   '.' -> type(PERIOD)
;

M_Interface_SLASH
:
   '/' -> type(FORWARD_SLASH)
;

M_Interface_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_KEY;

M_KEY_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_KEY_NON_NEWLINE
:
   F_NonNewline+
;

mode M_MOTD_C;

M_MOTD_C_ESCAPE_C
:
   (
      '^C'
      | '\u0003'
   ) -> type(ESCAPE_C), mode(DEFAULT_MODE)
;

M_MOTD_C_MOTD
:
   (
      (
         '^' ~[^C\u0003]
      )
      | ~[^\u0003]
   )+
;

mode M_MOTD_EOF;

M_MOTD_EOF_EOF
:
   'EOF' -> type(EOF_LITERAL), mode(DEFAULT_MODE)
;

M_MOTD_EOF_MOTD
:
   (
      ~'E'
      |
      (
         'E' ~'O'
      )
      |
      (
         'EO' ~'F'
      )
   )+
;

mode M_MOTD_HASH;

M_MOTD_HASH_HASH
:
   '#' -> type(POUND), mode(DEFAULT_MODE)
;

M_MOTD_HASH_MOTD
:
   ~'#'+
;

mode M_NAME;

M_NAME_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

M_NAME_NAME
:
   F_NonNewline+ -> popMode
;

mode M_NEIGHBOR;

M_NEIGHBOR_VARIABLE
:
   (
      (
         (
            F_Variable_RequiredVarChar_Ipv6
            | '-'
         ) F_Variable_VarChar_Ipv6*
      )
      |
      (
         F_Variable_VarChar_Ipv6 F_Variable_VarChar_Ipv6*
         (
            F_Variable_RequiredVarChar_Ipv6
            | '-'
         ) F_Variable_VarChar_Ipv6*
      )
   ) -> type(VARIABLE), popMode
;

M_NEIGHBOR_IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte -> type(IP_ADDRESS),
   popMode
;

M_NEIGHBOR_IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit? ->
   type(IP_PREFIX), popMode
;

M_NEIGHBOR_IPV6_ADDRESS
:
   (
      (
         (
            '::'
            (
               (
                  F_HexDigit+ ':'
               )* F_HexDigit+
            )?
         )
         |
         (
            F_HexDigit+ ':' ':'?
         )+
      )
      (
         F_HexDigit+
      )?
   ) -> type ( IPV6_ADDRESS ), popMode
;

M_NEIGHBOR_IPV6_PREFIX
:
   (
      (
         '::'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+ ':' ':'?
      )+
      (
         F_HexDigit+
      )? '/' F_DecByte
   ) -> type ( IPV6_PREFIX ), popMode
;

M_NEIGHBOR_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_NEIGHBOR_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;

mode M_REMARK;

M_REMARK_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

M_REMARK_REMARK
:
   F_NonNewline+
;

mode M_Rule;

M_Rule_LINE
:
   F_NonNewline+
;

M_Rule_NEWLINE
:
   F_Newline+ -> type(NEWLINE), popMode
;

mode M_SHA1;

M_SHA1_DEC_PART
:
   F_Digit+
;

M_SHA1_HEX_PART
:
   F_HexDigit+ -> popMode
;

M_SHA1_WS
:
   F_Whitespace+ -> channel(HIDDEN)
;
