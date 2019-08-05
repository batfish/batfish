lexer grammar CumulusInterfacesLexer;

options {
  superClass = 'org.batfish.grammar.cumulus_interfaces.parsing.CumulusInterfacesBaseLexer';
}

COMMENT_LINE
:
  (
    F_Whitespace
  )* [#]
  {lastTokenType() == NEWLINE || lastTokenType() == -1}?

  F_NonNewline*
  (
    F_Newline+
    | EOF
  ) -> channel ( HIDDEN )
;


NEWLINE
:
  F_Newline+
;

// Fragments

fragment
F_Newline
:
  [\n\r]
;

fragment
F_NonNewline
:
  ~[\n\r]
;

fragment
F_Whitespace
:
  ' '
  | '\t'
  | '\u000C'
  | '\u00A0'
;

