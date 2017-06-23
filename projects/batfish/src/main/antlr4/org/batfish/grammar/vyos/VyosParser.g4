parser grammar VyosParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = VyosLexer;
}

@header {
package org.batfish.grammar.vyos;
}

braced_clause
:
   OPEN_BRACE NEWLINE statement* CLOSE_BRACE NEWLINE
;

statement
:
   word+
   (
      braced_clause
      | terminator
   )
;

terminator
:
   NEWLINE
;

vyos_configuration
:
   statement+ EOF
;

word
:
   WORD
;