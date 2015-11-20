lexer grammar FlatJuniperLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.flatjuniper;
}

@members {
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   sb.append("enableDEC: " + enableDEC + "\n");
   return sb.toString();
}

}

tokens {
   ISO_ADDRESS,
   PIPE
}

// Juniper Keywords

ACCEPT
:
   'accept'
;

ACCEPT_DATA
:
   'accept-data'
;

ACCESS
:
   'access'
;

ACCESS_PROFILE
:
   'access-profile'
;

ACCOUNTING
:
   'accounting'
;

ACTIVE
:
   'active'
;

ADD
:
   'add'
;

ADD_PATH
:
   'add-path'
;

ADDRESS
:
   'address'
;

ADDRESS_BOOK
:
   'address-book'
;

ADDRESS_MASK
:
   'address-mask'
;

ADDRESS_SET
:
   'address-set'
;

ADVERTISE_INACTIVE
:
   'advertise-inactive'
;

ADVERTISE_INTERVAL
:
   'advertise-interval'
;

ADVERTISE_PEER_AS
:
   'advertise-peer-as'
;

AFS
:
   'afs'
;

AGGREGATE
:
   'aggregate'
;

AGGREGATED_ETHER_OPTIONS
:
   'aggregated-ether-options'
;

AGGRESSIVE
:
   'aggressive'
;

AES_128_CBC
:
   'aes-128-cbc'
;

AES_192_CBC
:
   'aes-192-cbc'
;

AES_256_CBC
:
   'aes-256-cbc'
;

AH
:
   'ah'
;

ALG
:
   'alg'
;

ALIAS
:
   'alias'
;

ALL
:
   'all'
;

ALLOW
:
   'allow'
;

ALWAYS_COMPARE_MED
:
   'always-compare-med'
;

ALWAYS_SEND
:
   'always-send'
;

ANY
:
   'any'
;

ANY_IPV4
:
   'any-ipv4'
;

ANY_IPV6
:
   'any-ipv6'
;

ANY_REMOTE_HOST
:
   'any-remote-host'
;

ANY_SERVICE
:
   'any-service'
;

APPLICATION
:
   'application'
;

APPLICATION_TRACKING
:
   'application-tracking'
;

APPLICATIONS
:
   'applications'
;

APPLY_GROUPS
:
   'apply-groups'
;

APPLY_GROUPS_EXCEPT
:
   'apply-groups-except'
;

APPLY_PATH
:
   'apply-path'
;

AREA
:
   'area'
;

AREA_RANGE
:
   'area-range'
;

ARP
:
   'arp'
;

ARP_RESP
:
   'arp-resp'
;

AS_OVERRIDE
:
   'as-override'
;

AS_PATH
:
   'as-path' -> pushMode(M_AsPath)
;

AS_PATH_EXPAND
:
   'as-path-expand'
;

AS_PATH_PREPEND
:
   'as-path-prepend'
;

ASCII_TEXT
:
   'ascii-text'
;

AUTHENTICATION
:
   'authentication'
;

AUTHENTICATION_ALGORITHM
:
   'authentication-algorithm'
;

AUTHENTICATION_KEY
:
   'authentication-key'
;

AUTHENTICATION_METHOD
:
   'authentication-method'
;

AUTHENTICATION_ORDER
:
   'authentication-order'
;

AUTONOMOUS_SYSTEM
:
   'autonomous-system'
;

AUTHENTICATION_TYPE
:
   'authentication-type'
;

AUTO_EXPORT
:
   'auto-export'
;

AUTO_NEGOTIATION
:
   'auto-negotiation'
;

BACKUP_ROUTER
:
   'backup-router'
;

BANDWIDTH
:
   'bandwidth'
;

BASIC
:
   'basic'
;

BFD
:
   'bfd'
;

BFD_LIVENESS_DETECTION
:
   'bfd-liveness-detection'
;

BGP
:
   'bgp'
;

BIFF
:
   'biff'
;

BIND_INTERFACE
:
   'bind-interface' -> pushMode(M_Interface)
;

BMP
:
   'bmp'
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

BRIDGE_DOMAINS
:
   'bridge-domains'
;

CCC
:
   'ccc'
;

CERTIFICATES
:
   'certificates'
;

CHASSIS
:
   'chassis'
;

CLASS
:
   'class'
;

CLASS_OF_SERVICE
:
   'class-of-service'
;

CLEAR
:
   'clear'
;

CLUSTER
:
   'cluster'
;

CMD
:
   'cmd'
;

COLOR
:
   'color'
;

COLOR2
:
   'color2'
;

COMMIT
:
   'commit'
;

COMMUNITY
:
   'community'
   {
      enableIPV6_ADDRESS = false;
   }

;

COMPATIBLE
:
   'compatible'
;

CONDITION
:
   'condition'
;

CONNECTIONS
:
   'connections'
;

CONNECTIONS_LIMIT
:
   'connections-limit'
;

COS_NEXT_HOP_MAP
:
   'cos-next-hop-map'
;

COUNT
:
   'count'
;

CREDIBILITY_PROTOCOL_PREFERENCE
:
   'credibility-protocol-preference'
;

CVSPSERVER
:
   'cvspserver'
;

DAMPING
:
   'damping'
;

DDOS_PROTECTION
:
   'ddos-protection'
;

DEACTIVATE
:
   'deactivate'
;

DEAD_INTERVAL
:
   'dead-interval'
;

DEAD_PEER_DETECTION
:
   'dead-peer-detection'
;

DEFAULT_ACTION
:
   'default-action'
;

DEFAULT_ADDRESS_SELECTION
:
   'default-address-selection'
;

DEFAULT_LSA
:
   'default-lsa'
;

DEFAULT_METRIC
:
   'default-metric'
