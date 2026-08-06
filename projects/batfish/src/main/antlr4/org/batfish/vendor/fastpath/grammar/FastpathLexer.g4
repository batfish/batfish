lexer grammar FastpathLexer;

options {
  superClass = 'FastpathBaseLexer';
}

tokens {
  QUOTED_TEXT,
  REMAINDER,
  WORD
}

ALERT: 'alert';

BROADCAST: 'broadcast';

BUFFERED: 'buffered';

CLI_COMMAND: 'cli-command';

CLIENT: 'client';

CONSOLE: 'console';

CRITICAL: 'critical';

DEBUG: 'debug';

DNS: 'dns';

DOMAIN: 'domain';

EMAIL
:
  'email' -> pushMode ( M_Remainder )
;

EMERGENCY: 'emergency';

ERROR: 'error';

HOST: 'host';

HOSTNAME
:
  'hostname' -> pushMode ( M_Word )
;

INFO: 'info';

IP: 'ip';

IPV4: 'ipv4';

IPV6: 'ipv6';

LIST: 'list';

LOGGING: 'logging';

LOOKUP: 'lookup';

LOOPBACK: 'loopback';

MODE: 'mode';

NAME: 'name';

NO: 'no';

NOTICE: 'notice';

PERSISTENT: 'persistent';

POLL_INTERVAL: 'poll-interval';

POLL_RETRY: 'poll-retry';

PORT: 'port';

PROMPT
:
  'prompt' -> pushMode ( M_Word )
;

RECONFIGURE: 'reconfigure';

REMOVE: 'remove';

RETRY: 'retry';

SERVER: 'server';

SERVICEPORT: 'serviceport';

SET: 'set';

SNTP: 'sntp';

SOURCE_INTERFACE: 'source-interface';

STATUS
:
  'status' -> pushMode ( M_Remainder )
;

SYSLOG: 'syslog';

TIMEOUT: 'timeout';

TRAPS: 'traps';

TUNNEL: 'tunnel';

UNICAST: 'unicast';

VLAN: 'vlan';

WARNING: 'warning';

WRAP: 'wrap';

// Other Tokens

COMMENT_LINE
:
  (
    F_Whitespace
  )* '!'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewlineChar*
  (
    F_Newline
    | EOF
  ) -> channel ( HIDDEN )
;

DOUBLE_QUOTE
:
  '"' -> pushMode ( M_DoubleQuote )
;

FORWARD_SLASH: '/';

IP_ADDRESS
:
  F_IpAddress
;

NEWLINE
:
  F_Newline
;

UINT8
:
  F_Uint8
;

UINT16
:
  F_Uint16
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
F_PositiveDigit
:
  [1-9]
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
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
F_Newline
:
  F_NewlineChar ( F_Whitespace* F_NewlineChar+ )*
;

fragment
F_NewlineChar
:
  '\r' '\n'?
  | '\n'
;

fragment
F_NonNewlineChar
:
  ~[\r\n]
;

fragment
F_Whitespace
:
  [ \t\u000C]
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/<>]
  | '-'
;

fragment
F_EscapedDoubleQuote
:
  '\\"'
;

mode M_DoubleQuote;

M_DoubleQuote_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , popMode
;

M_DoubleQuote_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;

M_DoubleQuote_QUOTED_TEXT
:
  ( F_EscapedDoubleQuote | ~["\r\n] )+ -> type ( QUOTED_TEXT )
;

mode M_Word;

M_Word_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , pushMode ( M_DoubleQuote )
;

M_Word_WORD
:
  F_Word -> type ( WORD )
;

M_Word_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;

M_Word_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

mode M_Remainder;

M_Remainder_REMAINDER
:
  F_NonNewlineChar+ -> type ( REMAINDER )
;

M_Remainder_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;
