lexer grammar CiscoLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@header {
package org.batfish.grammar.cisco;
}

@members {
private int lastTokenType = -1;
private boolean enableIPV6_ADDRESS = true;
private boolean enableIP_ADDRESS = true;
private boolean enableDEC = true;
private boolean enableACL_NUM = false;
private boolean enableCOMMUNITY_LIST_NUM = false;
private boolean inCommunitySet = false;
private boolean _foundry = false;

@Override
public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
       lastTokenType = token.getType();
    }
}

public void setFoundry(boolean foundry) {
   _foundry = foundry;
}

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
   ACL_NUM_EXTENDED_MAC,
   ACL_NUM_FOUNDRY_L2,
   ACL_NUM_IPX,
   ACL_NUM_IPX_SAP,
   ACL_NUM_MAC,
   ACL_NUM_OTHER,
   ACL_NUM_PROTOCOL_TYPE_CODE,
   ACL_NUM_STANDARD,
   AS_PATH_SET_REGEX,
   CHAIN,
   COMMUNITY_LIST_NUM_EXPANDED,
   COMMUNITY_LIST_NUM_STANDARD,
   COMMUNITY_SET_REGEX,
   CONFIG_SAVE,
   HEX_FRAGMENT,
   ISO_ADDRESS,
   PAREN_LEFT_LITERAL,
   PAREN_RIGHT_LITERAL,
   PIPE,
   PROMPT_TIMEOUT,
   RAW_TEXT,
   SELF_SIGNED,
   SLIP_PPP,
   VALUE
} // Cisco Keywords

AAA
:
   'aaa'
;

AAA_SERVER
:
   'aaa-server'
;

AAA_USER
:
   'aaa-user'
;

ABSOLUTE_TIMEOUT
:
   'absolute-timeout'
;

ACCEPT_DIALIN
:
   'accept-dialin'
;

ACCEPT_LIFETIME
:
   'accept-lifetime'
;

ACCEPT_OWN
:
   'accept-own'
;

ACCEPT_REGISTER
:
   'accept-register'
;

ACCEPT_RP
:
   'accept-rp'
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

ACCESS_MAP
:
   'access-map'
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

ACCOUNTING_THRESHOLD
:
   'accounting-threshold'
;

ACCT_PORT
:
   'acct-port'
;

ACK
:
   'ack'
;

ACL_POLICY
:
   'acl-policy'
;

ACLLOG
:
   'acllog'
;

ACTION
:
   'action'
;

ACTION_TYPE
:
   'action-type'
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

ADD_VLAN
:
   'add-vlan'
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

ADDRESS_POOLS
:
   'address-pools'
;

ADDRESS_RANGE
:
   'address-range'
;

ADDRESS_TABLE
:
   'address-table'
;

ADDRGROUP
:
   'addrgroup'
;

ADJACENCY
:
   'adjacency'
;

ADJACENCY_CHECK
:
   'adjacency-check'
;

ADJACENCY_STALE_TIMER
:
   'adjacency-stale-timer'
;

ADJMGR
:
   'adjmgr'
;

ADMIN
:
   'admin'
;

ADMIN_STATE
:
   'admin-state'
;

ADMINISTRATIVE_WEIGHT
:
   'administrative-weight'
;

ADMISSION
:
   'admission'
;

ADVERTISE
:
   'advertise'
;

ADVERTISEMENT_INTERVAL
:
   'advertisement-interval'
;

ADVERTISE_INACTIVE
:
   'advertise-inactive'
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

AF_GROUP
:
   'af-group'
;

AF11
:
   'af11'
;

AF12
:
   'af12'
;

AF13
:
   'af13'
;

AF21
:
   'af21'
;

AF22
:
   'af22'
;

AF23
:
   'af23'
;

AF31
:
   'af31'
;

AF32
:
   'af32'
;

AF33
:
   'af33'
;

AF41
:
   'af41'
;

AF42
:
   'af42'
;

AF43
:
   'af43'
;

AFFINITY
:
   'affinity'
;

AFFINITY_MAP
:
   'affinity-map'
;

AGE
:
   'age'
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

AIS_SHUT
:
   'ais-shut'
;

ALARM
:
   'alarm'
;

ALARM_REPORT
:
   'alarm-report'
;

ALERT_GROUP
:
   'alert-group'
;

ALERTS
:
   'alerts'
;

ALIAS
:
   'alias'
;

ALL_ALARMS
:
   'all-alarms'
;

ALL_OF_ROUTER
:
   'all-of-router'
;

ALL
:
   'all'
;

ALLOCATE
:
   'allocate'
;

ALLOW
:
   'allow'
;

ALLOW_CONNECTIONS
:
   'allow-connections'
;

ALLOW_DEFAULT
:
   'allow-default'
;

ALLOW_SELF_PING
:
   'allow-self-ping'
;

ALLOWED
:
   'allowed'
;

ALLOWAS_IN
:
   'allowas-in'
;

ALTERNATE_ADDRESS
:
   'alternate-address'
;

ALWAYS
:
   'always'
;

ALWAYS_COMPARE_MED
:
   'always-compare-med'
;

ALWAYS_ON_VPN
:
   'always-on-vpn'
;

AND
:
   'and'
;

ANTENNA
:
   'antenna'
;

ANY
:
   'any'
;

ANY4
:
   'any4'
;

ANY6
:
   'any6'
;

ANYCONNECT
:
   'anyconnect'
;

ANYCONNECT_ESSENTIALS
:
   'anyconnect-essentials'
;

AOL
:
   'aol'
;

AP
:
   'ap'
;

API
:
   'api'
;

APPLETALK
:
   'appletalk'
;

APPLICATION
:
   'application'
;

APPLY
:
   'apply'
;

AQM_REGISTER_FNF
:
   'aqm-register-fnf'
;

ARAP
:
   'arap'
;

ARCHIVE
:
   'archive'
;

ARCHIVE_LENGTH
:
   'archive-length'
;

ARCHIVE_SIZE
:
   'archive-size'
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

AS_OVERRIDE
:
   'as-override'
;

AS_PATH
:
   'as-path' -> pushMode ( M_AsPath )
;

AS_PATH_SET
:
   'as-path-set'
;

AS_SET
:
   'as-set'
;

AS
:
   'as'
;

ASA
:
   'ASA'
;

ASCII_AUTHENTICATION
:
   'ascii-authentication'
;

ASDM
:
   'asdm'
;

ASDM_BUFFER_SIZE
:
   'asdm-buffer-size'
;

ASN
:
   'asn'
;

ASSEMBLER
:
   'assembler'
;

ASSOC_RETRANSMIT
:
   'assoc-retransmit'
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

ATTEMPTS
:
   'attempts'
;

ATTRIBUTE_DOWNLOAD
:
   'attribute-download'
;

ATTRIBUTE_MAP
:
   'attribute-map'
;

ATTRIBUTE_NAMES
:
   'attribute-names'
;

ATTRIBUTE_SET
:
   'attribute-set'
;

AUDIT
:
   'audit'
;

AUTH
:
   'auth'
;

AUTH_PORT
:
   'auth-port'
;

AUTH_PROXY
:
   'auth-proxy'
;

AUTHENTICATE
:
   'authenticate'
;

AUTHENTICATION
:
   'authentication' -> pushMode ( M_Authentication )
;

AUTHENTICATION_KEY
:
   'authentication-key'
;

AUTHENTICATION_PORT
:
   'authentication-port'
;

AUTHENTICATION_RETRIES
:
   'authentication-retries'
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

AUTO_LOCAL_ADDR
:
   'auto-local-addr'
;

AUTO_RECOVERY
:
   'auto-recovery'
;

AUTO_RP
:
   'auto-rp'
;

AUTO_SHUTDOWN_NEW_NEIGHBORS
:
   'auto-shutdown-new-neighbors'
;

AUTO_SUMMARY
:
   'auto-summary'
;

AUTO_SYNC
:
   'auto-sync'
;

AUTO_TUNNEL
:
   'auto-tunnel'
;

AUTOHANGUP
:
   'autohangup'
;

AUTOROUTE
:
   'autoroute'
;

AUTORP
:
   'autorp'
;

AUTOSELECT
:
   'autoselect'
;

AUTOSTATE
:
   'autostate'
;

AUX
:
   'aux'
;

BACK_UP
:
   'back-up'
;

BACKBONEFAST
:
   'backbonefast'
;

BACKGROUND_ROUTES_ENABLE
:
   'background-routes-enable'
;

BACKUP
:
   'backup'
;

BACKUPCRF
:
   'backupcrf'
;

BANDWIDTH
:
   'bandwidth'
;

BANDWIDTH_PERCENTAGE
:
   'bandwidth-percentage'
;

BANNER
:
   'banner' -> pushMode ( M_Banner )
;

BASH
:
   'bash'
;

BEACON
:
   'beacon'
;

BESTPATH
:
   'bestpath'
;

BEYOND_SCOPE
:
   'beyond-scope'
;

BFD
:
   'bfd'
;

BFD_ENABLE
:
   'bfd-enable'
;

BGP
:
   'bgp'
;

BGP_COMMUNITY
:
   'bgp-community'
;

BGP_POLICY
:
   'bgp-policy'
;

BIDIR_ENABLE
:
   'bidir-enable'
;

BIDIR_OFFER_INTERVAL
:
   'bidir-offer-interval'
;

BIDIR_OFFER_LIMIT
:
   'bidir-offer-limit'
;

BIFF
:
   'biff'
;

BIND
:
   'bind'
;

BLOCK
:
   'block'
;

BLOGGERD
:
   'bloggerd'
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

BOOTFILE
:
   'bootfile'
;

BOOTP_RELAY
:
   'bootp-relay'
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

BORDER
:
   'border'
;

BORDER_ROUTER
:
   'border-router'
;

BOTH
:
   'both'
;

BOUNDARY
:
   'boundary'
;

BREAKOUT
:
   'breakout'
;

BRIDGE
:
   'bridge'
;

BRIDGE_DOMAIN
:
   'bridge-domain'
;

BRIDGE_GROUP
:
   'bridge-group'
;

BRIDGE_PRIORITY
:
   'bridge-priority'
;

BROADCAST
:
   'broadcast'
;

BROADCAST_ADDRESS
:
   'broadcast-address'
;

BSD_CLIENT
:
   'bsd-client'
;

BSD_USERNAME
:
   'bsd-username'
;

BSR_BORDER
:
   'bsr-border'
;

BSR_CANDIDATE
:
   'bsr-candidate'
;

BUCKETS
:
   'buckets'
;

BUFFER_SIZE
:
   'buffer-size'
;

BUFFERED
:
   'buffered'
;

BUNDLE
:
   'bundle'
;

BUFFERS
:
   'buffers'
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
   'cablelength' -> pushMode ( M_COMMENT )
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

CALLHOME
:
   'callhome'
;

CAM_ACL
:
   'cam-acl'
;

CAM_PROFILE
:
   'cam-profile'
;

CAPABILITY
:
   'capability'
;

CARD
:
   'card'
;

CARRIER_DELAY
:
   'carrier-delay'
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

CDP_URL
:
   'cdp-url'
;

CEF
:
   'cef'
;

CERTIFICATE
:
   'certificate' -> pushMode ( M_Certificate )
;

CFS
:
   'cfs'
;

CGMP
:
   'cgmp'
;

CHANGES
:
   'changes'
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

CHAP
:
   'chap'
;

CHARGEN
:
   'chargen'
;

CHASSIS_ID
:
   'chassis-id'
;

CHAT_SCRIPT
:
   'chat-script'
;

CIPC
:
   'cipc'
;

CIR
:
   'cir'
;

CIRCUIT_TYPE
:
   'circuit-type'
;

CISP
:
   'cisp'
;

CITRIX_ICA
:
   'citrix-ica'
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

CLEANUP
:
   'cleanup'
;

CLEAR
:
   'clear'
;

