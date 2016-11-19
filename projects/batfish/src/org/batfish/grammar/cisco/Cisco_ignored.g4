parser grammar Cisco_ignored;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

null_block_stanza
:
   NO?
   (
      AAA_SERVER
      | ACCESS_GROUP
      | ACCESS
      | ACL_POLICY
      | ACLLOG
      | ADMIN
      | ALLOW
      | APPLETALK
      | AS_PATH_SET
      | ATM
      | BASH
      | BFD
      | BGP DISABLE_ADVERTISEMENT
      | BLOGGERD
      | BSD_CLIENT
      | BSD_USERNAME
      | BUFFERS
      | CAM_ACL
      | CAM_PROFILE
      | CEF
      | CHAT_SCRIPT
      | CISP
      | CLI
      | CLNS
      | CLOCK
      | COMMIT
      | CONFDCONFIG
      | CONFIGURATION
      | CONFIGURE
      | CONTROLLER
      | COAP
      | COPP
      | COPY
      | CPD
      | DAEMON
      | DCB
      | DCB_BUFFER_THRESHOLD
      | DEBUG
      | DEFAULT_MAX_FRAME_SIZE
      | DEFAULT_VALUE
      | DEVICE
      | DHCPRELAY
      | DO STOP
      | DOMAIN
      | DOT1X
      | DOT1X_ENABLE
      | DUAL_MODE_DEFAULT_VLAN
      | DYNAMIC_ACCESS_POLICY_RECORD
      | ENABLE
      | ENABLE_ACL_COUNTER
      | ENABLE_QOS_STATISTICS
      | END
      | ETHERNET
      | EVENT_HANDLER
      |
      (
         EXCEPTION
         (
            CRASHINFO
            | MEMORY
         )
      )
      | EXCEPTION_SLAVE
      | EXIT
      | FABRICPATH
      | FEATURE_SET
      | FEX
      | FPD
      | GATEKEEPER
      | GATEWAY
      | GLOBAL_PORT_SECURITY
      | GROUP_POLICY
      | HASH_ALGORITHM
      | HPM
      | HSRP
      | HW_SWITCH
      | INSTALL
      | INTERFACE BREAKOUT
      |
      (
         IP
         (
            (
               ACCESS_LIST LOGGING
            )
            | ACCOUNTING_LIST
            | ACCOUNTING_THRESHOLD
            | ADJACENCY
            | ADJMGR
            | BOOTP_RELAY
            | DECAP_GROUP
            | DHCP
            | DNS
            | ECMP_GROUP
            | FLOW_AGGREGATION
            | FLOW_CAPTURE
            | FLOW_SAMPLING_MODE
            | FLOW_TOP_TALKERS
            | GLOBAL_MTU
            | HARDWARE
            | ICMP_ERRORS
            | INSPECT
            | INTERNAL
            | NAME_SERVER
            |
            (
               OSPF NAME_LOOKUP
            )
            | PIM
            | POLICY_LIST
            | RATE_LIMIT
            | RECEIVE
            | REFLEXIVE_LIST
            | ROUTER_ID
            | RSVP
            | SDR
            | SLA
            | SOURCE
            | SYSLOG
            | VIRTUAL_ROUTER
            |
            (
               VRF ~NEWLINE
            )
         )
      )
      | IPSLA
      |
      (
         IPV4
         (
            ASSEMBLER
            | CONFLICT_POLICY
            | HARDWARE
            | ROUTING
            | UNNUMBERED
            | VIRTUAL
         )
      )
      |
      (
         IPV6
         (
            ADJACENCY
            | ADJACENCY_STALE_TIMER
            | CONFLICT_POLICY
            | DHCP
            | GLOBAL_MTU
            | ENABLE_ACL_CAM_SHARING
            | HARDWARE
            | ICMP
            | MROUTE
            | NEIGHBOR
            | ROUTING
         )
      )
      | KEY
      | KEYSTORE
      | KRON
      | L2TP_CLASS
      | LACP
      | LAG
      | LINECARD
      | LOAD_BALANCE
      | LOGIN
      | MAC
      | MAC_LEARN
      | MACRO
      | MANAGEMENT_ACCESS
      | MAP_CLASS
      | MAP_LIST
      | MEDIA_TERMINATION
      | MENU
      | MLAG
      | MODULE
      | MONITOR
      | MONITOR_INTERFACE
      |
      (
         MPLS
         (
            (
               IP
               | IPV6
               | LDP ~NEWLINE
            )
            | OAM
            | TRAFFIC_ENG
         )
      )
      | MULTI_CONFIG
      | NLS
      | NO_BANNER
      | NO_L4R_SHIM
      | NSR
      | NV
      | ONE
      | OPENFLOW
      | OPTICAL_MONITOR
      |
      (
         OSPF
         (
            NAME_LOOKUP
         )
      )
      | PASSWORD_POLICY
      | PLAT
      | PLATFORM
      | POLICY_MAP_INPUT
      | POLICY_MAP_OUTPUT
      | PORT_PROFILE
      | POWEROFF
      | POWER_MGR
      | PRIORITY_FLOW_CONTROL
      | PSEUDOWIRE_CLASS
      | PTP
      | QOS_POLICY
      | QOS_POLICY_OUTPUT
      | RELOAD_TYPE
      | REMOVED
      | RMON
      | ROLE
      | ROUTE_ONLY
      | ROUTER
      (
         LOG
         | VRRP
      )
      | RP
      | RX_COS_SLOT
      | SAMPLER
      | SAMPLER_MAP
      | SAP
      | SCCP
      | SCHEDULE
      | SDR
      | SENSOR
      | SERVICE_CLASS
      | SFLOW
      | SLOT
      | SLOT_TABLE_COS
      | STACK_MAC
      | STACK_UNIT
      | SVCLC
      | SWITCH
      | SWITCH_PROFILE
      | SWITCH_TYPE
      | SYSLOGD
      | SYSTEM_INIT
      | SYSTEM_MAX
      | TABLE_MAP
      | TACACS
      | TACACS_SERVER
      | TAG_TYPE
      | TAP
      | TASKGROUP
      | TCP
      | TEMPLATE
      | TERMINAL
      | TIME_RANGE
      | TIMEOUT
      | TFTP
      | TLS_PROXY
      | TRACE
      | TRANSCEIVER
      | TRANSCEIVER_TYPE_CHECK
      | TRANSPARENT_HW_FLOODING
      | TUNNEL_GROUP
      | UDF
      | USERGROUP
      | USERNAME
      | VDC
      | VER
      |
      (
         VLAN
         (
            DOT1Q
         )
      )
      | VLAN_GROUP
      | VLAN_POLICY
      | VLT
      | VOICE
      | VOICE_PORT
      | VPC
      | VXLAN
      | VTY_POOL
      | WISM
      | WRED_PROFILE
      | WSMA
      | XDR
      | XML
   ) ~NEWLINE* NEWLINE
   (
      description_line
      | null_block_substanza
      | null_block_substanza_full
      | unrecognized_line
   )*
