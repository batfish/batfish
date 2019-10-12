lexer grammar F5BigipStructuredLexer;

options {
  superClass = 'org.batfish.grammar.f5_bigip_structured.parsing.F5BigipStructuredBaseLexer';
}

tokens {
  ARRAY,
  ASTERISK,
  BACKSLASH_CARRIAGE_RETURN,
  BACKSLASH_CHAR,
  BACKSLASH_NEWLINE,
  BACKSLASH_NEWLINE_WS,
  BANG,
  BREAK,
  CASE,
  CHARS,
  COMMENT,
  DASH,
  DOLLAR,
  DOUBLE_QUOTED_STRING,
  ELSE,
  ELSEIF,
  EQ,
  EVENT,
  EXISTS,
  EXPR,
  FORWARD_SLASH,
  IDENTIFIER,
  IF,
  NE,
  OP_AND,
  OP_EQ,
  OP_EXP,
  OP_GE,
  OP_GT,
  OP_LE,
  OP_LT,
  OP_NE,
  OP_OR,
  PAREN_LEFT,
  PAREN_RIGHT,
  PERCENT,
  PLUS,
  PUTS,
  RULE_SPECIAL,
  SIZE,
  SWITCH,
  THEN,
  VALUE_INTEGER,
  VALUE_DOUBLE,
  VALUE_STRING,
  WHEN
}

// Keywords

ACTION
:
  'action'
;

ACTIVATE
:
  'activate'
;

ADDRESS
:
  'address'
;

ADDRESS_FAMILY
:
  'address-family'
;

ALL
:
  'all'
;

ALLOW_SERVICE
:
  'allow-service'
;

ALWAYS
:
  'always'
;

ANALYTICS
:
  'analytics'
;

AND
:
  'and'
;

ANY
:
  'any'
;

ARP
:
  'arp'
;

BGP
:
  'bgp'
;

BUNDLE
:
  'bundle'
;

BUNDLE_SPEED
:
  'bundle-speed'
;

CERTIFICATE_AUTHORITY
:
  'certificate-authority'
;

CLASSIFICATION
:
  'classification'
;

CLIENT_LDAP
:
  'client-ldap'
;

CLIENT_SSL
:
  'client-ssl'
;

COMMUNITY
:
  'community'
;

DATA_GROUP
:
  'data-group'
;

DEFAULT
:
  'default'
;

DEFAULTS_FROM
:
  'defaults-from'
;

DENY
:
  'deny'
;

DESCRIPTION
:
  'description'
;

DESTINATION
:
  'destination'
;

DHCPV4
:
  'dhcpv4'
;

DHCPV6
:
  'dhcpv6'
;

DIAMETER
:
  'diameter'
;

DISABLED
:
  'disabled'
;

DNS
:
  'dns'
;

EBGP_MULTIHOP
:
  'ebgp-multihop'
;

ENABLED
:
  'enabled'
;

ENTRIES
:
  'entries'
;

EXTERNAL
:
  'external'
;

FASTHTTP
:
  'fasthttp'
;

FASTL4
:
  'fastl4'
;

FIX
:
  'fix'
;

FORTY_G
:
  '40G'
;

FTP
:
  'ftp'
;

GATEWAY_ICMP
:
  'gateway-icmp'
;

GLOBAL_SETTINGS
:
  'global-settings'
;

GTP
:
  'gtp'
;

GW
:
  'gw'
;

HOSTNAME
:
  'hostname'
;

HTML
:
  'html'
;

HTTP
:
  'http'
;

HTTP_COMPRESSION
:
  'http-compression'
;

HTTP_PROXY_CONNECT
:
  'http-proxy-connect'
;

HTTP2
:
  'http2'
;

HTTPS
:
  'https'
;

ICAP
:
  'icap'
;

ICMP_ECHO
:
  'icmp-echo'
;

ILX
:
  'ilx'
;

INTERFACE
:
  'interface'
;

INTERFACES
:
  'interfaces'
;

INTERNAL
:
  'internal'
;

IP_FORWARD
:
  'ip-forward'
;

IP_PROTOCOL
:
  'ip-protocol'
;

IPOTHER
:
  'ipother'
;

IPSECALG
:
  'ipsecalg'
;

IPV4
:
  'ipv4'
;

IPV6
:
  'ipv6'