;

DEFAULT_POLICY
:
   'default-policy'
;

DEFAULTS
:
   'defaults'
;

DELETE
:
   'delete'
;

DENY
:
   'deny'
;

DENY_ALL
:
   'deny-all'
;

DES_CBC
:
   'des-cbc'
;

DESCRIPTION
:
   'description' -> pushMode(M_Description)
;

DESTINATION_ADDRESS
:
   'destination-address'
;

DESTINATION_HOST_UNKNOWN
:
   'destination-host-unknown'
;

DESTINATION_IP
:
   'destination-ip'
;

DESTINATION_NETWORK_UNKNOWN
:
   'destination-network-unknown'
;

DESTINATION_PORT
:
   'destination-port'
;

DESTINATION_PORT_EXCEPT
:
   'destination-port-except'
;

DESTINATION_PREFIX_LIST
:
   'destination-prefix-list'
;

DESTINATION_UNREACHABLE
:
   'destination-unreachable'
;

DF_BIT
:
   'df-bit'
;

DH_GROUP
:
   'dh-group'
;

DHCP
:
   'dhcp'
;

DIRECT
:
   'direct'
;

DISABLE
:
   'disable'
;

DISABLE_4BYTE_AS
:
   'disable-4byte-as'
;

DISCARD
:
   'discard'
;

DNS
:
   'dns'
;

DOMAIN
:
   'domain'
;

DOMAIN_NAME
:
   'domain-name'
;

DOMAIN_SEARCH
:
   'domain-search'
;

DSA_SIGNATURES
:
   'dsa-signatures'
;

DSCP
:
   'dscp' -> pushMode(M_DSCP)
;

DSTOPTS
:
   'dstopts'
;

DUMPONPANIC
:
   'dump-on-panic'
;

DVMRP
:
   'dvmrp'
;

DYNAMIC
:
   'dynamic'
;

ECHO_REPLY
:
   'echo-reply'
;

ECHO_REQUEST
:
   'echo-request'
;

EGP
:
   'egp'
;

EIGHT02_3AD
:
   '802.3ad'
;

EKLOGIN
:
   'eklogin'
;

EKSHELL
:
   'ekshell'
;

ENABLE
:
   'enable'
;

ENCAPSULATION
:
   'encapsulation'
;

ENCRYPTION_ALGORITHM
:
   'encryption-algorithm'
;

ESP
:
   'esp'
;

ESTABLISH_TUNNELS
:
   'establish-tunnels'
;

ETHER_OPTIONS
:
   'ether-options'
;

ETHERNET_SWITCHING
:
   'ethernet-switching'
;

ETHERNET_SWITCHING_OPTIONS
:
   'ethernet-switching-options'
;

EVENT_OPTIONS
:
   'event-options'
;

EXACT
:
   'exact'
;

EXCEPT
:
   'except'
;

EXEC
:
   'exec'
;

EXP
:
   'exp'
;

EXPORT
:
   'export'
;

EXPORT_RIB
:
   'export-rib'
;

EXPRESSION
:
   'expression'
;

EXTERNAL
:
   'external'
;

EXTERNAL_INTERFACE
:
   'external-interface' -> pushMode(M_Interface)
;

EXTERNAL_PREFERENCE
:
   'external-preference'
;

FABRIC_OPTIONS
:
   'fabric-options'
;

FAIL_FILTER
:
   'fail-filter'
;

FAMILY
:
   'family'
;

FASTETHER_OPTIONS
:
   'fastether-options'
;

FILE
:
   'file'
;

FILTER
:
   'filter'
;

FINGER
:
   'finger'
;

FIREWALL
:
   'firewall'
;

FIRST_FRAGMENT
:
   'first-fragment'
;

FLEXIBLE_VLAN_TAGGING
:
   'flexible-vlan-tagging'
;

FLOW
:
   'flow'
;

FLOW_CONTROL
:
   'flow-control'
;

FORWARDING
:
   'forwarding'
;

FORWARDING_CLASS
:
   'forwarding-class'
;

FORWARDING_OPTIONS
:
   'forwarding-options'
;

FORWARDING_TABLE
:
   'forwarding-table'
;

FRAGMENT
:
   'fragment'
;

FRAGMENTATION_NEEDED
:
   'fragmentation-needed'
;

FRAMING
:
   'framing'
;

FROM
:
   'from'
;

FROM_ZONE
:
   'from-zone'
;

FTP
:
   'ftp'
;

FTP_DATA
:
   'ftp-data'
;

FULL_DUPLEX
:
   'full-duplex'
;

G
:
   'g'
;

GATEWAY
:
   'gateway'
;

GENERATE
:
   'generate'
;

GIGETHER_OPTIONS
:
   'gigether-options'
;

GRACEFUL_RESTART
:
   'graceful-restart'
;

GRE
:
   'gre'
;

GROUP
:
   'group' -> pushMode(M_VarOrWildcard)
;

GROUP_IKE_ID
:
   'group-ike-id'
;

GROUP1
:
   'group1'
;

GROUP14
:
   'group14'
;

GROUP2
:
   'group2'
;

GROUP5
:
   'group5'
;

GROUPS
:
   'groups'
;

HELLO_AUTHENTICATION_KEY
:
   'hello-authentication-key'
;

HELLO_AUTHENTICATION_TYPE
:
   'hello-authentication-type'
;

HELLO_INTERVAL
:
   'hello-interval'
;

HELLO_PADDING
:
   'hello-padding'
;

HIGH
:
   'high'
;

HMAC_MD5_96
:
   'hmac-md5-96'
;

HMAC_SHA1_96
:
   'hmac-sha1-96'
;

HOLD_TIME
:
   'hold-time'
;

