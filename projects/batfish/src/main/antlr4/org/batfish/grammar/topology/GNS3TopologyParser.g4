parser grammar GNS3TopologyParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = GNS3TopologyLexer;
}

@header {
package org.batfish.grammar.topology;
}

topology
:
   (
      router_line
      | edge_line
      | ignored_line
   )*
;

router_line
:
   ROUTER_HEADING name=VARIABLE ROUTER_TAIL NEWLINE
;

edge_line
:
   int1 = INTERFACE EQUALS host2 = VARIABLE int2 = INTERFACE NEWLINE
;

ignored_line
:
   ~( ROUTER_HEADING | INTERFACE ) ~NEWLINE* NEWLINE
;
