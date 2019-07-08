lexer grammar CiscoNxosLexer;

options {
  superClass = 'org.batfish.grammar.cisco_nxos.parsing.CiscoNxosBaseLexer';
}

tokens {
  REMARK_TEXT,
  SUBDOMAIN_NAME,
  WORD
}

ACCESS
:
  'access'
;

ACCESS_GROUP
:
  'access-group'
;

ACCESS_LIST
:
  'access-list' -> pushMode ( M_Word )
;

ACCESS_MAP
:
  'access-map'
;

ACK
:
  'ack'
;

ACL_COMMON_IP_OPTIONS
:
  'acl_common_ip_options'
;

ACL_GLOBAL_OPTIONS
:
  'acl_global_options'
;

ACL_ICMP
:
  'acl_icmp'
;

ACL_IGMP
:
  'acl_igmp'
;

ACL_INDICES
:
  'acl_indices'
;

ACL_SIMPLE_PROTOCOLS
:
  'acl_simple_protocols'
;

ACL_TCP
:
  'acl_tcp'
;

ACL_UDP
:
  'acl_udp'
;

ADD
:
  'add'
;

ADDRESS
:
  'address'
;

ADDRESS_FAMILY
:
  'address-family'
;

ADDRGROUP
:
  'addrgroup' -> pushMode ( M_Word )
;

ADMINISTRATIVELY_PROHIBITED
:
  'administratively-prohibited'
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

AHP
:
  'ahp'
;

ALLOWED
:
  'allowed'
;

ALTERNATE_ADDRESS
:
  'alternate-address'
;

ANY
:
  'any'
;

AUTOSTATE
:
  'autostate'
;

BANDWIDTH
:
  'bandwidth'
;

BGP
:
  'bgp'
;

BIFF
:
  'biff'
;

BOOTPC
:
  'bootpc'
;

BOOTPS
:
  'bootps'
;

CHANNEL_GROUP
:
  'channel-group'
;

CHARGEN
:
  'chargen'
;

CMD
:
  'cmd'
;

CONFIGURATION
:
  'configuration'
;

CONNECT
:
  'connect'
;

CONTEXT
:
  'context' -> pushMode ( M_Word )
;

CONVERSION_ERROR
:
  'conversion-error'
;

CRITICAL
:
  'critical'
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

DAYTIME
:
  'daytime'
;

DEFAULT
:
  'default'
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

DESCRIPTION
:
  'description'
;

DISCARD
:
  'discard'
;

DNSIX
:
  'dnsix'
;

DOD_HOST_PROHIBITED
:
  'dod-host-prohibited'
;

DOD_NET_PROHIBITED
:
  'dod-net-prohibited'
;

DOMAIN
:
  'domain'
;

DOT1Q
:
  [Dd] [Oo] [Tt] '1' [Qq]
;

DRIP
:
  'drip'
;

DSCP
:
  'dscp'
;

DVMRP
:
  'dvmrp'
;

ECHO
:
  'echo'
;

ECHO_REPLY
:
  'echo-reply'
;

EF
:
  'ef'
;

EIGRP
:
  'eigrp'
;

ENCAPSULATION
:
  'encapsulation'
;

EQ
:
  'eq'
;

ESP
:
  'esp'
;

ESTABLISHED
:
  'established'
;

ETHERNET
:
  [Ee] [Tt] [Hh] [Ee] [Rr] [Nn] [Ee] [Tt]
;

EXCEPT
:
  'except'
;

EXEC
:
  'exec'
;

EXPLICIT_TRACKING
:
  'explicit-tracking'
;

FAST_LEAVE
:
  'fast-leave'
;

FEATURE
:
  'feature'
;

FILTER
:
  'filter'
;

FIN
:
  'fin'
;

FINGER
:
  'finger'
;

FLASH
:
  'flash'
;

FLASH_OVERRIDE
:
  'flash-override'
;

FORCE
:
  'force'
;

FRAGMENTS
:
  'fragments'
;

FTP_DATA
:
  'ftp-data'
;

FTP
:
  'ftp'
;

GENERAL_PARAMETER_PROBLEM
:
  'general-parameter-problem'
;

GET
:
  'get'
;

GOPHER
:
  'gopher'
;

GRE
:
  'gre'
;

GROUP_TIMEOUT
:
  'group-timeout'
;

GT
:
  'gt'
;

HEAD
:
  'head'
;

HOST
:
  'host'
;

HOST_ISOLATED
:
  'host-isolated'
;

HOST_PRECEDENCE_UNREACHABLE
:
  'host-precedence-unreachable'
;

HOST_QUERY
:
  'host-query'
;

HOST_REDIRECT
:
  'host-redirect'
;

HOST_REPORT
:
  'host-report'
;

HOST_TOS_REDIRECT
:
  'host-tos-redirect'
;

