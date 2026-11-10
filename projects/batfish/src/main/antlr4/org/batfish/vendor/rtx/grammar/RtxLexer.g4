lexer grammar RtxLexer;

options {
  superClass = 'RtxBaseLexer';
}

tokens {
  FILTER_PASS,
  FILTER_REJECT,
  FILTER_RESTRICT,
  FILTER_WORD,
  IGNORED_TEXT,
  MASQUERADE_PORT_RANGE,
  WORD
}

// BEGIN keywords
//
// Many RTX commands do not affect forwarding behavior. Rather than writing a rule for each, the
// leading keyword of such a command pushes M_Ignore, which swallows the rest of the line. See
// s_null in RtxParser.g4.

ADDRESS
:
  'address'
  {
    // `tunnel endpoint address <local> <remote>` takes IPv6 endpoints, which are not modeled.
    if (lastTokenType() == ENDPOINT) {
      pushMode(M_Ignore);
    }
  }
;

ADMINISTRATOR: 'administrator' -> pushMode(M_Ignore);
AUTO: 'auto';
AUTONOMOUS_SYSTEM: 'autonomous-system';
BGP: 'bgp';
CHARACTER: 'character' -> pushMode(M_Ignore);
COLUMNS: 'columns' -> pushMode(M_Ignore);
CONSOLE: 'console';
DEFAULT: 'default';

DESCRIPTOR
:
  'descriptor'
  {
    // A line-initial `nat descriptor <sub-command>` defines a descriptor, and each sub-command has
    // its own argument syntax. `ip <if> nat descriptor <n>` merely applies one, so only the
    // line-initial form switches into the sub-command mode.
    if (lastTokenType() == NAT
        && (secondToLastTokenType() == NEWLINE || secondToLastTokenType() == -1)) {
      pushMode(M_NatDescriptor);
    }
  }
;

DISABLE: 'disable';
DNS: 'dns' -> pushMode(M_Ignore);
DYNAMIC: 'dynamic' -> pushMode(M_Ignore);
ENABLE: 'enable';
ENCAPSULATION: 'encapsulation' -> pushMode(M_Ignore);
ENDPOINT: 'endpoint';
EQUAL: 'equal';
EXPORT: 'export';

FILTER
:
  'filter'
  {
    // `ip filter ...` defines a filter; its arguments have an irregular syntax that is lexed in
    // dedicated modes. Every other use of `filter` (e.g. `ip <if> secure filter`,
    // `ip route ... filter`) takes a plain list of filter numbers.
    if (lastTokenType() == IP) {
      pushMode(M_FilterDef);
    }
  }
;

GATEWAY: 'gateway';
HB46PP: 'hb46pp';
HIDE: 'hide';
HTTPD: 'httpd' -> pushMode(M_Ignore);
IMPORT: 'import';
INCLUDE: 'include';
IN: 'in';
INNER: 'inner';
IP: 'ip';
IPCP: 'ipcp';
IPV6: 'ipv6' -> pushMode(M_Ignore);
KEEPALIVE: 'keepalive';
LAN: 'lan' -> pushMode(M_Ignore);
LOGIN: 'login' -> pushMode(M_Ignore);
MAP_E: 'map-e';
// Declared before MASQUERADE and NAT so that the longer keyword wins.
NAT_MASQUERADE: 'nat-masquerade';
MASQUERADE: 'masquerade';
METRIC: 'metric';
NAT: 'nat';
NEIGHBOR: 'neighbor';
NGN: 'ngn' -> pushMode(M_Ignore);
NONE: 'none';
OFF: 'off';
ON: 'on';
OUT: 'out';
OUTER: 'outer';
PRE_SHARED_KEY: 'pre-shared-key' -> pushMode(M_Ignore);
PRIMARY: 'primary';
PROMPT: 'prompt' -> pushMode(M_Word);
RANGE: 'range';
REFINES: 'refines';
REJECT: 'reject';
REVERSE: 'reverse';
ROUTE: 'route';
SCHEDULE: 'schedule' -> pushMode(M_Ignore);
SECONDARY: 'secondary';
SECURE: 'secure';
SELECT: 'select';
SFTPD: 'sftpd' -> pushMode(M_Ignore);
SHUTDOWN: 'shutdown' -> pushMode(M_Ignore);
SNMPV2C: 'snmpv2c' -> pushMode(M_Ignore);
SNTPD: 'sntpd' -> pushMode(M_Ignore);
SSHD: 'sshd' -> pushMode(M_Ignore);
STATIC: 'static';
STATISTICS: 'statistics' -> pushMode(M_Ignore);
SYSLOG: 'syslog' -> pushMode(M_Ignore);
SYSTEM: 'system' -> pushMode(M_Ignore);
TELNETD: 'telnetd' -> pushMode(M_Ignore);

TUNNEL
:
  'tunnel'
  {
    // `ip tunnel mtu|tcp|secure filter ...` configures the tunnel interface. None of it is
    // modeled, since the J57 tunnels have IPv6 endpoints. `tunnel <subcommand>` at the start of a
    // line is modeled.
    if (lastTokenType() == IP) {
      pushMode(M_Ignore);
    }
  }
;

TYPE: 'type';
USE: 'use';
USER: 'user' -> pushMode(M_Ignore);
VLAN: 'vlan';
VRRP: 'vrrp';
WEIGHT: 'weight';

// END keywords

// BEGIN other tokens

// Separates the endpoints of an address or port range in the `nat descriptor` commands. Every
// other use of `-` in the configuration language is inside a keyword or inside swallowed text.
DASH: '-';

DOT1Q: '802.1q';

