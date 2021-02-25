lexer grammar IptablesLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
boolean enableIPV6_ADDRESS = true;
boolean enableIP_ADDRESS = true;
boolean enableDEC = true;

@Override
public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   sb.append("enableIPV6_ADDRESS: " + enableIPV6_ADDRESS + "\n");
   sb.append("enableIP_ADDRESS: " + enableIP_ADDRESS + "\n");
   sb.append("enableDEC: " + enableDEC + "\n");
   return sb.toString();
}

}

// Iptables Keywords

ACCEPT: 'ACCEPT';

AH: 'ah';

ALL: 'all';

COMMIT: 'COMMIT';

DROP: 'DROP';

ESP: 'esp';

FLAG_APPEND
:
   '-A'
   | '--append'
;

FLAG_CHECK
:
   '-C'
   | '--check'
;

FLAG_DELETE
:
   '-D'
   | '--delete'
;

FLAG_DELETE_CHAIN
:
   '-X'
   | '--delete-chain'
;

FLAG_FLUSH
:
   '-F'
   | '--flush'
;

FLAG_HELP: '-h';

FLAG_INSERT
:
   '-I'
   | '--insert'
;

FLAG_LIST
:
   '-L'
   | '--list'
;

FLAG_LIST_RULES
:
   '-S'
   | '--list-rules'
;

FLAG_NEW_CHAIN
:
   '-N'
   | '--new-chain'
;

FLAG_POLICY
:
   '-P'
   | '--policy'
;

FLAG_RENAME_CHAIN
:
   '-E'
   | '--rename-chain'
;

FLAG_REPLACE
:
   '-R'
   | '--replace'
;

FLAG_TABLE
:
   '-t'
   | '--table'
;

FLAG_ZERO
:
   '-Z'
   | '--zero'
;

FORWARD: 'FORWARD';

ICMP: 'icmp';

ICMPV6: 'icmpv6';

INPUT: 'INPUT';

IPTABLES: 'iptables';

MH: 'mh';

OUTPUT: 'OUTPUT';

OPTION_DESTINATION
:
   '-d'
   | '--dst'
   | '--destination'
;

OPTION_DESTINATION_PORT
:
   '--dport'
   | '--destination-port'
;

OPTION_GOTO
:
   '-g'
   | '--goto'
;

OPTION_IN_INTERFACE
:
   '-i'
   | '--in-interface'
;

OPTION_IPV4
:
   '-4'
   | '--ipv4'
;

OPTION_IPV6
:
   '-6'
   | '--ipv6'
;

OPTION_FRAGMENT
:
   '-f'
   | '--fragment'
;

OPTION_JUMP
:
   '-j'
   | '--jump'
;

OPTION_MATCH
:
   '-m'
   | '--match'
;

OPTION_OUT_INTERFACE
:
   '-o'
   | '--out-interface'
;

OPTION_PROTOCOL
:
   '-p'
   | '--protocol'
;

OPTION_SOURCE
:
   '-s'
   | '--src'
   | '--source'
;

OPTION_SOURCE_PORT
:
   '--sport'
   | '--source-port'
;

OPTION_VERBOSE
:
   '-v'
   | '--verbose'
;

POSTROUTING: 'POSTROUTING';

PREROUTING: 'PREROUTING';

RETURN: 'RETURN';

SCTP: 'sctp';

TABLE_FILTER: 'filter';

TABLE_MANGLE: 'mangle';

TABLE_NAT: 'nat';

TABLE_RAW: 'raw';

TABLE_SECURITY: 'security';

TCP: 'tcp';

UDP: 'udp';

UDPLITE: 'udplite';

// Other tokens

ASTERISK
:
   '*'
;

BRACKET_LEFT
:
   '['
;

BRACKET_RIGHT
:
   ']'
;

COLON
:
   ':'
;

DASH: '-';

DEC
:
   F_Digit
   {enableDEC}?

   F_Digit*
;

IP_ADDRESS
:
  F_IpAddress {enableIP_ADDRESS}?
;

IP_PREFIX
:
  F_IpPrefix {enableIP_ADDRESS}?
;

IPV6_ADDRESS
:
  F_Ipv6Address {enableIPV6_ADDRESS}?
;

IPV6_PREFIX
:
   F_Ipv6Prefix {enableIPV6_ADDRESS}?
;