HOST_TOS_UNREACHABLE
:
  'host-tos-unreachable'
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
  // Mode is needed so as not to interfere with interface names.
  // E.g. 'Ethernet1' should be ETHERNET UINT8 rather than SUBDOMAIN_NAME
  // May be revisited as grammar is fleshed out.
  {
    if (lastTokenType() == NEWLINE || lastTokenType() == -1) {
      pushMode(M_Hostname);
    }
  }

;

HTTP_METHOD
:
  'http-method'
;

ICMP
:
  'icmp'
;

IDENT
:
  'ident'
;

IGMP
:
  'igmp'
;

IGNORE
:
  'ignore'
;

IMMEDIATE
:
  'immediate'
;

INFORMATION_REPLY
:
  'information-reply'
;

INFORMATION_REQUEST
:
  'information-request'
;

INTERFACE
:
// most common abbreviation
  'int'
  (
    'erface'
  )?
;

INTERNET
:
  'internet'
;

IP
:
  'ip'
;

IPV4
:
  'ipv4'
;

IPV6
:
  'ipv6'
;

IRC
:
  'irc'
;

ISAKMP
:
  'isakmp'
;

KLOGIN
:
  'klogin'
;

KSHELL
:
  'kshell'
;

LAST_MEMBER_QUERY_INTERVAL
:
  'last-member-query-interval'
;

LINK_LOCAL_GROUPS_SUPPRESSION
:
  'link-local-groups-suppression'
;

LOG
:
  'log'
;

LOGIN
:
  'login'
;

LOOPBACK
:
// most common abbreviation
  [Ll] [Oo]
  (
    [Oo] [Pp] [Bb] [Aa] [Cc] [Kk]
  )?
;

LPD
:
  'lpd'
;

LT
:
  'lt'
;

MASK_REPLY
:
  'mask-reply'
;

MASK_REQUEST
:
  'mask-request'
;

MAXIMUM
:
  'maximum'
;

MEDIA
:
  'media'
;

MEMBER
:
  'member' -> pushMode ( M_Word )
;

MGMT
:
  [Mm] [Gg] [Mm] [Tt]
;

MOBILE_IP
:
  'mobile-ip'
;

MOBILE_REDIRECT
:
  'mobile-redirect'
;

MROUTER
:
  'mrouter'
;

NAME
:
  'name' -> pushMode ( M_Word )
;

NAMESERVER
:
  'nameserver'
;

NATIVE
:
  'native'
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

NET_REDIRECT
:
  'net-redirect'
;

NET_TOS_REDIRECT
:
  'net-tos-redirect'
;

NET_TOS_UNREACHABLE
:
  'net-tos-unreachable'
;

NET_UNREACHABLE
:
  'net-unreachable'
;

NETWORK
:
  'network'
;

NETWORK_UNKNOWN
:
  'network-unknown'
;

NNTP
:
  'nntp'
;

NO
:
  'no'
;

NO_ROOM_FOR_OPTION
:
  'no-room-for-option'
;

NON500_ISAKMP
:
  'non500-isakmp'
;

NONE
:
  'none'
;

NOS
:
  'nos'
;

NTP
:
  'ntp'
;

NULL0
:
  [Nn] [Uu] [Ll] [Ll] ' '* '0'
;

NVE
:
  'nve'
;

OPTION_MISSING
:
  'option-missing'
;

OSPF
:
  'ospf'
;

PACKET_LENGTH
:
  'packet-length'
;

PACKET_TOO_BIG
:
  'packet-too-big'
;

PARAMETER_PROBLEM
:
  'parameter-problem'
;

PCP
:
  'pcp'
;

PER_ENTRY
:
  'per-entry'
;

PERMIT
:
  'permit'
;

PERMIT_ALL
:
  'permit-all'
;

PIM
:
  'pim'
;

PIM_AUTO_RP
:
  'pim-auto-rp'
;

POP2
:
  'pop2'
;

POP3
:
  'pop3'
;

PORT_CHANNEL
:
  [Pp] [Oo] [Rr] [Tt] '-' [Cc] [Hh] [Aa] [Nn] [Nn] [Ee] [Ll]
;

PORT_UNREACHABLE
:
  'port-unreachable'
;

PORTGROUP
:
  'portgroup' -> pushMode ( M_Word )
;

POST
:
  'post'
;

PRECEDENCE
:
  'precedence'
;

PRECEDENCE_UNREACHABLE
:
  'precedence-unreachable'
;

PRIORITY
:
  'priority'
;

PROTOCOL_UNREACHABLE
:
  'protocol-unreachable'
;

PROXY
:
  'proxy'
;

PROXY_LEAVE
:
  'proxy-leave'
;

PSH
:
  'psh'
;

PUT
:
  'put'
;

QUERIER
:
  'querier'
;

QUERIER_TIMEOUT
:
  'querier-timeout'
;

QUERY_INTERVAL
:
  'query-interval'
;

QUERY_MAX_RESPONSE_TIME
:
  'query-max-response-time'
;

RANGE
:
  'range'
;

REASSEMBLY_TIMEOUT
:
  'reassembly-timeout'