HOP_BY_HOP
:
   'hop-by-hop'
;

HOST
:
   'host'
;

HOST_INBOUND_TRAFFIC
:
   'host-inbound-traffic'
;

HOST_NAME
:
   'host-name'
;

HOST_UNREACHABLE
:
   'host-unreachable'
;

HOSTNAME
:
   'hostname'
;

HTTP
:
   'http'
;

HTTPS
:
   'https'
;

ICMP
:
   'icmp'
;

ICMP_CODE
:
   'icmp-code'
;

ICMP_TYPE
:
   'icmp-type'
;

ICMP6
:
   'icmp6'
;

ICMPV6
:
   'icmpv6'
;

IDENT
:
   'ident'
;

IDENT_RESET
:
   'ident-reset'
;

IGMP
:
   'igmp'
;

IGMP_SNOOPING
:
   'igmp-snooping'
;

IGNORE_L3_INCOMPLETES
:
   'ignore-l3-incompletes'
;

IGP
:
   'igp'
;

IKE
:
   'ike'
;

IKE_POLICY
:
   'ike-policy'
;

IKE_USER_TYPE
:
   'ike-user-type'
;

IMAP
:
   'imap'
;

IMMEDIATELY
:
   'immediately'
;

IMPORT
:
   'import'
;

IMPORT_POLICY
:
   'import-policy'
;

IMPORT_RIB
:
   'import-rib'
;

INACTIVE
:
   'inactive'
;

INACTIVITY_TIMEOUT
:
   'inactivity-timeout'
;

INCLUDE_MP_NEXT_HOP
:
   'include-mp-next-hop'
;

INCOMPLETE
:
   'incomplete'
;

INET
:
   'inet'
;

INET6
:
   'inet6'
;

INET_MDT
:
   'inet-mdt'
;

INET_MVPN
:
   'inet-mvpn'
;

INET_VPN
:
   'inet-vpn'
;

INET6_VPN
:
   'inet6-vpn'
;

INNER
:
   'inner'
;

INPUT
:
   'input'
;

INPUT_LIST
:
   'input-list'
;

INPUT_VLAN_MAP
:
   'input-vlan-map'
;

INSTALL
:
   'install'
;

INSTALL_NEXTHOP
:
   'install-nexthop'
;

INSTANCE
:
   'instance'
;

INSTANCE_TYPE
:
   'instance-type'
;

INTERFACE
:
   'interface' -> pushMode(M_Interface)
;

INTERFACE_MODE
:
   'interface-mode'
;

INTERFACE_SPECIFIC
:
   'interface-specific'
;

INTERFACE_SWITCH
:
   'interface-switch'
;

INTERFACE_TRANSMIT_STATISTICS
:
   'interface-transmit-statistics'
;

INTERFACES
:
   'interfaces'
;

INTERFACE_ROUTES
:
   'interface-routes'
;

INTERFACE_TYPE
:
   'interface-type'
;

INTERNAL
:
   'internal'
;

INTERNET_OPTIONS
:
   'internet-options'
;

IP
:
   'ip'
;

IP_OPTIONS
:
   'ip-options'
;

IPIP
:
   'ipip'
;

IPSEC
:
   'ipsec'
;

IPSEC_POLICY
:
   'ipsec-policy'
;

IPSEC_VPN
:
   'ipsec-vpn'
;

IPV6
:
   'ipv6'
;

IS_FRAGMENT
:
   'is-fragment'
;

ISIS
:
   'isis'
;

ISO
:
   'iso' -> pushMode(M_ISO)
;

JUNOS_HOST
:
   'junos-host'
;

KEEP
:
   'keep'
;

KERBEROS_SEC
:
   'kerberos-sec'
;

KEYS
:
   'keys'
;

KLOGIN
:
   'klogin'
;

KPASSWD
:
   'kpasswd'
;

KRB_PROP
:
   'krb-prop'
;

KRBUPDATE
:
   'krbupdate'
;

KSHELL
:
   'kshell'
;

L
:
   'L'
;

L2CIRCUIT
:
   'l2circuit'
;

L2VPN
:
   'l2vpn'
;

L3_INTERFACE
:
   'l3-interface'
;

LABEL_SWITCHED_PATH
:
   'label-switched-path'
;

LABELED_UNICAST
:
   'labeled-unicast'
;

LACP
:
   'lacp'
;

LAN
:
   'lan'
;

LAST_AS
:
   'last-as'
;

LDP_SYNCHRONIZATION
:
   'ldp-synchronization'
;

LICENSE
:
   'license'
;

LINK_MODE
:
   'link-mode'
;

LDAP
:
   'ldap'
;

LDP
:
   'ldp'
;

LEARN_VLAN_1P_PRIORITY
:
   'learn-vlan-1p-priority'
;

LEVEL
:
   'level'
;

LIFETIME_KILOBYTES
:
   'lifetime-kilobytes'
;

LIFETIME_SECONDS
:
   'lifetime-seconds'
;

LINK_PROTECTION
:
   'link-protection'
;

LLDP
:
   'lldp'
;

LLDP_MED
:
   'lldp-med'
;

LOAD_BALANCE
:
   'load-balance'
;

LOCAL
:
   'local'
;

LOCAL_ADDRESS
:
   'local-address'
;

LOCAL_AS
:
   'local-as'
;

LOCAL_IDENTITY
:
   'local-identity'
;
LOCAL_PREFERENCE
:
   'local-preference'
;

LOCATION
:
   'location'
;

LOG
:
   'log'
;

LOG_UPDOWN
:
   'log-updown'
;

LOGICAL_SYSTEMS
:
   'logical-systems'
;

LOGIN
:
   'login'
;

LONGER
:
   'longer'
;

LOOPS
:
   'loops'
;

