lexer grammar CiscoGrammarCommonLexer;

@header {
package batfish.grammar.cisco;
}

@members {
boolean inComment = false;
boolean inMultilineComment = false;
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;
boolean enableACL_NUM = false;
boolean enableCOMMUNITY_LIST_NUM = false;
}

tokens {
	ACL_NUM_APPLETALK,
	ACL_NUM_EXTENDED,
	ACL_NUM_EXTENDED_IPX,
	ACL_NUM_IPX,
	ACL_NUM_IPX_SAP,
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

AGGREGATE_ADDRESS
:
	'aggregate-address'
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

ALWAYS
:
	'always'
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

ARCHIVE
:
	'archive'
;

AREA
:
	'area'
;

AS_PATH
:
	'as-path'
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

AUTOSELECT
:
	'autoselect'
;

AUTO_SUMMARY
:
	'auto-summary'
;

AUTO_SYNC
:
	'auto-sync'
;

BANDWIDTH
:
	'bandwidth'
;

BANNER
:
	'banner'
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

BRIDGE
:
	'bridge'
;

CA
:
	'ca'
;

CACHE
:
	'cache'
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

CONFIG_REGISTER
:
	'config-register'
;

CONFORM_ACTION
:
	'conform-action'
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

CONTROL_PLANE
:
	'control-plane'
;

CONTROLLER
:
	'controller'
;

COST
:
	'cost'
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

DBL
:
	'dbl'
;

DEAD_INTERVAL
:
	'dead-interval'
;

DEFAULT
:
	'default'
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

DEFINITION
:
	'definition'
;

DELETE
:
	'delete'
;

DENY
:
	'deny'
;

DES_SHA1
:
	'des-sha1'
;

DESIRABLE
:
	'desirable'
;

DESTINATION
:
	'destination'
;

DEVICE
:
	'device'
;

DEVICE_SENSOR
:
	'device-sensor'
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

DIRECTED_BROADCAST
:
	'directed-broadcast'
;

DISABLE
:
	'disable'
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

DOT11
:
	'dot11'
;

DOT1Q
:
	'dot1q'
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

DUPLEX
:
	'duplex'
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

ENROLLMENT
:
	'enrollment'
;

ENVIRONMENT
:
	'environment'
;

EQ
:
	'eq'
;

ERRDISABLE
:
	'errdisable'
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

EXTENDED
:
	'extended'
;

FABRIC
:
	'fabric'
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

FORWARD_PROTOCOL
:
	'forward-protocol'
;

FQDN
:
	'fqdn'
;

FRAGMENTS
:
	'fragments'
;

FRAMING
:
	'framing'
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

HIDDEN_SHARES
:
	'hidden-shares'
;

HIDEKEYS
:
	'hidekeys'
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

HOSTNAME
:
	'hostname'
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

IDENTITY
:
	'identity'
;

IGMP
:
	'igmp'
;

IKEV1
:
	'ikev1'
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

INSPECT
:
	'inspect'
;

INTERNAL
:
	'internal'
;

INTERNET
:
	'internet'
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

KEYPAIR
:
	'keypair'
;

LAPB
:
	'lapb'
;

LE
:
	'le'
;

L2TP
:
	'l2tp'
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

LICENSE
:
	'license'
;

LIFETIME
:
	'lifetime'
;

LINE
:
	'line'
;

LINECODE
:
	'linecode'
;

LLDP
:
	'lldp'
;

LOAD_INTERVAL
:
	'load-interval'
;

LOCAL
:
	'local'
;

LOCAL_AS
:
	'local-as'
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

MANAGEMENT_ONLY
:
	'management-only'
;

MAP
:
	'map'
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

MAXIMUM_PREFIX
:
	'maximum-prefix'
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

MLD
:
	'mld'
;

MLS
:
	'mls'
;

MODE
:
	'mode'
;

MODEM
:
	'modem'
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
	'neighbor'
;

NEQ
:
	'neq'
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

NEXT_HOP
:
	'next-hop'
;

NEXT_HOP_SELF
:
	'next-hop-self'
;

NO
:
	'no'
;

NO_ADVERTISE
:
	'no-advertise'
;

NO_EXPORT
:
	'no-export'
;

NO_SUMMARY
:
	'no-summary'
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

OUT
:
	'out'
;

PACKET_TOO_BIG
:
	'packet-too-big'
;

PAGER
:
	'pager'
;

PARAMETERS
:
	'parameters'
;

PARENT
:
	'parent'
;

PARTICIPATE
:
	'participate'
;

PASSIVE_INTERFACE
:
	'passive-interface'
;

PASSWORD_STORAGE
:
	'password-storage'
;

PEER
:
	'peer'
;

PEER_GROUP
:
	'peer-group'
;

PERMANENT
:
	'permanent'
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

PRIORITY_QUEUE
:
	'priority-queue'
;

PRIVATE_VLAN
:
	'private-vlan'
;

PREFIX
:
	'prefix'
;

PREFIX_LIST
:
	'prefix-list'
;

PRIORITY_
:
	'priority'
;

PRIORITY_QUEUE_
:
	'priority-queue'
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

RANGE
:
	'range'
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

SENDER
:
	'sender'
;

SEQ
:
	'seq'
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

SHELL
:
	'shell'
;

SHUTDOWN
:
	'shutdown'
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

SPE
:
	'spe'
;

SOFT_RECONFIGURATION
:
	'soft-reconfiguration'
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

SPANNING_TREE
:
	'spanning-tree'
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

STANDARD
:
	'standard'
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

SUMMARY_ONLY
:
	'summary-only'
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

SWITCHPORT
:
	'switchport'
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

TCP
:
	'tcp'
;

TELNET
:
	'telnet'
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

TRACK
:
	'track'
;

TRANSLATE
:
	'translate'
;

TRANSPORT
:
	'transport'
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

TYPE
:
	'type'
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

UPDATE_SOURCE
:
	'update-source'
;

UPGRADE
:
	'upgrade'
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

WINS_SERVER
:
	'wins-server'
;

WITHOUT_CSD
:
	'without-csd'
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

XLATE
:
	'xlate'
;

// Other Tokens

ACL_NUM
:
	{enableACL_NUM}?

	F_Digit+
	{
	int val = Integer.parseInt(getText());
	if ((1 <= val && val <= 199) || (1300 <= val && val <= 1999)) {
		_type = ACL_NUM_STANDARD;
	}
	else if ((100 <= val && val <= 199) || (2000 <= val && val <= 2699)) {
		_type = ACL_NUM_EXTENDED;
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
	enableDEC = true;
	enableACL_NUM = false;
}

;

AMPERSAND
:
	'&'
;

ARP
:
	'arp'
	{
                   enableIPV6_ADDRESS = false;
                  }

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

CABLELENGTH
:
	'cablelength'
	{
                                   inComment = true;
                                  }

;

CARAT
:
	'^'
;

CERTIFICATE
:
	'certificate'
	{
                                   inComment = true;
                                   inMultilineComment = true;
                                  }

;

COLON
:
	':'
;

COMMA
:
	','
;

COMMANDER_ADDRESS
:
	'commander-address'
	{
                                               enableIPV6_ADDRESS = false;
                                              }

;

COMMUNITY
:
	'community'
	{
                               enableIPV6_ADDRESS = false;
                              }

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

COMMUNITY_LIST_NUM:
	{enableCOMMUNITY_LIST_NUM}? F_Digit+
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
	{!inComment}?

	(
		'!' .+? F_Newline
	)
;

COMMENT_CLOSING_LINE
:
	{!inComment}?

	(
		'!' F_Newline
	)
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
	{enableDEC}?

	F_Digit+
;

DESCRIPTION
:
	'description'
	{
                                   inComment = true;
                                  }

;

DOUBLE_QUOTE
:
	'"'
;

ENGINEID
:
	'engineid'
	{
                             inComment = true;
                            }

;

EQUALS
:
	'='
;

ESCAPE_C
:
	(
		'^C'
	)
	{
   inMultilineComment = !inMultilineComment;
   inComment = !inComment;
  }

;

EXCLAMATION_MARK
:
	{inComment}?

	'!'
;

FIREWALL
:
	'firewall'
	{
                             enableIPV6_ADDRESS = false;
                            }

;

FLOAT
:
	{!inComment}?

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

//HEX_STRING
//  :
//  {inComment || !enableIPV6_ADDRESS}?=> HEX_DIGIT+
//  ;

INTERFACE
:
	'interface'
	{enableIPV6_ADDRESS = false;}

;

SHA1_HASH
:
	'sha1' ' ' F_Digit+ ' ' F_HexDigit+
;

DES_HASH
:
	'des' ' ' F_Digit+ ' ' F_HexDigit+
;

IP_ADDRESS
:
	{!inComment && enableIP_ADDRESS}?

	(
		F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
	)
;

IPV6_ADDRESS
:
	{!inComment && enableIPV6_ADDRESS}?

	(
		(
			COLON COLON
			(
				(
					F_HexDigit+ COLON
				)* F_HexDigit+
			)?
		)
		|
		(
			F_HexDigit+ COLON COLON?
		)+
		(
			F_HexDigit+
		)?
	)
;

LOCATION
:
	'location'
	{
                             inComment = true;
                            }

;

MAC
:
	'mac'
	{
                   inComment = true;
                  }

;

MAC_ADDRESS
:
	'mac-address'
	{
                                   inComment = true;
                                  }

;

NAME
:
	'name'
	{
                     inComment = true;
                    }

;

NEWLINE
:
	F_Newline
	{
               if (!inMultilineComment) {
               	inComment = false;
               	enableIPV6_ADDRESS = true;
               	enableIP_ADDRESS = true;
               }
              }

;

OUI
:
	'oui'
	{
                   inComment = true;
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

PASSWORD
:
	'password'
	{
                             inComment = true;
                            }

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

REMARK
:
	'remark'
	{
                         inComment = true;
                        }

;

QUIT
:
	'quit'
	{
                     inMultilineComment = false;
                     inComment = false;
                    }

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

VARIABLE
:
	F_Letter
	(
		F_Letter
		| F_Digit
		| '-'
		| '_'
		| '.'
		| '/'
		| '&'
		| '+'
		| '['
		| ']'
		|
		(
			{!enableIPV6_ADDRESS}?

			':'
		)
	)*
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

WS
:
	(
		' '
		| '\t'
		| '\u000C'
	) -> skip
;

// Fragments

fragment
F_Newline
:
	[\n\r]+
;

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

