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

ADDRESS_MASK
:
   'address-mask'
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

AH
:
   'ah'
;

ALIAS
:
   'alias'
;

ALLOW
:
   'allow'
;

ALWAYS_COMPARE_MED
:
   'always-compare-med'
;

ANY
:
   'any'
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

AUTHENTICATION
:
   'authentication'
;

AUTHENTICATION_KEY
:
   'authentication-key'
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

BACKUP_ROUTER
:
   'backup-router'
;

BANDWIDTH
:
   'bandwidth'
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

CONDITION
:
   'condition'
;

CONNECTIONS
:
   'connections'
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

DEFAULTS
:
   'defaults'
;

DELETE
:
   'delete'
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

ESP
:
   'esp'
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

HOST_NAME
:
   'host-name'
;

HOST_UNREACHABLE
:
   'host-unreachable'
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

IGMP
:
   'igmp'
;

IGMP_SNOOPING
:
   'igmp-snooping'
;

IGP
:
   'igp'
;

IMAP
:
   'imap'
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

KEEP
:
   'keep'
;

KERBEROS_SEC
:
   'kerberos-sec'
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

M
:
   'm'
;

MAC
:
   'mac' -> pushMode(M_MacAddress)
;

MARTIANS
:
   'martians'
;

MASTER_ONLY
:
   'master-only'
;

MAX_CONFIGURATIONS_ON_FLASH
:
   'max-configurations-on-flash'
;

MAX_CONFIGURATION_ROLLBACKS
:
   'max-configuration-rollbacks'
;

MAXIMUM_LABELS
:
   'maximum-labels'
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

NAME_SERVER
:
   'name-server'
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

NO_CLIENT_REFLECT
:
   'no-client-reflect'
;

NO_EXPORT
:
   'no-export'
;

NO_INSTALL
:
   'no-install'
;

NO_IPV4_ROUTING
:
   'no-ipv4-routing'
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

NSSA
:
   'nssa'
;

NTP
:
   'ntp'
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

PIM
:
   'pim'
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

QUALIFIED_NEXT_HOP
:
   'qualified-next-hop'
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

REDUNDANT_ETHER_OPTIONS
:
   'redundant-ether-options'
;

REFERENCE_BANDWIDTH
:
   'reference-bandwidth'
;

REJECT
:
   'reject'
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

RKINIT
:
   'rkinit'
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

RSTP
:
   'rstp'
;

RSVP
:
   'rsvp'
;

SAMPLE
:
   'sample'
;

SAMPLING
:
   'sampling'
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

SHORTCUTS
:
   'shortcuts'
;

SIMPLE
:
   'simple'
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

SOURCE_ADDRESS
:
   'source-address'
;

SOURCE_ADDRESS_FILTER
:
   'source-address-filter'
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

STANZA_REMOVED
:
   'Stanza Removed'
;

STATIC
:
   'static'
;

STATION_ADDRESS
:
   'station-address'
;

STATION_PORT
:
   'station-port'
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

TRACEOPTIONS
:
   'traceoptions'
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
   'vlan'
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

XDMCP
:
   'xdmcp'
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

M_Version_VERSION_STRING
:
   ~[ \t\u000C\r\n;]+ -> popMode
;

M_Version_WS
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
