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
  statement+ MULTILINE_COMMENT? EOF
;

statement
:
  flat_statement
  | hierarchical_statement
;

flat_statement
:
  START_FLAT_LINE words += word+ NEWLINE
;

hierarchical_statement
:
  MULTILINE_COMMENT?
  (
    INACTIVE
    | REPLACE
  )? words += word+
  (
    braced_clause close = CLOSE_BRACE
    | bracketed_clause close = CLOSE_BRACKET terminator
    | terminator
  )
;

terminator
:
  SEMICOLON
;

word
:
  WORD
;
