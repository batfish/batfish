lexer grammar A10Lexer;

options {
   superClass = 'A10BaseLexer';
}

tokens {
  QUOTED_TEXT,
  RBA_LINE,
  RBA_TAIL,
  WORD,
  WORD_SEPARATOR
}

// A10 keywords
ACTIVE: 'active';
ADDRESS: 'address';
DISABLE: 'disable';
ENABLE: 'enable';
ETHERNET: 'ethernet';
HOSTNAME: 'hostname' -> pushMode(M_Word);
IP: 'ip';
INTERFACE: 'interface';
LACP: 'lacp';
LACP_UDLD: 'lacp-udld';
LONG: 'long';
LOOPBACK: 'loopback';
MODE: 'mode';
MTU: 'mtu';
NAME: 'name' -> pushMode(M_Word);
PASSIVE: 'passive';
RBA: 'rba' -> pushMode(M_Rba);
ROLE: 'role';
ROUTER_INTERFACE: 'router-interface';
STATIC: 'static';
SHORT: 'short';
TAGGED: 'tagged';
TIMEOUT: 'timeout';
TO: 'to';
TRUNK: 'trunk';
TRUNK_GROUP: 'trunk-group';
UNTAGGED: 'untagged';
USER_TAG: 'user-tag' -> pushMode(M_Word);
VE: 've';
VLAN: 'vlan';

// Complex tokens
COMMENT_LINE
:
  F_Whitespace* '!'
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?
  F_NonNewlineChar* (F_Newline | EOF) -> skip
;

DOUBLE_QUOTE
:
  '"' -> pushMode ( M_DoubleQuote )
;

SINGLE_QUOTE
:
  ['] -> pushMode ( M_SingleQuote )
;

SUBNET_MASK: F_SubnetMask;

IP_ADDRESS: F_IpAddress;

IP_SLASH_PREFIX: F_IpSlashPrefix;

NEWLINE: F_Newline+;

UINT8: F_Uint8;

UINT16: F_Uint16;

UINT32: F_Uint32;

WS: F_Whitespace+ -> skip;

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
F_IpSlashPrefix: '/' F_IpPrefixLength;

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
F_SubnetMask
:
  F_SubnetMaskOctet '.0.0.0'
  | '255.' F_SubnetMaskOctet '.0.0'
  | '255.255.' F_SubnetMaskOctet '.0'
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
F_Whitespace
:
    [ \t\u000C] // tab or space or unicode 0x000C
;

fragment
F_NonWhitespace
:
  ~[ \t\u000C\u00A0\n\r]
;

fragment
F_Word: F_WordChar+;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/]
  | '-'
;

fragment
F_StrChar: ~( [ \t\u000C\u00A0\n\r(),!$'"*#] | '[' | ']' );
fragment
F_Str: F_StrChar+;

fragment
F_EscapedDoubleQuote: '\\"';

fragment
F_EscapedSingleQuote: '\\' ['];

// Modes
mode M_DoubleQuote;
M_DoubleQuote_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), popMode;
M_DoubleQuote_QUOTED_TEXT: (F_EscapedDoubleQuote | ~'"')+ -> type(QUOTED_TEXT);

mode M_SingleQuote;
M_SingleQuote_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), popMode;
M_SingleQuote_QUOTED_TEXT: (F_EscapedSingleQuote | ~['])+ -> type(QUOTED_TEXT);

mode M_Word;
M_Word_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_WordValue);
M_Word_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;

mode M_WordValue;
M_WordValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_WordValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_WordValue_WORD: F_Word -> type(WORD);
M_WordValue_WS: F_Whitespace+ -> skip, popMode;
M_WordValue_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;

mode M_Rba;
M_Rba_WS: F_Whitespace+ -> skip;
M_Rba_ROLE: ROLE -> type(ROLE), mode(M_RbaRoleName);
M_Rba_NEWLINE: F_Newline+ -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaRoleName;
M_RbaRoleName_WS: F_Whitespace+ -> type(WORD_SEPARATOR), mode(M_RbaRoleNameValue);
M_RbaRoleName_NEWLINE: F_Newline+ -> type(NEWLINE), popMode;

mode M_RbaRoleNameValue;
M_RbaRoleNameValue_DOUBLE_QUOTE: '"' -> type(DOUBLE_QUOTE), pushMode(M_DoubleQuote);
M_RbaRoleNameValue_SINGLE_QUOTE: ['] -> type(SINGLE_QUOTE), pushMode(M_SingleQuote);
M_RbaRoleNameValue_WORD: F_Word -> type(WORD);
M_RbaRoleNameValue_WS: F_Whitespace+ -> skip, mode(M_RbaTail);
M_RbaRoleNameValue_NEWLINE: F_Newline+ -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaTail;
M_RbaTail_RBA_TAIL: F_NonNewlineChar+ -> type(RBA_TAIL);
M_RbaTail_NEWLINE: F_Newline+ -> type(NEWLINE), mode(M_RbaLine);

mode M_RbaLine;
M_RbaLine_RBA_LINE: F_Whitespace* F_Word F_Whitespace+ ('no-access'|'read'|'partition-only'|'oper'|'write') F_Newline+ -> type(RBA_LINE);
M_RbaLine_COMMENT_LINE: F_Whitespace* '!' F_NonNewlineChar* (F_Newline | EOF) -> skip;
M_RbaLine_END: F_NonWhitespace+ {less();} -> popMode;