CLI
:
   'cli'
;

CLIENT
:
   'client'
;

CLIENT_GROUP
:
   'client-group'
;

CLIENT_IDENTIFIER
:
   'client-identifier'
;

CLIENT_NAME
:
   'client-name'
;

CLIENT_TO_CLIENT
:
   'client-to-client'
;

CLNS
:
   'clns'
;

CLOCK
:
   'clock'
;

CLOCK_PERIOD
:
   'clock-period'
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

COAP
:
   'coap'
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

COMMAND
:
   'command'
;

COMMANDER_ADDRESS
:
   'commander-address'
   { enableIPV6_ADDRESS = false; }

;

COMMANDS
:
   'commands'
;

COMMIT
:
   'commit'
;

COMMON
:
   'common'
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

COMMUNITY_MAP
:
   'community-map'
;

COMMUNITY_SET
:
   'community-set'
   {
      inCommunitySet = true;
      enableIPV6_ADDRESS = false;
   }

;

COMPARE_ROUTERID
:
   'compare-routerid'
;

CON
:
   'con'
;

CONFDCONFIG
:
   'confdConfig'
;

CONFIG_COMMANDS
:
   'config-commands'
;

CONFIG_REGISTER
:
   'config-register'
;

CONFIGURATION
:
   'configuration'
;

CONFIGURE
:
   'configure'
;

CONFLICT_POLICY
:
   'conflict-policy'
;

CONFORM_ACTION
:
   'conform-action'
;

CONGESTION_CONTROL
:
   'congestion-control'
;

CONNECT_SOURCE
:
   'connect-source'
;

CONNECTED
:
   'connected'
;

CONNECTION
:
   'connection'
;

CONSOLE
:
   'console'
;

CONTACT
:
   'contact'
;

CONTACT_EMAIL_ADDR
:
   'contact-email-addr'
;

CONTACT_NAME
:
   'contact-name' -> pushMode ( M_Description )
;

CONTEXT
:
   'context'
;

CONTINUE
:
   (
      'continue'
   )
   |
   (
      'Continue'
   )
;

CONTRACT_ID
:
   'contract-id'
;

CONTROL_APPS_USE_MGMT_PORT
:
   'control-apps-use-mgmt-port'
;

CONTROL_DIRECTION
:
   'control-direction'
;

CONTROL_PLANE
:
   'control-plane'
;

CONTROLLER
:
   'controller'
;

CONVERSION_ERROR
:
   'conversion-error'
;

COPP
:
   'copp'
;

COPY
:
   'copy'
;

COS
:
   'cos'
;

COS_QUEUE_GROUP
:
   'cos-queue-group'
;

COST
:
   'cost'
;

COUNT
:
   'count'
;

COUNTER
:
   'counter'
;

COUNTERS
:
   'counters'
;

CPD
:
   'cpd'
;

CPTONE
:
   'cptone'
;

CPU_SHARE
:
   'cpu-share'
;

CRASHINFO
:
   'crashinfo'
;

CRC
:
   'crc'
;

CREDENTIALS
:
   'credentials'
;

CRITICAL
:
   'critical'
;

CRYPTO
:
   'crypto'
;

CRYPTOGRAPHIC_ALGORITHM
:
   'cryptographic-algorithm'
;

CRL
:
   'crl'
;

CS1
:
   'cs1'
;

CS2
:
   'cs2'
;

CS3
:
   'cs3'
;

CS4
:
   'cs4'
;

CS5
:
   'cs5'
;

CS6
:
   'cs6'
;

CS7
:
   'cs7'
;

CSD
:
   'csd'
;

CSNP_INTERVAL
:
   'csnp-interval'
;

CTIQBE
:
   'ctiqbe'
;

CTL_FILE
:
   'ctl-file'
;

CTS
:
   'cts'
;

CUSTOMER_ID
:
   'customer-id'
;

CWR
:
   'cwr'
;

DAEMON
:
   'daemon'
;

DAMPEN
:
   'dampen'
;

DAMPEN_IGP_METRIC
:
   'dampen-igp-metric'
;

DAMPENING
:
   'dampening'
;

DATABITS
:
   'databits'
;

DAYTIME
:
   'daytime'
;

DBL
:
   'dbl'
;

DCB
:
   'dcb'
;

DCB_BUFFER_THRESHOLD
:
   'dcb-buffer-threshold'
;

DCB_POLICY
:
   'dcb-policy'
;

DCBX
:
   'dcbx'
;

DEAD_INTERVAL
:
   'dead-interval'
;

DEADTIME
:
   'deadtime'
;

DEBUG
:
   'debug'
;

DEBUG_TRACE
:
   'debug-trace'
;

DEBUGGING
:
   'debugging'
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

DEFAULT_GW
:
   'default-gw'
;

DEFAULT_INFORMATION
:
   'default-information'
;

DEFAULT_INFORMATION_ORIGINATE
:
   'default-information-originate'
;

DEFAULT_INSPECTION_TRAFFIC
:
   'default-inspection-traffic'
;

DEFAULT_MAX_FRAME_SIZE
:
   'default-max-frame-size'
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

DEFAULT_ROLE
:
   'default-role'
;

DEFAULT_ROUTER
:
   'default-router'
;

DEFAULT_TASKGROUP
:
   'default-taskgroup'
;

DEFAULT_VALUE
:
   'default-value'
;

DEFINITION
:
   'definition'
;

DEL
:
   'Del'
;

DELAY
:
   'delay'
;

DELAY_START
:
   'delay-start'
;

DELETE
:
   'delete'
;

DELETE_DYNAMIC_LEARN
:
   'delete-dynamic-learn'
;

DEMAND_CIRCUIT
:
   'demand-circuit'
;

DENY
:
   'deny'
;

DEPLOY
:
   'deploy'
;

DES
:
   'des' -> pushMode ( M_DES )
;

DES_SHA1
:
   'des-sha1'
;

DESCRIPTION
:
   'description' -> pushMode ( M_Description )
;

DESIRABLE
:
   'desirable'
;

DESTINATION
:
   'destination'
;

DESTINATION_PATTERN
:
   'destination-pattern'
;

DESTINATION_PROFILE
:
   'destination-profile'
;

DESTINATION_SLOT
:
   'destination-slot'
;

DESTINATION_UNREACHABLE
:
   'destination-unreachable'
;

DESTINATION_VRF
:
   'destination-vrf'
;

DETAIL
:
   'detail'
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

DISABLE_PEER_AS_CHECK
:
   'disable-peer-as-check'
;

DISCRIMINATOR
:
   'discriminator'
;

DISPUTE
:
   'dispute'
;

DF
:
   'df'
;

DFA_REGEX
:
   'dfa-regex'
;

DFS
:
   'dfs'
;

DHCP
:
   'dhcp'
;

DHCPD
:
   'dhcpd'
;

DHCPRELAY
:
   'dhcprelay'
;

DIAGNOSTIC
:
   'diagnostic'
;

DIAL_PEER
:
   'dial-peer'
;

DIALER
:
   'dialer'
;

DIALER_GROUP
:
   'dialer-group'
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

DISABLE_ADVERTISEMENT
:
   'disable-advertisement'
;

DISCARD
:
   'discard'
;

DISCARD_ROUTE
:
   'discard-route'
;

DISCOVERY
:
   'discovery'
;

DISTANCE
:
   'distance'
;

DISTRIBUTE
:
   'distribute'
;

DISTRIBUTE_LIST
:
   'distribute-list'
;

DISTRIBUTION
:
   'distribution'
;

DM_FALLBACK
:
   'dm-fallback'
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

DNSIX
:
   'dnsix'
;

DO
:
   'do'
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

DONE
:
   'done'
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
   'dot1' [Qq]
;

DOT1Q_TUNNEL
:
   'dot1q-tunnel'
;

DOT1X
:
   'dot1x'
;

DOT1X_ENABLE
:
   'dot1x-enable'
;

DOWNLINK
:
   'downlink'
;

DR_PRIORITY
:
   'dr-priority'
;

DROP
:
   'drop'
;

DS0_GROUP
:
   'ds0-group'
;

DSCP
:
   'dscp'
;

DSCP_VALUE
:
   'dscp-value'
;

DSL
:
   'dsl'
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

DTMF_RELAY
:
   'dtmf-relay'
;

DUAL_ACTIVE
:
   'dual-active'
;

DUAL_MODE_DEFAULT_VLAN
:
   'dual-mode-default-vlan'
;

DUPLEX
:
   'duplex'
;

DUPLICATE_MESSAGE
:
   'duplicate-message'
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

DYNAMIC_CAPABILITY
:
   'dynamic-capability'
;

DYNAMIC_MAP
:
   'dynamic-map'
;

EBGP
:
   'ebgp'
;

EBGP_MULTIHOP
:
   'ebgp-multihop'
;

ECE
:
   'ece'
;

ECHO
:
   'echo'
;

ECHO_CANCEL
:
   'echo-cancel'
;

ECHO_REPLY
:
   'echo-reply'
;

ECHO_REQUEST
:
   'echo-request'
;

ECMP
:
   'ecmp'
;

ECMP_GROUP
:
   'ecmp-group'
;

ECN
:
   'ecn'
;

EF
:
   'ef'
;

EGP
:
   'egp'
;

EGRESS
:
   'egress'
;

EGRESS_INTERFACE_SELECTION
:
   'egress-interface-selection'
;

EIGRP
:
   'eigrp'
;

ELSE
:
   'else'
;

ELSEIF
:
   'elseif'
;

EMAIL
:
   'email'
;

EMAIL_ADDR
:
   'email-addr' -> pushMode ( M_Description )
;

EMAIL_CONTACT
:
   'email-contact' -> pushMode ( M_Description )
;

EMERGENCIES
:
   'emergencies'
;

EMPTY
:
   'empty'
;

ENABLE
:
   'enable'
;

ENABLE_ACL_CAM_SHARING
:
   'enable-acl-cam-sharing'
;

ENABLE_ACL_COUNTER
:
   'enable-acl-counter'
;

ENABLE_QOS_STATISTICS
:
   'enable-qos-statistics'
;

ENABLED
:
   'enabled'
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

ENDIF
:
   'endif'
;

END_CLASS_MAP
:
   'end-class-map'
;

END_POLICY
:
   'end-policy'
;

END_POLICY_MAP
:
   'end-policy-map'
;

END_SET
:
   'end-set'
   { inCommunitySet = false; }

;

ENFORCE_FIRST_AS
:
   'enforce-first-as'
;

ENGINEID
:
   (
      'engineid'
      | 'engineID'
   ) -> pushMode ( M_COMMENT )
;

ENROLLMENT
:
   'enrollment'
;

ENVIRONMENT
:
   'environment'
;

ENVIRONMENT_MONITOR
:
   'environment-monitor'
;

EOF_LITERAL
:
   'EOF'
;

EOU
:
   'eou'
;

EQ
:
   'eq'
;

ERRDISABLE
:
   'errdisable'
;

ERROR
:
   'error'
;

ERROR_ENABLE
:
   'error-enable'
;

ERROR_RECOVERY
:
   'error-recovery'
;

ERRORS
:
   'errors'
;

ERSPAN_ID
:
   'erspan-id'
;

ESCAPE_CHARACTER
:
   'escape-character'
;

ESM
:
   'esm'
;

ESP
:
   'esp'
;

ESTABLISHED
:
   'established'
;

ETHERCHANNEL
:
   'etherchannel'
;

ETHERNET
:
   'ethernet'
;

ETHERNET_SERVICES
:
   'ethernet-services'
;

ETYPE
:
   'etype'
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

EVENT_HISTORY
:
   'event-history'
;

EVENTS
:
   'events'
;

EXACT
:
   'exact'
;

EXCEED_ACTION
:
   'exceed-action'
;

EXCEPT
:
   'except'
;

EXCEPTION
:
   'exception'
;

EXCEPTION_SLAVE
:
   'exception-slave'
