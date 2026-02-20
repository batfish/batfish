lexer grammar HuaweiLexer;

options {
  superClass = 'HuaweiBaseLexer';
}

// Keywords we need to recognize at line start
ACL: 'acl';
ADDRESS: 'address';
AREA: 'area';
AS_NUMBER: 'as-number';
BATCH: 'batch';
BGP: 'bgp';
DENY: 'deny';
DESCRIPTION: 'description';
INTERFACE: 'interface';
IP: 'ip';
LOOPBACK: 'LoopBack';
NETWORK: 'network';
OSPF: 'ospf';
PEER: 'peer';
PERMIT: 'permit';
QUIT: 'quit';
RETURN: 'return';
ROUTE_DISTINGUISHER: 'route-distinguisher';
ROUTE_STATIC: 'route-static';
ROUTER_ID: 'router-id';
RULE: 'rule';
SHUTDOWN: 'shutdown';
SYSNAME: 'sysname';
UNDO: 'undo';
VLAN: 'vlan';
VPN_INSTANCE: 'vpn-instance';

// Literals
IP_ADDRESS: F_IpAddress;
UINT8: F_Uint8;
UINT16: F_Uint16;
UINT32: F_Uint32;

// Punctuation
COMMA: ',';
MINUS: '-';

WORD: [a-zA-Z0-9_.:/-]+;

// Whitespace and newlines
WS: [ \t]+ -> skip;
NEWLINE: '\r'? '\n';

// Comments
COMMENT: ('#' | '!') ~[\r\n]* -> skip;

// Fragments
fragment F_IpAddress: F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte;
fragment F_Digit: [0-9];
fragment F_PositiveDigit: [1-9];
fragment F_Uint8:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' F_Digit F_Digit
  | '2' [0-4] F_Digit
  | '25' [0-5];
fragment F_Uint16:
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit?
  | [1-5] F_Digit F_Digit F_Digit F_Digit
  | '6' [0-4] F_Digit F_Digit F_Digit
  | '65' [0-4] F_Digit F_Digit
  | '655' [0-2] F_Digit
  | '6553' [0-5];
fragment F_Uint32:
  // 0-4294967295
  F_Digit
  | F_PositiveDigit F_Digit F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit? F_Digit?
  | [1-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '4' [0-1] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '429' [0-3] F_Digit F_Digit F_Digit F_Digit F_Digit F_Digit
  | '4294' [0-8] F_Digit F_Digit F_Digit F_Digit F_Digit
  | '42949' [0-5] F_Digit F_Digit F_Digit F_Digit
  | '429496' [0-6] F_Digit F_Digit F_Digit
  | '4294967' [0-1] F_Digit F_Digit
  | '42949672' [0-8] F_Digit
  | '429496729' [0-5];
fragment F_DecByte:
  [0-9]
  | [1-9][0-9]
  | '1'[0-9][0-9]
  | '2'[0-4][0-9]
  | '25'[0-5];
