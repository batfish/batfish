lexer grammar FastpathLexer;

options {
  superClass = 'FastpathBaseLexer';
}

tokens {
  QUOTED_TEXT,
  REMAINDER,
  WORD,
  WORD_SEPARATOR
}

ALERT: 'alert';

BUFFERED: 'buffered';

CLI_COMMAND: 'cli-command';

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

LOGGING: 'logging';

LOOPBACK: 'loopback';

NAME: 'name';

NO: 'no';

NOTICE: 'notice';

PERSISTENT: 'persistent';

PORT: 'port';

PROMPT
:
  'prompt' -> pushMode ( M_Word )
;

RECONFIGURE: 'reconfigure';

REMOVE: 'remove';

SERVER: 'server';

SET: 'set';

SNTP: 'sntp';

SOURCE_INTERFACE: 'source-interface';

SYSLOG: 'syslog';

TRAPS: 'traps';

TUNNEL: 'tunnel';

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
  ) -> skip
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
  F_Whitespace+ -> skip
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

M_DoubleQuote_QUOTED_TEXT
:
  ( F_EscapedDoubleQuote | ~'"' )+ -> type ( QUOTED_TEXT )
;

mode M_Word;

M_Word_WS
:
  F_Whitespace+ -> type ( WORD_SEPARATOR ) , mode ( M_WordValue )
;

M_Word_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
;

mode M_WordValue;

M_WordValue_DOUBLE_QUOTE
:
  '"' -> type ( DOUBLE_QUOTE ) , pushMode ( M_DoubleQuote )
;

M_WordValue_WORD
:
  F_Word -> type ( WORD )
;

M_WordValue_WS
:
  F_Whitespace+ -> skip , popMode
;

M_WordValue_NEWLINE
:
  F_Newline -> type ( NEWLINE ) , popMode
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
