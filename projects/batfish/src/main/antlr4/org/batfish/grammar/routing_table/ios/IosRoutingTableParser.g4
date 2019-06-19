parser grammar IosRoutingTableParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = IosRoutingTableLexer;
}

@members {

}

code
:
   (
      code_parts +=
      (
         ASTERISK
         | PERCENT
         | PLUS
         | WORD
      )
   )+ DASH
   (
      description += WORD
      | description += DEC
   )+
;

codes_declaration
:
   CODES COLON code_line+
;

code_line
:
   (
      code COMMA
   )+ code NEWLINE
;

ios_routing_table
:
   NEWLINE? vrf_routing_table+ EOF
;

gateway_header
:
   GATEWAY WORD+ ~NEWLINE* NEWLINE
;

identifier
:
   ~NEWLINE+
;

info
:
   IP_PREFIX IS_VARIABLY_SUBNETTED ~NEWLINE* NEWLINE
;

protocol
:
   WORD+
;

route
:
   protocol IP_PREFIX
   (
      (
         (
            BRACKET_LEFT admin = DEC FORWARD_SLASH cost = DEC BRACKET_RIGHT VIA
            nexthops += IP_ADDRESS COMMA time
            (
               COMMA nexthopifaces += identifier
            )? NEWLINE
         )+
      )
      |
      (
         IS_DIRECTLY_CONNECTED COMMA nexthopifaces += identifier NEWLINE
      )
   )
;

time
:
   ~( COMMA | NEWLINE )*
;

vrf_declaration
:
   VRF NAME COLON identifier NEWLINE
;

vrf_routing_table
:
   vrf_declaration? codes_declaration gateway_header?
   (
      info
      | route
   )*
;