;

null_block_substanza
:
   (
      NO?
      (
         ACCEPT_DIALIN
         | ACCEPT_LIFETIME
         | ACCOUNTING
         | ACCOUNTING_SERVER_GROUP
         | ACTIVE
         | ADD_VLAN
         | ADDRESS
         | ADDRESS_POOL
         | ADDRESS_POOLS
         | ADMINISTRATIVE_WEIGHT
         | ADMIN_STATE
         | ADVERTISE
         | AESA
         | AIS_SHUT
         | ALARM_REPORT
         | ALLOCATE
         | ALLOW_CONNECTIONS
         | ALWAYS_ON_VPN
         | ANYCONNECT
         | ANYCONNECT_ESSENTIALS
         | APPLICATION
         | ARCHIVE_LENGTH
         | ARCHIVE_SIZE
         | ASSOCIATE
         | AUTHENTICATION_SERVER_GROUP
         | AUTHORIZATION_REQUIRED
         | AUTHORIZATION_SERVER_GROUP
         | AUTO_RECOVERY
         | BACK_UP
         | BACKGROUND_ROUTES_ENABLE
         | BANDWIDTH_PERCENTAGE
         |
         (
            BANNER
            (
               NONE
               | VALUE
            )
         )
         | BIND
         | BRIDGE_PRIORITY
         | BUCKETS
         | CABLELENGTH
         | CACHE_TIMEOUT
         | CALL
         | CALLER_ID
         | CAS_CUSTOM
         | CERTIFICATE
         | CHANNEL_GROUP
         | CHANNELIZED
         | CLIENT_GROUP
         | CLOCK
         | CODEC
         | COMMAND
         | CONNECT_SOURCE
         | CONTEXT
         | CPU_SHARE
         | CREDENTIALS
         | CRYPTOGRAPHIC_ALGORITHM
         | CSD
         | DEADTIME
         | DEFAULT
         | DEFAULT_ACTION
         | DEFAULT_DOMAIN
         | DEFAULT_GROUP_POLICY
         | DEFAULT_ROUTER
         | DELAY
         | DENY
         | DEPLOY
         | DESTINATION_PATTERN
         | DESTINATION_SLOT
         | DIAGNOSTIC
         | DISABLE
         | DISTRIBUTION
         | DNS_SERVER
         | DOMAIN_ID
         | DOMAIN_NAME
         | DROP
         | DS0_GROUP
         | DUAL_ACTIVE
         | ECHO
         | ECHO_CANCEL
         | EGRESS
         | ENABLED
         | ENCAPSULATION
         | ERROR_RECOVERY
         | ERSPAN_ID
         | ESCAPE_CHARACTER
         | EXIT
         | EXPECT
         | EXPORT
         | EXTENDED_COUNTERS
         | FABRIC
         | FAILED
         | FAIR_QUEUE
         | FALLBACK_DN
         | FIELDS
         | FILE_BROWSING
         | FILE_ENTRY
         | FILE_SIZE
         | FLUSH_AT_ACTIVATION
         | FORWARD_DIGITS
         | FRAMING
         | FREQUENCY
         | FT
         | G709
         | GATEWAY
         | GID
         | GROUP_ALIAS
         | GROUP_LOCK
         | GROUP_POLICY
         | GROUP_URL
         | H225
         | H323
         | HA_POLICY
         |
         (
            HASH SYMMETRIC
         )
         | HEARTBEAT_INTERVAL
         | HEARTBEAT_TIME
         | HELPER_ADDRESS
         | HIDDEN_LITERAL
         | HIDDEN_SHARES
         | HIDEKEYS
         | HIGH_AVAILABILITY
         | HISTORY
         | HOMEDIR
         | HOPS_OF_STATISTICS_KEPT
         | ICMP_ECHO
         | IDLE
         | IDLE_TIMEOUT
         | IMPORT
         | INCOMING
         | INGRESS
         | INSERVICE
         | INSTANCE
         |
         (
            INTERFACE POLICY
         )
         | INTERVAL
         |
         (
            (
               IP
               | IPV6
            )
            (
               ACCESS_GROUP
               | ADDRESS
               | ARP
               | FLOW
            )
         )
         | IPSEC_UDP
         | IPX
         | IPV6_ADDRESS_POOL
         | ISAKMP
         | FREQUENCY
         | KEEPOUT
         | KEY_STRING
         | KEYPATH
         | LACP_TIMEOUT
         | LEASE
         | LENGTH
         | LIFE
         | LIMIT_RESOURCE
         | LINECODE
         | LLDP
         | LOCAL_INTERFACE
         | LOG
         | LPTS
         | MAC_ADDRESS
         | MAP
         | MEDIA
         | MEMBER
         | MESH_GROUP
         | MONITORING
         | MSDP_PEER
         | MSIE_PROXY
         | NAMESPACE
         | NAT
         | NATPOOL
         | NEGOTIATE
         | NETWORK
         | NODE
         | NOTIFY
         | OPEN
         | OPERATION
         | OPTION
         | OPTIONS
         | OPS
         | ORIGINATOR_ID
         | OUI
         | PARAMETERS
         | PARITY
         | PASSWORD
         | PASSWORD_STORAGE
         | PATH_ECHO
         | PATH_JITTER
         | PATHS_OF_STATISTICS_KEPT
         | PEER_ADDRESS
         | PEER_CONFIG_CHECK_BYPASS
         | PEER_GATEWAY
         | PEER_ID_VALIDATE
         | PEER_KEEPALIVE
         | PEER_LINK
         | PEER_SWITCH
         | PERIODIC
         | PERMIT
         | PERSISTENT
         | PHYSICAL_PORT
         | PICKUP
         | PINNING
         | PM
         | POLICY
         | POLICY_LIST
         | PORT_NAME
         | PORTS
         | PREDICTOR
         | PREEMPT
         | PREFERRED_PATH
         | PREFIX
         | PRI_GROUP
         | PRIMARY_PORT
         | PRIMARY_PRIORITY
         | PROACTIVE
         | PROBE
         | PROPOSAL
         | PROVISION
         | PROXY_SERVER
         | RANDOM
         | RANDOM_DETECT
         | RD
         | REACT
         | REACTION
         | REAL
         | RECEIVE
         | REDISTRIBUTE
         | RELOAD
         | RELOAD_DELAY
         | REMARK
         | REMOTE_AS
         | REQUEST
         | REQUEST_DATA_SIZE
         | RESOURCES
         | RESPONDER
         | RETRANSMIT
         | RETRIES
         | REVISION
         | RING
         | ROLE
         | ROUTE
         | ROUTE_TARGET
         | RP_ADDRESS
         | RULE
         | SA_FILTER
         | SAMPLES_OF_HISTORY_KEPT
         | SATELLITE
         | SECRET
         | SEND_LIFETIME
         | SEQUENCE
         | SERVER
         | SERVERFARM
         | SERVER_PRIVATE
         | SERVICE_POLICY
         | SERVICE_QUEUE
         | SERVICE_TYPE
         | SESSION
         | SEVERITY
         | SHUT
         | SIGNAL
         | SINGLE_CONNECTION
         | SINGLE_ROUTER_MODE
         | SLOT
         | SORT_BY
         | SPEED
         | SPLIT_TUNNEL_NETWORK_LIST
         | SPLIT_TUNNEL_POLICY
         | SSH_KEYDIR
         | START_TIME
         | STICKY
         | STS_1
         | SWITCHBACK
         | SWITCHPORT
         | SYNC
         | SYSTEM_PRIORITY
         | TAG
         | TAGGED
         | TASK
         | TASK_SPACE_EXECUTE
         | TASKGROUP
         | TCP_CONNECT
         | THRESHOLD
         | TIMEOUT
         | TIMEOUTS
         | TIMER
         | TIMING
         | TM_VOQ_COLLECTION
         | TOP
         | TOS
         | TRACKING_PRIORITY_INCREMENT
         | TRANSLATION_PROFILE
         | TRIGGER
         | TRUNK
         | TRUNK_THRESHOLD
         | TRUST
         | TTL_THRESHOLD
         | TUNNEL
         | TUNNEL_GROUP
         | TUNNEL_GROUP_LIST
         | TYPE
         | UDP_JITTER
         | UID
         | USE_VRF
         | USERS
         | VERIFY_DATA
         | VERSION
         | VIRTUAL
         | VIRTUAL_ROUTER
         | VM_CPU
         | VM_MEMORY
         | VPN_FILTER
         | VPN_GROUP_POLICY
         | VPN_IDLE_TIMEOUT
         | VPN_SESSION_TIMEOUT
         | VPN_SIMULTANEOUS_LOGINS
         | VPN_TUNNEL_PROTOCOL
         | VSERVER
         | WAVELENGTH
         | WINS_SERVER
         | WITHOUT_CSD
         | WRED
         | XML_CONFIG
      )
      (
         remaining_tokens += ~NEWLINE
      )* NEWLINE
   )