LOSS_PRIORITY
:
   'loss-priority'
;

LOW
:
   'low'
;

LSP
:
   'lsp'
;

LSP_EQUAL_COST
:
   'lsp-equal-cost'
;

LSP_INTERVAL
:
   'lsp-interval'
;

LSP_LIFETIME
:
   'lsp-lifetime'
;

LSPING
:
   'lsping'
;

M
:
   'm'
;

MAC
:
   'mac' -> pushMode(M_MacAddress)
;

MAIN
:
   'main'
;

MAPPED_PORT
:
   'mapped-port'
;

MARTIANS
:
   'martians'
;

MASTER_ONLY
:
   'master-only'
;

MATCH
:
   'match'
;

MAX_CONFIGURATIONS_ON_FLASH
:
   'max-configurations-on-flash'
;

MAX_CONFIGURATION_ROLLBACKS
:
   'max-configuration-rollbacks'
;

MAX_SESSION_NUMBER
:
   'max-session-number'
;

MAXIMUM_LABELS
:
   'maximum-labels'
;

MD5
:
   'md5'
;

MEDIUM_HIGH
:
   'medium-high'
;

MEDIUM_LOW
:
   'medium-low'
;

METRIC
:
   'metric'
;

METRIC2
:
   'metric2'
;

METRIC_OUT
:
   'metric-out'
;

METRIC_TYPE
:
   'metric-type' -> pushMode(M_MetricType)
;

MEMBERS
:
   'members' -> pushMode(M_Members)
;

MLD
:
   'mld'
;

MOBILEIP_AGENT
:
   'mobileip-agent'
;

MOBILIP_MN
:
   'mobilip-mn'
;

MODE
:
   'mode'
;

MPLS
:
   'mpls'
;

MSDP
:
   'msdp'
;

MSTP
:
   'mstp'
;

MTU
:
   'mtu'
;

MTU_DISCOVERY
:
   'mtu-discovery'
;

MULTICAST
:
   'multicast'
;

MULTICAST_MAC
:
   'multicast-mac' -> pushMode(M_MacAddress)
;

MULTIHOP
:
   'multihop'
;

MULTIPATH
:
   'multipath'
;

MULTIPLE_AS
:
   'multiple-as'
;

MULTIPLIER
:
   'multiplier'
;

MULTISERVICE_OPTIONS
:
   'multiservice-options'
;

MVPN
:
   'mvpn'
;

NAME_RESOLUTION
:
   'name-resolution'
;

NAME_SERVER
:
   'name-server'
;

NAT
:
   'nat'
;

NATIVE_VLAN_ID
:
   'native-vlan-id'
;

NEIGHBOR
:
   'neighbor'
;

NEIGHBOR_ADVERTISEMENT
:
   'neighbor-advertisement'
;

NEIGHBOR_DISCOVERY
:
   'neighbor-discovery'
;

NEIGHBOR_SOLICIT
:
   'neighbor-solicit'
;

NETBIOS_DGM
:
   'netbios-dgm'
;

NETBIOS_NS
:
   'netbios-ns'
;

NETBIOS_SSN
:
   'netbios-ssn'
;

NETCONF
:
   'netconf'
;

NETWORK_SUMMARY_EXPORT
:
   'network-summary-export'
;

NETWORK_UNREACHABLE
:
   'network-unreachable'
;

NEXT
:
   'next'
;

NEXT_HEADER
:
   'next-header'
;

NEXT_HOP
:
   'next-hop'
;

NEXT_TABLE
:
   'next-table'
;

NFSD
:
   'nfsd'
;

NHRP
:
   'nhrp'
;

NNTP
:
   'nntp'
;

NTALK
:
   'ntalk'
;

NO_ACTIVE_BACKBONE
:
   'no-active-backbone'
;

NO_ANTI_REPLAY
:
   'no-anti-replay'
;

NO_AUTO_NEGOTIATION
:
   'no-auto-negotiation'
;

NO_CLIENT_REFLECT
:
   'no-client-reflect'
;

NO_EXPORT
:
   'no-export'
;

NO_FLOW_CONTROL
:
   'no-flow-control'
;

NO_INSTALL
:
   'no-install'
;

NO_IPV4_ROUTING
:
   'no-ipv4-routing'
;

NO_NAT_TRAVERSAL
:
   'no-nat-traversal'
;

NO_NEIGHBOR_DOWN_NOTIFICATION
:
   'no-neighbor-down-notification'
;

NO_NEXTHOP_CHANGE
:
   'no-nexthop-change'
;

NO_READVERTISE
:
   'no-readvertise'
;

NO_REDIRECTS
:
   'no-redirects'
;

NO_RESOLVE
:
   'no-resolve'
;

NO_RETAIN
:
   'no-retain'
;

NO_NEIGHBOR_LEARN
:
   'no-neighbor-learn'
;

NO_TRAPS
:
   'no-traps'
;

NONSTOP_ROUTING
:
   'nonstop-routing'
;

NSSA
:
   'nssa'
;

NTP
:
   'ntp'
;

OFF
:
   'off'
;

OFFSET
:
   'offset'
;

OPTIONS
:
   'options'
;

ORIGIN
:
   'origin'
;

ORLONGER
:
   'orlonger'
;

OSPF
:
   'ospf'
;

OSPF3
:
   'ospf3'
;

OUT_DELAY
:
   'out-delay'
;

OUTPUT
:
   'output'
;

OUTPUT_LIST
:
   'output-list'
;

OUTPUT_VLAN_MAP
:
   'output-vlan-map'
;

OUTER
:
   'outer'
;

OVERLOAD
:
   'overload'
;

P2P
:
   'p2p'
;

PACKET_TOO_BIG
:
   'packet-too-big'
;

