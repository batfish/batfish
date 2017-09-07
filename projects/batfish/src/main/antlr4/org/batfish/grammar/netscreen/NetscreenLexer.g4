lexer grammar NetscreenLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
boolean enableIP_ADDRESS = true;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   return sb.toString();
}

}

tokens {
   QUOTED_TEXT
}

// Netscreen Keywords

ACCOUNTING
:
   'accounting'
;

ADD
:
   'add'
;

ADD_DEFAULT_ROUTE
:
   'add-default-route'
;

ADDRESS
:
   'address'
;

ADMIN
:
   'admin'
;

AUTH
:
   'auth'
;

AUTH_SERVER
:
   'auth-server'
;

AUTO_ROUTE_EXPORT
:
   'auto-route-export'
;

BLOCK
:
    'block'
;

BYPASS_NON_IP
:
    'bypass-non-ip'
;

BYPASS_OTHERS_IPSEC
:
    'bypass-others-ipsec'
;

CLOCK
:
   'clock'
;

CONFIG
:
   'config'
;

CONSOLE
:
    'console'
;

DEFAULT
:
   'default'
;

DENY
:
   'deny'
;

DST_PORT
:
   'dst-port'
;

EXIT
:
   'exit'
;

HOST
:
   'host'
;

HOSTNAME
:
   'hostname'
;

IKE
:
   'ike'
;

INTERFACE
:
   'interface'
;

ID
:
   'id'
;

IP
:
   'ip'
;

IPSEC
:
   'ipsec'
;

FLOW
:
   'flow'
;

FORMAT
:
   'format'
;

FROM
:
   'from'
;

GROUP
:
   'group'
;

LOG
:
   'log'
;

MANAGEABLE
:
    'manageable'
;

MIP
:
    'mip'
;

NAT
:
    'nat'
;

NETMASK
:
    'netmask'
;

NSMGMT
:
   'nsmgmt'
;

NSRP
:
    'nsrp'
;

PASSWORD
:
   'password'
;

PERMIT
:
   'permit'
;

PKI
:
   'pki'
;

POLICY
:
   'policy'
;

PORT
:
   'port'
;

PROTOCOL
:
   'protocol'
;

RADIUS
:
   'radius'
;

ROUTE
:
   'route'
;

SCREEN
:
   'screen'
;

SERVER
:
   'server'
;

SERVER_NAME
:
   'server-name'
;

SERVICE
:
   'service'
;

SET
:
   'set'
;

SHARABLE
:
   'sharable'
;

SNMP
:
   'snmp'
;

SRC_PORT
:
   'src-port'
;

SSH
:
   'ssh'
;

TCP
:
   'tcp'
;

TCP_RST
:
    'tcp-rst'
;

TIMEZONE
:
    'timezone'
;

TO
:
    'to'
;

UDP
:
   'udp'
;

URL
:
   'url'
;

UNSET
:
   'unset'
;

VR
:
   'vr'
;

VROUTER
:
   'vrouter'
;

ZONE
:
   'zone'
;

VARIABLE
:
   (
      'a' .. 'z'
      | 'A' .. 'Z'
   )
   (
       '-'
       | '_'
       | '0' .. '9'
       | 'a' .. 'z'
       | 'A' .. 'Z'
   )*
   (
      'a' .. 'z'
      | 'A' .. 'Z'
      | '0' .. '9'
   )
;

// Other tokens

DASH
:
    '-'
;

DEC
:
   F_Digit+
;

DOUBLE_QUOTE
:
   '"' -> pushMode ( M_QuotedString )
;

IP_ADDRESS
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte
;

IP_PREFIX
:
   F_DecByte '.'
   {enableIP_ADDRESS}?

   F_DecByte '.' F_DecByte '.' F_DecByte '/' F_Digit F_Digit?
;

NEWLINE
:
   F_Newline+
   {enableIP_ADDRESS = true;}
;


WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

fragment
F_DecByte
:
   (
      F_PositiveDigit F_Digit F_Digit
   )
   |
   (
      F_PositiveDigit F_Digit
   )
   | F_Digit
;

fragment
F_Digit
:
   '0' .. '9'
;

fragment
F_Newline
:
   [\r\n]+
;

fragment
F_NonNewline
:
   ~[\r\n]
;

fragment
F_PositiveDigit
:
   '1' .. '9'
;

fragment
F_Whitespace
:
   [ \n\t\u000C]
;

mode M_QuotedString;

M_QuotedString_QUOTED_TEXT
:
   ~'"'+ -> type ( QUOTED_TEXT )
;

M_QuotedString_DOUBLE_QUOTE
:
   '"' -> type ( DOUBLE_QUOTE ) , popMode
;