;

null_block_substanza_full
:
   (
      (
         VLAN DEC
         (
            CLIENT
            | SERVER
         )
      )
   ) NEWLINE
;

null_no_stanza
:
   NO
   (
      (
         AAA
         (
            AUTHENTICATION
            | NEW_MODEL
            | ROOT
            |
            (
               USER DEFAULT_ROLE
            )
         )
      )
      | CLASS_MAP
      |
      (
         IP
         (
            AS_PATH
         )
      )
      | LOGGING
      |
      (
         SNMP_SERVER
         (
            AAA
            | ENABLE
            | GLOBALENFORCEPRIV
         )
      )
      | SSH
   ) ~NEWLINE* NEWLINE
;

null_standalone_stanza_DEPRECATED_DO_NOT_ADD_ITEMS
:
   (
      NO
   )?
   (
      ABSOLUTE_TIMEOUT
      |
      (
         ACCESS_LIST
         (
            (
               DEC REMARK
            )
            | VARIABLE
         )
      )
      | ACCOUNTING_PORT
      | ALIAS
      | AP
      | AQM_REGISTER_FNF
      | ARP
      | ASA
      | ASDM
      | ASSOCIATE
      | ASYNC_BOOTP
      | AUTHENTICATION_PORT
      | AUTO
      | BOOT
      | BOOT_END_MARKER
      | BOOT_START_MARKER
      | BRIDGE
      | CALL
      | CARD
      | CCM_MANAGER
      | CDP
      | CFS
      | CIPC
      | CLOCK
      | CLUSTER
      | CNS
      | CODEC
      | CONFIG_REGISTER
      | CONSOLE
      | CTS
      | DEC
      | DEFAULT
      | DEVICE_SENSOR
      | DHCPD
      | DIAGNOSTIC
      | DIALER_LIST
      | DISABLE
      | DNS
      | DNS_GUARD
      | DOMAIN_NAME
      | DSP
      | DSPFARM
      | DSS
      | ENVIRONMENT
      | ERRDISABLE
      | ESCAPE_CHARACTER
      | EXEC
      | FABRIC
      | FACILITY_ALARM
      | FILE
      | FIREWALL
      | FIRMWARE
      | FLOWCONTROL
      | FRAME_RELAY
      | FTP
      | FTP_SERVER
      | HARDWARE
      | HISTORY
      | HOST
      | HTTP
      | HW_MODULE
      | ICMP
      | ICMP_OBJECT
      | IDENTITY
      | INACTIVITY_TIMER
      |
      (
         IP
         (
            ADDRESS_POOL
            | ADMISSION
            | ALIAS
            | ARP
            | AUDIT
            | AUTH_PROXY
            | BOOTP
            | BGP_COMMUNITY
            | CEF
            | CLASSLESS
            | DEFAULT_NETWORK
            | DEVICE
            | DOMAIN
            | DOMAIN_LIST
            | DOMAIN_LOOKUP
            | DVMRP
            | EXTCOMMUNITY_LIST
            | FINGER
            | FLOW_CACHE
            | FLOW_EXPORT
            | FORWARD_PROTOCOL
            | FTP
            | GRATUITOUS_ARPS
            | HOST
            | HOST_ROUTING
            | HTTP
            | ICMP
            | IGMP
            | LOAD_SHARING
            | LOCAL
            | MFIB
            | MROUTE
            | MSDP
            | MULTICAST
            | MULTICAST_ROUTING
            | NAT
            | RADIUS
            | RCMD
            | ROUTING //might want to use this eventually

            | SAP
            | SCP
            | SLA
            | SUBNET_ZERO
            | TACACS
            | TCP
            | TELNET
            | TFTP
            | VERIFY
         )
      )
      | IP_ADDRESS_LITERAL
      |
      (
         IPV6
         (
            CEF
            | HOST
            | LOCAL
            | MFIB
            | MFIB_MODE
            | MLD
            | MULTICAST
            | MULTICAST_ROUTING
            | ND
            |
            (
               OSPF NAME_LOOKUP
            )
            | PIM
            | ROUTE
            | SOURCE_ROUTE
            | UNICAST_ROUTING
         )
      )
      | ISDN
      | LDAP_BASE_DN
      | LDAP_LOGIN
      | LDAP_LOGIN_DN
      | LDAP_NAMING_ATTRIBUTE
      | LDAP_SCOPE
      | LICENSE
      | LLDP
      | LOCATION
      | MAC_ADDRESS_TABLE
      | MEMORY_SIZE
      | MGCP
      | MICROCODE
      | MLS
      | MODEM
      | MTA
      | MULTILINK
      | MVR
      | NAME_SERVER
      | NAMES
      | NAT
      | NAT_CONTROL
      | NETCONF
      | NETWORK_OBJECT
      | NETWORK_CLOCK_PARTICIPATE
      | NETWORK_CLOCK_SELECT
      | OWNER
      | PAGER
      | PARSER
      | PARTICIPATE
      | PASSWORD
      | PERCENT
      | PHONE_PROXY
      | PLATFORM
      | PORT_CHANNEL
      | PORT_OBJECT
      | POWER
      | PRIORITY_QUEUE
      | PROCESS
      | PROMPT
      | PROTOCOL_OBJECT
      | QOS
      | QUIT
      | RADIUS_COMMON_PW
      | RADIUS_SERVER
      | RD
      | REDIRECT_FQDN
      | RESOURCE
      | RESOURCE_POOL
      | ROUTE
      | ROUTE_TARGET
      | RTR
      | SAME_SECURITY_TRAFFIC
      | SCHEDULER
      | SCRIPTING
      | SDM
      | SECURITY
      | SERVER_TYPE
      | SERVICE_POLICY
      | SETUP
      | SHELL
      | SMTP_SERVER
      | SNMP
      | SPD
      | SPE
      | SPEED
      | STOPBITS
      | SSL
      | STATIC
      | SUBNET
      | SUBSCRIBER
      | SUBSCRIBE_TO
      | SYSOPT
      | SYSTEM
      | TAG_SWITCHING
      | TELNET
      | TFTP_SERVER
      | THREAT_DETECTION
      | TRANSLATE
      | TYPE
      | UDLD
      | UNABLE
      | UPGRADE
      | USER_IDENTITY
      | USE_VRF
      | VERSION
      |
      (
         VLAN
         (
            ACCESS_LOG
            | CONFIGURATION
            | DOT1Q
            | INTERNAL
         )
      )
      | VMPS
      | VPDN
      | VPN
      | VTP
      | VOICE_CARD
      | WLAN
      | X25
      | X29
      | XLATE
      | XML SERVER
   )
   (
      remaining_tokens += ~NEWLINE
   )* NEWLINE
;

unrecognized_block_stanza
:
   unrecognized_line null_block_substanza*
;
