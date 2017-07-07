lexer grammar EosRoutingTableLexer;

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

// Simple tokens

CODES
:
   'Codes'
;

GATEWAY
:
   'Gateway'
;

IS_DIRECTLY_CONNECTED
:
   'is directly connected'
;

NAME
:
   'name'
;

VIA
:
   'via'
;

VRF
:
   'VRF'
;

// Complex tokens

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
   '!' F_NonNewline+ F_Newline+ -> channel ( HIDDEN )
;

DASH
:
   '-'
;

DEC
:
   F_Digit+
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

WORD
:
   [A-Za-z0-9]+
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