;

EXCLUDE
:
   'exclude'
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
   'execute' -> pushMode ( M_Execute )
;

EXIT
:
   'exit'
;

EXIT_ADDRESS_FAMILY
:
   'exit-address-family'
;

EXIT_PEER_POLICY
:
   'exit-peer-policy'
;

EXIT_PEER_SESSION
:
   'exit-peer-session'
;

EXIT_VRF
:
   'exit-vrf'
;

EXPECT
:
   'expect'
;

EXPLICIT_NULL
:
   'explicit-null'
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

EXPORTER_MAP
:
   'exporter-map'
;

EXPANDED
:
   'expanded'
;

EXTCOMM_LIST
:
   'extcomm-list'
;

EXTCOMMUNITY
:
   'extcommunity' -> pushMode ( M_Extcommunity )
;

EXTCOMMUNITY_LIST
:
   'extcommunity-list'
;

EXTEND
:
   'extend'
;

EXTENDED
:
   'extended'
   { enableDEC = true; enableACL_NUM = false; }

;

EXTENDED_COUNTERS
:
   'extended-counters'
;

EXTENDED_DELAY
:
   'extended-delay'
;

EXTERNAL
:
   'external'
;

FABRIC
:
   'fabric'
;

FABRIC_MODE
:
   'fabric-mode'
;

FABRICPATH
:
   'fabricpath'
;

FACILITY
:
   'facility'
;

FACILITY_ALARM
:
   'facility-alarm'
;

FAIL_MESSAGE
:
   'fail-message'
;

FAILED
:
   'failed'
;

FAILOVER
:
   'failover'
;

FAILURE
:
   'failure'
;

FAIL_OVER
:
   'fail-over'
;

FAIR_QUEUE
:
   'fair-queue'
;

FALL_OVER
:
   'fall-over'
;

FALLBACK
:
   'fallback'
;

FALLBACK_DN
:
   'fallback-dn'
;

FAST_DETECT
:
   'fast-detect'
;

FAST_EXTERNAL_FALLOVER
:
   'fast-external-fallover'
;

FAST_FLOOD
:
   'fast-flood'
;

FAST_REROUTE
:
   'fast-reroute'
;

FAX
:
   'fax'
;

FCOE
:
   'fcoe'
;

FEATURE
:
   'feature'
;

FEATURE_SET
:
   'feature-set'
;

FEX
:
   'fex'
;

FEX_FABRIC
:
   'fex-fabric'
;

FIELDS
:
   'fields'
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

FILE_SIZE
:
   'file-size'
;

FILE_TRANSFER
:
   'file-transfer'
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

FIN
:
   'fin'
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

FLOW_AGGREGATION
:
   'flow-aggregation'
;

FLOW_CAPTURE
:
   'flow-capture'
;

FLOW_CACHE
:
   'flow-cache'
;

FLOW_CONTROL
:
   'flow-control'
;

FLOW_EXPORT
:
   'flow-export'
;

FLOW_SAMPLING_MODE
:
   'flow-sampling-mode'
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

FORCE
:
   'force'
;

FOR
:
   'for'
;

FORMAT
:
   'format'
;

FORWARD_DIGITS
:
   'forward-digits'
;

FORWARD_PROTOCOL
:
   'forward-protocol'
;

FORWARDER
:
   'forwarder'
;

FORWARDING
:
   'forwarding'
;

FPD
:
   'fpd'
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

FRI
:
   'Fri'
;

FROM
:
   'from'
;

FT
:
   'ft'
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

FULL_TXT
:
   'full-txt'
;

G709
:
   'g709'
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

GID
:
   'gid'
;

GIG_DEFAULT
:
   'gig-default'
;

GLBP
:
   'glbp'
;

GLOBALENFORCEPRIV
:
   'globalEnforcePriv'
;

GLOBAL_MTU
:
   'global-mtu'
;

GLOBAL_PORT_SECURITY
:
   'global-port-security'
;

GOPHER
:
   'gopher'
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

GREEN
:
   'green'
;

GROUP
:
   'group'
;

GROUP_ALIAS
:
   'group-alias'
;

GROUP_LIST
:
   'group-list'
;

GROUP_LOCK
:
   'group-lock'
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

GSHUT
:
   'GSHUT'
;

GT
:
   'gt'
;

GW_TYPE_PREFIX
:
   'gw-type-prefix'
;

GUEST_MODE
:
   'guest-mode'
;

H225
:
   'h225'
;

H323
:
   'h323'
;

H323_GATEWAY
:
   'h323-gateway'
;

HA_POLICY
:
   'ha-policy'
;

HALF_DUPLEX
:
   'half-duplex'
;

HARDWARE
:
   'hardware'
;

HARDWARE_ADDRESS
:
   'hardware-address'
;

HARDWARE_COUNT
:
   'hardware-count'
;

HASH
:
   'hash'
;

HASH_ALGORITHM
:
   'hash-algorithm'
;

HEARTBEAT_INTERVAL
:
   'heartbeat-interval'
;

HEARTBEAT_TIME
:
   'heartbeat-time'
;

HELLO
:
   'hello'
;

HELLO_INTERVAL
:
   'hello-interval'
;

HELLO_MULTIPLIER
:
   'hello-multiplier'
;

HELLO_PADDING
:
   'hello-padding'
;

HELLO_PASSWORD
:
   'hello-password'
;

HELPER_ADDRESS
:
   'helper-address'
;

HIDDEN_LITERAL
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

HIGH_RESOLUTION
:
   'high-resolution'
;

HISTORY
:
   'history'
;

HOLD_QUEUE
:
   'hold-queue'
;

HOMEDIR
:
   'homedir'
;

HOP_LIMIT
:
   'hop-limit'
;

HOPLIMIT
:
   'hoplimit'
;

HOPS_OF_STATISTICS_KEPT
:
   'hops-of-statistics-kept'
;

HOST
:
   'host'
;

HOST_ASSOCIATION
:
   'host-association'
;

HOST_INFO
:
   'host-info'
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

HPM
:
   'hpm'
;

HSRP
:
   'hsrp'
;

HTTP
:
   'http'
;

HTTP_COMMANDS
:
   'http-commands'
;

HTTPS
:
   'https'
;

HW_MODULE
:
   'hw-module'
;

HW_SWITCH
:
   'hw-switch'
;

ICMP
:
   'icmp'
;

ICMP_ECHO
:
   'icmp-echo'
;

ICMP_ERRORS
:
   'icmp-errors'
;

ICMP_OBJECT
:
   'icmp-object'
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

ID
:
   'id'
;

ID_MISMATCH
:
   'id-mismatch'
;

ID_RANDOMIZATION
:
   'id-randomization'
;

IDENT
:
   'ident'
;

IDENTITY
:
   'identity'
;

IDLE
:
   'idle'
;

IDLE_TIMEOUT
:
   'idle-timeout'
;

IF
:
   'if'
;

IFACL
:
   'ifacl'
;

IF_NEEDED
:
   'if-needed'
;

IFINDEX
:
   'ifindex'
;

IGMP
:
   'igmp'
;

IGP_COST
:
   'igp-cost'
;

IGRP
:
   'igrp'
;

IGNORE
:
   'ignore'
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

IMAP4
:
   'imap4'
;

IMPLICIT_USER
:
   'implicit-user'
;

IMPORT
:
   'import'
;

IN
:
   'in'
;

INACTIVITY_TIMER
:
   'inactivity-timer'
;

INBAND
:
   'inband'
;

INBOUND
:
   'inbound'
;

INCLUDE
:
   'include'
;

INCOMING
:
   'incoming'
;

INCOMPLETE
:
   'incomplete'
;

INFINITY
:
   'infinity'
;

INFORMATION_REPLY
:
   'information-reply'
;

INFORMATION_REQUEST
:
   'information-request'
;

INFORMATIONAL
:
   'informational'
;

INFORMS
:
   'informs'
;

INGRESS
:
   'ingress'
;

INHERIT
:
   'inherit'
;

INHERITANCE_DISABLE
:
   'inheritance-disable'
;

INPUT
:
   'input'
;

INSERVICE
:
   'inservice'
;

INSIDE
:
   'inside'
;

INSPECT
:
   'inspect'
;

INSTALL
:
   'install'
;

INSTANCE
:
   'instance'
;

INTEGRITY
:
   'integrity'
;

INTERAREA
:
   'interarea'
;

INTERFACE
:
   'interface'
   { enableIPV6_ADDRESS = false; }

   -> pushMode ( M_Interface )
;

INTERNAL
:
   'internal'
;

INTERNET
:
   'internet'
;

INTERVAL
:
   'interval'
;

INTERWORKING
:
   'interworking'
;

INVALID_SPI_RECOVERY
:
   'invalid-spi-recovery'
;

INVALID_USERNAME_LOG
:
   'invalid-username-log'
;

IOS_REGEX
:
   'ios-regex' -> pushMode ( M_IosRegex )
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

IPENACL
:
   'ipenacl'
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

IPSLA
:
   'ipsla'
;

IPV4
:
   [iI] [pP] [vV] '4'
;

IPV4_L5
:
   'ipv4-l5'
;

IPV6
:
   [iI] [pP] [vV] '6'
;

IPV6_ADDRESS_POOL
:
   'ipv6-address-pool'
;

IPX
:
   'ipx'
;

IRC
:
   'irc'
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

IS
:
   'is'
;

IS_TYPE
:
   'is-type'
;

ISIS
:
   'isis'
;

ISIS_METRIC
:
   'isis-metric'
;

ISL
:
   'isl'
;

ISPF
:
   'ispf'
;

ISSUER_NAME
:
   'issuer-name'
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

KERBEROS
:
   'kerberos'
;

KEY
:
   'key'
;

KEY_EXCHANGE
:
   'key-exchange'
;

KEY_HASH
:
   'key-hash'
;

KEY_SOURCE
:
   'key-source'
;

KEY_STRING
:
   'key-string'
;

KEYPAIR
:
   'keypair'
;

KEYPATH
:
   'keypath'
;

KEYRING
:
   'keyring'
;

KEYSTORE
:
   'keystore'
;

KLOGIN
:
   'klogin'
;

KRON
:
   'kron'
;

KSHELL
:
   'kshell'
;

L2
:
   'l2'
;

L2_FILTER
:
   'l2-filter'
;

L2_SRC
:
   'l2-src'
;

L2PROTOCOL
:
   'l2protocol'
;

L2PROTOCOL_TUNNEL
:
   'l2protocol-tunnel'
;

L2TP
:
   'l2tp'
;

L2TP_CLASS
:
   'l2tp-class'
;

L2TRANSPORT
:
   'l2transport'
;

L2VPN
:
   'l2vpn'
;

LABEL
:
   'label'
;

LABELED_UNICAST
:
   'labeled-unicast'
;

LACP
:
   'lacp'
;

LACP_TIMEOUT
:
   'lacp-timeout'
;

LAG
:
   'lag'
;

LAN
:
   'lan'
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

LDAP
:
   'ldap'
;

LDAPS
:
   'ldaps'
;

LDP
:
   'ldp'
;

LE
:
   'le'
;

LEASE
:
   'lease'
;

LEVEL
:
   'level'
;

LEVEL_1
:
   'level-1'
;

LEVEL_1_2
:
   'level-1-2'
;

LEVEL_2
:
   'level-2'
;

LEVEL_2_ONLY
:
   'level-2-only'
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

LIFE
:
   'life'
;

LIFETIME
:
   'lifetime'
;

LIMIT
:
   'limit'
;

LIMIT_RESOURCE
:
   'limit-resource'
;

LINE
:
   'line'
;

LINECARD
:
   'linecard'
;

LINECODE
:
   'linecode'
;

LINK
:
   'link'
;

LINK_FAULT_SIGNALING
:
   'link-fault-signaling'
;

LIST
:
   'list'
;

LISTEN
:
   'listen'
;

