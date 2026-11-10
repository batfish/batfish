lexer grammar NecIxLexer;

options {
  superClass = 'NecIxBaseLexer';
}

@members {
  /**
   * Whether the lexer is at the start of a line. Leading whitespace there is the indentation of a
   * command inside a mode rather than a separator.
   */
  private boolean atLineStart() {
    return lastTokenType() == NEWLINE || lastTokenType() == -1;
  }
}

// NEC IX has a mode-based, Cisco-like CLI. `show running-config` indents every command inside a
// mode by two spaces per level, so the lexer turns leading whitespace into an INDENT token and the
// parser uses it to tell commands inside a block from the next top-level command.
//
// Every keyword below must be defined before WORD, which matches any other run of non-whitespace
// characters (names, interface names, IPv6 addresses, free text).

// BEGIN keywords

ACCESS_LIST: 'access-list';
ACK: 'ack';
ADDITIVE: 'additive';
ADDRESS: 'address';
ADDRESS_FAMILY: 'address-family';
ADVERTISEMENT_INTERVAL: 'advertisement-interval';
ALWAYS: 'always';
ANY: 'any';
AS_PATH: 'as-path';
AUTO_CONNECT: 'auto-connect';
BGP: 'bgp';
BRIDGE: 'bridge';
BRIDGE_GROUP: 'bridge-group';
CODE: 'code';
COMMUNITY: 'community';
CONNECTED: 'connected';
CONNECT_INTERVAL: 'connect-interval';
DEFAULT: 'default';
DEFAULT_LOCAL_PREFERENCE: 'default-local-preference';
DEFAULT_METRIC: 'default-metric';
DENY: 'deny';
DESCRIPTION: 'description';
DEST: 'dest';
DEST_DOMAIN: 'dest-domain';
DEVICE: 'device';
DHCP: 'dhcp';
DISTANCE: 'distance';
DOT1Q: 'dot1q';
DPORT: 'dport';
DSCP: 'dscp';
EBGP_MULTIHOP: 'ebgp-multihop';
ENABLE: 'enable';
ENCAPSULATION: 'encapsulation';
EQ: 'eq';
ESTABLISHED: 'established';
FILTER: 'filter';
FIN: 'fin';
FORWARDING: 'forwarding';
FRAGMENTS: 'fragments';
GT: 'gt';
HOSTNAME: 'hostname';
ICMP: 'icmp';
IKEV2: 'ikev2';
IN: 'in';
INSIDE: 'inside';
INTERFACE: 'interface';
IP: 'ip';
IPV4: 'ipv4';
IPV6: 'ipv6';
KEEPALIVE: 'keepalive';
LINKMGR: 'linkmgr';
LIST: 'list';
LOCAL_PREFERENCE: 'local-preference';
LOGGING: 'logging';
LT: 'lt';
MATCH: 'match';
MAX: 'max';
METRIC: 'metric';
MIN: 'min';
MTU: 'mtu';
MULTIPATH: 'multipath';
NAPT: 'napt';
NAT: 'nat';
NEIGHBOR: 'neighbor';
NEQ: 'neq';
NETWORK: 'network';
NEXT_HOP_SELF: 'next-hop-self';
NO: 'no';
NO_ADVERTISE: 'no-advertise';
NO_EXPORT: 'no-export';
NO_EXPORT_SUBCONFED: 'no-export-subconfed';
NTP: 'ntp';
ORIGINATE_DEFAULT: 'originate-default';
OUT: 'out';
OUTSIDE: 'outside';
PASSIVE: 'passive';
PASSWORD: 'password';
PERMIT: 'permit';
POLICY_MAP: 'policy-map';
PORT_RANGE: 'port-range';
PPP: 'ppp';
PRECEDENCE: 'precedence';
PREFIX_LIST: 'prefix-list';
PREPEND: 'prepend';
PROXY_DNS: 'proxy-dns';
PSH: 'psh';
RANGE: 'range';
REDISTRIBUTE: 'redistribute';
REMOTE_AS: 'remote-as';
ROUTE: 'route';
ROUTER: 'router';
ROUTER_ID: 'router-id';
ROUTE_MAP: 'route-map';
RST: 'rst';
SECONDARY: 'secondary';
SEND_CAPABILITY: 'send-capability';
SEND_DEFAULT: 'send-default';
SEQUENCE_MODE: 'sequence-mode';
SERVICE: 'service';
SERVICE_POLICY: 'service-policy';
SET: 'set';
SHUTDOWN: 'shutdown';
SNMP_AGENT: 'snmp-agent';
SPORT: 'sport';
SRC: 'src';
SRC_DOMAIN: 'src-domain';
SSH_SERVER: 'ssh-server';
STATIC: 'static';
SUPPRESS_LOGGING: 'suppress-logging';
SYN: 'syn';
SYSLOG: 'syslog';
TAG: 'tag';
TCP: 'tcp';
TERMINAL: 'terminal';
TIMERS: 'timers';
TIMEZONE: 'timezone';
TOS: 'tos';
TPID: 'tpid';
TRANSLATION: 'translation';
TUNNEL: 'tunnel';
TYPE: 'type';
UDP: 'udp';
UFS_CACHE: 'ufs-cache';
UNICAST: 'unicast';
UPDATE_SOURCE: 'update-source';
URG: 'urg';
URL_LIST: 'url-list';
VRF: 'vrf';

// END keywords

// Lines that are not configuration. The sample configurations are terminal captures of
// `show running-config`, so they start with the login banner and the commands typed at the prompt
// (`host# configure`, `host(config)# show run`) and end with a bare prompt.
BANNER_LINE
:
  (
    'NEC Portable Internetwork Core Operating System Software'
    | 'Copyright'
    | 'Enter configuration commands'
  )
  {atLineStart()}?

  F_NonNewlineChar* ( F_Newline | EOF ) -> channel(HIDDEN)
;

PROMPT_LINE
:
  F_PromptName ( '(' ~[)\r\n]* ')' )? '#'
  {atLineStart()}?

  F_NonNewlineChar* ( F_Newline | EOF ) -> channel(HIDDEN)
;

// `!` separates blocks in `show running-config`; the header lines are `!` comments too.
COMMENT_LINE
:
  '!'
  {atLineStart()}?

  F_NonNewlineChar* ( F_Newline | EOF ) -> channel(HIDDEN)
;

// Leading whitespace of a command inside a mode.
INDENT
:
  F_Whitespace+
  {atLineStart()}?

;

STANDARD_COMMUNITY: F_Digit+ ':' F_Digit+;

IP_PREFIX: F_Ipv4Prefix;
IP_ADDRESS: F_Ipv4Address;

UINT: F_Digit+;

WORD: F_NonWhitespaceChar+;

NEWLINE: F_Newline;
WS: F_Whitespace+ -> channel(HIDDEN);

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
F_PromptName
:
  [A-Za-z0-9] [A-Za-z0-9._-]*
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
