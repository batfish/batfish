lexer grammar CumulusFrrLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_frr.parsing.CumulusFrrBaseLexer';
}

tokens {
  REMARK_TEXT,
  WORD
}

ADDRESS
:
  'address'
;

AGENTX
:
  'agentx'
;

BFD
:
  'bfd' -> pushMode(M_Words)
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* [!]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;

ACTIVATE
:
  'activate'
;

ADDRESS_FAMILY
:
  'address-family'
;

ADVERTISE
:
  'advertise'
;

ADVERTISE_ALL_VNI
:
  'advertise-all-vni'
;

ALWAYS_COMPARE_MED
:
  'always-compare-med'
;

BGP
:
  'bgp'
;

COLON
:
  ':'
;

COMMUNITY
:
  'community'
  // All other instances are followed by keywords or tokens in default mode
  {
    switch (lastTokenType()) {
      case MATCH:
        pushMode(M_Words);
        break;
      default:
        break;
    }
  }
;

COMMUNITY_LIST
:
  'community-list'
;

CONNECTED
:
  'connected'
;

DATACENTER
:
  'datacenter'
;

DEFAULTS
:
  'defaults'
;

DENY
:
  'deny'
;

DESCRIPTION
:
  'description' -> pushMode ( M_Remark )
;

EVPN
:
  'evpn'
;

EXIT_ADDRESS_FAMILY
:
  'exit-address-family'
;

EXIT_VRF
:
  'exit-vrf'
;

EXPANDED
:
  'expanded' -> pushMode(M_Word)
;

EXTERNAL
:
  'external'
;

FRR
:
  'frr'
;

GE
:
  'ge'
;

HOSTNAME
:
  'hostname' -> pushMode(M_Word)
;

INFORMATIONAL
:
  'informational'
;

INTEGRATED_VTYSH_CONFIG
:
  'integrated-vtysh-config'
;

INTERFACE
:
  'interface'
  {
    switch (lastTokenType()) {
      case MATCH:
        pushMode(M_Word);
        break;
      default:
        break;
    }
  }
;

INBOUND
:
  'inbound'
;

INTERNAL
:
  'internal'
;

IP
:
  'ip'
;

IPV4
:
  'ipv4'
;

IP_ADDRESS
:
  F_IpAddress
;

IP_PREFIX
:
  F_IpPrefix
;

L2VPN
:
  'l2vpn'
;

LE
:
  'le'
;

LOG
:
  'log'
;

LINE
:
  'line'
;

NEIGHBOR
:
  'neighbor' -> pushMode(M_Neighbor)
;

NETWORK
:
  'network'
;

NEXT_HOP_SELF
:
  'next-hop-self'
;

PASSWORD
:
  'password' -> pushMode(M_Remark)
;

PEER_GROUP
:
  'peer-group' -> pushMode(M_PeerGroup)
;

PERMIT
:
  'permit'
;

PREFIX_LIST
:
  'prefix-list' -> pushMode ( M_Word )
;

REDISTRIBUTE
:
  'redistribute'
;

REMOTE_AS
:
  'remote-as'
;

ROUTE_MAP
:
  'route-map' -> pushMode(M_Word)
;

ROUTE
:
  'route'
;

ROUTER
:
  'router'
;

ROUTER_ID
:
  'router-id'
;

SET
:
  'set'
;

SEQ
:
  'seq'
;

SERVICE
:
  'service'
;

SOFT_RECONFIGURATION
:
  'soft-reconfiguration'
;

STATIC
:
  'static'
;

SUBNET_MASK
:
  F_SubnetMask
;

SYSLOG
:
  'syslog'
;

MATCH
:
  'match'
;

METRIC
:
  'metric'
;

NEWLINE
:
  F_Newline+
;

NEXT_HOP
:
  'next-hop'
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

USERNAME
:
  'username' -> pushMode(M_Words)
;

UNICAST
:
  'unicast'
;

DEC
:
  F_Digit+
;

VERSION
:
  'version' -> pushMode(M_Remark)
;

VNI
:
  'vni'
;

VRF
:
  'vrf' -> pushMode(M_Word)
;

VTY
:
  'vty'
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel

;

BLANK_LINE
:
  F_Whitespace* F_Newline+
  {lastTokenType() == NEWLINE|| lastTokenType() == -1}?
    -> channel ( HIDDEN )
;

// Fragments
fragment
F_Digit
:
  [0-9]
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
F_SubnetMask
:
  F_SubnetMaskOctet '.0.0.0'
  | '255.' F_SubnetMaskOctet . '.0.0'
  | '255.255.' F_SubnetMaskOctet . '.0'
  | '255.255.255.' F_SubnetMaskOctet
;

fragment
F_SubnetMaskOctet
:
  '0'
  | '128'
  | '192'
  | '224'
  | '240'
  | '248'
  | '252'
  | '254'
  | '255'
;

fragment
F_PositiveDigit
:
  [1-9]
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

fragment
F_Newline
:
  [\n\r] // carriage return or line feed

;

fragment
F_NonNewline
:
  ~[\n\r]
;

fragment
F_NonWhitespace
:
  ~[ \t\u000C\u00A0\n\r]
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

// modes
mode M_Neighbor;

M_Neighbor_IP_Address
:
  F_IpAddress -> type(IP_ADDRESS) , popMode
;

M_Neighbor_Word
:
  F_Word -> type(WORD) , popMode
;

M_Neighbor_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

mode M_PeerGroup;

M_Newline
:
  F_Newline -> type(NEWLINE), popMode
;

M_PeerGroup_Word
:
  F_Word -> type(WORD) , popMode
;

M_PeerGroup_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
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

mode M_Words;

M_Words_NEWLINE
:
  F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Words_WORD
:
  F_Word -> type ( WORD )
;

M_Words_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Remark;

M_Remark_REMARK_TEXT
:
  F_NonWhitespace F_NonNewline* -> type ( REMARK_TEXT ) , popMode
;

M_Remark_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;