PARAMETER_PROBLEM
:
   'parameter-problem'
;

PASSIVE
:
   'passive'
;

PATH
:
   'path'
;

PATH_COUNT
:
   'path-count'
;

PATH_SELECTION
:
   'path-selection'
;

PEER_ADDRESS
:
   'peer-address'
;

PEER_AS
:
   'peer-as'
;

PEER_UNIT
:
   'peer-unit'
;

PER_PACKET
:
   'per-packet'
;

PER__UNIT_SCHEDULER
:
   'per-unit-scheduler'
;

PERFECT_FORWARD_SECRECY
:
   'perfect-forward-secrecy'
;

PERMIT
:
   'permit'
;

PERMIT_ALL
:
   'permit-all'
;

PERSISTENT_NAT
:
   'persistent-nat'
;

PGM
:
   'pgm'
;

PIM
:
   'pim'
;

PING
:
   'ping'
;

POE
:
   'poe'
;

POINT_TO_POINT
:
   'point-to-point'
;

POLICER
:
   'policer'
;

POLICIES
:
   'policies'
;

POLICY
:
   'policy'
;

POLICY_OPTIONS
:
   'policy-options'
;

POLICY_STATEMENT
:
   'policy-statement'
;

POLL_INTERVAL
:
   'poll-interval'
;

POOL
:
   'pool'
;

POP3
:
   'pop3'
;

PORT
:
   'port'
;

PORTS
:
   'ports'
;

PORT_MIRROR
:
   'port-mirror'
;

PORT_MODE
:
   'port-mode'
;

PORT_OVERLOADING
:
   'port-overloading'
;

PORT_OVERLOADING_FACTOR
:
   'port-overloading-factor'
;

PORT_RANDOMIZATION
:
   'port-randomization'
;

PORT_UNREACHABLE
:
   'port-unreachable'
;

PPM
:
   'ppm'
;

PPTP
:
   'pptp'
;

PRE_SHARED_KEY
:
   'pre-shared-key'
;

PRE_SHARED_KEYS
:
   'pre-shared-keys'
;

PRECEDENCE
:
   'precedence'
;

PRECISION_TIMERS
:
   'precision-timers'
;

PREEMPT
:
   'preempt'
;

PREFERENCE
:
   'preference'
;

PREFERRED
:
   'preferred'
;

PREFIX
:
   'prefix'
;

PREFIX_EXPORT_LIMIT
:
   'prefix-export-limit'
;

PREFIX_LENGTH_RANGE
:
   'prefix-length-range'
;

PREFIX_LIMIT
:
   'prefix-limit'
;

PREFIX_LIST
:
   'prefix-list' -> pushMode(M_PrefixListName)
;

PREFIX_LIST_FILTER
:
   'prefix-list-filter'
;

PREFIX_POLICY
:
   'prefix-policy'
;

PRIMARY
:
   'primary'
;

PRINTER
:
   'printer'
;

PRIORITY
:
   'priority'
;

PRIORITY_COST
:
   'priority-cost'
;

PRIVATE
:
   'private'
;

PROCESSES
:
   'processes'
;

PROPOSAL
:
   'proposal'
;

PROPOSAL_SET
:
   'proposal-set'
;

PROPOSALS
:
   'proposals'
;

PROTOCOL
:
   'protocol'
;

PROTOCOLS
:
   'protocols'
;

PROVIDER_TUNNEL
:
   'provider-tunnel'
;

PROXY_ARP
:
   'proxy-arp'
;

PROXY_IDENTITY
:
   'proxy-identity'
;

QUALIFIED_NEXT_HOP
:
   'qualified-next-hop'
;

R2CP
:
   'r2cp'
;

RADACCT
:
   'radacct'
;

RADIUS
:
   'radius'
;

RADIUS_OPTIONS
:
   'radius-options'
;

RADIUS_SERVER
:
   'radius-server'
;

READVERTISE
:
   'readvertise'
;

RECEIVE
:
   'receive'
;

REDUNDANCY_GROUP
:
   'redundancy-group'
;

REDUNDANT_ETHER_OPTIONS
:
   'redundant-ether-options'
;

REDUNDANT_PARENT
:
   'redundant-parent'
;

REFERENCE_BANDWIDTH
:
   'reference-bandwidth'
;

REJECT
:
   'reject'
;

REMOTE
:
   'remote'
;

REMOVE_PRIVATE
:
   'remove-private'
;

REMOVED
:
   'Removed'
;

RESOLUTION
:
   'resolution'
;

RESOLVE
:
   'resolve'
;

RESTRICT
:
   'restrict'
;

RETAIN
:
   'retain'
;

REVERSE_SSH
:
   'reverse-ssh'
;

REVERSE_TELNET
:
   'reverse-telnet'
;

RIB
:
   'rib'
;

RIB_GROUP
:
   'rib-group'
;

RIB_GROUPS
:
   'rib-groups'
;

RIP
:
   'rip'
;

RIPNG
:
   'ripng'
;

RKINIT
:
   'rkinit'
;

RLOGIN
:
   'rlogin'
;

ROOT_AUTHENTICATION
:
   'root-authentication'
;

ROUTE
:
   'route'
;

ROUTE_DISTINGUISHER
:
   'route-distinguisher' -> pushMode(M_RouteDistinguisher)
;

ROUTE_FILTER
:
   'route-filter'
;

ROUTE_TYPE
:
   'route-type'
;

ROUTER_ADVERTISEMENT
:
   'router-advertisement'
;

ROUTER_DISCOVERY
:
   'router-discovery'
;

ROUTER_ID
:
   'router-id'
;

ROUTING_INSTANCE
:
   'routing-instance'
;

ROUTING_INSTANCES
:
   'routing-instances'
;

