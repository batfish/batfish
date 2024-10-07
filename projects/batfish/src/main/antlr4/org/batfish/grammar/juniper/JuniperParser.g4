parser grammar JuniperParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = JuniperLexer;
}

braced_clause
:
  OPEN_BRACE statement*
;

bracketed_clause
:
  OPEN_BRACKET word+
;

juniper_configuration
:
  (statement | extra_close_brace)+ MULTILINE_COMMENT? EOF
;

extra_close_brace: CLOSE_BRACE;

statement
:
  empty_statement
  | flat_statement
  | hierarchical_statement
;

flat_statement
:
  START_FLAT_LINE words += word+ NEWLINE
;

hierarchical_statement
:
  descriptive_comment += MULTILINE_COMMENT*
  tag* words += word+
  (
    braced_clause MULTILINE_COMMENT* close = CLOSE_BRACE
    | bracketed_clause close = CLOSE_BRACKET terminator
    | terminator
  )
;

empty_statement
:
   (
      MULTILINE_COMMENT
   )*
   SEMICOLON
;

terminator
:
  SEMICOLON
  | {_input.LA(1) == CLOSE_BRACKET || _input.LA(1) == CLOSE_BRACE}?
;

word
:
  WORD
;

tag
:
  ACTIVE
  | DELETE
  | INACTIVE
  | REPLACE
;
