parser grammar NxosRoutingTableParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = NxosRoutingTableLexer;
}

@members {

}

double_quoted_string
:
   DOUBLE_QUOTE ID? DOUBLE_QUOTE
;

interface_name
:
   ~COMMA+
;

network
:
   IP_PREFIX COMMA UNICAST_MULTICAST_COUNT
   (
      COMMA ATTACHED
   )? NEWLINE route+
;

nxos_routing_table
:
   NEWLINE? vrf_routing_table+ EOF
;

protocol
:
   (
      BGP DASH DEC COMMA
      (
         EXTERNAL
         | INTERNAL
      )
   )
   | DIRECT
   | LOCAL
   |
   (
      OSPF DASH DEC COMMA
      (
         INTER
         | INTRA
         | TYPE_1
         | TYPE_2
      )
   )
   | STATIC
;

route
:
   ASTERISK VIA
   (
      nexthop = IP_ADDRESS
      | nexthopint = interface_name
   )
   (
      COMMA nexthopint = interface_name
   )? COMMA BRACKET_LEFT admin = DEC FORWARD_SLASH cost = DEC BRACKET_RIGHT
   COMMA ELAPSED_TIME COMMA protocol
   (
      COMMA TAG DEC
   )? NEWLINE
;

vrf_declaration
:
   VRF_HEADER double_quoted_string NEWLINE
;

vrf_routing_table
:
   vrf_declaration NEWLINE? network*
;