ROUTING_OPTIONS
:
   'routing-options'
;

RPF_CHECK
:
   'rpf-check'
;

RPM
:
   'rpm'
;

RSA_SIGNATURES
:
   'rsa-signatures'
;

RSH
:
   'rsh'
;

RSTP
:
   'rstp'
;

RSVP
:
   'rsvp'
;

RULE
:
   'rule'
;

RULE_SET
:
   'rule-set'
;

SAMPLE
:
   'sample'
;

SAMPLING
:
   'sampling'
;

SAP
:
   'sap'
;

SCREEN
:
   'screen'
;

SCRIPTS
:
   'scripts'
;

SCTP
:
   'sctp'
;

SECURITY
:
   'security'
;

SECURITY_ZONE
:
   'security-zone'
;

SERVICE
:
   'service'
;

SERVICE_FILTER
:
   'service-filter'
;

SERVICES
:
   'services'
;

SELF
:
   'self'
;

SEND
:
   'send'
;

SET
:
   'set'
;

SFLOW
:
   'sflow'
;

SHA_256
:
   'sha-256'
;

SHA_384
:
   'sha-384'
;

SHA1
:
   'sha1'
;

SHARED_IKE_ID
:
   'shared-ike-id'
;

SHORTCUTS
:
   'shortcuts'
;

SIMPLE
:
   'simple'
;

SIP
:
   'sip'
;

SRLG
:
   'srlg'
;

SRLG_COST
:
   'srlg-cost'
;

SRLG_VALUE
:
   'srlg-value'
;

SMTP
:
   'smtp'
;

SNMP
:
   'snmp'
;

SNMP_TRAP
:
   'snmp-trap'
;

SNMPTRAP
:
   'snmptrap'
;

SNPP
:
   'snpp'
;

SOCKS
:
   'socks'
;

SONET_OPTIONS
:
   'sonet-options'
;

SOURCE
:
   'source'
;

SOURCE_ADDRESS
:
   'source-address'
;

SOURCE_ADDRESS_FILTER
:
   'source-address-filter'
;

SOURCE_IDENTITY
:
   'source-identity'
;

SOURCE_INTERFACE
:
   'source-interface'
;

SOURCE_NAT
:
   'source-nat'
;

SOURCE_PORT
:
   'source-port'
;

SOURCE_PREFIX_LIST
:
   'source-prefix-list'
;

SOURCE_QUENCH
:
   'source-quench'
;

SPEED
:
   'speed' -> pushMode(M_Speed)
;

SPF_OPTIONS
:
   'spf-options'
;

SSH
:
   'ssh'
;

STANDARD
:
   'standard'
;

STATIC
:
   'static'
;

STATIC_NAT
:
   'static-nat'
;

STATION_ADDRESS
:
   'station-address'
;

STATION_PORT
:
   'station-port'
;

STP
:
   'stp'
;

SUBTRACT
:
   'subtract'
;

SUNRPC
:
   'sunrpc'
;

SWITCH_OPTIONS
:
   'switch-options'
;

SYSLOG
:
   'syslog'
;

SYSTEM
:
   'system'
;

SYSTEM_SERVICES
:
   'system-services'
;

TACACS
:
   'tacacs'
;

TACACS_DS
:
   'tacacs-ds'
;

TACPLUS_SERVER
:
   'tacplus-server'
;

TAG
:
   'tag'
;

TALK
:
   'talk'
;

TARGET
:
   'target'
;

TARGET_HOST
:
   'target-host'
;

TARGET_HOST_PORT
:
   'target-host-port'
;

TARGETED_BROADCAST
:
   'targeted-broadcast'
;

TCP
:
   'tcp'
;

TCP_ESTABLISHED
:
   'tcp-established'
;

TCP_FLAGS
:
   'tcp-flags'
;

TCP_INITIAL
:
   'tcp-initial'
;

TCP_MSS
:
   'tcp-mss'
;

TCP_RST
:
   'tcp-rst'
;

TE_METRIC
:
   'te-metric'
;

TELNET
:
   'telnet'
;

TERM
:
   'term'
;

TFTP
:
   'tftp'
;

THEN
:
   'then'
;

THREEDES_CBC
:
   '3des-cbc'
;

THROUGH
:
   'through'
;

TIME_EXCEEDED
:
   'time-exceeded'
;

TIME_ZONE
:
   'time-zone'
;

TIMED
:
   'timed'
;

TO
:
   'to'
;

TO_ZONE
:
   'to-zone'
;

TRACEOPTIONS
:
   'traceoptions'
;

TRACEROUTE
:
   'traceroute'
;

TRACK
:
   'track'
;

TRAFFIC_ENGINEERING
:
   'traffic-engineering'
;

TRAPS
:
   'traps'
;

TRUNK
:
   'trunk'
;

TRUST
:
   'trust'
;

TTL
:
   'ttl'
;

TUNNEL
:
   'tunnel'
;

TYPE
:
   'type'
;

TYPE_7
:
   'type-7'
;

UDP
:
   'udp'
;

UNICAST
:
   'unicast'
;

UNIT
:
   'unit'
;

UNREACHABLE
:
   'unreachable'
;

UNTRUST
:
   'untrust'
;

UNTRUST_SCREEN
:
   'untrust-screen'
;

UPTO
:
   'upto'
;

URPF_LOGGING
:
   'urpf-logging'
;

USER
:
   'user'
;

V1_ONLY
:
   'v1-only'
;

VERSION
:
   'version' -> pushMode(M_Version)
;

VIRTUAL_ADDRESS
:
   'virtual-address'
;

VIRTUAL_CHASSIS
:
   'virtual-chassis'
;

VIRTUAL_SWITCH
:
   'virtual-switch'
;

