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
      | ARCHIVE
      | ATM
      | BASH
      | CLASS_MAP
      | CLI
      | CONTROL_PLANE
      | CONTROLLER
      | COPY
      |
      (
         CRYPTO
         (
            IPSEC
            |
            (
               ISAKMP
               (
                  KEY
                  | PEER
                  | POLICY
                  | PROFILE
               )
            )
            | KEY
            | KEYRING
            | MAP
            | PKI
         )
      )
      | DIAL_PEER
      | EVENT_HANDLER
      | FEX
      |
      (
         FLOW
         (
            EXPORTER
            | MONITOR
            | RECORD
         )
      )
      | GATEWAY
      | GROUP_POLICY
      |
      (
         IP
         (
            (
               ACCESS_LIST LOGGING
            )
            | ACCOUNTING_LIST
            | DECAP_GROUP
            | DHCP
            | FLOW_TOP_TALKERS
            | INSPECT
            | POLICY_LIST
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
         IPV6 ACCESS_LIST
      )
      | L2TP_CLASS
      | LINE
      | MAC
      | MANAGEMENT
      | MAP_CLASS
      | MAP_LIST
      | MLAG
      | MODULE
      | MONITOR
      | NO_BANNER
      | OPENFLOW
      | PLAT
      | POLICY_MAP
      | POWEROFF
      | PSEUDOWIRE_CLASS
      | REDUNDANCY
      | RMON
      | ROLE
      | SCCP
      | SPANNING_TREE
      | STCAPP
      | TEMPLATE
      | TERMINAL
      | TRANSCEIVER
      | VDC
      |
      (
         VLAN DEC
      )
      | VOICE
      | VOICE_PORT
      | VPC
      | VPDN_GROUP
   ) ~NEWLINE* NEWLINE
   (
      description_line
      | null_block_substanza
      | null_block_substanza_full
   )*
;

