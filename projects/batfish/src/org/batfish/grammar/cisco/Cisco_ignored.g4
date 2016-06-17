parser grammar Cisco_ignored;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

null_block_stanza
:
   NO?
   (
      AAA
      | ACCESS_GROUP
      | ACCESS
      | ADMIN
      | ALLOW
      | ARCHIVE
      | ATM
      | BASH
      | BFD
      | BGP DISABLE_ADVERTISEMENT
      | BSD_USERNAME
      | CALL_HOME
      | CAM_ACL
      | CAM_PROFILE
      | CEF
      | CHAT_SCRIPT
      | CLI
      | CLOCK
      | CONFDCONFIG
      | CONFIGURATION
      | CONTROLLER
      | COPY
      | CPD
      | CRYPTO
      | CTL_FILE
      | DAEMON
      | DCB
      | DCB_BUFFER_THRESHOLD
      | DEBUG
      | DEFAULT_MAX_FRAME_SIZE
      | DEFAULT_VALUE
      | DIAL_PEER
      | DOMAIN
      | DO STOP
      | END
      | EVENT_HANDLER
      | FEX
      | FLOW
      | FPD
      | GATEKEEPER
      | GATEWAY
      | GROUP
      | GROUP_POLICY
      | HASH_ALGORITHM
      | HW_SWITCH
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
            | BOOTP_RELAY
            | DECAP_GROUP
            | DHCP
            | DNS
            | ECMP_GROUP
            | FLOW_TOP_TALKERS
            | HARDWARE
            | ICMP_ERRORS
            | INSPECT
            |
            (
               OSPF NAME_LOOKUP
            )
            | POLICY_LIST
            | ROUTER_ID
            | SLA
            | SOURCE
            | VIRTUAL_ROUTER
            |
            (
               VRF ~NEWLINE
            )
         )
      )
      | IPC
      |
      (
         IPV4
         (
            ASSEMBLER
            | CONFLICT_POLICY
            | HARDWARE
            | UNNUMBERED
            | VIRTUAL
         )
      )
      |
      (
         IPV6
         (
            CONFLICT_POLICY
            | HARDWARE
         )
      )
      | KEY
      | KRON
      | L2TP_CLASS
      | LACP
      | LINECARD
      | LOGGING
      | MAC
      | MAC_LEARN
      | MACRO
      | MAP_CLASS
      | MAP_LIST
      | MAXIMUM_PATHS
      | MEDIA_TERMINATION
      | MLAG
      | MODULE
      | MONITOR
      |
      (
         MPLS
         (
            (
               IP
               | LDP ~NEWLINE
            )
            | OAM
         )
      )
      | MULTI_CONFIG
      |
      (
         NO
         (
            IP AS_PATH
         )
      )
      | NLS
      | NO_BANNER
      | NSR
      | ONE
      | OPENFLOW
      | PLAT
      | PLATFORM
      | POLICY_MAP
      | POLICY_MAP_INPUT
      | POLICY_MAP_OUTPUT
      | POWEROFF
      | PRIORITY_FLOW_CONTROL
      | PROTOCOL
      | PSEUDOWIRE_CLASS
      | PTP
      | QOS_POLICY_OUTPUT
      | REDUNDANCY
      | RELOAD_TYPE
      | RMON
      | ROLE
      | ROUTER
      (
         LOG
         | VRRP
      )
      | SAMPLER
      | SAMPLER_MAP
      | SCCP
      | SDR
      | SERVICE_CLASS
      | SFLOW
      | SPANNING_TREE
      | STACK_MAC
      | STACK_UNIT
      | STCAPP
      | SVCLC
      | SWITCH
      | SWITCH_PROFILE
      | SWITCH_TYPE
      | SYSTEM_INIT
      | SYSTEM_MAX
      | TABLE_MAP
      | TACACS
      | TACACS_SERVER
      | TCP
      | TEMPLATE
      | TERMINAL
      | TIMEOUT
      | TRACE
      | TRACK
      | TRANSCEIVER
      | UDF
      | USERGROUP
      | USERNAME
      | VDC
      | VER
      |
      (
         VLAN
         (
            DEC
            | ACCESS_MAP
         )
      )
      | VLT
      | VOICE
      | VOICE_PORT
      | VPC
      | VPDN_GROUP
      | WEBVPN
      | WRED_PROFILE
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
         ABSOLUTE_TIMEOUT
         | ACCEPT_DIALIN
         | ACCEPT_LIFETIME
         | ACCOUNTING
         | ACCOUNTING_SERVER_GROUP
         | ACTION
         | ACTIVATION_CHARACTER
         | ADDRESS
         | ADDRESS_POOL
         | ADMINISTRATIVE_WEIGHT
         | ADVERTISE
         | AESA
         | ALERT_GROUP
         | ANYCONNECT
         | ANYCONNECT_ESSENTIALS
         | APPLICATION
         | ARCHIVE_LENGTH
         | ARCHIVE_SIZE
         | ASSOCIATE
         | ASSOCIATION
         | AUTHENTICATION
         | AUTHENTICATION_SERVER_GROUP
         | AUTHORIZATION
         | AUTHORIZATION_REQUIRED
         | AUTHORIZATION_SERVER_GROUP
         | AUTO_RECOVERY
         | AUTO_SYNC
         | AUTOSELECT
         | BACK_UP
         | BACKGROUND_ROUTES_ENABLE
         | BACKUPCRF
         | BANDWIDTH
         | BANDWIDTH_PERCENTAGE
         | BANNER
         | BIND
         | BRIDGE
         | BRIDGE_PRIORITY
         | CABLELENGTH
         | CACHE
         | CACHE_TIMEOUT
         | CALL
         | CALLER_ID
         | CAS_CUSTOM
         | CDP_URL
         | CERTIFICATE
         | CHANNEL_GROUP
         | CHANNELIZED
         | CLASS
         | CLIENT_GROUP
         | CLOCK
         | CODEC
         | COLLECT
         | COMMAND
         | CONFORM_ACTION
         | CONGESTION_CONTROL
         | CONNECT_SOURCE
         | CONTEXT
         | CONTACT_EMAIL_ADDR
         | CONTRACT_ID
         | CPTONE
         | CREDENTIALS
         | CRL
         | CRYPTOGRAPHIC_ALGORITHM
         | DATABITS
         | DBL
         | DEFAULT
         | DEFAULT_ACTION
         | DEFAULT_DOMAIN
         | DEFAULT_GROUP_POLICY
         | DEFAULT_ROUTER
         | DELAY
         | DENY
         | DESCRIPTION
         | DESTINATION
         | DESTINATION_PATTERN
         | DEVICE
         | DIAGNOSTIC
         | DISABLE
         | DNS_SERVER
         | DOMAIN_ID
         | DOMAIN_NAME
         | DROP
         | DS0_GROUP
         | DTMF_RELAY
         | DUAL_ACTIVE
         | ECHO
         | ECHO_CANCEL
         | EGRESS
         | ENABLE
         | ENCAPSULATION
         | ENCRYPTION
         | END_POLICY_MAP
         | ENROLLMENT
         | ERSPAN_ID
         | ESCAPE_CHARACTER
         | EXCEED_ACTION
         | EXEC
         | EXEC_TIMEOUT
         | EXIT
         | EXPECT
         | EXPORT
         | EXPORT_PROTOCOL
         | EXPORTER
         | FABRIC
         | FAILED
         | FAILOVER
         | FAIR_QUEUE
         | FALLBACK_DN
         | FILE_BROWSING
         | FILE_ENTRY
         | FILE_SIZE
         | FLUSH_AT_ACTIVATION
         | FORWARD_DIGITS
         | FQDN
         | FRAMING
         | FREQUENCY
         | FT
         | GATEWAY
         | GID
         | GROUP
         | GROUP_ALIAS
         | GROUP_POLICY
         | GROUP_URL
         | HEARTBEAT_INTERVAL
         | HEARTBEAT_TIME
         | HIDDEN_LITERAL
         | HIDDEN_SHARES
         | HIDEKEYS
         | HIGH_AVAILABILITY
         | HISTORY
         | HOMEDIR
         | ICMP_ECHO
         | IDLE
         | IDLE_TIMEOUT
         | IMPORT
         | INCOMING
         | INGRESS
         | INSERVICE
         | INSPECT
         | INSTANCE
         | INTEGRITY
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
               ACCESS_CLASS
               | ACCESS_GROUP
               | ADDRESS
               | FLOW
            )
         )
         | IPSEC_UDP
         | IPX
         | IPV6_ADDRESS_POOL
         | ISAKMP
         | ISSUER_NAME
         | KEEPALIVE_ENABLE
         | KEEPOUT
         | KEY_STRING
         | KEYPAIR
         | KEYPATH
         | KEYRING
         | L2TP
         | LEASE
         | LENGTH
         | LIMIT_RESOURCE
         | LINE
         | LINECODE
         | LLDP
         | LOCAL_INTERFACE
         | LOCAL_IP
         | LOCAL_PORT
         | LOCATION
         | LOG
         | LOGGING
         | LOGIN
         | LPTS
         | MAC_ADDRESS
         | MAIN_CPU
         | MAP
         | MATCH
         | MAXIMUM
         | MEMBER
         | MESH_GROUP
         | MESSAGE_LENGTH
         | MODE
         | MODEM
         | MONITORING
         | MTU
         | NAME
         | NAMESPACE
         | NAT
         | NATPOOL
         | NEGOTIATE
         | NETWORK
         | NODE
         | NOTIFICATION_TIMER
         | NOTIFY
         | OPEN
         | OPTION
         | OPTIONS
         | OPS
         | OUI
         | PARAMETERS
         | PARENT
         | PARITY
         | PASSWORD
         | PASSWORD_STORAGE
         | PATH_JITTER
         | PAUSE
         | PEER_ADDRESS
         | PEER_CONFIG_CHECK_BYPASS
         | PEER_GATEWAY
         | PEER_KEEPALIVE
         | PEER_LINK
         | PERMIT
         | PERSISTENT
         | PHONE_NUMBER
         | PICKUP
         | PINNING
         | POLICE
         | POLICY
         | POLICY_LIST
         | POLICY_MAP
         | PORT
         | PREDICTOR
         | PRE_SHARED_KEY
         | PREEMPT
         | PREFIX
         | PRI_GROUP
         | PRIMARY_PRIORITY
         | PRIORITY
         | PRIVILEGE
         | PROBE
         | PROPOSAL
         | PROTOCOL
         | PROXY_SERVER
         | QUEUE_BUFFERS
         | QUEUE_LIMIT
         | RANDOM
         | RANDOM_DETECT
         | RD
         | REAL
         | RECEIVE
         | RECORD
         | RECORD_ENTRY
         | REDISTRIBUTE
         | RELOAD
         | RELOAD_DELAY
         | REMARK
         | REMOTE_AS
         | REMOTE_IP
         | REMOTE_PORT
         | REMOTE_SPAN
         | REMOVED
         | REQUEST
         | REQUEST_DATA_SIZE
         | RESOURCES
         | RETRANSMIT
         | RETRIES
         | REVERSE_ROUTE
         | REVISION
         | RING
         | ROLE
         | ROTARY
         | ROUTE
         | ROUTE_TARGET
         | RULE
         | SCHEME
         | SECRET
         | SEND_LIFETIME
         | SENDER
         | SEQUENCE
         | SERVER
         | SERVERFARM
         | SERVER_PRIVATE
         | SERVICE
         | SERVICE_POLICY
         | SERVICE_QUEUE
         | SERVICE_TYPE
         | SESSION_DISCONNECT_WARNING
         | SESSION_LIMIT
         | SESSION_TIMEOUT
         | SET
         | SEVERITY
         | SHAPE
         | SHUT
         | SHUTDOWN
         | SIGNAL
         | SINGLE_CONNECTION
         | SINGLE_ROUTER_MODE
         | SITE_ID
         | SLOT
         | SMTP
         | SORT_BY
         | SOURCE
         | SPANNING_TREE
         | SPEED
         | SPLIT_TUNNEL_NETWORK_LIST
         | SPLIT_TUNNEL_POLICY
         | SSH_KEYDIR
         | STICKY
         | STOPBITS
         | STP
         | STREET_ADDRESS
         | SUBJECT_NAME
         | SWITCHBACK
         | SWITCHPORT
         | SYNC
         | SYSTEM_PRIORITY
         | TAG
         | TASKGROUP
         | TB_VLAN1
         | TB_VLAN2
         | TCP_CONNECT
         | TERMINAL_TYPE
         | THRESHOLD
         | TIMEOUT
         | TIMEOUTS
         | TIMER
         | TIMESTAMP
         | TIMING
         | TOP
         | TOS
         | TRACKING_PRIORITY_INCREMENT
         | TRANSPORT
         | TRIGGER
         | TRUNK
         | TRUST
         | TUNNEL
         | TUNNEL_GROUP
         | TUNNEL_GROUP_LIST
         | UDP_JITTER
         | UID
         | UNTAGGED
         | USE_VRF
         | USERS
         | VAD
         | VERSION
         | VIOLATE_ACTION
         | VIRTUAL
         | VIRTUAL_ROUTER
         | VIRTUAL_TEMPLATE
         | VM_CPU
         | VM_MEMORY
         | VPN_FILTER
         | VPN_IDLE_TIMEOUT
         | VPN_TUNNEL_PROTOCOL
         | VSERVER
         | VTY_POOL
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
      |
      (
         VRF variable
      )
   ) NEWLINE