LLDP
:
   'lldp'
;

LOAD_BALANCE
:
   'load-balance'
;

LOAD_BALANCING
:
   'load-balancing'
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

LOCAL_ASA
:
   'LOCAL'
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

LOCAL_V6_ADDR
:
   'local-v6-addr'
;

LOCATION
:
   'location' -> pushMode ( M_COMMENT )
;

LOCALE
:
   'locale'
;

LOG
:
   'log'
;

LOG_ADJACENCY_CHANGES
:
   'log-adjacency-changes'
;

LOG_ENABLE
:
   'log-enable'
;

LOG_INPUT
:
   'log-input'
;

LOG_INTERNAL_SYNC
:
   'log-internal-sync'
;

LOG_NEIGHBOR_CHANGES
:
   'log-neighbor-changes'
;

LOGFILE
:
   'logfile'
;

LOGGING
:
   'logging'
;

LOGIN
:
   'login'
;

LOGIN_ATTEMPTS
:
   'login-attempts'
;

LOGOUT_WARNING
:
   'logout-warning'
;

LOOPBACK
:
   'loopback'
;

LOOPGUARD
:
   'loopguard'
;

LOTUSNOTES
:
   'lotusnotes'
;

LPD
:
   'lpd'
;

LPTS
:
   'lpts'
;

LRE
:
   'lre'
;

LRQ
:
   'lrq'
;

LSP_GEN_INTERVAL
:
   'lsp-gen-interval'
;

LSP_INTERVAL
:
   'lsp-interval'
;

LSP_PASSWORD
:
   'lsp-password'
;

LSP_REFRESH_INTERVAL
:
   'lsp-refresh-interval'
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
   'mac-address' -> pushMode ( M_COMMENT )
;

MAC_ADDRESS_TABLE
:
   'mac-address-table'
;

MAC_LEARN
:
   'mac-learn'
;

MAC_MOVE
:
   'mac-move'
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

MANAGEMENT_ACCESS
:
   'management-access'
;

MANAGEMENT_ONLY
:
   'management-only'
;

MANAGEMENT_PLANE
:
   'management-plane'
;

MANAGER
:
   'manager'
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

MAPPING
:
   'mapping'
;

MASK
:
   'mask'
;

MASK_REPLY
:
   'mask-reply'
;

MASK_REQUEST
:
   'mask-request'
;

MASTER
:
   'master'
;

MATCH
:
   'match'
;

MATCH_ALL
:
   'match-all'
;

MATCH_ANY
:
   'match-any'
;

MATCHES_ANY
:
   'matches-any'
;

MATCHES_EVERY
:
   'matches-every'
;

MAX_ASSOCIATIONS
:
   'max-associations'
;

MAX_IFINDEX_PER_MODULE
:
   'max-ifindex-per-module'
;

MAX_LSA
:
   'max-lsa'
;

MAX_LSP_LIFETIME
:
   'max-lsp-lifetime'
;

MAX_METRIC
:
   'max-metric'
;

MAX_ROUTE
:
   'max-route'
;

MAXIMUM
:
   'maximum'
;

MAXIMUM_ACCEPTED_ROUTES
:
   'maximum-accepted-routes'
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

MBSSID
:
   'mbssid'
;

MCAST_BOUNDARY
:
   'mcast-boundary'
;

MDIX
:
   'mdix'
;

MDT
:
   'mdt'
;

MED
:
   'med'
;

MEDIUM
:
   'medium'
;

MEDIA
:
   'media'
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

MEMORY
:
   'memory'
;

MEMORY_SIZE
:
   'memory-size'
;

MENU
:
   'menu'
;

MESH_GROUP
:
   'mesh-group'
;

MESSAGE_COUNTER
:
   'message-counter'
;

MESSAGE_DIGEST
:
   'message-digest'
;

MESSAGE_DIGEST_KEY
:
   'message-digest-key'
;

MESSAGE_LENGTH
:
   'message-length'
;

MESSAGE_LEVEL
:
   'message-level'
;

MESSAGE_SIZE
:
   'message-size'
;

METRIC
:
   'metric'
;

METRIC_STYLE
:
   'metric-style'
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

MFWD
:
   'mfwd'
;

MGCP
:
   'mgcp'
;

MIB
:
   'mib'
;

MICROCODE
:
   'microcode'
;

MINIMAL
:
   'minimal'
;

MINIMUM_INTERVAL
:
   'minimum-interval'
;

MINIMUM_LINKS
:
   'minimum-links'
;

MINPOLL
:
   'minpoll'
;

MIRROR
:
   'mirror'
;

MLAG
:
   'mlag'
;

MLD
:
   'mld'
;

MLD_QUERY
:
   'mld-query'
;

MLD_REDUCTION
:
   'mld-reduction'
;

MLD_REPORT
:
   'mld-report'
;

MLDV2
:
   'mldv2'
;

MLS
:
   'mls'
;

MOBILE_IP
:
   'mobile-ip'
;

MOBILE_REDIRECT
:
   'mobile-redirect'
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

MON
:
   'Mon'
;

MONITOR
:
   'monitor'
;

MONITOR_INTERFACE
:
   'monitor-interface'
;

MONITOR_MAP
:
   'monitor-map'
;

MONITOR_SESSION
:
   'monitor-session'
;

MONITORING
:
   'monitoring'
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

MSDP_PEER
:
   'msdp-peer'
;

MSCHAP
:
   'mschap'
;

MSCHAPV2
:
   'mschapv2'
;

MSIE_PROXY
:
   'msie-proxy'
;

MSRPC
:
   'msrpc'
;

MST
:
   'mst'
;

MTA
:
   'mta'
;

MTU
:
   'mtu'
;

MTU_IGNORE
:
   'mtu-ignore'
;

MULTICAST
:
   'multicast'
;

MULTICAST_BOUNDARY
:
   'multicast-boundary'
;

MULTICAST_ROUTING
:
   'multicast-routing'
;

MULTILINK
:
   'multilink'
;

MULTIPATH
:
   'multipath'
;

MULTIPATH_RELAX
:
   'multipath-relax'
;

MULTIPLIER
:
   'multiplier'
;

MULTIPOINT
:
   'multipoint'
;

MULTI_CONFIG
:
   'multi-config'
;

MULTI_TOPOLOGY
:
   'multi-topology'
;

MVR
:
   'mvr'
;

NAME
:
   'name' -> pushMode ( M_Name )
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

NAMESPACE
:
   'namespace'
;

NAMES
:
   'names'
;

NAMESERVER
:
   'nameserver'
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

NATPOOL
:
   'natpool'
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

NEGOTIATED
:
   'negotiated'
;

NEGOTIATION
:
   'negotiation'
;

NEIGHBOR
:
   'neighbor' -> pushMode ( M_NEIGHBOR )
;

NEIGHBOR_DOWN
:
   'neighbor-down'
;

NEIGHBOR_FILTER
:
   'neighbor-filter'
;

NEIGHBOR_GROUP
:
   'neighbor-group'
;

NEIGHBOR_IS
:
   'neighbor-is'
;

NEQ
:
   'neq'
;

NESTED
:
   'nested'
;

NET
:
   'net' -> pushMode ( M_ISO_Address )
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

NETBIOS_SSN
:
   'netbios-ssn'
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

NEW_MODEL
:
   'new-model'
;

NEWINFO
:
   'newinfo'
;

NEXT_HOP
:
   'next-hop'
;

NEXT_HOP_SELF
:
   'next-hop-self'
;

NEXT_HOP_THIRD_PARTY
:
   'next-hop-third-party'
;

NEXT_SERVER
:
   'next-server'
;

NEXTHOP
:
   'nexthop'
;

NEXTHOP1
:
   'nexthop1'
;

NEXTHOP2
:
   'nexthop2'
;

NEXTHOP_ATTRIBUTE
:
   'nexthop-attribute'
;

NHOP_ONLY
:
   'nhop-only'
;

NHRP
:
   'nhrp'
;

NLRI
:
   'nlri'
;

NLS
:
   'nls'
;

NMSP
:
   'nmsp'
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

NO_L4R_SHIM
:
   'No l4r_shim'
;

NO_PREPEND
:
   'no-prepend'
;

NO_REDISTRIBUTION
:
   'no-redistribution'
;

NO_SUMMARY
:
   'no-summary'
;

NOAUTH
:
   'noauth'
;

NODE
:
   'node'
;

NON500_ISAKMP
:
   'non500-isakmp'
;

NON_BROADCAST
:
   'non-broadcast'
;

NON_CLIENT_NRT
:
   'non-client-nrt'
;

NON_DETERMINISTIC_MED
:
   'non-deterministic-med'
;

NONE
:
   'none'
;

NONEGOTIATE
:
   'nonegotiate'
;

NOS
:
   'nos'
;

NOT
:
   'not'
;

NOT_ADVERTISE
:
   'not-advertise'
;

NOTIFICATION
:
   'notification'
;

NOTIFICATION_TIMER
:
   'notification-timer'
;

NOTIFICATIONS
:
   'notifications'
;

NOTIFY
:
   'notify'
;

NSF
:
   'nsf'
;

NSR
:
   'nsr'
;

NSSA
:
   'nssa'
;

NSSA_EXTERNAL
:
   'nssa-external'
;

NTP
:
   'ntp'
;

NULL
:
   'null'
;

NV
:
   'nv'
;

OAM
:
   'oam'
;

OBJECT
:
   'object'
;

OBJECT_GROUP
:
   'object-group'
;

ON
:
   'on'
;

ONE
:
   'one'
;

ONEP
:
   'onep'
;

OPEN
:
   'open'
;

OPENFLOW
:
   'openflow'
;

OPERATION
:
   'operation'
;

OPS
:
   'ops'
;

OPTICAL_MONITOR
:
   'optical-monitor'
;

OPTIMIZE
:
   'optimize'
;

OPTIMIZED
:
   'optimized'
;

OPTION
:
   'option'
;

OPTIONS
:
   'options'
;

OR
:
   'or'
;

ORIGIN
:
   'origin'
;

ORIGINATE
:
   'originate'
;

ORIGINATES_FROM
:
   'originates-from'
;

ORIGINATOR_ID
:
   'originator-id'
;

OSPF
:
   'ospf'
;

OSPF_EXTERNAL_TYPE_1
:
   'ospf-external-type-1'
;

OSPF_EXTERNAL_TYPE_2
:
   'ospf-external-type-2'
;

OSPF_INTER_AREA
:
   'ospf-inter-area'
;

OSPF_INTRA_AREA
:
   'ospf-intra-area'
;

OSPF_NSSA_TYPE_1
:
   'ospf-nssa-type-1'
;

OSPF_NSSA_TYPE_2
:
   'ospf-nssa-type-2'
;

OSPFV3
:
   'ospfv3'
;

OTHER_ACCESS
:
   'other-access'
;

OUI
:
   'oui' -> pushMode ( M_COMMENT )
;

OUT
:
   'out'
;

OUT_OF_BAND
:
   'out-of-band'
;

OUTPUT
:
   'output'
;

OUTSIDE
:
   'outside'
;

OVERLOAD
:
   'overload'
;

OVERLOAD_CONTROL
:
   'overload-control'
;

OVERRIDE
:
   'override'
;

OWNER
:
   'owner'
;

P2P
:
   'p2p'
;

PACKET
:
   'packet'
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

PARITY
:
   'parity'
;

PARSER
:
   'parser'
;

PARTICIPATE
:
   'participate'
;

PASS
:
   'pass'
;

PASSES_THROUGH
:
   'passes-through'
;

PASSIVE
:
   'passive'
;

PASSIVE_INTERFACE
:
   'passive-interface' -> pushMode ( M_Interface )
;

PASSIVE_ONLY
:
   'passive-only'
;

PASSWORD
:
   'password' -> pushMode ( M_COMMENT )
;

PASSWORD_POLICY
:
   'password-policy'
;