VLAN
:
   'vlan' -> pushMode(M_Vlan)
;

VLANS
:
   'vlans'
;

VLAN_ID
:
   'vlan-id'
;

VLAN_ID_LIST
:
   'vlan-id-list'
;

VLAN_TAGS
:
   'vlan-tags'
;

VLAN_TAGGING
:
   'vlan-tagging'
;

VPLS
:
   'vpls'
;

VPN
:
   'vpn'
;

VPN_MONITOR
:
   'vpn-monitor'
;

VRF
:
   'vrf'
;

VRF_EXPORT
:
   'vrf-export'
;

VRF_IMPORT
:
   'vrf-import'
;

VRF_TABLE_LABEL
:
   'vrf-table-label'
;

VRF_TARGET
:
   'vrf-target' -> pushMode(M_VrfTarget)
;

VRRP
:
   'vrrp'
;

VRRP_GROUP
:
   'vrrp-group'
;

VSTP
:
   'vstp'
;

WHO
:
   'who'
;

WIDE_METRICS_ONLY
:
   'wide-metrics-only'
;

XAUTH
:
   'xauth'
;

XDMCP
:
   'xdmcp'
;

XNM_CLEAR_TEXT
:
   'xnm-clear-text'
;

XNM_SSL
:
   'xnm-ssl'
;

ZONE
:
   'zone'
;

ZONES
:
   'zones'
;

// End of Juniper keywords

COMMUNITY_LITERAL
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
            F_Variable_LeadingVarChar
            {!enableIPV6_ADDRESS}?

            F_Variable_VarChar* F_Variable_RequiredVarChar F_Variable_VarChar*
         )
         |
         (
            F_Variable_LeadingVarChar_Ipv6
            {enableIPV6_ADDRESS}?

            F_Variable_VarChar_Ipv6* F_Variable_RequiredVarChar_Ipv6
            F_Variable_VarChar_Ipv6*
         )
      )
   )
;

AMPERSAND
:
   '&'
;

ASTERISK
:
   '*'
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

DASH
:
   '-'
;

DEC
:
   '0'
   | F_PositiveDigit F_Digit*
;

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

FLOAT
:
   F_PositiveDigit* F_Digit '.'
   (
      '0'
      | F_Digit* F_PositiveDigit
   )
;

FORWARD_SLASH
:
   '/'
;

GREATER_THAN
:
   '>'
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
   (
      F_Digit+ '.' F_Digit+ '.' F_Digit+ '.' F_Digit+
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
   )
   (
      F_HexDigit+
   )?
   (
      F_Digit+ '.' F_Digit+ '.' F_Digit+ '.' F_Digit+
   )? '/' F_DecByte
;

LINE_COMMENT
:
   '#' F_NonNewlineChar* F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> channel(HIDDEN)
;

MULTILINE_COMMENT
:
   '/*' .*? '*/' -> channel(HIDDEN)
;

NEWLINE
:
   F_NewlineChar+
   {
      enableIPV6_ADDRESS = true;
   }

;

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

WILDCARD
:
   '<' ~'>'* '>'
;

WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

fragment
F_DecByte
: // TODO: This is a hack.

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
   [0-9]
;

fragment
F_HexDigit
:
   [0-9a-fA-F]
;

fragment
F_Letter
:
   [A-Za-z]
;

