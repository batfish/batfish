lexer grammar PaloAltoLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in PaloAltoLexer.java goes here
}

// Keywords


ACTION
:
    'action'
;

ADMIN_DIST
:
    'admin-dist'
;

AES_128_CBC
:
    'aes-128-cbc'
;

AES_128_GCM
:
    'aes-128-gcm'
;

AES_192_CBC
:
    'aes-192-cbc'
;

AES_256_CBC
:
    'aes-256-cbc'
;

AES_256_GCM
:
    'aes-256-gcm'
;

ALLOW
:
    'allow'
;

ANY
:
    'any'
;

APPLICATION
:
    'application'
;

AUTHENTICATION
:
    'authentication'
;

AUTHENTICATION_TYPE
:
    'authentication-type'
;

AUTO
:
    'auto'
;

CLOSE_BRACKET
:
    ']'
;

COMMENT
:
    'comment'
;

CONFIG
:
    'config'
;

CRYPTO_PROFILES
:
    'crypto-profiles'
;

DAYS
:
    'days'
;

DEFAULT_GATEWAY
:
    'default-gateway'
;

DENY
:
    'deny'
;

DES
:
    'des'
;

DESCRIPTION
:
    'description'
;

DESTINATION
:
    'destination'
;

DEVICES
:
    'devices'
;

DEVICECONFIG
:
    'deviceconfig'
;

DH_GROUP
:
    'dh-group'
;

DISABLED
:
    'disabled'
;

DNS
:
    'dns'
;

DNS_SETTING
:
    'dns-setting'
;

DOWN
:
    'down'
;

DROP
:
    'drop'
;

ENCRYPTION
:
    'encryption'
;

ESP
:
    'esp'
;

ETHERNET
:
    'ethernet'
;

FROM
:
    'from'
;

GATEWAY
:
    'gateway'
;

GLOBAL_PROTECT_APP_CRYPTO_PROFILES
:
    'global-protect-app-crypto-profiles'
;

GROUP1
:
    'group1'
;

GROUP2
:
    'group2'
;

GROUP5
:
    'group5'
;

GROUP14
:
    'group14'
;

GROUP19
:
    'group19'
;

GROUP20
:
    'group20'
;

HASH
:
    'hash'
;

HOSTNAME
:
    'hostname'
;

HOURS
:
    'hours'
;

ICMP
:
    'icmp'
;

IKE
:
    'ike'
;

IKE_CRYPTO_PROFILES
:
    'ike-crypto-profiles'
;

INTERFACE
:
    'interface'
;

IP
:
    'ip'
;

IP_ADDRESS_LITERAL
:
    'ip-address'
;

IPSEC_CRYPTO_PROFILES
:
    'ipsec-crypto-profiles'
;

LAYER3
:
    'layer3'
;

LIFETIME
:
    'lifetime'
;

LINK_STATUS
:
    'link-status'
;

LOG_SETTINGS
:
    'log-settings'
;

MD5
:
    'md5'
;

MINUTES
:
    'minutes'
;

MEMBERS
:
    'members'
;

METRIC
:
    'metric'
;

MTU
:
    'mtu'
;

NETMASK
:
    'netmask'
;

NETWORK
:
    'network'
;

NEXTHOP
:
    'nexthop'
;

NO
:
    'no'
;

NONE
:
    'none'
;

NTP_SERVER_ADDRESS
:
    'ntp-server-address'
;

NTP_SERVERS
:
    'ntp-servers'
;

NULL
:
    'null'
;

OPEN_BRACKET
:
    '['
;

PANORAMA_SERVER
:
    'panorama-server'
;

PORT
:
    'port'
;

PRIMARY
:
    'primary'
;

PRIMARY_NTP_SERVER
:
    'primary-ntp-server'
;

PROFILES
:
    'profiles'
;

PROTOCOL
:
    'protocol'
;

QOS
:
    'qos'
;

RESET_BOTH
:
    'reset-both'
;

RESET_CLIENT
:
    'reset-client'
;

RESET_SERVER
:
    'reset-server'
;

ROUTING_TABLE
:
    'routing-table'
;

RULEBASE
:
    'rulebase'
;

RULES
:
    'rules'
;

SCTP
:
    'sctp'
;

SECONDARY
:
    'secondary'
;

SECONDARY_NTP_SERVER
:
    'secondary-ntp-server'
;

SECONDS
:
    'seconds'
;

SECURITY
:
    'security'
;

SERVER
:
    'server'
;

SERVERS
:
    'servers'
;

SERVICE
:
    'service'
;

SERVICE_GROUP
:
    'service-group'
;

SET
:
    'set'
;

SHA1
:
    'sha1'
;

SHA256
:
    'sha256'
;

SHA384
:
    'sha384'
;

SHA512
:
    'sha512'
;

SHARED
:
    'shared'
;

SOURCE
:
    'source'
;

SOURCE_PORT
:
    'source-port'
;

STATIC_ROUTE
:
    'static-route'
;

SYSLOG
:
    'syslog'
;

SYSTEM
:
    'system'
;

TAG
:
    'tag'
;

TCP
:
    'tcp'
;

THREE_DES
:
    '3des'
;

TIMEZONE
:
    'timezone'
;

TO
:
    'to'
;

TYPE
:
    'type'
;

UDP
:
    'udp'
;

UNITS
:
    'units'
;

UP
:
    'up'
;

UPDATE_SCHEDULE
:
    'update-schedule'
;

UPDATE_SERVER
:
    'update-server'
;

VIRTUAL_ROUTER
:
    'virtual-router'
;

YES
:
    'yes'
;

VSYS
:
    'vsys'
;

ZONE
:
    'zone'
;

// Complex tokens

COMMA
:
    ','
;

DEC
:
    F_Digit+
;

DOUBLE_QUOTED_STRING
:
    '"' ~'"'* '"'
;

IP_ADDRESS
:
    F_IpAddress
;

IP_PREFIX
:
    F_IpAddress '/' F_PrefixLength
;

IP_RANGE
:
    F_IpAddress '-' F_IpAddress
;

LINE_COMMENT
:
    (
        '#'
        | '!'
    )
    F_NonNewlineChar* F_Newline+ -> channel ( HIDDEN )
;

NEWLINE
:
    F_Newline+
;

SINGLE_QUOTED_STRING
:
    '\'' ~'\''* '\''
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
    (
        F_Digit
        | F_DecByteTwoDigit
        | F_DecByteThreeDigit
    )
;

fragment
F_DecByteThreeDigit
:
    (
        ([1] F_Digit F_Digit)
        | ([2] [0-4] F_Digit)
        | ([2] [5] [0-5])
    )
;

fragment
F_DecByteTwoDigit
:
    [1-9] F_Digit
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
F_Newline
:
    [\r\n] // carriage return or line feed
;

fragment
F_PrefixLength
:
    (
        F_Digit
        | [12] F_Digit
        | [3] [012]
    )
;

fragment
F_NonNewlineChar
:
    ~[\r\n] // carriage return or line feed
;

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_Variable_VarChar
:
    ~[ \t\n\r;,{}[\]&|()"']
;

// Modes
// Blank for now, not all lexers will require modes
