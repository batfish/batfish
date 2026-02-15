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
ROUTE_STATIC: 'route-static';
ROUTER_ID: 'router-id';
RULE: 'rule';
SHUTDOWN: 'shutdown';
SYSNAME: 'sysname';
UNDO: 'undo';
VLAN: 'vlan';
VPN_INSTANCE: 'vpn-instance';

// Literals - SUBNET_MASK must come before IP_ADDRESS since subnet masks are also valid IPs
SUBNET_MASK: F_SubnetMask;
IP_ADDRESS: F_IpAddress;
DECIMAL: [0-9]+;
WORD: [a-zA-Z0-9_.:/-]+;

// Punctuation
COMMA: ',';
MINUS: '-';

// Whitespace and newlines
WS: [ \t]+ -> skip;
NEWLINE: '\r'? '\n';

// Comments
COMMENT: ('#' | '!') ~[\r\n]* -> skip;

// Fragments
fragment F_IpAddress: F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte;
fragment F_SubnetMask:
  '0.0.0.0'
  | '128.0.0.0'
  | '192.0.0.0'
  | '224.0.0.0'
  | '240.0.0.0'
  | '248.0.0.0'
  | '252.0.0.0'
  | '254.0.0.0'
  | '255.0.0.0'
  | '255.128.0.0'
  | '255.192.0.0'
  | '255.224.0.0'
  | '255.240.0.0'
  | '255.248.0.0'
  | '255.252.0.0'
  | '255.254.0.0'
  | '255.255.0.0'
  | '255.255.128.0'
  | '255.255.192.0'
  | '255.255.224.0'
  | '255.255.240.0'
  | '255.255.248.0'
  | '255.255.252.0'
  | '255.255.254.0'
  | '255.255.255.0'
  | '255.255.255.128'
  | '255.255.255.192'
  | '255.255.255.224'
  | '255.255.255.240'
  | '255.255.255.248'
  | '255.255.255.252'
  | '255.255.255.254'
  | '255.255.255.255';
fragment F_DecByte:
  [0-9]
  | [1-9][0-9]
  | '1'[0-9][0-9]
  | '2'[0-4][0-9]
  | '25'[0-5];
