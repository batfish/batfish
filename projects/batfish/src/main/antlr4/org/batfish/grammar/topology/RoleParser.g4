parser grammar RoleParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = RoleLexer;
}

@header {
package org.batfish.grammar.topology;
}

role_declarations
:
   HEADER NEWLINE
   (
      el += node_role_declarations_line
      | NEWLINE
   )* EOF
;

node_role_declarations_line
:
   node = VARIABLE COLON roles+= VARIABLE (COMMA roles+= VARIABLE)*
;
