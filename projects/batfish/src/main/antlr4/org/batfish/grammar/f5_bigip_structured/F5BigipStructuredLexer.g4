lexer grammar F5BigipStructuredLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
// Java code to end up in F5BigipStructuredLexer.java goes here

private int lastTokenType = -1;

@Override
public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
       lastTokenType = token.getType();
    }
}

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

CLIENT_SSL
:
  'client-ssl'
;

COMMUNITY
:
  'community'
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

DISABLED
:
  'disabled'
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

FORTY_G
:
  '40G'
;

GLOBAL_SETTINGS
:
  'global-settings'
;

GW
:
  'gw'
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

ICMP_ECHO
:
  'icmp-echo'
;

IF
:
  'if'
;

INTERFACE
:
  'interface'
;

INTERFACES
:
  'interfaces'
;

IP_FORWARD
:
  'ip-forward'
;

IP_PROTOCOL
:
  'ip-protocol'
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

LOCAL_AS
:
  'local-as'
;

LTM
:
  'ltm'
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

RULE
:
  'rule'
;

RULES
:
  'rules'
;

SELECTIVE
:
  'selective'
;

SELF
:
  'self'
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

SSL
:
  'ssl'
;

SSL_PROFILE
:
  'ssl-profile'
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

// Complex tokens

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

COMMENT_LINE
:
  (
    F_Whitespace
  )* '#'
  {lastTokenType == NEWLINE || lastTokenType == -1}?

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

DOUBLE_QUOTED_STRING
:
  '"' ~'"'* '"'
;

IMISH_CHUNK
:
  '!'
  {lastTokenType == NEWLINE}?

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
  F_Digit? F_Digit? F_Digit?
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
  ~[ \t\n\r{}[\]/:]
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
