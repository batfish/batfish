lexer grammar CumulusInterfacesLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_interfaces.parsing.CumulusInterfacesBaseLexer';
}
tokens {
  TEXT, WORD
}

// Keyword tokens

ADD: 'add';

ADDRESS: 'address';

ADDRESS_VIRTUAL: 'address-virtual';

ALIAS
:
  'alias' -> pushMode(M_LineText)
;

AUTO
:
  'auto' -> pushMode (M_Word)
;

BOND_LACP_BYPASS_ALLOW
:
  'bond-lacp-bypass-allow' -> pushMode(M_DropUntilNewline)
;

BOND_LACP_RATE
:
  'bond-lacp-rate' -> pushMode(M_DropUntilNewline)
;

BOND_MASTER
:
  'bond-master' -> pushMode(M_DropUntilNewline)
;

BOND_MIIMON
:
  'bond-miimon' -> pushMode(M_DropUntilNewline)
;

BOND_MIN_LINKS
:
  'bond-min-links' -> pushMode(M_DropUntilNewline)
;

BOND_MODE
:
  'bond-mode' -> pushMode(M_DropUntilNewline)
;

BOND_SLAVES
:
  'bond-slaves' -> pushMode(M_Words)
;

BOND_XMIT_HASH_POLICY
:
  'bond-xmit-hash-policy' -> pushMode(M_DropUntilNewline)
;

BRIDGE_PORTS
:
  'bridge-ports' -> pushMode(M_Words)
;

BRIDGE_ACCESS: 'bridge-access';

BRIDGE_ARP_ND_SUPPRESS
:
  'bridge-arp-nd-suppress' -> pushMode(M_DropUntilNewline)
;

BRIDGE_LEARNING
:
  'bridge-learning' -> pushMode(M_DropUntilNewline)
;

BRIDGE_PVID: 'bridge-pvid';

BRIDGE_VIDS: 'bridge-vids';

BRIDGE_VLAN_AWARE: 'bridge-vlan-aware';

CLAG_ID: 'clag-id';

CLAGD_VXLAN_ANYCAST_IP: 'clagd-vxlan-anycast-ip';

CLAGD_BACKUP_IP: 'clagd-backup-ip';

CLAGD_PEER_IP: 'clagd-peer-ip';

CLAGD_PRIORITY: 'clagd-priority';

CLAGD_SYS_MAC: 'clagd-sys-mac';

DASH: '-';

DEV
:
  'dev'-> pushMode(M_Word)
;

DHCP: 'dhcp';

GATEWAY: 'gateway';

HWADDRESS: 'hwaddress';

IFACE
:
  'iface' -> pushMode(M_Word)
;

INTERFACE
:
  'interface' -> pushMode(M_Word)
;

INET: 'inet';

IP: 'ip';

LINK: 'link';

LINK_LOCAL: 'linklocal';

LINK_SPEED: 'link-speed';

LINK_AUTONEG: 'link-autoneg';

LOOPBACK: 'loopback';

MANUAL: 'manual';

MTU: 'mtu';

OFF: 'off';

ON: 'on';

MSTPCTL_BPDUGUARD
:
  'mstpctl-bpduguard' -> pushMode(M_DropUntilNewline)
;

MSTPCTL_PORTADMINEDGE
:
  'mstpctl-portadminedge' -> pushMode(M_DropUntilNewline)
;

MSTPCTL_PORTBPDUFILTER
:
  'mstpctl-portbpdufilter' -> pushMode(M_DropUntilNewline)
;

NO: 'no';

POST_UP: 'post-up';

ROUTE: 'route';

SET
:
  'set' -> pushMode(M_DropUntilNewline)
;

STATIC: 'static';

VIA: 'via';

VLAN_ID: 'vlan-id';

VLAN_RAW_DEVICE
:
  'vlan-raw-device' -> pushMode(M_Word)
;

VRF
:
  'vrf' -> pushMode(M_Word)
