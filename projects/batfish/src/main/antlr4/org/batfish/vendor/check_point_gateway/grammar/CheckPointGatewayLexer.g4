lexer grammar CheckPointGatewayLexer;

options {
   superClass = 'CheckPointGatewayBaseLexer';
}

tokens {
  DOUBLE_QUOTE,
  QUOTED_TEXT,
  SINGLE_QUOTE,
  STR_SEPARATOR,
  WORD
}

// CheckPointGateway keywords
ACTIVE_BACKUP: 'active-backup';
ADD: 'add';
ADDRESS: 'address';
AUTO_NEGOTIATION: 'auto-negotiation';
BLACKHOLE: 'blackhole';
BONDING: 'bonding';
COMMENT: 'comment' -> pushMode(M_SingleStr);
COMMENTS: 'comments' -> pushMode(M_SingleStr);
DEFAULT: 'default';
FAST: 'fast';
FORCE: 'force';
GATEWAY: 'gateway';
GROUP: 'group';
HOSTNAME: 'hostname' -> pushMode(M_SingleStr);
INTERFACE: 'interface' -> pushMode(M_SingleStr);
IPV4_ADDRESS: 'ipv4-address';
LACP_RATE: 'lacp-rate';
LAYER2: 'layer2';
LAYER3_4: 'layer3+4';
LINK_SPEED: 'link-speed';
LOGICAL: 'logical' -> pushMode(M_SingleStr);
MASK_LENGTH: 'mask-length';
MODE: 'mode';
MTU: 'mtu';
NEXTHOP: 'nexthop';
OFF: 'off';
ON: 'on';
PRIORITY: 'priority';
REJECT: 'reject';
ROUND_ROBIN: 'round-robin';
SET: 'set';
SLOW: 'slow';
STATE: 'state';
STATIC_ROUTE: 'static-route';
SUBNET_MASK: 'subnet-mask';
VLAN: 'vlan';
XMIT_HASH_POLICY: 'xmit-hash-policy';
XOR: 'xor';

// Numeric tokens
EIGHT_ZERO_TWO_THREE_AD: '8023AD';
HUNDRED_M_FULL: '100M/full';
HUNDRED_M_HALF: '100M/half';
TEN_M_FULL: '10M/full';
TEN_M_HALF: '10M/half';
THOUSAND_M_FULL: '1000M/full';

// Complex tokens
COMMENT_LINE
:
  F_Whitespace* '#'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_Newline | EOF) -> channel(HIDDEN)
;

IP_ADDRESS: F_IpAddress;

IP_PREFIX: F_IpPrefix;

NEWLINE: F_Newline+;

UINT8: F_Uint8;

UINT16: F_Uint16;

UINT32: F_Uint32;

WS: F_Whitespace+ -> channel(HIDDEN);

// Fragments

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
F_Digit: [0-9];

fragment
F_IpAddress: F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte;

fragment
F_IpPrefix: F_IpAddress '/' F_IpPrefixLength;

fragment
F_IpPrefixLength
:
    F_Digit
    | [12] F_Digit
    | [3] [012]
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
F_PositiveDigit: [1-9];

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
F_EscapedDoubleQuote: '\\"';

fragment
F_EscapedSingleQuote: '\\' ['];

fragment
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_WordChar: ~( [ \t\u000C\u00A0\n\r(),!$'"*#] | '[' | ']' );

fragment
F_Word: F_WordChar+;

// Modes
mode M_DoubleQuote;

M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;

M_DoubleQuote_QUOTED_TEXT: (F_EscapedDoubleQuote | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;

M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;

M_SingleQuote_QUOTED_TEXT: (F_EscapedSingleQuote | ~['])+ -> type(QUOTED_TEXT);

mode M_SingleStr;

M_SingleStr_WS: F_Whitespace+ -> type(STR_SEPARATOR), mode(M_SingleStrValue);

M_SingleStr_NEWLINE: F_Newline -> type(NEWLINE), popMode;

mode M_SingleStrValue;

M_SingleStrValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);

M_SingleStrValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);

M_SingleStrValue_WORD: F_Word -> type(WORD);

M_SingleStrValue_WS: F_Whitespace+ -> skip, popMode;

M_SingleStrValue_NEWLINE: F_Newline -> type(NEWLINE), popMode;