fragment
F_NewlineChar
:
   [\r\n]
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
F_Variable_RequiredVarChar
:
   ~[ 0-9\t\n\r/.,\-;{}<>[\]&|()"']
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
   [ \t\u000C]
;

mode M_AsPath;

M_AsPath_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_AsPath_ORIGIN
:
   'origin' -> type(ORIGIN), popMode
;

M_AsPath_PATH
:
   'path' -> type(PATH), mode(M_AsPathPath)
;

M_AsPath_TERM
:
   'term' -> type(TERM), popMode
;

M_AsPath_VARIABLE
:
   (
      F_Digit
      | F_Variable_RequiredVarChar
   ) F_Variable_VarChar* -> type(VARIABLE), mode(M_AsPathRegex)
;

M_AsPath_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_AsPathPath;

M_AsPathPath_DEC
:
   [0-9]+ -> type(DEC), popMode
;

M_AsPathPath_DOUBLE_QUOTE
:
   '"' -> channel(HIDDEN), mode(M_AsPathExpr)
;

M_AsPathPath_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_AsPathExpr;

M_AsPathExpr_DEC
:
   [0-9]+ -> type(DEC)
;

M_AsPathExpr_OPEN_BRACKET
:
   '[' -> type(OPEN_BRACKET)
;

M_AsPathExpr_CLOSE_BRACKET
:
   ']' -> type(CLOSE_BRACKET)
;

M_AsPathExpr_DOUBLE_QUOTE
:
   '"' -> channel(HIDDEN), popMode
;

M_AsPathExpr_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_AsPathRegex;

AS_PATH_REGEX
:
   [0-9,^$[\]\-*.{}+|()] [0-9,^$[\]\-*.{}+|() _?]*
;

M_AsPathRegex_DOUBLE_QUOTE
:
   '"' -> channel(HIDDEN)
;

M_AsPathRegex_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_AsPathRegex_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Description;

M_Description_DESCRIPTION
:
   F_NonWhitespaceChar F_NonNewlineChar*
;

M_Description_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_Description_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_DSCP;

M_DSCP_VARIABLE
:
   F_NonWhitespaceChar+ -> type(VARIABLE)
;

M_DSCP_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_DSCP_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Interface;

M_Interface_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_Interface_INTERFACE
:
   'interface' -> type(INTERFACE)
;

M_Interface_PORT_OVERLOADING
:
   'port-overloading' -> type(PORT_OVERLOADING), popMode
;

M_Interface_PORT_OVERLOADING_FACTOR
:
   'port-overloading-factor' -> type(PORT_OVERLOADING_FACTOR), popMode
;

M_Interface_QUOTE
:
   '"' -> channel(HIDDEN), mode(M_InterfaceQuote)
;

M_Interface_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_InterfaceVarChar* -> type(VARIABLE), popMode
;

M_Interface_WILDCARD
:
   '<' ~'>'* '>' -> type(WILDCARD), popMode
;

M_Interface_IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte -> type(IP_ADDRESS), popMode
;

M_Interface_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_InterfaceQuote;

M_InterfaceQuote_QUOTE
:
   '"' -> channel(HIDDEN), popMode
;

M_InterfaceQuote_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_InterfaceVarChar* -> type(VARIABLE)
;

M_InterfaceQuote_WILDCARD
:
   '<' ~'>'* '>' -> type(WILDCARD)
;

mode M_ISO;

M_ISO_ADDRESS
:
   'address' -> type(ADDRESS), mode(M_ISO_Address)
;

M_ISO_MTU
:
   'mtu' -> type(MTU), popMode
;

M_ISO_Newline
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_ISO_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_ISO_Address;

M_ISO_Address_ISO_ADDRESS
:
   F_HexDigit+ ('.' F_HexDigit+)+ -> type(ISO_ADDRESS), popMode
;

M_ISO_Address_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_MacAddress;

MAC_ADDRESS
:
   F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit
   ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit
   F_HexDigit -> popMode
;

M_MacAddress_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Members;

M_Members_ASTERISK
:
   '*' -> type(ASTERISK)
;

M_Members_CARAT
:
   '^' -> type(CARAT)
;

M_Members_CLOSE_BRACE
:
   '}' -> type(CLOSE_BRACE)
;

M_Members_CLOSE_BRACKET
:
   ']' -> type(CLOSE_BRACKET)
;

M_Members_CLOSE_PAREN
:
   ')' -> type(CLOSE_PAREN)
;

M_Members_COLON
:
   ':' -> type(COLON)
;

M_Members_DASH
:
   '-' -> type(DASH)
;

M_Members_DEC
:
   F_Digit+ -> type(DEC)
;

M_Members_DOLLAR
:
   '$' -> type(DOLLAR)
;

M_Members_DOUBLE_QUOTE
:
   '"' -> channel(HIDDEN)
;

M_Members_L
:
   'L' -> type(L)
;

M_Members_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

NO_ADVERTISE
:
   'no-advertise'
;

M_Members_NO_EXPORT
:
   'no-export' -> type(NO_EXPORT)
;

M_Members_OPEN_BRACE
:
   '{' -> type(OPEN_BRACE)
;

M_Members_OPEN_BRACKET
:
   '[' -> type(OPEN_BRACKET)
;

M_Members_OPEN_PAREN
:
   '(' -> type(OPEN_PAREN)
;

M_Members_ORIGIN
:
   'origin' -> type(ORIGIN)
;

M_Members_PERIOD
:
   '.' -> type(PERIOD)
;

M_Members_PIPE
:
   '|' -> type(PIPE)
;

M_Members_TARGET
:
   'target' -> type(TARGET)
;

M_Members_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_PrefixListName;

M_PrefixLsitName_VARIABLE
:
      ~[ \t\n\r&|()"]+ -> type(VARIABLE), popMode
;

M_PrefixListName_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
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
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_RouteDistinguisher;

M_RouteDistinguisher_COLON
:
   ':' -> type(COLON)
;

M_RouteDistinguisher_IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte -> type(IP_ADDRESS)
;

M_RouteDistinguisher_DEC
:
   F_Digit+ -> type(DEC)
;

M_RouteDistinguisher_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_RouteDistinguisher_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Speed;

M_Speed_DEC
:
   F_Digit+ -> type(DEC)
;

M_Speed_G
:
   'g' -> type(G), popMode
;

M_Speed_M
:
   'm' -> type(M), popMode
;

M_Speed_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_VarOrWildcard;

M_VarOrWildcard_VARIABLE
:
   ~[ \t\u000C\r\n<]+ -> type(VARIABLE), popMode
;

M_VarOrWildcard_WILDCARD
:
   '<' ~'>'* '>' -> type(WILDCARD), popMode
;

M_VarOrWildcard_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Version;

M_Version_V1_ONLY
:
   'v1-only' -> type(V1_ONLY), popMode
;

M_Version_VERSION_STRING
:
   ~[ \t\u000C\r\n;]+ -> popMode
;

M_Version_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_Vlan;

M_Vlan_MEMBERS
:
   'members' -> type(MEMBERS), popMode
;

M_Vlan_UNIT
:
   'unit' -> type(UNIT), popMode
;

M_Vlan_VARIABLE
:
   ~[ \t\n\r&|()"]+ -> type(VARIABLE), popMode
;

M_Vlan_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;

mode M_VrfTarget;

M_VrfTarget_COLON
:
   ':' -> type(COLON)
;

M_VrfTarget_DEC
:
   F_Digit+ -> type(DEC)
;

M_VrfTarget_EXPORT
:
   'export' -> type(EXPORT)
;

M_VrfTarget_IMPORT
:
   'import' -> type(IMPORT)
;

M_VrfTarget_L
:
   'L' -> type(L)
;

M_VrfTarget_NEWLINE
:
   F_NewlineChar+ {enableIPV6_ADDRESS = true;} -> type(NEWLINE), popMode
;

M_VrfTarget_PERIOD
:
   '.' -> type(PERIOD)
;

M_VrfTarget_TARGET
:
   'target' -> type(TARGET)
;

M_VrfTarget_WS
:
   F_WhitespaceChar+ -> channel(HIDDEN)
;
