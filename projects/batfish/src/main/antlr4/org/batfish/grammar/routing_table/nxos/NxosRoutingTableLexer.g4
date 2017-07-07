lexer grammar NxosRoutingTableLexer;

options {
   superClass = 'org.batfish.grammar.BatfishLexer';
}

@members {
private int lastTokenType = -1;

@Override
public void emit(Token token) {
    super.emit(token);
    if (token.getChannel() != HIDDEN) {
       lastTokenType = token.getType();
    }
}

public String printStateVariables() {
   StringBuilder sb = new StringBuilder();
   return sb.toString();
}

}

tokens {
   ID
}

// Simple tokens

ATTACHED
:
   'attached'
;

BGP
:
   'bgp'
;

DIRECT
:
   'direct'
;

ETH
:
   'Eth'
;

EXTERNAL
:
   'external'
;

INTER
:
   'inter'
;

INTERNAL
:
   'internal'
;

INTRA
:
   'intra'
;

LO
:
   'Lo'
;

LOCAL
:
   'local'
;

NULL
:
   'Null'
;

OSPF
:
   'ospf'
;

STATIC
:
   'static'
;

TAG
:
   'tag'
;

TYPE_1
:
   'type-1'
;

TYPE_2
:
   'type-2'
;

VIA
:
   'via'
;

VRF_HEADER
:
   'IP Route Table for VRF'
;

// Complex tokens

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

COMMA
:
   ','
;

COMMENT
:
   '\'' F_NonNewline+ F_Newline -> channel ( HIDDEN )
;

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
   '"' -> pushMode ( M_DoubleQuote )
;

ELAPSED_TIME
:
   F_Digit+ 'w' F_Digit 'd'
   | F_Digit 'd' F_Digit F_Digit? 'h'
   | F_Digit F_Digit ':' F_Digit F_Digit ':' F_Digit F_Digit
;

FORWARD_SLASH
:
   '/'
;

IP_ADDRESS
:
   F_IpAddress
;

IP_PREFIX
:
   F_IpAddress '/' F_Byte
;

NEWLINE
:
   F_Newline+
;

UNICAST_MULTICAST_COUNT
:
   'ubest/mbest: ' F_Digit+ '/' F_Digit+
;

WS
:
   F_Whitespace+ -> channel ( HIDDEN )
;

fragment
F_Byte
:
   F_Digit F_Digit F_Digit
   | F_Digit F_Digit
   | F_Digit
;

fragment
F_Digit
:
   [0-9]
;

fragment
F_IpAddress
:
   F_Byte '.' F_Byte '.' F_Byte '.' F_Byte
;

fragment
F_Newline
:
   [\r\n]
;

fragment
F_NonNewline
:
   ~[\r\n]
;

fragment
F_Whitespace
:
   [ \t]
;

mode M_DoubleQuote;

M_DoubleQuote_ID
:
   ~'"'+ -> type ( ID )
;

M_DoubleQuote_DOUBLE_QUOTE
:
   '"' -> type ( DOUBLE_QUOTE ) , popMode
;