// `hairpin=on|off`, an optional trailing parameter of `nat descriptor type`.
HAIRPIN: 'hairpin=' ( 'on' | 'off' );

// `vid=<vlan-id>`, the only form of the vlan command's arguments used in practice. Trailing
// arguments (e.g. `name=...`) are swallowed.
VLAN_VID: 'vid=' F_Digit+ -> pushMode(M_Ignore);

// VRRP parameters are all `key=value` with no surrounding whitespace.
VRRP_ADVERTISE_INTERVAL: 'advertise-interval=' F_Digit+;
VRRP_AUTH: 'auth=' F_NonWhitespaceChar+;
VRRP_DOWN_INTERVAL: 'down-interval=' F_Digit+;
VRRP_PREEMPT: 'preempt=' ( 'on' | 'off' );
VRRP_PRIORITY: 'priority=' F_Digit+;

// Comment lines only appear in the header block emitted by `show config`.
COMMENT_LINE
:
  F_Whitespace* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewlineChar* ( F_Newline | EOF ) -> channel(HIDDEN)
;

INTERFACE_NAME: F_InterfaceName;

IPV4_PREFIX: F_Ipv4Prefix;
IPV4_ADDRESS: F_Ipv4Address;

UINT: F_Digit+;

NEWLINE: F_Newline;
WS: F_Whitespace+ -> channel(HIDDEN);

// END other tokens

fragment
F_Digit
:
  [0-9]
;

fragment
F_Uint8
:
  // 0-255
  F_Digit
  | [1-9] F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
;

fragment
F_Ipv4Address
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_Ipv4Prefix
:
  F_Ipv4Address '/' F_Ipv4PrefixLength
;

fragment
F_Ipv4PrefixLength
:
  // 0-32
  F_Digit
  | [12] F_Digit
  | '3' [012]
;

fragment
F_InterfaceName
:
  // LAN interface (lan1) or 802.1Q sub-interface (lan3/1). Switch ports (lan1:1) only appear in
  // commands that are swallowed wholesale, so they are not tokenized here.
  'lan' F_Digit+ ( '/' F_Digit+ )?
;

fragment
F_Newline
:
  // Any number of newlines, allowing whitespace in between.
  F_NewlineChar ( F_Whitespace* F_NewlineChar+ )*
;

fragment
F_NewlineChar
:
  // A single newline character sequence: \r, \r\n, or \n.
  '\r' '\n'?
  | '\n'
;

fragment
F_NonNewlineChar
:
  ~[\r\n]
;

fragment
F_NonWhitespaceChar
:
  ~[ \t\r\n]
;

fragment
F_Whitespace
:
  [ \t]
;

// A single word, used for the hostname in `console prompt <name>`.
mode M_Word;

M_Word_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Word_WS: F_Whitespace+ -> channel(HIDDEN);
M_Word_WORD: F_NonWhitespaceChar+ -> type(WORD), popMode;

// Swallows the remainder of a line.
mode M_Ignore;

M_Ignore_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_Ignore_WS: F_Whitespace+ -> channel(HIDDEN);
M_Ignore_TEXT: F_NonWhitespaceChar+ -> type(IGNORED_TEXT);

// Entered after a line-initial `nat descriptor`. Recognizes the sub-commands that are modeled and
// sends every other one (log, timer, session limit, static, backward-compatibility, ...) to
// M_Ignore, so that an unmodeled sub-command does not become an unrecognized line.
mode M_NatDescriptor;

M_NatDescriptor_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_NatDescriptor_WS: F_Whitespace+ -> channel(HIDDEN);
M_NatDescriptor_ADDRESS: 'address' -> type(ADDRESS), popMode;
M_NatDescriptor_TYPE: 'type' -> type(TYPE), popMode;

M_NatDescriptor_MASQUERADE_PORT_RANGE
:
  'masquerade' F_Whitespace+ 'port' F_Whitespace+ 'range' -> type(MASQUERADE_PORT_RANGE), popMode
;

M_NatDescriptor_OTHER: F_NonWhitespaceChar+ -> type(IGNORED_TEXT), mode(M_Ignore);

// Entered after `ip filter`. Reads the filter number, then hands off to M_FilterArgs (for a filter
// definition) or M_Ignore (for `ip filter dynamic ...` and other unmodeled sub-commands).
mode M_FilterDef;

M_FilterDef_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_FilterDef_WS: F_Whitespace+ -> channel(HIDDEN);
M_FilterDef_UINT: F_Digit+ -> type(UINT);
M_FilterDef_PASS
:
  ( 'pass' | 'pass-log' | 'pass-nolog' ) -> type(FILTER_PASS), mode(M_FilterArgs)
;

M_FilterDef_REJECT
:
  ( 'reject' | 'reject-log' | 'reject-nolog' ) -> type(FILTER_REJECT), mode(M_FilterArgs)
;

M_FilterDef_RESTRICT
:
  ( 'restrict' | 'restrict-log' | 'restrict-nolog' ) -> type(FILTER_RESTRICT), mode(M_FilterArgs)
;

M_FilterDef_OTHER: F_NonWhitespaceChar+ -> type(IGNORED_TEXT), mode(M_Ignore);

// The arguments of a filter definition. Their grammar is irregular enough (comma-separated lists,
// ranges whose endpoints may be numbers or mnemonics, embedded '/' and '*') that tokenizing them
// precisely would make the lexer ambiguous. Each whitespace-delimited argument is returned as one
// word and interpreted in RtxConfigurationBuilder.
mode M_FilterArgs;

M_FilterArgs_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_FilterArgs_WS: F_Whitespace+ -> channel(HIDDEN);
M_FilterArgs_WORD: F_NonWhitespaceChar+ -> type(FILTER_WORD);