;

REDIRECT
:
  'redirect'
;

REDIRECTS
:
  'redirects'
;

REMARK
:
  'remark' -> pushMode ( M_Remark )
;

REMOVE
:
  'remove'
;

REPORT_FLOOD
:
  'report-flood'
;

REPORT_POLICY
:
  'report-policy'
;

REPORT_SUPPRESSION
:
  'report-suppression'
;

RIP
:
  'rip'
;

ROBUSTNESS_VARIABLE
:
  'robustness-variable'
;

ROUTABLE
:
  'routable'
;

ROUTE
:
  'route'
;

ROUTER_ADVERTISEMENT
:
  'router-advertisement'
;

ROUTER_SOLICITATION
:
  'router-solicitation'
;

ROUTINE
:
  'routine'
;

RST
:
  'rst'
;

SECONDARY
:
  'secondary'
;

SHUTDOWN
:
  'shutdown'
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

SOURCE_QUENCH
:
  'source-quench'
;

SOURCE_ROUTE_FAILED
:
  'source-route-failed'
;

STARTUP_QUERY_COUNT
:
  'startup-query-count'
;

STARTUP_QUERY_INTERVAL
:
  'startup-query-interval'
;

STATE
:
  'state'
;

STATIC_GROUP
:
  'static-group'
;

STATISTICS
:
  'statistics'
;

SUNRPC
:
  'sunrpc'
;

SWITCHPORT
:
  'switchport'
;

SYN
:
  'syn'
;

SYSLOG
:
  'syslog'
;

TACACS
:
  'tacacs'
;

TAG
:
  'tag'
;

TALK
:
  'talk'
;

TCP
:
  'tcp'
;

TCP_FLAGS_MASK
:
  'tcp-flags-mask'
;

TCP_OPTION_LENGTH
:
  'tcp-option-length'
;

TELNET
:
  'telnet'
;

TFTP
:
  'tftp'
;

TIME
:
  'time'
;

TIME_EXCEEDED
:
  'time-exceeded'
;

TIMESTAMP_REPLY
:
  'timestamp-reply'
;

TIMESTAMP_REQUEST
:
  'timestamp-request'
;

TRACEROUTE
:
  'traceroute'
;

TRACE
:
  'trace'
;

TRACK
:
  'track'
;

TRUNK
:
  'trunk'
;

TTL
:
  'ttl'
;

TTL_EXCEEDED
:
  'ttl-exceeded'
;

UDP
:
  'udp'
;

UNICAST
:
  'unicast'
;

UNREACHABLE
:
  'unreachable'
;

URG
:
  'urg'
;

UUCP
:
  'uucp'
;

V3_REPORT_SUPPRESSION
:
  'v3-report-suppression'
;

VERSION
:
  'version'
;

VLAN
:
  [Vv] [Ll] [Aa] [Nn]
;

VNI
:
  'vni'
;

VRF
:
  'vrf'
  // If not first word on line, should be followed by VRF name
  {
    if (!(lastTokenType() == NEWLINE || lastTokenType() == -1)) {
      pushMode(M_Word);
    }
  }

;

WHOIS
:
  'whois'
;

WHO
:
  'who'
;

WWW
:
  'www'
;

XCONNECT
:
  'xconnect'
;

XDMCP
:
  'xdmcp'
;

// Other Tokens

BLANK_LINE
:
  (
    F_Whitespace
  )* F_Newline
  {lastTokenType() == NEWLINE|| lastTokenType() == -1}?

  F_Newline* -> channel ( HIDDEN )
;

COMMA
:
  ','
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* [!#]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;

DASH
:
  '-'
;

FORWARD_SLASH
:
  '/'
;

IP_ADDRESS
:
  F_IpAddress
;

IP_PREFIX
:
  F_IpPrefix
;

NEWLINE
:
  F_Newline+
;

PERIOD
:
  '.'
;

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

WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

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
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
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
F_PositiveDigit
:
  [1-9]
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_Uint8
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Uint16
:
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit?
  | [1-5] F_Digit F_Digit F_Digit F_Digit
  | '6' [0-4] F_Digit F_Digit F_Digit
  | '65' [0-4] F_Digit F_Digit
  | '655' [0-2] F_Digit
  | '6553' [0-5]
;

fragment
F_Uint32
:
// 0-4294967295
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
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$^*_=+.;:{}]
  | '-'
;

mode M_Hostname;

M_Hostname_SUBDOMAIN_NAME
:
  (
    (
      [A-Za-z0-9_]
      | '-'
    )+ '.'
  )*
  (
    [A-Za-z0-9_]
    | '-'
  )+ -> type ( SUBDOMAIN_NAME ) , popMode
;

M_Hostname_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Remark;

M_Remark_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

M_Remark_REMARK_TEXT
// Keep below M_Remark_WS

:
  F_NonNewline+ -> type ( REMARK_TEXT ) , popMode
;

mode M_Word;

M_Word_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_Word_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;