;

VRF_TABLE
:
  'vrf-table' -> pushMode(M_Word)
;

VXLAN_ID: 'vxlan-id';

VXLAN_LOCAL_TUNNEL_IP: 'vxlan-local-tunnelip';

YES: 'yes';

// Complex tokens
BLANK_LINE
:
  (
    F_Whitespace
  )* F_Newline
  {lastTokenType() == NEWLINE|| lastTokenType() == -1}?

  F_Newline* -> channel ( HIDDEN )
;

COMMENT_LINE
:
  (
    F_Whitespace
  )* [#]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;

IP_ADDRESS
:
  F_IpAddress
;

IPV6_ADDRESS
:
  F_Ipv6Address
;

IP_PREFIX
:
  F_IpPrefix
;

IPV6_PREFIX
:
  F_Ipv6Prefix
;

MAC_ADDRESS
:
  F_MacAddress
;

NEWLINE
:
  F_Newline+
;

NUMBER
:
  F_Digit+
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN ) // parser never sees tokens on hidden channel
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
  '::' F_Ipv6HexWordLE7
  | F_Ipv6HexWord '::' F_Ipv6HexWordLE6
  | F_Ipv6HexWord2 '::' F_Ipv6HexWordLE5
  | F_Ipv6HexWord3 '::' F_Ipv6HexWordLE4
  | F_Ipv6HexWord4 '::' F_Ipv6HexWordLE3
  | F_Ipv6HexWord5 '::' F_Ipv6HexWordLE2
  | F_Ipv6HexWord6 '::' F_Ipv6HexWordLE1
  | F_Ipv6HexWord7 '::'
  | F_Ipv6HexWord8
;

fragment
F_Ipv6HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_Ipv6HexWord2
:
  F_Ipv6HexWord ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord3
:
  F_Ipv6HexWord2 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord4
:
  F_Ipv6HexWord3 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord5
:
  F_Ipv6HexWord4 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord6
:
  F_Ipv6HexWord5 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord7
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWord
;

fragment
F_Ipv6HexWord8
:
  F_Ipv6HexWord6 ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal2
:
  F_Ipv6HexWord2
  | F_IpAddress
;

fragment
F_Ipv6HexWordFinal3
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordFinal4
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordFinal5
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordFinal6
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordFinal7
:
  F_Ipv6HexWord ':' F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE1
:
  F_Ipv6HexWord?
;

fragment
F_Ipv6HexWordLE2
:
  F_Ipv6HexWordLE1
  | F_Ipv6HexWordFinal2
;

fragment
F_Ipv6HexWordLE3
:
  F_Ipv6HexWordLE2
  | F_Ipv6HexWordFinal3
;

fragment
F_Ipv6HexWordLE4
:
  F_Ipv6HexWordLE3
  | F_Ipv6HexWordFinal4
;

fragment
F_Ipv6HexWordLE5
:
  F_Ipv6HexWordLE4
  | F_Ipv6HexWordFinal5
;

fragment
F_Ipv6HexWordLE6
:
  F_Ipv6HexWordLE5
  | F_Ipv6HexWordFinal6
;

fragment
F_Ipv6HexWordLE7
:
  F_Ipv6HexWordLE6
  | F_Ipv6HexWordFinal7
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
F_MacAddress
:
  F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':'
  F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit ':' F_HexDigit F_HexDigit
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
  ~[ \t\u000C\u00A0\n\r]
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
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
  | '\\\n'  // continue on next line
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z_.:] | '-'
;

mode M_DropUntilNewline;

M_DropUntilNewline_NonNewline
:
  F_NonNewline+ -> channel(HIDDEN)
;

M_DropUntilNewline_Newline
:
  F_Newline+ -> type(NEWLINE), popMode
;

mode M_LineText;

M_LineText_TEXT
:
  F_NonWhitespace F_NonNewline* -> type (TEXT), popMode
;

M_LineText_WS
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
