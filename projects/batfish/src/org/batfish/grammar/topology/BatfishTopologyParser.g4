parser grammar BatfishTopologyParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = BatfishTopologyLexer;
}

@header {
package org.batfish.grammar.topology;
}

topology
:
   HEADER NEWLINE
   (
      el += edge_line
      | NEWLINE
   )*
;

edge_line
:
   node1 = VARIABLE COLON int1 = VARIABLE COMMA node2 = VARIABLE COLON int2 =
   VARIABLE NEWLINE
;

