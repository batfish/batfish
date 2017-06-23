parser grammar RoleParser;

options {
   superClass = 'org.batfish.grammar.BatfishParser';
   tokenVocab = RoleLexer;
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
