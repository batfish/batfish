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
DECIMAL: [0-9]+;

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
fragment F_DecByte:
  [0-9]
  | [1-9][0-9]
  | '1'[0-9][0-9]
  | '2'[0-4][0-9]
  | '25'[0-5];