PASSWORD_PROMPT
:
   'password-prompt'
;

PASSWORD_STORAGE
:
   'password-storage'
;

PATH_ECHO
:
   'path-echo'
;

PATH_JITTER
:
   'path-jitter'
;

PATH_OPTION
:
   'path-option'
;

PATH_RETRANSMIT
:
   'path-retransmit'
;

PATHCOST
:
   'pathcost'
;

PATHS
:
   'paths'
;

PATHS_OF_STATISTICS_KEPT
:
   'paths-of-statistics-kept'
;

PAUSE
:
   'pause'
;

PCANYWHERE_DATA
:
   'pcanywhere-data'
;

PCANYWHERE_STATUS
:
   'pcanywhere-status'
;

PCP
:
   'pcp'
;

PCP_VALUE
:
   'pcp-value'
;

PEAKDETECT
:
   'peakdetect'
;

PEER
:
   'peer'
;

PEER_ADDRESS
:
   'peer-address'
;

PEER_CONFIG_CHECK_BYPASS
:
   'peer-config-check-bypass'
;

PEER_GROUP
:
   'peer-group' -> pushMode ( M_NEIGHBOR )
;

PEER_GATEWAY
:
   'peer-gateway'
;

PEER_ID_VALIDATE
:
   'peer-id-validate'
;

PEER_KEEPALIVE
:
   'peer-keepalive'
;

PEER_LINK
:
   'peer-link'
;

PEER_POLICY
:
   'peer-policy'
;

PEER_SESSION
:
   'peer-session'
;

PEER_SWITCH
:
   'peer-switch'
;

PERIODIC
:
   'periodic'
;

PERIODIC_INVENTORY
:
   'periodic-inventory'
;

PERMANENT
:
   'permanent'
;

PERMIT
:
   'permit'
;

PERMIT_HOSTDOWN
:
   'permit-hostdown'
;

PERSISTENT
:
   'persistent'
;

PFC
:
   'pfc'
;

PHONE_CONTACT
:
   'phone-contact' -> pushMode ( M_Description )
;

PHONE_NUMBER
:
   'phone-number'
;

PHONE_PROXY
:
   'phone-proxy'
;

PHYSICAL_LAYER
:
   'physical-layer'
;

PHYSICAL_PORT
:
   'physical-port'
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

PIM_SPARSE
:
   'pim-sparse'
;

PINNING
:
   'pinning'
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

PM
:
   'pm'
;

POAP
:
   'poap'
;

POINT_TO_MULTIPOINT
:
   'point-to-multipoint'
;

POINT_TO_POINT
:
   'point-to-point'
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

POLICY_MAP_INPUT
:
   'policy-map-input'
;

POLICY_MAP_OUTPUT
:
   'policy-map-output'
;

POOL
:
   'pool'
;

POP2
:
   'pop2'
;

POP3
:
   'pop3'
;

PORT
:
   'port'
;

PORTFAST
:
   'portfast'
;

PORTS
:
   'ports'
;

PORT_CHANNEL
:
   'port-channel'
;

PORT_CHANNEL_PROTOCOL
:
   'port-channel-protocol'
;

PORT_NAME
:
   'port-name'
;

PORT_OBJECT
:
   'port-object'
;

PORT_PROFILE
:
   'port-profile'
;

PORT_SECURITY
:
   'port-security'
;

PORT_TYPE
:
   'port-type'
;

PORT_UNREACHABLE
:
   'port-unreachable'
;

PORTMODE
:
   'portmode'
;

POS
:
   'pos'
;

POWER
:
   'power'
;

POWEROFF
:
   'poweroff'
;

POWER_MGR
:
   'power-mgr'
;

PPP
:
   'ppp'
;

PPTP
:
   'pptp'
;

PRC_INTERVAL
:
   'prc-interval'
;

PRE_SHARED_KEY
:
   'pre-shared-key'
;

PRECEDENCE
:
   'precedence'
;

PRECONFIGURE
:
   'preconfigure'
;

PREDICTOR
:
   'predictor'
;

PREEMPT
:
   'preempt'
;

PREFER
:
   'prefer'
;

PREFERRED
:
   'preferred'
;

PREFERRED_PATH
:
   'preferred-path'
;

PREFIX
:
   'prefix'
;

PREFIX_LIST
:
   'prefix-list'
;

PREFIX_SET
:
   'prefix-set'
;

PREPEND
:
   'prepend'
;

PRF
:
   'prf'
;

PRI_GROUP
:
   'pri-group'
;

PRIMARY
:
   'primary'
;

PRIMARY_PORT
:
   'primary-port'
;

PRIMARY_PRIORITY
:
   'primary-priority'
;

PRIORITY
:
   'priority'
;

PRIORITY_FLOW_CONTROL
:
   'priority-flow-control'
;

PRIORITY_FORCE
:
   'priority-force'
;

PRIORITY_MAPPING
:
   'priority-mapping'
;

PRIORITY_QUEUE
:
   'priority-queue'
;

PRIV
:
   'priv'
;

PRIVATE_AS
:
   'private-as'
;

PRIVATE_VLAN
:
   'private-vlan'
;

PRIVILEGE
:
   'privilege'
;

PRIVILEGE_MODE
:
   'privilege-mode'
;

PROACTIVE
:
   'proactive'
;

PROBE
:
   'probe'
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

PROPAGATE
:
   'propagate'
;

PROPOSAL
:
   'proposal'
;

PROTOCOL
:
   'protocol'
;

PROTOCOL_OBJECT
:
   'protocol-object'
;

PROTOCOL_VIOLATION
:
   'protocol-violation'
;

PROVISION
:
   'provision'
;

PROXY_ARP
:
   'proxy-arp'
;

PROXY_SERVER
:
   'proxy-server'
;

PSEUDOWIRE
:
   'pseudowire'
;

PSEUDOWIRE_CLASS
:
   'pseudowire-class'
;

PSH
:
   'psh'
;

PTP
:
   'ptp'
;

PUBKEY_CHAIN
:
   'pubkey-chain'
;

PVC
:
   'pvc'
;

QOS
:
   'qos'
;

QOS_GROUP
:
   'qos-group'
;

QOS_MAPPING
:
   'qos-mapping'
;

QOS_POLICY
:
   'qos-policy'
;

QOS_POLICY_OUTPUT
:
   'qos-policy-output'
;

QUERY_INTERVAL
:
   'query-interval'
;

QUERY_ONLY
:
   'query-only'
;

QUEUE
:
   'queue'
;

QUEUE_BUFFERS
:
   'queue-buffers'
;

QUEUE_LENGTH
:
   'queue-length'
;

QUEUE_LIMIT
:
   'queue-limit'
;

QUEUE_MONITOR
:
   'queue-monitor'
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

RADIUS_ACCT
:
   'radius-acct'
;

RADIUS_COMMON_PW
:
   'radius-common-pw'
;

RADIUS_SERVER
:
   'radius-server'
;

RANDOM
:
   'random'
;

RANDOM_DETECT
:
   'random-detect'
;

RANDOM_DETECT_LABEL
:
   'random-detect-label'
;

RANGE
:
   'range'
;

RATE_LIMIT
:
   'rate-limit'
;

RATE_MODE
:
   'rate-mode'
;

RBACL
:
   'rbacl'
;

RC4_SHA1
:
   'rc4-sha1'
;

RCMD
:
   'rcmd'
;

RCP
:
   'rcp'
;

RCV_QUEUE
:
   'rcv-queue'
;

RD
:
   'rd'
;

REACHABLE_VIA
:
   'reachable-via'
;

REACT
:
   'react'
;

REACTION
:
   'reaction'
;

REAL
:
   'real'
;

RECEIVE
:
   'receive'
;

RECONNECT_INTERVAL
:
   'reconnect-interval'
;

RECORD
:
   'record'
;

RECORD_ENTRY
:
   'record-entry'
;

RED
:
   'red'
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

REDISTRIBUTE_INTERNAL
:
   'redistribute-internal'
;

REDISTRIBUTED_PREFIXES
:
   'redistributed-prefixes'
;

REDUNDANCY
:
   'redundancy'
;

REFERENCE_BANDWIDTH
:
   'reference-bandwidth'
;

REFLECT
:
   'reflect'
;

REFLEXIVE_LIST
:
   'reflexive-list'
;

REGEX_MODE
:
   'regex-mode'
;

REGISTER_RATE_LIMIT
:
   'register-rate-limit'
;

REGISTER_SOURCE
:
   'register-source'
;

RELAY
:
   'relay'
;

RELOAD
:
   'reload'
;

RELOAD_DELAY
:
   'reload-delay'
;

RELOAD_TYPE
:
   'reload-type'
;

REMARK
:
   'remark' -> pushMode ( M_REMARK )
;

REMOTE
:
   'remote'
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

REMOTE_PORTS
:
   'remote-ports'
;

REMOVE_PRIVATE_AS
:
   'remove-private-as'
;

REMOVE_PRIVATE_CAP_A_CAP_S
:
   'remove-private-AS'
;

REMOTE_SERVER
:
   'remote-server'
;

REMOTE_SPAN
:
   'remote-span'
;

REMOVED
:
   '<removed>'
;

REPLACE_AS
:
   'replace-as'
;

REPLY_TO
:
   'reply-to'
;

REOPTIMIZE
:
   'reoptimize'
;

REQUEST
:
   'request'
;

REQUEST_DATA_SIZE
:
   'request-data-size'
;

RESOURCE
:
   'resource'
;

RESOURCE_POOL
:
   'resource-pool'
;

RESOURCES
:
   'resources'
;

RESPONDER
:
   'responder'
;

RETRANSMIT
:
   'retransmit'
;

RETRANSMIT_TIMEOUT
:
   'retransmit-timeout'
;

RETRIES
:
   'retries'
;

REVERSE_ACCESS
:
   'reverse-access'
;

REVERSE_PATH
:
   'reverse-path'
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

REWRITE
:
   'rewrite'
;

RFC1583COMPATIBILITY
:
   'rfc1583compatibility'
;

RIB_HAS_ROUTE
:
   'rib-has-route'
;

RIB_METRIC_AS_EXTERNAL
:
   'rib-metric-as-external'
;

RIB_METRIC_AS_INTERNAL
:
   'rib-metric-as-internal'
;

RING
:
   'ring'
;

RIP
:
   'rip'
;

RMON
:
   'rmon'
;

RO
:
   [rR] [oO]
;

ROLE
:
   'role'
;

ROOT
:
   'root'
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
   'route-map' -> pushMode ( M_RouteMap )
;

ROUTE_ONLY
:
   'route-only'
;

ROUTE_POLICY
:
   'route-policy'
;

ROUTE_REFLECTOR_CLIENT
:
   'route-reflector-client'
;

ROUTE_SOURCE
:
   'route-source'
;

ROUTE_TARGET
:
   'route-target'
;

ROUTE_TYPE
:
   'route-type'
;

ROUTED
:
   'routed'
;

ROUTER
:
   'router'
;

ROUTER_ADVERTISEMENT
:
   'router-advertisement'
;

ROUTER_ID
:
   'router-id'
;

ROUTER_INTERFACE
:
   'router-interface'
;

ROUTER_SOLICITATION
:
   'router-solicitation'
;

ROUTING
:
   'routing'
;

RP
:
   'rp'
;

RPF_VECTOR
:
   'rpf-vector'
;

RP_ADDRESS
:
   'rp-address'
;

RP_ANNOUNCE_FILTER
:
   'rp-announce-filter'
;

RP_CANDIDATE
:
   'rp-candidate'
;

RP_LIST
:
   'rp-list'
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

RSVP
:
   'rsvp'
;

RT
:
   'rt'
;

RULE
:
   'rule' -> pushMode ( M_Rule )
;

RUN
:
   'run'
;

RX_COS_SLOT
:
   'rx-cos-slot'
;

RW
:
   [Rr] [Ww]
;

RX
:
   'rx'
