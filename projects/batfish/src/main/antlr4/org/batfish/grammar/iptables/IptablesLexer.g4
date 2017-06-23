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

ACCEPT
:
   'ACCEPT'
;

AH
:
   'ah'
;

ALL
:
   'all'
;

COMMIT
:
   'COMMIT'
;

DROP
:
   'DROP'
;

ESP
:
   'esp'
;

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

FLAG_HELP
:
   '-h'
;

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

FORWARD
:
   'FORWARD'
;

ICMP
:
   'icmp'
;

ICMPV6
:
   'icmpv6'
;

INPUT
:
   'INPUT'
;

IPTABLES
:
   'iptables'
;

MH
:
   'mh'
;

OUTPUT
:
   'OUTPUT'
;

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

POSTROUTING
:
   'POSTROTUING'
;

PREROUTING
:
   'PREROTUING'
;

RETURN
:
   'RETURN'
;

SCTP
:
   'sctp'
;

TABLE_FILTER
:
   'filter'
;

TABLE_MANGLE
:
   'mangle'
;

TABLE_NAT
:
   'nat'
;

TABLE_RAW
:
   'raw'
;

TABLE_SECURITY
:
   'security'
;

TCP
:
   'tcp'
;

UDP
:
   'udp'
;

UDPLITE
:
   'udplite'
;

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

DASH
:
   '-'
;

DEC
:
   F_Digit
   {enableDEC}?

   F_Digit*
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

IPV6_ADDRESS
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
   )
   (
      F_HexDigit+
   )?
   (
      F_Digit+ '.' F_Digit+ '.' F_Digit+ '.' F_Digit+
   )?
;

IPV6_PREFIX
:
   (
      (
         ':'
         {enableIPV6_ADDRESS}?

         ':'
         (
            (
               F_HexDigit+ ':'
            )* F_HexDigit+
         )?
      )
      |
      (
         F_HexDigit+
         {enableIPV6_ADDRESS}?

         ':' ':'?
      )+
   )
   (
      F_HexDigit+
   )?
   (
      F_Digit+ '.' F_Digit+ '.' F_Digit+ '.' F_Digit+
   )? '/' F_DecByte
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
: // TODO: This is a hack.

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
F_HexDigit
:
   [0-9a-fA-F]
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
