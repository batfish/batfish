parser grammar JuniperParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = JuniperLexer;
}

braced_clause
:
  OPEN_BRACE statement* CLOSE_BRACE
;

bracketed_clause
:
  OPEN_BRACKET word+ CLOSE_BRACKET
;

juniper_configuration
:
  statement+ EOF
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
  (
    INACTIVE
    | REPLACE
  )? words += word+
  (
    braced_clause
    | bracketed_clause terminator
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
