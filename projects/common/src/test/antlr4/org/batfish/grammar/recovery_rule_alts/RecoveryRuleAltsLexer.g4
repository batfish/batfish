lexer grammar RecoveryRuleAltsLexer;

options {
  superClass = 'org.batfish.grammar.BatfishLexer';
}

tokens {
  WORD
}

// Keywords

ADDRESS: 'address';

COST: 'cost';

DNS: 'dns';

INTERFACE
:
  'interface' -> pushMode ( M_Word )
;

IP: 'ip';

MTU: 'mtu';

OSPF: 'ospf';

PERMIT: 'permit';

ROUTING: 'routing';

SSH: 'ssh';

// Complext tokens

IP_ADDRESS
:
  F_IpAddress
;

NEWLINE
:
  F_Newline
;

UINT32
:
  F_Uint32
;

WS
:
  F_Whitespace+ -> channel ( HIDDEN )
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
F_PositiveDigit
:
  [1-9]
;

fragment
F_Digit
:
  [0-9]
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
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

fragment
F_Newline
:
  [\n\r]
;

fragment
F_Word
:
  F_WordChar+
;

fragment
F_WordChar
:
  [0-9A-Za-z!@#$%^&*()_=+.;:{}/]
  | '-'
;

mode M_Word;

M_Word_NEWLINE
:
  F_Newline+ -> type ( NEWLINE ) , popMode
;

M_Word_WORD
:
  F_Word -> type ( WORD ) , popMode
;

M_Word_WS
:
  F_Whitespace+ -> channel ( HIDDEN )
;

