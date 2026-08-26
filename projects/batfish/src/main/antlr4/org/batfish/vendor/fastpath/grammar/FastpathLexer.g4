lexer grammar FastpathLexer;

options {
  superClass = 'FastpathBaseLexer';
}

tokens {
  DEFAULT,
  QUOTED_TEXT,
  REMAINDER,
  WORD
}

AAA: 'aaa';

ACCOUNTING: 'accounting';

ALERT: 'alert';

AUTHENTICATION: 'authentication';

AUTHORIZATION: 'authorization';

BROADCAST: 'broadcast';

BUFFERED: 'buffered';

CLI_COMMAND: 'cli-command';

CLIENT: 'client';

COMMANDS
:
  'commands'
  {
    if (lastTokenType() == AUTHORIZATION || lastTokenType() == ACCOUNTING) {
      pushMode(M_AaaListName);
    }
  }
;

CONSOLE: 'console';

CRITICAL: 'critical';

DEBUG: 'debug';

DENY: 'deny';

DNS: 'dns';

DOMAIN: 'domain';

DOT1X
:
  'dot1x'
  {
    if (lastTokenType() == ACCOUNTING) {
      pushMode(M_AaaListName);
    }
  }
;

EMAIL
:
  'email' -> pushMode ( M_Remainder )
;

EMERGENCY: 'emergency';

ENABLE
:
  'enable'
  {
    if (lastTokenType() == AUTHENTICATION) {
      pushMode(M_AaaListName);
    }
  }
;

ENCRYPTED: 'encrypted';

ERROR: 'error';

EXEC
:
  'exec'
  {
    if (lastTokenType() == AUTHORIZATION || lastTokenType() == ACCOUNTING) {
      pushMode(M_AaaListName);
    }
  }
;

EXIT: 'exit';

HOST
:
  'host'
  {
    // `logging host <host>` / `tacacs-server host <host>`: the host (quoted, IP, or bare word)
    // follows in M_HostValue. `ip host ...` is consumed as a null rest-of-line in DEFAULT mode.
    if (lastTokenType() == LOGGING || lastTokenType() == TACACS_SERVER) {
      pushMode(M_HostValue);
    }
  }
;

HOSTNAME
:
  'hostname' -> pushMode ( M_Word )
;

IAS_USER: 'ias-user';

INFO: 'info';

IP: 'ip';

IPV4: 'ipv4';

IPV6: 'ipv6';

KEY
:
  'key' -> pushMode ( M_Key )
;

KEYSTRING: 'keystring';

LINE: 'line';

LIST: 'list';

LOCAL: 'local';

LOGGING: 'logging';

LOGIN
:
  'login'
  {
    if (lastTokenType() == AUTHENTICATION) {
      pushMode(M_AaaListName);
    }
  }
;

LOOKUP: 'lookup';

LOOPBACK: 'loopback';

MODE: 'mode';

NAME
:
  'name'
  {
    // `ip domain name <value>`: the domain follows in M_Word (quoted or bare word). Other uses
    // (`ip name server`, `ip name source-interface`) are followed by keywords in DEFAULT mode.
    if (lastTokenType() == DOMAIN) {
      pushMode(M_Word);
    }
  }
;

NO: 'no';

NONE: 'none';

NOTICE: 'notice';

PASSWORD
:
  'password' -> pushMode ( M_Remainder )
;

PERSISTENT: 'persistent';

POLL_INTERVAL: 'poll-interval';

POLL_RETRY: 'poll-retry';

PORT: 'port';

PRIORITY: 'priority';

PROMPT
:
  'prompt' -> pushMode ( M_Word )
;

RADIUS: 'radius';

RECONFIGURE: 'reconfigure';

REMOVE: 'remove';

RETRY: 'retry';

SERVER
:
  'server'
  {
    // `sntp server <host>`: the host (quoted, IP, or bare word) follows in M_HostValue.
    // `ip name server <ip>...` is followed by bare IP addresses in DEFAULT mode.
    if (lastTokenType() == SNTP) {
      pushMode(M_HostValue);
    }
  }
;

SERVICEPORT: 'serviceport';

SESSION_ID
:
  'session-id' -> pushMode ( M_Remainder )
;

SET: 'set';

SNTP: 'sntp';

SOURCE_INTERFACE: 'source-interface';

START_STOP: 'start-stop';

STATUS
:
  'status' -> pushMode ( M_Remainder )
;

STOP_ONLY: 'stop-only';

SYSLOG: 'syslog';

TACACS: 'tacacs';

TACACS_SERVER: 'tacacs-server';

TIMEOUT: 'timeout';

TRAPS: 'traps';

TUNNEL: 'tunnel';

UNICAST: 'unicast';

USERNAME
:
  'username' -> pushMode ( M_Word )
;

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
F_PositiveDigit
:
  [1-9]
;

fragment
F_FiveDigits
:
  F_Digit F_Digit F_Digit F_Digit F_Digit
;

fragment
F_IpAddress
:
  F_Uint8 '.' F_Uint8 '.' F_Uint8 '.' F_Uint8
;

fragment
F_Uint8
:
// 0-255
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5]
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
  | F_PositiveDigit F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit F_Digit F_FiveDigits
  | '4' [0-1] F_Digit F_Digit F_Digit F_FiveDigits
  | '42' [0-8] F_Digit F_Digit F_FiveDigits
  | '429' [0-3] F_Digit F_FiveDigits
  | '4294' [0-8] F_FiveDigits
  | '42949' [0-5] F_Digit F_Digit F_Digit F_Digit
  | '429496' [0-6] F_Digit F_Digit F_Digit
  | '4294967' [0-1] F_Digit F_Digit
  | '42949672' [0-8] F_Digit
  | '429496729' [0-5]
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
  '"' -> type ( DOUBLE_QUOTE ) , mode ( M_DoubleQuote )
;

M_Word_WORD
:
  F_Word -> type ( WORD ) , popMode
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

// Host value for `sntp server` / `logging host` / `tacacs-server host`: a quoted string, a bare IP,
// or a bare word (hostname). The keyword alternatives that can appear in this position instead of
// a host are recognized explicitly so they route to their own parser rules: `status` (sntp
// operational leakage) and `reconfigure`/`remove` (logging host maintenance). Emits exactly one
// token then returns to DEFAULT so any trailing tokens (priority/version/port, address-type,
// severity) lex normally.
mode M_HostValue;

M_HostValue_STATUS
:
  'status' -> type ( STATUS ) , mode ( M_Remainder )
;

M_HostValue_RECONFIGURE
:
  'reconfigure' -> type ( RECONFIGURE ) , popMode
;

M_HostValue_REMOVE
:
  'remove' -> type ( REMOVE ) , popMode
;

M_HostValue_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , mode ( M_DoubleQuote )
;

M_HostValue_IP_ADDRESS
:
  F_IpAddress -> type ( IP_ADDRESS ) , popMode
;

M_HostValue_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_HostValue_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

M_HostValue_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;

mode M_AaaListName;

M_AaaListName_DEFAULT
:
  'default' -> type ( DEFAULT ) , popMode
;

M_AaaListName_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , mode ( M_DoubleQuote )
;

M_AaaListName_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_AaaListName_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

M_AaaListName_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;

mode M_Key;

M_Key_ENCRYPTED
:
  'encrypted' -> type ( ENCRYPTED )
;

M_Key_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , mode ( M_DoubleQuote )
;

M_Key_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_Key_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

M_Key_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;