;

KERNEL
:
  'kernel'
;

LACP
:
  'lacp'
;

LDAP
:
  'ldap'
;

LOCAL_AS
:
  'local-as'
;

LTM
:
  'ltm'
;

MAP_T
:
  'map-t'
;

MASK
:
  'mask'
;

MATCH
:
  'match'
;

MEMBERS
:
  'members'
;

MQTT
:
  'mqtt'
;

MONITOR
:
  'monitor'
;

NEIGHBOR
:
  'neighbor'
;

NET
:
  'net'
;

NETFLOW
:
  'netflow'
;

NETWORK
:
  'network'
;

NODE
:
  'node'
;

NTP
:
  'ntp'
;

OCSP_STAPLING_PARAMS
:
  'ocsp-stapling-params'
;

ONE_CONNECT
:
  'one-connect'
;

ONE_HUNDRED_G
:
  '100G'
;

ORIGINS
:
  'origins'
;

OUT
:
  'out'
;

PCP
:
  'pcp'
;

PERMIT
:
  'permit'
;

PERSIST
:
  'persist'
;

PERSISTENCE
:
  'persistence'
;

POOL
:
  'pool'
;

PPTP
:
  'pptp'
;

PREFIX
:
  'prefix'
;

PREFIX_LEN_RANGE
:
  'prefix-len-range'
;

PREFIX_LIST
:
  'prefix-list'
;

PROFILE
:
  'profile'
;

PROFILES
:
  'profiles'
;

QOE
:
  'qoe'
;

RADIUS
:
  'radius'
;

REDISTRIBUTE
:
  'redistribute'
;

REJECT
:
  'reject'
;

REMOTE_AS
:
  'remote-as'
;

REQUEST_ADAPT
:
  'request-adapt'
;

REQUEST_LOG
:
  'request-log'
;

RESPONSE_ADAPT
:
  'response-adapt'
;

REWRITE
:
  'rewrite'
;

ROUTE
:
  'route'
;

ROUTE_ADVERTISEMENT
:
  'route-advertisement'
;

ROUTE_DOMAIN
:
  'route-domain'
;

ROUTE_MAP
:
  'route-map'
;

ROUTER_ID
:
  'router-id'
;

ROUTING
:
  'routing'
;

RTSP
:
  'rtsp'
;

RULE
:
  'rule'
  {
    if (lastTokenType() == LTM && secondToLastTokenType() == NEWLINE) {
      setLtmRuleDeclaration();
      setType(RULE_SPECIAL);
    }
  }
;

RULES
:
  'rules'
;

SCTP
:
  'sctp'
;

SELECTIVE
:
  'selective'
;

SELF
:
  'self'
;

SERVER_LDAP
:
  'server-ldap'
;

SERVER_SSL
:
  'server-ssl'
;

SERVERS
:
  'servers'
;

SET
:
  'set'
;

SIP
:
  'sip'
;

SMTPS
:
  'smtps'
;

SNAT
:
  'snat'
;

SNAT_TRANSLATION
:
  'snat-translation'
;

SNATPOOL
:
  'snatpool'
;

SOCKS
:
  'socks'
;

SOURCE
:
  'source'
;

SOURCE_ADDR
:
  'source-addr'
;

SOURCE_ADDRESS_TRANSLATION
:
  'source-address-translation'
;

SPLITSESSIONCLIENT
:
  'splitsessionclient'
;

SPLITSESSIONSERVER
:
  'splitsessionserver'
;

SSL
:
  'ssl'
;

SSL_PROFILE
:
  'ssl-profile'
;

STATISTICS
:
  'statistics'
;

STREAM
:
  'stream'
;

SYS
:
  'sys'
;

TAG
:
  'tag'
;

TCP
:
  'tcp'
;

TCP_ANALYTICS
:
  'tcp-analytics'
;

TFTP
:
  'tftp'
;

TRAFFIC_ACCELERATION
:
  'traffic-acceleration'
;

TRAFFIC_GROUP
:
  'traffic-group'
;

TRANSLATE_ADDRESS
:
  'translate-address'
;

TRANSLATE_PORT
:
  'translate-port'
;

TRUNK
:
  'trunk'
;

TYPE
:
  'type'
;

UDP
:
  'udp'
;

UPDATE_SOURCE
:
  'update-source'
