lexer grammar CoolNosLexer;

options {
  superClass = 'org.batfish.grammar.cool_nos.parsing.CoolNosBaseLexer';
}

tokens {
  RIGHT_BRACKET,
  STRING
}

// BEGIN keywords
ADD: 'add';
DELETE: 'delete';
DISABLE: 'disable';
DISCARD: 'discard';
ENABLE: 'enable';
ETHERNET: 'ethernet';
GATEWAY: 'gateway';
HOST_NAME: 'host-name' -> pushMode(M_String);
INTERFACE: 'interface';
LINE: 'line';
LOG: 'log';
LOGIN_BANNER: 'login-banner';
MODIFY: 'modify';
STATIC_ROUTES: 'static-routes';
SYSLOG: 'syslog';
SYSTEM: 'system';
VLAN: 'vlan';
VTY: 'vty';

// END keywords

// BEGIN other tokens

COMMENT_LINE
:
  F_Whitespace* '!' F_NonNewline*
  (
    F_Newline
    | EOF
  )
  {lastTokenType() == NEWLINE || lastTokenType() == -1}? -> channel(HIDDEN)
;

DOUBLE_QUOTE: '"' -> pushMode(M_DoubleQuotedString);
LEFT_BRACKET: '[' -> pushMode(M_StringList);

IPV4_ADDRESS: F_Ipv4Address;
IPV4_PREFIX: F_Ipv4Prefix;

UINT8: F_Uint8;
UINT16: F_Uint16;

NEWLINE: F_Newline;
WS: F_Whitespace -> channel(HIDDEN);

// END other tokens

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
F_UnquotedStringChar
:
  F_Digit
  | [A-Za-z_.]
  | '-'
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
  | [3] [012]
;

fragment
F_Newline: '\n'+;

fragment
F_NonNewline: ~'\n';

fragment
F_Whitespace: ' '+;

mode M_String;
M_String_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_String_WS: F_Whitespace -> channel(HIDDEN);
M_String_UNQUOTED_STRING: F_UnquotedStringChar+ -> type(STRING), popMode;
M_String_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_DoubleQuotedString);

mode M_DoubleQuotedString;

M_DoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_DoubleQuotedString_STRING: F_NonNewline+ -> type(STRING);
M_DoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

mode M_StringList;

M_StringList_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringList_WS: F_Whitespace -> channel(HIDDEN);
M_StringList_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringListDoubleQuotedString);
M_StringList_UNQUOTED_STRING: F_UnquotedStringChar+ -> type(STRING);
M_StringLiteral_RIGHT_BRACKET: ']' -> type(RIGHT_BRACKET), popMode;

mode M_StringListDoubleQuotedString;

M_StringListDoubleQuotedString_NEWLINE: F_Newline -> type(NEWLINE), popMode;
M_StringListDoubleQuotedString_STRING: F_NonNewline+ -> type(STRING);
M_StringListDoubleQuotedString_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), mode(M_StringList);