;

SA_FILTER
:
   'sa-filter'
;

SAME_SECURITY_TRAFFIC
:
   'same-security-traffic'
;

SAMPLER
:
   'sampler'
;

SAMPLER_MAP
:
   'sampler-map'
;

SAMPLES_OF_HISTORY_KEPT
:
   'samples-of-history-kept'
;

SAP
:
   'sap'
;

SAT
:
   'Sat'
;

SATELLITE
:
   'satellite'
;

SATELLITE_FABRIC_LINK
:
   'satellite-fabric-link'
;

SCALE_FACTOR
:
   'scale-factor'
;

SCAN_TIME
:
   'scan-time'
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

SCOPE
:
   'scope'
;

SCP
:
   'scp'
;

SCRAMBLE
:
   'scramble'
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

SDR
:
   'sdr'
;

SDROWNER
:
   'SDROwner'
;

SECONDARY
:
   'secondary'
;

SECRET
:
   'secret'
;

SECUREID_UDP
:
   'secureid-udp'
;

SECURE_MAC_ADDRESS
:
   'secure-mac-address'
;

SECURITY
:
   'security'
;

SECURITY_LEVEL
:
   'security-level'
;

SELF
:
   'self'
;

SEND
:
   'send'
;

SEND_COMMUNITY
:
   'send-community'
;

SEND_COMMUNITY_EBGP
:
   'send-community-ebgp'
;

SEND_EXTENDED_COMMUNITY_EBGP
:
   'send-extended-community-ebgp'
;

SEND_LABEL
:
   'send-label'
;

SEND_LIFETIME
:
   'send-lifetime'
;

SEND_RP_ANNOUNCE
:
   'send-rp-announce'
;

SEND_RP_DISCOVERY
:
   'send-rp-discovery'
;

SENDER
:
   'sender'
;

SENSOR
:
   'sensor'
;

SEQ
:
   'seq'
;

SEQUENCE
:
   'sequence'
;

SEQUENCE_NUMS
:
   'sequence-nums'
;

SERIAL
:
   'serial'
;

SERIAL_NUMBER
:
   'serial-number'
;

SERVE
:
   'serve'
;

SERVE_ONLY
:
   'serve-only'
;

SERVER
:
   'server'
;

SERVER_ARP
:
   'server-arp'
;

SERVERFARM
:
   'serverfarm'
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

SERVICE_CLASS
:
   'service-class'
;

SERVICE_MODULE
:
   'service-module'
;

SERVICE_OBJECT
:
   'service-object'
;

SERVICE_POLICY
:
   'service-policy'
;

SERVICE_QUEUE
:
   'service-queue'
;

SERVICE_TYPE
:
   'service-type'
;

SESSION
:
   'session'
;

SESSION_DISCONNECT_WARNING
:
   'session-disconnect-warning' -> pushMode ( M_COMMENT )
;

SESSION_GROUP
:
   'session-group'
;

SESSION_ID
:
   'session-id'
;

SESSION_LIMIT
:
   'session-limit'
;

SESSION_OPEN_MODE
:
   'session-open-mode'
;

SESSION_PROTECTION
:
   'session-protection'
;

SESSION_TIMEOUT
:
   'session-timeout'
;

SET
:
   'set'
;

SET_COLOR
:
   'set-color'
;

SET_OVERLOAD_BIT
:
   'set-overload-bit'
;

SETUP
:
   'setup'
;

SEVERITY
:
   'severity'
;

SFLOW
:
   'sflow'
;

SFTP
:
   'sftp'
;

SGBP
:
   'sgbp'
;

SHA1
:
   'sha1' -> pushMode ( M_SHA1 )
;

SHAPE
:
   'shape'
;

SHELL
:
   'shell'
;

SHORT_TXT
:
   'short-txt'
;

SHOW
:
   'show'
;

SHUT
:
   'shut'
;

SHUTDOWN
:
   'shutdown'
;

SIGNAL
:
   'signal'
;

SIGNALLED_BANDWIDTH
:
   'signalled-bandwidth'
;

SIGNALLED_NAME
:
   'signalled-name'
;

SIGNALLING
:
   'signalling'
;

SIGNING
:
   'signing'
;

SINGLE_CONNECTION
:
   'single-connection'
;

SINGLE_ROUTER_MODE
:
   'single-router-mode'
;

SINGLE_TOPOLOGY
:
   'single-topology'
;

SIP_UA
:
   'sip-ua'
;

SITE_ID
:
   'site-id'
;

SLA
:
   'sla'
;

SLOT
:
   'slot'
;

SLOT_TABLE_COS
:
   'slot-table-cos'
;

SMTP
:
   'smtp'
;

SMTP_SERVER
:
   'smtp-server'
;

SNMP_AUTHFAIL
:
   'snmp-authfail'
;

SNMP
:
   'snmp'
;

SNMP_SERVER
:
   'snmp-server'
;

SNMP_TRAP
:
   'snmp-trap'
;

SNMPTRAP
:
   'snmptrap'
;

SNOOPING
:
   'snooping'
;

SNP
:
   'snp'
;

SNTP
:
   'sntp'
;

SORT_BY
:
   'sort-by'
;

SPE
:
   'spe'
;

SPF_INTERVAL
:
   'spf-interval'
;

SOFT_PREEMPTION
:
   'soft-preemption'
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
   'source-interface' -> pushMode ( M_Interface )
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

SPAN
:
   'span'
;

SPANNING_TREE
:
   'spanning-tree'
;

SPARSE_DENSE_MODE
:
   'sparse-dense-mode'
;

SPARSE_MODE
:
   'sparse-mode'
;

SPD
:
   'spd'
;

SPEED
:
   'speed'
;

SPEED_DUPLEX
:
   'speed-duplex'
;

SPLIT_TUNNEL_NETWORK_LIST
:
   'split-tunnel-network-list'
;

SPLIT_TUNNEL_POLICY
:
   'split-tunnel-policy'
;

SPT_THRESHOLD
:
   'spt-threshold'
;

SQLNET
:
   'sqlnet'
;

SRLG
:
   'srlg'
;

SRR_QUEUE
:
   'srr-queue'
;

SSH
:
   'ssh'
;

SSH_CERTIFICATE
:
   'ssh-certificate'
;

SSH_KEYDIR
:
   'ssh_keydir'
;

SSH_PUBLICKEY
:
   'ssh-publickey'
;

SSID
:
   'ssid'
;

SSL
:
   'ssl'
;

SSM
:
   'ssm'
;

STACK_MAC
:
   'stack-mac'
;

STACK_MIB
:
   'stack-mib'
;

STACK_UNIT
:
   'stack-unit'
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

START_STOP
:
   'start-stop'
;

START_TIME
:
   'start-time'
;

STATE
:
   'state'
;

STATE_REFRESH
:
   'state-refresh'
;

STATIC
:
   'static'
;

STATION_ROLE
:
   'station-role'
;

STATISTICS
:
   'statistics'
;

STBC
:
   'stbc'
;

STCAPP
:
   'stcapp'
;

STICKY
:
   'sticky'
;

STOP
:
   'stop'
;

STOP_ONLY
:
   'stop-only'
;

STOP_RECORD
:
   'stop-record'
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

STREET_ADDRESS
:
   'street-address'
;

STREETADDRESS
:
   'streetaddress' -> pushMode ( M_Description )
;

STRICTHOSTKEYCHECK
:
   'stricthostkeycheck'
;

STRING
:
   'string'
;

STRIP
:
   'strip'
;

STS_1
:
   'sts-1'
;

STUB
:
   'stub'
;

SUBJECT_NAME
:
   'subject-name'
;

SUBNET
:
   'subnet'
;

SUBNET_MASK
:
   'subnet-mask'
;

SUBNETS
:
   'subnets'
;

SUBNET_ZERO
:
   'subnet-zero'
;

SUB_ROUTE_MAP
:
   'sub-route-map'
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

SUCCESS
:
   'success'
;

SUMMARY_ADDRESS
:
   'summary-address'
;

SUMMARY_ONLY
:
   'summary-only'
;

SUN
:
   'Sun'
;

SUNRPC
:
   'sunrpc'
;

SUPPLEMENTARY_SERVICES
:
   'supplementary-services'
;

SUPPRESS
:
   'suppress'
;

SUPPRESS_FIB_PENDING
:
   'suppress-fib-pending'
;

SVC
:
   'svc'
;

SVCLC
:
   'svclc'
;

SWITCH
:
   'switch'
;

SWITCH_PRIORITY
:
   'switch-priority'
;

SWITCH_PROFILE
:
   'switch-profile'
;

SWITCH_TYPE
:
   'switch-type'
;

SWITCHBACK
:
   'switchback'
;

SWITCHING_MODE
:
   'switching-mode'
;

SWITCHNAME
:
   'switchname'
;

SWITCHPORT
:
   'switchport'
;

SYMMETRIC
:
   'symmetric'
;

SYN
:
   'syn'
;

SYNC
:
   'sync'
;

SYNCHRONIZATION
:
   'synchronization'
;

SYNCHRONOUS
:
   'synchronous'
;

SYSLOG
:
   'syslog'
;

SYSLOGD
:
   'syslogd'
;

SYSOPT
:
   'sysopt'
;

SYSTEM
:
   'system'
;

SYSTEM_INIT
:
   'system-init'
;

SYSTEM_MAX
:
   'system-max'
;

SYSTEM_PRIORITY
:
   'system-priority'
;

SYSTEMOWNER
:
   'SystemOwner'
;

TABLE_MAP
:
   'table-map'
;

TACACS
:
   'tacacs'
;

TACACS_DS
:
   'tacacs-ds'
;

TACACS_PLUS
:
   'tacacs+'
;

TACACS_PLUS_ASA
:
   'TACACS+'
;

TACACS_SERVER
:
   'tacacs-server'
;

TAC_PLUS
:
   'tac_plus'
;

TAG
:
   'tag'
;

TAG_SWITCHING
:
   'tag-switching'
;

TAG_TYPE
:
   'tag-type'
;

TAGGED
:
   'tagged'
;

TALK
:
   'talk'
;

TAP
:
   'tap'
;

TASK
:
   'task'
;

TASK_SPACE_EXECUTE
:
   'task execute'
;

TASKGROUP
:
   'taskgroup'
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

TCP_CONNECT
:
   'tcp-connect'
;

TCP_SESSION
:
   'tcp-session'
;

TCP_UDP
:
   'tcp-udp'
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

TFTP_SERVER_LIST
:
   'tftp-server-list'
;

THEN
:
   'then'
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

THRESHOLD
:
   'threshold'
;

THU
:
   'Thu'
;

TIME
:
   'time'
;

TIME_EXCEEDED
:
   'time-exceeded'
;

TIME_RANGE
:
   'time-range'
;

TIME_OUT
:
   'time-out'
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

TIMESTAMP
:
   'timestamp'
;

TIMESTAMP_REPLY
:
   'timestamp-reply'
;

TIMESTAMP_REQUEST
:
   'timestamp-request'
;

TIMING
:
   'timing'
;

TLS_PROXY
:
   'tls-proxy'
;

TM_VOQ_COLLECTION
:
   'tm-voq-collection'
;

TOOL
:
   'tool'
;

TOP
:
   'top'
;

TOS
:
   'tos'
;

TRACE
:
   'trace'
;

TRACEROUTE
:
   'traceroute'
;

TRACK
:
   'track'
;

TRACKED
:
   'tracked'
;

TRACKING_PRIORITY_INCREMENT
:
   'tracking-priority-increment'
;

TRADITIONAL
:
   'traditional'
;

TRAFFIC_ENG
:
   'traffic-eng'
;

TRAFFIC_INDEX
:
   'traffic-index'
;

TRANSCEIVER
:
   'transceiver'
;

TRANSCEIVER_TYPE_CHECK
:
   'transceiver-type-check'
;

TRANSLATE
:
   'translate'
