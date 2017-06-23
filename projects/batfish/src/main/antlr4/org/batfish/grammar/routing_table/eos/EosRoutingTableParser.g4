parser grammar EosRoutingTableParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = EosRoutingTableLexer;
}

@members {

}

code
:
   (
      code_parts += WORD
   )+ DASH
   (
      description += WORD
      | description += DEC
   )+
;

codes_declaration
:
   CODES COLON code
   (
      COMMA NEWLINE? code
   )+ NEWLINE
;

eos_routing_table
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

protocol
:
   WORD+
;

route
:
   protocol IP_PREFIX
   (
      (
         BRACKET_LEFT admin = DEC FORWARD_SLASH cost = DEC BRACKET_RIGHT
         (
            VIA nexthops += IP_ADDRESS COMMA nexthopifaces += identifier
            NEWLINE
         )+
      )
      |
      (
         IS_DIRECTLY_CONNECTED COMMA nexthopifaces += identifier NEWLINE
      )
   )
;

vrf_declaration
:
   VRF NAME COLON identifier NEWLINE
;

vrf_routing_table
:
   vrf_declaration? codes_declaration gateway_header? route*
;