;

VALUE
:
  'value'
;

VIRTUAL
:
  'virtual'
;

VIRTUAL_ADDRESS
:
  'virtual-address'
;

VLAN
:
  'vlan'
;

VLANS
:
  'vlans'
;

VLANS_DISABLED
:
  'vlans-disabled'
;

VLANS_ENABLED
:
  'vlans-enabled'
;

WEB_ACCELERATION
:
  'web-acceleration'
;

WEB_SECURITY
:
  'web-security'
;

WEBSOCKET
:
  'websocket'
;

XML
:
  'xml'
;

// Complex tokens

BRACE_LEFT
:
  '{'
  {
    if (isLtmRuleDeclaration()) {
      pushMode(M_Irule);
      unsetLtmRuleDeclaration();
    }
  }
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

COMMENT_LINE
:
  (
    F_Whitespace
  )* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewlineChar* F_Newline+ -> channel ( HIDDEN )
;

COMMENT_TAIL
:
  '#' F_NonNewlineChar* -> channel ( HIDDEN )
;

VLAN_ID
:
  F_VlanId
;

UINT16
:
  F_Uint16
;

UINT32
:
  F_Uint32
;

DEC
:
  F_Digit+
;

DOUBLE_QUOTE
:
  '"' -> more, pushMode(M_DoubleQuote)
;

IMISH_CHUNK
:
  '!'
  {lastTokenType() == NEWLINE}?

  F_NonNewlineChar* F_Newline+ F_Anything*
;

IP_ADDRESS
:
  F_IpAddress
;

IP_ADDRESS_PORT
:
  F_IpAddressPort
;

IP_PREFIX
:
  F_IpPrefix
;

IPV6_ADDRESS
:
  F_Ipv6Address
;

IPV6_ADDRESS_PORT
:
  F_Ipv6AddressPort
;

IPV6_PREFIX
:
  F_Ipv6Prefix
;

NEWLINE
:
  F_Newline+
;

PARTITION
:
  F_Partition
;

SEMICOLON
:
  ';' -> channel ( HIDDEN )
;

STANDARD_COMMUNITY
:
  F_StandardCommunity
;

WORD_PORT
:
  F_WordPort
;

WORD_ID
:
  F_WordId
;

WORD
:
  F_Word
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

// Fragments

fragment
F_Anything
:
  .
;

fragment
F_DecByte
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Digit
:
  [0-9]
;

fragment
F_BackslashChar
:
  ~[0-9abfnrtvxuU\r\n]
;

fragment
F_BackslashNewlineWhitespace
:
  '\\' F_Newline
  (
    F_Newline
    | F_Whitespace
  )*
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
  F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

fragment
F_IpAddressPort
:
  F_IpAddress ':' F_Uint16
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
F_Ipv6AddressPort
:
  F_Ipv6Address '.' F_Uint16
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
  [\r\n] // carriage return or line feed

;

fragment
F_NonNewlineChar
:
  ~[\r\n] // carriage return or line feed

;

fragment
F_Partition
:
  '/'
  (
    F_PartitionChar+ '/'
  )*
;

fragment
F_PartitionChar
:
  F_WordCharCommon
  | [:]
;

fragment
F_PositiveDigit
:
  '1' .. '9'
;

fragment
F_StandardCommunity
:
  F_Uint16 ':' F_Uint16
;

fragment
F_TclIdentifier
:
  [a-zA-Z_] [a-zA-Z0-9_]*
;

fragment
F_Uint16
:
// 0-65535
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
F_VlanId
:
// 1-4094
  F_PositiveDigit F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit
  | '40' [0-8] F_Digit
  | '409' [0-4]
;

fragment
F_Whitespace
:
  [ \t\u000C] // tab or space or unicode 0x000C

;

fragment
F_Word
:
  F_WordCharCommon
  (
    F_WordChar* F_WordCharCommon
  )?
;

fragment
F_WordCharCommon
:
  ~[ \t\n\r{}[\]/:"]
;

fragment
F_WordChar
:
  F_WordCharCommon
  | [:/]
;

fragment
F_WordPort
:
  F_WordId ':' F_Uint16
;

fragment
F_WordId
:
  F_WordCharCommon+
;

mode M_DoubleQuote;

M_DoubleQuote_BODY_CHAR
:
  ~'"' -> more
;

M_DoubleQuote_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTED_STRING), popMode
;