null_block_substanza
:
   (
      NO?
      (
         ABSOLUTE_TIMEOUT
         | ACCEPT_DIALIN
         | ACCESS_CLASS
         | ACCOUNTING_SERVER_GROUP
         | ACTION
         | ACTIVATION_CHARACTER
         | ADDRESS_POOL
         | ADMINISTRATIVE_WEIGHT
         | AESA
         | ANYCONNECT
         | ASSOCIATE
         | ASSOCIATION
         | AUTHENTICATION
         | AUTHENTICATION_SERVER_GROUP
         | AUTHORIZATION
         | AUTHORIZATION_REQUIRED
         | AUTHORIZATION_SERVER_GROUP
         | AUTO_SYNC
         | AUTOSELECT
         | BACKGROUND_ROUTES_ENABLE
         | BACKUPCRF
         | BANDWIDTH
         | BANNER
         | BIND
         | BRIDGE
         | CABLELENGTH
         | CACHE
         | CACHE_TIMEOUT
         | CALL
         | CALLER_ID
         | CAS_CUSTOM
         | CERTIFICATE
         | CHANNEL_GROUP
         | CHANNELIZED
         | CLASS
         | CLIENT_GROUP
         | CLOCK
         | COLLECT
         | CONFORM_ACTION
         | CONGESTION_CONTROL
         | CPTONE
         | CREDENTIALS
         | CRL
         | CRYPTO
         | DATABITS
         | DBL
         | DEFAULT_ACTION
         | DEFAULT_DOMAIN
         | DEFAULT_GROUP_POLICY
         | DEFAULT_ROUTER
         | DELAY
         | DENY
         | DESCRIPTION
         | DESTINATION
         | DIAGNOSTIC
         | DNS_SERVER
         | DOMAIN_ID
         | DROP
         | DS0_GROUP
         | DOMAIN_NAME
         | ENCAPSULATION
         | ENROLLMENT
         | ESCAPE_CHARACTER
         | EXCEED_ACTION
         | EXEC
         | EXEC_TIMEOUT
         | EXIT
         | EXPECT
         | EXPORT_PROTOCOL
         | EXPORTER
         | FABRIC
         | FAILED
         | FAILOVER
         | FAIR_QUEUE
         | FALLBACK_DN
         | FILE_BROWSING
         | FILE_ENTRY
         | FLUSH_AT_ACTIVATION
         | FQDN
         | FRAMING
         | FT
         | GATEWAY
         | GROUP_ALIAS
         | GROUP_POLICY
         | GROUP_URL
         | HEARTBEAT_TIME
         | HIDDEN
         | HIDDEN_SHARES
         | HIDEKEYS
         | HIGH_AVAILABILITY
         | HISTORY
         | IDLE
         | IDLE_TIMEOUT
         | IMPORT
         | INSERVICE
         | INSPECT
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
         | KEEPALIVE_ENABLE
         | KEYPAIR
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
         | MAIN_CPU
         | MATCH
         | MAXIMUM
         | MESSAGE_LENGTH
         | MODE
         | MODEM
         | MTU
         | NAME
         | NAT
         | NATPOOL
         | NEGOTIATE
         | NETWORK
         | NODE
         | NOTIFY
         | OPEN
         | PARAMETERS
         | PARENT
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
         | PICKUP
         | PINNING
         | POLICE
         | POLICY
         | POLICY_MAP
         | PORT
         | PREDICTOR
         | PREEMPT
         | PREFIX
         | PRI_GROUP
         | PRIORITY
         | PRIVILEGE
         | PROBE
         | PROTOCOL
         | QUEUE_BUFFERS
         | QUEUE_LIMIT
         | RANDOM_DETECT
         | RD
         | REAL
         | RECEIVE
         | RECORD
         | RECORD_ENTRY
         | REDISTRIBUTE
         | RELOAD_DELAY
         | REMARK
         | REMOTE_IP
         | REMOTE_PORT
         | REMOTE_SPAN
         | REMOVED
         | REQUEST
         | REQUEST_DATA_SIZE
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
         | SEQUENCE
         | SERVER
         | SERVERFARM
         | SERVER_PRIVATE
         | SERVICE
         | SERVICE_POLICY
         | SERVICE_TYPE
         | SESSION_DISCONNECT_WARNING
         | SESSION_LIMIT
         | SESSION_TIMEOUT
         | SET
         | SHAPE
         | SHUT
         | SHUTDOWN
         | SINGLE_ROUTER_MODE
         | SORT_BY
         | SOURCE
         | SPANNING_TREE
         | SPEED
         | SPLIT_TUNNEL_NETWORK_LIST
         | SPLIT_TUNNEL_POLICY
         | STICKY
         | STOPBITS
         | STP
         | SUBJECT_NAME
         | SWITCHBACK
         | SWITCHPORT
         | SYNC
         | TB_VLAN1
         | TB_VLAN2
         | TERMINAL_TYPE
         | TIMEOUTS
         | TIMER
         | TIMING
         | TOP
         | TOS
         | TRANSPORT
         | TRIGGER
         | TRUNK
         | TUNNEL
         | TUNNEL_GROUP
         | UDP_JITTER
         | USE_VRF
         | VIOLATE_ACTION
         | VIRTUAL
         | VIRTUAL_TEMPLATE
         | VPN_FILTER
         | VPN_IDLE_TIMEOUT
         | VPN_TUNNEL_PROTOCOL
         | VSERVER
         | WEBVPN
         | WINS_SERVER
         | WITHOUT_CSD
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

null_standalone_stanza
:
   (
      NO
   )?
   (
      AAA
      | AAA_SERVER
      | ABSOLUTE_TIMEOUT
      | ACCESS_GROUP
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
      | ADDRESS
      | ALERT_GROUP
      | ALIAS
      | ANYCONNECT
      | ANYCONNECT_ESSENTIALS
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
      | CALL_HOME
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
      | CONTACT_EMAIL_ADDR
      | CRL
      |
      (
         CRYPTO
         (
            CA
            |
            (
               ISAKMP
               (
                  ENABLE
                  | KEY
                  | INVALID_SPI_RECOVERY
               )
            )
         )
      )
      | CTL_FILE
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
      | ENABLE
      | ENCR
      | ENCRYPTION
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
      | FREQUENCY
      | FQDN
      | FTP
      | FTP_SERVER
      | GATEKEEPER
      | GROUP
      | GROUP_OBJECT
      | HARDWARE
      | HASH
      | HISTORY
      | HOST
      | HTTP
      | HW_MODULE
      | ICMP
      | ICMP_ECHO
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
            |
            (
               OSPF NAME_LOOKUP
            )
            | PIM
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
      | KEEPOUT
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
      | LOGGING
      | MAC_ADDRESS_TABLE
      | MAIL_SERVER
      | MAXIMUM
      | MEDIA_TERMINATION
      | MEMORY_SIZE
      | MGCP
      | MICROCODE
      | MLS
      | MODE
      | MODEM
      | MPLS
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
      | NTP
      | OBJECT
      | OBJECT_GROUP
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
      | PRE_SHARED_KEY
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
      | SENDER
      | SERIAL_NUMBER
      | SERVER
      | SERVER_TYPE
      | SERVICE
      | SERVICE_POLICY
      | SETUP
      | SFLOW
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
      | SWITCH
      | SYSOPT
      | SYSTEM
      | TABLE_MAP
      | TACACS_SERVER
      | TAG
      | TAG_SWITCHING
      | TELNET
      | TFTP_SERVER
      | THREAT_DETECTION
      | TIMEOUT
      | TLS_PROXY
      | TRACK
      | TRANSLATE
      | TRANSPORT
      | TUNNEL_GROUP_LIST
      | TYPE
      | UDLD
      | UNABLE
      | UPGRADE
      | USER_IDENTITY
      | USE_VRF
      | USERNAME
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
      | WEBVPN
      | WLAN
      | WSMA
      | X25
      | X29
      | XLATE
      | XML SERVER
      | XX_HIDE
   )
   (
      remaining_tokens += ~NEWLINE
   )* NEWLINE
;