;

TRANSLATION
:
   'translation'
;

TRANSLATION_PROFILE
:
   'translation-profile'
;

TRANSMIT
:
   'transmit'
;

TRANSPARENT_HW_FLOODING
:
   'transparent-hw-flooding'
;

TRANSPORT
:
   'transport'
;

TRANSPORT_METHOD
:
   'transport-method'
;

TRANSPORT_MODE
:
   'transport-mode'
;

TRAP
:
   'trap'
;

TRAP_SOURCE
:
   'trap-source' -> pushMode ( M_Interface )
;

TRAPS
:
   'traps'
;

TRIGGER
:
   'trigger'
;

TRUNK
:
   'trunk'
;

TRUNK_THRESHOLD
:
   'trunk-threshold'
;

TRUST
:
   'trust'
;

TRUSTED_KEY
:
   'trusted-key'
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

TTL_THRESHOLD
:
   'ttl-threshold'
;

TTY
:
   'tty'
;

TUE
:
   'Tue'
;

TUNABLE_OPTIC
:
   'tunable-optic'
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

TUNNEL_ID
:
   'tunnel-id'
;

TX_QUEUE
:
   'tx-queue'
;

TYPE
:
   'type'
;

TYPE_1
:
   'type-1'
;

TYPE_2
:
   'type-2'
;

UC_TX_QUEUE
:
   'uc-tx-queue'
;

UDF
:
   'udf'
;

UDLD
:
   'udld'
;

UDP
:
   'udp'
;

UDP_JITTER
:
   'udp-jitter'
;

UID
:
   'uid'
;

UNABLE
:
   'Unable'
;

UNICAST_ROUTING
:
   'unicast-routing'
;

UNIQUE
:
   'unique'
;

UNIT
:
   'unit'
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

UNTAGGED
:
   'untagged'
;

UPDATE
:
   'update'
;

UPDATE_CALENDAR
:
   'update-calendar'
;

UPDATE_SOURCE
:
   'update-source' -> pushMode ( M_Interface )
;

UPGRADE
:
   'upgrade'
;

UPLINKFAST
:
   'uplinkfast'
;

URG
:
   'urg'
;

URL_LIST
:
   'url-list'
;

USE
:
   'use'
;

USE_ACL
:
   'use-acl'
;

USE_IPV4_ACL
:
   'use-ipv4-acl'
;

USE_IPV6_ACL
:
   'use-ipv6-acl'
;

USE_VRF
:
   'use-vrf'
;

USER
:
   'user'
;

USER_IDENTITY
:
   'user-identity'
;

USER_MESSAGE
:
   'user-message' -> pushMode ( M_Description )
;

USER_STATISTICS
:
   'user-statistics'
;

USERGROUP
:
   'usergroup'
;

USERNAME
:
   'username'
;

USERNAME_PROMPT
:
   'username-prompt'
;

USERS
:
   'users'
;

UUCP
:
   'uucp'
;

V1_RP_REACHABILITY
:
   'v1-rp-reachability'
;

V2
:
   'v2'
;

V4
:
   'v4'
;

V6
:
   'v6'
;

VACANT_MESSAGE
:
   'vacant-message'
;

VACL
:
   'vacl'
;

VAD
:
   'vad'
;

VALIDATION_USAGE
:
   'validation-usage'
;

VDC
:
   'vdc'
;

VER
:
   'ver'
;

VERIFY
:
   'verify'
;

VERIFY_DATA
:
   'verify-data'
;

VERSION
:
   'version'
;

VIEW
:
   'view'
;

VIOLATE_ACTION
:
   'violate-action'
;

VIOLATION
:
   'violation'
;

VIRTUAL
:
   'virtual'
;

VIRTUAL_ADDRESS
:
   'virtual-address'
;

VIRTUAL_REASSEMBLY
:
   'virtual-reassembly'
;

VIRTUAL_ROUTER
:
   'virtual-router'
;

VIRTUAL_SERVICE
:
   'virtual-service'
;

VIRTUAL_TEMPLATE
:
   'virtual-template'
;

VFI
:
   'vfi'
;

VLAN
:
   'vlan'
;

VLAN_GROUP
:
   'vlan-group'
;

VLAN_POLICY
:
   'vlan-policy'
;

VLT
:
   'vlt'
;

VLT_PEER_LAG
:
   'vlt-peer-lag'
;

VM_CPU
:
   'vm-cpu'
;

VM_MEMORY
:
   'vm-memory'
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

VPLS
:
   'vpls'
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

VPN_GROUP_POLICY
:
   'vpn-group-policy'
;

VPN_IDLE_TIMEOUT
:
   'vpn-idle-timeout'
;

VPN_SESSION_TIMEOUT
:
   'vpn-session-timeout'
;

VPN_SIMULTANEOUS_LOGINS
:
   'vpn-simultaneous-logins'
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

VRF_ALSO
:
   'vrf-also'
;

VRRP
:
   'vrrp'
;

VRRP_GROUP
:
   'vrrp-group'
;

VSERVER
:
   'vserver'
;

VTP
:
   'vtp'
;

VTY
:
   'vty'
;

VTY_POOL
:
   'vty-pool'
;

VXLAN
:
   'vxlan'
;

WAIT_START
:
   'wait-start'
;

WARNINGS
:
   'warnings'
;

WATCH_LIST
:
   'watch-list'
;

WAVELENGTH
:
   'wavelength'
;

WEBVPN
:
   'webvpn'
;

WED
:
   'Wed'
;

WEIGHT
:
   'weight'
;

WEIGHTING
:
   'weighting'
;

WHO
:
   'who'
;

WHOIS
:
   'whois'
;

WIDE
:
   'wide'
;

WINDOW_SIZE
:
   'window-size'
;

WINS_SERVER
:
   'wins-server'
;

WISM
:
   'wism'
;

WITHOUT_CSD
:
   'without-csd'
;

WLAN
:
   'wlan'
;

WRED
:
   'wred'
;

WRED_PROFILE
:
   'wred-profile'
;

WRR
:
   'wrr'
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

XDMCP
:
   'xdmcp'
;

XDR
:
   'xdr'
;

XLATE
:
   'xlate'
;

XML
:
   'XML'
   | 'xml'
;

XML_CONFIG
:
   'xml-config'
;

YELLOW
:
   'yellow'
;

ZONE
:
   'zone'
; // Other Tokens

MULTICONFIGPART
:
   '############ MultiConfigPart' F_NonNewline* F_Newline+ -> channel ( HIDDEN
   )
;

POUND
:
   '#' -> pushMode ( M_Description )
;

COMMUNITY_NUMBER
:
   F_Digit
   {!enableIPV6_ADDRESS}?

   F_Digit* ':' F_Digit+
;

MAC_ADDRESS_LITERAL
:
   F_HexDigit F_HexDigit F_HexDigit F_HexDigit '.' F_HexDigit F_HexDigit
   F_HexDigit F_HexDigit '.' F_HexDigit F_HexDigit F_HexDigit F_HexDigit
;

HEX
:
   '0x' F_HexDigit+
;