LINE_COMMENT
:
   '#' F_NonNewlineChar* F_NewlineChar+
   {enableIPV6_ADDRESS = true;}

   -> channel ( HIDDEN )
;

NEWLINE
:
   F_NewlineChar+
   {
      enableIPV6_ADDRESS = true;
      enableIP_ADDRESS = true;
   }

;

NOT
:
   '!'
;

WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

VARIABLE
:
   F_Variable_RequiredVarChar F_Variable_VarChar*
;

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
F_Digit
:
  [0-9]
;

fragment
F_HexDigit
:
  [0-9A-Fa-f]
;

fragment
F_HexWord
:
  F_HexDigit F_HexDigit? F_HexDigit? F_HexDigit?
;

fragment
F_HexWord2
:
  F_HexWord ':' F_HexWord
;

fragment
F_HexWord3
:
  F_HexWord2 ':' F_HexWord
;

fragment
F_HexWord4
:
  F_HexWord3 ':' F_HexWord
;

fragment
F_HexWord5
:
  F_HexWord4 ':' F_HexWord
;

fragment
F_HexWord6
:
  F_HexWord5 ':' F_HexWord
;

fragment
F_HexWord7
:
  F_HexWord6 ':' F_HexWord
;

fragment
F_HexWord8
:
  F_HexWord6 ':' F_HexWordFinal2
;

fragment
F_HexWordFinal2
:
  F_HexWord2
  | F_IpAddress
;

fragment
F_HexWordFinal3
:
  F_HexWord ':' F_HexWordFinal2
;

fragment
F_HexWordFinal4
:
  F_HexWord ':' F_HexWordFinal3
;

fragment
F_HexWordFinal5
:
  F_HexWord ':' F_HexWordFinal4
;

fragment
F_HexWordFinal6
:
  F_HexWord ':' F_HexWordFinal5
;

fragment
F_HexWordFinal7
:
  F_HexWord ':' F_HexWordFinal6
;

fragment
F_HexWordLE1
:
  F_HexWord?
;

fragment
F_HexWordLE2
:
  F_HexWordLE1
  | F_HexWordFinal2
;

fragment
F_HexWordLE3
:
  F_HexWordLE2
  | F_HexWordFinal3
;

fragment
F_HexWordLE4
:
  F_HexWordLE3
  | F_HexWordFinal4
;

fragment
F_HexWordLE5
:
  F_HexWordLE4
  | F_HexWordFinal5
;

fragment
F_HexWordLE6
:
  F_HexWordLE5
  | F_HexWordFinal6
;

fragment
F_HexWordLE7
:
  F_HexWordLE6
  | F_HexWordFinal7
;

fragment
F_IpAddress
:
  F_DecByte '.' F_DecByte '.' F_DecByte '.' F_DecByte
;

fragment
F_IpPrefix
:
  F_IpAddress '/' F_IpPrefixLength
;

fragment
F_IpPrefixLength
:
  F_Digit
  | [12] F_Digit
  | [3] [012]
;

fragment
F_Ipv6Address
:
  '::' F_HexWordLE7
  | F_HexWord '::' F_HexWordLE6
  | F_HexWord2 '::' F_HexWordLE5
  | F_HexWord3 '::' F_HexWordLE4
  | F_HexWord4 '::' F_HexWordLE3
  | F_HexWord5 '::' F_HexWordLE2
  | F_HexWord6 '::' F_HexWordLE1
  | F_HexWord7 '::'
  | F_HexWord8
;

fragment
F_Ipv6Prefix
:
  F_Ipv6Address '/' F_Ipv6PrefixLength
;

fragment
F_Ipv6PrefixLength
:
  F_Digit
  | F_PositiveDigit F_Digit
  | '1' [01] F_Digit
  | '12' [0-8]
;

fragment
F_NewlineChar
:
   [\r\n]
;

fragment
F_NonNewlineChar
:
   ~[\r\n]
;

fragment
F_PositiveDigit
:
   [1-9]
;

fragment
F_Variable_RequiredVarChar
:
   ~[ 0-9\t\n\r/.,\-;{}<>[\]&|()"'*:]
;

fragment
F_Variable_VarChar
:
   ~[ \t\n\r;{}[\]&|()"'*:/,]
;

fragment
F_Whitespace
:
   ' '
   | '\t'
   | '\u000C'
;