;

null_standalone_stanza_DEPRECATED_DO_NOT_ADD_ITEMS
:
   (
      NO
   )?
   (
      AAA_SERVER
      | ABSOLUTE_TIMEOUT
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
      | ACTION
      | ACTIVE
      | ALIAS
      | AP
      | AQM_REGISTER_FNF
      | ARP
      | ASA
      | ASDM
      | ASSOCIATE
      | ASYNC_BOOTP
      | AUTHENTICATION
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
      | CRL
      | CTS
      | DEC
      | DEFAULT
      | DESCRIPTION
      | DESTINATION
      | DEVICE_SENSOR
      | DHCPD
      | DIAGNOSTIC
      | DIALER_LIST
      | DISABLE
      | DNS
      | DNS_GUARD
      | DOMAIN_NAME
      | DOT11
      | DSP
      | DSPFARM
      | DSS
      | DYNAMIC_ACCESS_POLICY_RECORD
      | ENCR
      | ENROLLMENT
      | ENVIRONMENT
      | ERRDISABLE
      | ESCAPE_CHARACTER
      | EVENT
      | EXCEPTION
      | EXEC
      | FABRIC
      | FACILITY_ALARM
      | FAILOVER
      | FEATURE
      | FILE
      | FIREWALL
      | FIRMWARE
      | FLOWCONTROL
      | FRAME_RELAY
      | FQDN
      | FTP
      | FTP_SERVER
      | HARDWARE
      | HASH
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
            | DOMAIN_NAME
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
            | NAME_SERVER
            | NAT
            | RADIUS
            | RCMD
            | ROUTING //might want to use this eventually

            | SAP
            | SCP
            | SLA
            | SOURCE_ROUTE
            | SSH
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
      | KEYPAIR
      | KEYRING
      | LDAP_BASE_DN
      | LDAP_LOGIN
      | LDAP_LOGIN_DN
      | LDAP_NAMING_ATTRIBUTE
      | LDAP_SCOPE
      | LICENSE
      | LIFETIME
      | LLDP
      | LOCATION
      | MAC_ADDRESS_TABLE
      | MAIL_SERVER
      | MAXIMUM
      | MEMORY_SIZE
      | MGCP
      | MICROCODE
      | MLS
      | MODE
      | MODEM
      | MTA
      | MTU
      | MULTILINK
      | MVR
      | NAME_SERVER
      | NAME
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
      | PRIORITY
      | PRIORITY_QUEUE
      | PRIVILEGE
      | PROCESS
      | PROFILE
      | PROMPT
      | PROTOCOL_OBJECT
      | QOS
      | QUIT
      | RADIUS_COMMON_PW
      | RADIUS_SERVER
      | RD
      | RECORD_ENTRY
      | REDIRECT_FQDN
      | RESOURCE
      | RESOURCE_POOL
      | REVERSE_ROUTE
      | REVOCATION_CHECK
      | ROUTE
      | ROUTE_TARGET
      | RSAKEYPAIR
      | RTR
      | SAME_SECURITY_TRAFFIC
      | SCHEDULE
      | SCHEDULER
      | SCRIPTING
      | SDM
      | SECURITY
      | SERIAL_NUMBER
      | SERVER
      | SERVER_TYPE
      | SERVICE
      | SERVICE_POLICY
      | SETUP
      | SHELL
      | SMTP_SERVER
      | SNMP
      | SNMP_SERVER
      | SOURCE
      | SOURCE_INTERFACE
      | SOURCE_IP_ADDRESS
      | SPANNING_TREE
      | SPD
      | SPE
      | SPEED
      | STOPBITS
      | SSH
      | SSL
      | STATIC
      | SUBJECT_NAME
      | SUBNET
      | SUBSCRIBER
      | SUBSCRIBE_TO
      | SUBSCRIBE_TO_ALERT_GROUP
      | SYSOPT
      | SYSTEM
      | TAG_SWITCHING
      | TELNET
      | TFTP_SERVER
      | THREAT_DETECTION
      | TLS_PROXY
      | TRANSLATE
      | TRANSPORT
      | TYPE
      | UDLD
      | UNABLE
      | UPGRADE
      | USER_IDENTITY
      | USE_VRF
      | VALIDATION_USAGE
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
      | WSMA
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