M_DoubleQuote_ESCAPED_DOUBLE_QUOTE
:
  '\\"' -> more
;

mode M_Irule;

M_Irule_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_Irule_COMMENT
:
  '#' F_NonNewlineChar* F_Newline+ -> channel(HIDDEN)
;

M_Irule_NEWLINE
:
  F_Newline+ -> type(NEWLINE)
;

M_Irule_WHEN
:
  'when' -> type(WHEN), pushMode(M_Event)
;

M_Irule_WS
:
  F_Whitespace+ -> channel(HIDDEN)
;

mode M_Event;

M_Event_EVENT
:
  ~'{'+ -> type(EVENT) 
;

M_Event_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), mode(M_Command)
;

mode M_Command;

M_Command_BRACE_LEFT
:
  '{' -> pushMode(M_BracedSegment), type(BRACE_LEFT)
;

M_Command_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_Command_BRACKET_LEFT
:
  '[' -> type ( BRACKET_LEFT ) , pushMode ( M_Command )
;

M_Command_BRACKET_RIGHT
:
  ']' -> type ( BRACKET_RIGHT ) , popMode
;

M_Command_CHARS
:
  ~[ \t\r\n\\{}[\]$"]+ -> type(CHARS)
;

M_Command_COMMENT
:
  '#' F_NonNewlineChar* F_Newline -> type(COMMENT)
;

M_Command_DOLLAR
:
  '$' -> type(DOLLAR), pushMode(M_VariableSubstitution)
;

M_Command_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuotedSegment)
;

M_Command_NEWLINE
:
  F_Newline+ -> type(NEWLINE)
;

M_Command_WS
:
  [ \t]+ -> type(WS)
;

mode M_BracedSegment;

M_BracedSegment_CHARS
:
  ~[{}]+ -> type(CHARS)
;

M_BracedSegment_BACKSLASH_NEWLINE_WS
:
  F_BackslashNewlineWhitespace -> type(BACKSLASH_NEWLINE_WS)
;

M_BracedSegment_BRACE_LEFT
:
  '{' -> pushMode(M_BracedSegment), type(BRACE_LEFT)
;

M_BracedSegment_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

mode M_DoubleQuotedSegment;

M_DoubleQuotedSegment_CHARS
:
  ~[["$\\]+ -> type(CHARS)
;

M_DoubleQuotedSegment_BACKSLASH_CHAR
:
  '\\' F_BackslashChar -> type(BACKSLASH_CHAR)
;

M_DoubleQuotedSegment_BACKSLASH_NEWLINE
:
  '\\n' -> type(BACKSLASH_NEWLINE)
;

M_DoubleQuotedSegment_BACKSLASH_NEWLINE_WS
:
  F_BackslashNewlineWhitespace -> type(BACKSLASH_NEWLINE_WS)
;

M_DoubleQuotedSegment_BRACKET_LEFT
:
  '[' -> type(BRACKET_LEFT), pushMode(M_Command)
;

M_DoubleQuotedSegment_DOLLAR
:
  '$' -> type(DOLLAR), pushMode(M_VariableSubstitution)
;

M_DoubleQuotedSegment_DOUBLE_QUOTE
:
  '"' -> type(DOUBLE_QUOTE), popMode
;

mode M_VariableSubstitution;

M_VariableSubstitution_BRACE_LEFT
:
  '{' -> type(BRACE_LEFT), pushMode(M_BracedVariableSubstitution)
;

M_VariableSubstitution_CHARS
:
  [0-9A-Za-z_]+
  (
    '::' [0-9A-Za-z_]+
  )* -> type(CHARS)
;

M_VariableSubstitution_DOLLAR
:
// kinda screwed here
  '$' -> type(DOLLAR), popMode
;

M_VariableSubstitution_NEWLINE
:
  F_Newline+ -> type(NEWLINE), popMode
;

M_VariableSubstitution_WS
:
  F_Whitespace+ -> type(WS), popMode
;

mode M_BracedVariableSubstitution;

M_BracedVariableSubstitution_BRACE_RIGHT
:
  '}' -> type(BRACE_RIGHT), popMode
;

M_BracedVariableSubstitution_CHARS
:
  ~'}'+ -> type(CHARS)
;