VARIABLE
:
   (
      (
         F_Variable_RequiredVarChar
         (
            (
               {!enableIPV6_ADDRESS}?

               F_Variable_VarChar*
            )
            |
            (
               {enableIPV6_ADDRESS}?

               F_Variable_VarChar_Ipv6*
            )
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

            F_Variable_VarChar_Ipv6* F_Variable_RequiredVarChar
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
   else if (_foundry && 400 <= val && val <= 1399) {
      _type = ACL_NUM_FOUNDRY_L2;
   }
	else if (600 <= val && val <= 699) {
		_type = ACL_NUM_APPLETALK;
	}
   else if (700 <= val && val <= 799) {
      _type = ACL_NUM_MAC;
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
	else if (1100 <= val && val <= 1199) {
		_type = ACL_NUM_EXTENDED_MAC;
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

BLANK_LINE
:
   (
      F_Whitespace
   )* F_Newline
   {lastTokenType == NEWLINE}?

   F_Newline* -> channel ( HIDDEN )
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
   (
      F_Whitespace
   )* [!#]
   {lastTokenType == NEWLINE || lastTokenType == -1}?

   F_NonNewline* F_Newline+ -> channel ( HIDDEN )
;

COMMENT_TAIL
:
   '!' F_NonNewline* -> channel ( HIDDEN )
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

DIGIT
:
   F_Digit
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
         (
            F_HexDigit+
            {enableIPV6_ADDRESS}?

            ':' ':'?
         )+ F_HexDigit*
      )
   )
   (
      ':' F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
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
         (
            F_HexDigit+
            {enableIPV6_ADDRESS}?

            ':' ':'?
         )+ F_HexDigit*
      )
   )
   (
      ':' F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
   )? '/' F_DecByte
;

NEWLINE
:
   F_Newline+
   {
      if (!inCommunitySet) {
   	  enableIPV6_ADDRESS = true;
   	}
   	enableIP_ADDRESS = true;
      enableDEC = true;
      enableACL_NUM = false;
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

RP_VARIABLE
:
   '$' F_Variable_RequiredVarChar F_Variable_VarChar_Ipv6*
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
   F_Whitespace+ -> channel ( HIDDEN )
; // Fragments

fragment
F_Dec16
:
   (
      F_PositiveDigit F_Digit F_Digit F_Digit F_Digit
   )
   |
   (
      F_PositiveDigit F_Digit F_Digit F_Digit
   )
   |
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
F_NonWhitespace
:
   ~( ' ' | '\t' | '\u000C' | '\n' | '\r' )
;

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
   ~( '0' .. '9' | '-' | [ \t\n\r(),!+$'*#] | '[' | ']' | [/.] | ':' )
;

fragment
F_Variable
:
   F_Variable_VarChar* F_Variable_RequiredVarChar F_Variable_VarChar*
;

fragment
F_Variable_VarChar
:
   ~( [ \t\n\r(),!+$'*#] | '[' | ']' )
;

fragment
F_Variable_VarChar_Ipv6
:
   ~( [ \t\n\r(),!+$'*#] | '[' | ']' | ':' )
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
   'access-list' -> type ( ACCESS_LIST ) , mode ( M_AsPathAccessList )
;

M_AsPath_DEC
:
   F_Digit+ -> type ( DEC ) , popMode
;

M_AsPath_RP_VARIABLE
:
   '$' F_Variable_RequiredVarChar F_Variable_VarChar_Ipv6* -> type (
   RP_VARIABLE ) , popMode
;

M_AsPath_IN
:
   'in' -> type ( IN ) , popMode
;

M_AsPath_NEIGHBOR_IS
:
   'neighbor-is' -> type ( NEIGHBOR_IS ) , popMode
;

M_AsPath_PASSES_THROUGH
:
   'passes-through' -> type ( PASSES_THROUGH ) , popMode
;

M_AsPath_PREPEND
:
   'prepend' -> type ( PREPEND ) , popMode
;

M_AsPath_ORIGINATES_FROM
:
   'originates-from' -> type ( ORIGINATES_FROM ) , popMode
;

M_AsPath_REGEX_MODE
:
   'regex-mode' -> type ( REGEX_MODE ) , popMode
;

M_AsPath_TAG
:
   'tag' -> type ( TAG ) , popMode
;

M_AsPath_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_VarChar* -> type ( VARIABLE ) ,
   popMode
;

M_AsPath_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_AsPathAccessList;

M_AsPathAccessList_DEC
:
   F_Digit+ -> type ( DEC )
;

M_AsPathAccessList_DENY
:
   'deny' -> type ( DENY ) , mode ( M_Description )
;

M_AsPathAccessList_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , mode ( DEFAULT_MODE )
;

M_AsPathAccessList_PERMIT
:
   'permit' -> type ( PERMIT ) , mode ( M_Description )
;

M_AsPathAccessList_SEQ
:
   'seq' -> type ( SEQ )
;

M_AsPathAccessList_VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_VarChar* -> type ( VARIABLE )
;

M_AsPathAccessList_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Authentication;

M_Authentication_BANNER
:
   'banner' -> type ( BANNER ) , mode ( M_BannerText )
;

M_Authentication_ARAP
:
   'arap' -> type ( ARAP ) , popMode
;

M_Authentication_ATTEMPTS
:
   'attempts' -> type ( ATTEMPTS ) , popMode
;

M_Authentication_COMMAND
:
   'command' -> type ( COMMAND ) , popMode
;

M_Authentication_CONTROL_DIRECTION
:
   'control-direction' -> type ( CONTROL_DIRECTION ) , popMode
;

M_Authentication_DOT1X
:
   'dot1x' -> type ( DOT1X ) , popMode
;

M_Authentication_ENABLE
:
   'enable' -> type ( ENABLE ) , popMode
;

M_Authentication_EOU
:
   'eou' -> type ( EOU ) , popMode
;

M_Authentication_FAIL_MESSAGE
:
   'fail-message' -> type ( FAIL_MESSAGE ) , popMode
;

M_Authentication_FAILURE
:
   'failure' -> type ( FAILURE ) , popMode
;

M_Authentication_HTTP
:
   'http' -> type ( HTTP ) , popMode
;

M_Authentication_INCLUDE
:
   'include' -> type ( INCLUDE ) , popMode
;

M_Authentication_LOGIN
:
   'login' -> type ( LOGIN ) , popMode
;

M_Authentication_MAC_MOVE
:
   'mac-move' -> type ( MAC_MOVE ) , popMode
;

M_Authentication_MESSAGE_DIGEST
:
   'message-digest' -> type ( MESSAGE_DIGEST ) , popMode
;

M_Authentication_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Authentication_ONEP
:
   'onep' -> type ( ONEP ) , popMode
;

M_Authentication_PASSWORD_PROMPT
:
   'password-prompt' -> type ( PASSWORD_PROMPT ) , popMode
;

M_Authentication_PPP
:
   'ppp' -> type ( PPP ) , popMode
;

M_Authentication_SGBP
:
   'sgbp' -> type ( SGBP ) , popMode
;

M_Authentication_SERIAL
:
   'serial' -> type ( SERIAL ) , popMode
;

M_Authentication_SSH
:
   'ssh' -> type ( SSH ) , popMode
;

M_Authentication_SUCCESS
:
   'success' -> type ( SUCCESS ) , popMode
;

M_Authentication_SUPPRESS
:
   'suppress' -> type ( SUPPRESS ) , popMode
;

M_Authentication_TELNET
:
   'telnet' -> type ( TELNET ) , popMode
;

M_Authentication_USERNAME_PROMPT
:
   'username-prompt' -> type ( USERNAME_PROMPT ) , popMode
;

M_Authentication_VARIABLE
:
   F_Variable -> type ( VARIABLE ) , popMode
;

M_Authentication_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Banner;

M_Banner_CONFIG_SAVE
:
   'config-save' -> type ( CONFIG_SAVE ) , mode ( M_BannerText )
;

M_Banner_EXEC
:
   'exec' -> type ( EXEC ) , mode ( M_BannerText )
;

M_Banner_INCOMING
:
   'incoming' -> type ( INCOMING ) , mode ( M_BannerText )
;

M_Banner_LOGIN
:
   'login' -> type ( LOGIN ) , mode ( M_BannerText )
;

M_Banner_MOTD
:
   'motd' -> type ( MOTD ) , mode ( M_BannerText )
;

M_Banner_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Banner_NONE
:
   'none' -> type ( NONE )
;

M_Banner_PROMPT_TIMEOUT
:
   'prompt-timeout' -> type ( PROMPT_TIMEOUT ) , mode ( M_BannerText )
;

M_Banner_SLIP_PPP
:
   'slip-ppp' -> type ( SLIP_PPP ) , mode ( M_BannerText )
;

M_Banner_VALUE
:
   'value' -> type ( VALUE ) , mode ( M_Description )
;

M_Banner_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_BannerText;

M_BannerText_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

M_BannerText_ESCAPE_C
:
   (
      '^C'
      | '^'
      | '\u0003'
   ) -> type ( ESCAPE_C ) , mode ( M_MOTD_C )
;

M_BannerText_HASH
:
   '#' -> type ( POUND ) , mode ( M_MOTD_HASH )
;

M_BannerText_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , mode ( M_MOTD_EOF )
;

mode M_Certificate;

M_Certificate_CA
:
   'ca' -> type ( CA ) , pushMode ( M_CertificateText )
;

M_Certificate_CHAIN
:
   'chain' -> type ( CHAIN ) , popMode
;

M_Certificate_SELF_SIGNED
:
   'self-signed' -> type ( SELF_SIGNED ) , pushMode ( M_CertificateText )
;

M_Cerficate_HEX_FRAGMENT
:
   [A-Fa-f0-9]+ -> type ( HEX_FRAGMENT ) , pushMode ( M_CertificateText )
;

M_Certificate_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_CertificateText;

M_CertificateText_QUIT
:
   'quit' -> type ( QUIT ) , mode ( DEFAULT_MODE )
;

M_CertificateText_REMOVED
:
   '<removed>' -> type ( REMOVED ) , mode ( DEFAULT_MODE )
;

M_CertificateText_HEX_FRAGMENT
:
   [A-Fa-f0-9]+ -> type ( HEX_FRAGMENT )
;

M_CertificateText_WS
:
   (
      F_Whitespace
      | F_Newline
   )+ -> channel ( HIDDEN )
;

mode M_COMMENT;

M_COMMENT_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
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

M_DES_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_DES_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Description;

M_Description_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Description_NON_NEWLINE
:
   F_NonNewline+ -> type ( RAW_TEXT )
;

mode M_Execute;

M_Execute_TEXT
:
   ~'}'+
;

M_Execute_BRACE_RIGHT
:
   '}' -> type ( BRACE_RIGHT ) , popMode
;

mode M_Extcommunity;

M_Extcommunity_COLON
:
   ':' -> type ( COLON )
;

M_Extcommunity_DEC
:
   F_Digit+ -> type ( DEC )
;

M_ExtCommunity_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Extcommunity_RT
:
   'rt' -> type ( RT )
;

M_Extcommunity_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Interface;

M_Interface_ALL
:
   'all' -> type ( ALL ) , popMode
;

M_Interface_BREAKOUT
:
   'breakout' -> type ( BREAKOUT ) , popMode
;

M_Interface_DEFAULT
:
   'default' -> type ( DEFAULT ) , popMode
;

M_Interface_DOLLAR
:
   '$' -> type ( DOLLAR ) , popMode
;

M_Interface_EQ
:
   'eq' -> type ( EQ ) , popMode
;

M_Interface_GT
:
   'gt' -> type ( GT ) , popMode
;

M_Interface_IP
:
   'ip' -> type ( IP ) , popMode
;

M_Interface_IPV4
:
   'IPv4' -> type ( IPV4 )
;

M_Interface_POINT_TO_POINT
:
   'point-to-point' -> type ( POINT_TO_POINT ) , popMode
;

M_Interface_POLICY
:
   'policy' -> type ( POLICY ) , popMode
;

M_Interface_L2TRANSPORT
:
   'l2transport' -> type ( L2TRANSPORT ) , popMode
;

M_Interface_LT
:
   'lt' -> type ( LT ) , popMode
;

M_Interface_MODULE
:
   'module' -> type ( MODULE )
;

M_Interface_MULTIPOINT
:
   'multipoint' -> type ( MULTIPOINT ) , popMode
;

M_Interface_VRF
:
   'vrf' -> type ( VRF ) , popMode
;

M_Interface_COLON
:
   ':' -> type ( COLON )
;

M_Interface_COMMA
:
   ',' -> type ( COMMA )
;

M_Interface_DASH
:
   '-' -> type ( DASH )
;

M_Interface_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Interface_NUMBER
:
   DEC -> type ( DEC )
;

M_Interface_PERIOD
:
   '.' -> type ( PERIOD )
;

M_Interface_PIPE
:
   '|' -> type ( PIPE ) , popMode
;

M_Interface_PRECFONFIGURE
:
   'preconfigure' -> type ( PRECONFIGURE )
;

M_Interface_PREFIX
:
   (
      F_Letter
      (
         F_Letter
         | '-'
         | '_'
      )*
   )
   | 'Dot11Radio'
;

M_Interface_RELAY
:
   'relay' -> type ( RELAY ) , popMode
;

M_Interface_SLASH
:
   '/' -> type ( FORWARD_SLASH )
;

M_Interface_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_IosRegex;

M_IosRegex_COMMUNITY_SET_REGEX
:
   '\'' ~[':&<> ]* ':' ~[':&<> ]* '\'' -> type ( COMMUNITY_SET_REGEX ) ,
   popMode
;

M_IosRegex_AS_PATH_SET_REGEX
:
   '\'' ~'\''* '\'' -> type ( AS_PATH_SET_REGEX ) , popMode
;

M_IosRegex_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
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
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_MOTD_C;

M_MOTD_C_ESCAPE_C
:
   (
      '^C'
      |
      (
         '^' F_Newline
      )
      | 'cC'
      | '\u0003'
   ) -> type ( ESCAPE_C ) , mode ( DEFAULT_MODE )
;

M_MOTD_C_MOTD
:
   (
      (
         '^' ~[^C\u0003\n\r]
      )
      |
      (
         'c' ~[^C\u0003]
      )
      | ~[c^\u0003]
   )+
;

mode M_MOTD_EOF;

M_MOTD_EOF_EOF
:
   'EOF' -> type ( EOF_LITERAL ) , mode ( DEFAULT_MODE )
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
   '#' -> type ( POUND ) , mode ( DEFAULT_MODE )
;

M_MOTD_HASH_MOTD
:
   ~'#'+
;

mode M_Name;

M_Name_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

M_Name_NAME
:
   F_NonWhitespace+ -> type ( VARIABLE ) , popMode
;

mode M_NEIGHBOR;

M_NEIGHBOR_CHANGES
:
   'changes' -> type ( CHANGES ) , popMode
;

M_NEIGHBOR_IP_ADDRESS
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte -> type ( IP_ADDRESS ) ,
   popMode
;

M_NEIGHBOR_IP_PREFIX
:
   F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit? ->
   type ( IP_PREFIX ) , popMode
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
            (
               F_HexDigit+ ':' ':'?
            )+ F_HexDigit*
         )
      )
      (
         ':' F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
      )?
   ) -> type ( IPV6_ADDRESS ) , popMode
;

M_NEIGHBOR_IPV6_PREFIX
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
            (
               F_HexDigit+ ':' ':'?
            )+ F_HexDigit*
         )
      )
      (
         ':' F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
      )? '/' F_DecByte
   ) -> type ( IPV6_PREFIX ) , popMode
;

M_NEIGHBOR_NLRI
:
   'nlri' -> type ( NLRI ) , popMode
;

M_NEIGHBOR_PASSIVE
:
   'passive' -> type ( PASSIVE ) , popMode
;

M_Neighbor_VARIABLE
:
   F_Variable_VarChar+ -> type ( VARIABLE ) , popMode
;

M_NEIGHBOR_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_NEIGHBOR_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_REMARK;

M_REMARK_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_REMARK_REMARK
:
   F_NonNewline+
;

mode M_RouteMap;

M_RouteMap_IN
:
   'in' -> type ( IN )
;

M_RouteMap_OUT
:
   'out' -> type ( OUT )
;

M_RouteMap_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
;

M_RouteMap_VARIABLE
:
   F_NonWhitespace+ -> type ( VARIABLE ) , popMode
;

M_RouteMap_WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Rule;

M_Rule_LINE
:
   F_NonNewline+
;

M_Rule_NEWLINE
:
   F_Newline+ -> type ( NEWLINE ) , popMode
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
   F_Whitespace+ -> channel ( HIDDEN )
;
