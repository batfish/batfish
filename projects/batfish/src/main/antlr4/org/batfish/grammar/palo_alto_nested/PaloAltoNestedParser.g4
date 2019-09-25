parser grammar PaloAltoNestedParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = PaloAltoNestedLexer;
}

braced_clause
:
  OPEN_BRACE statement* CLOSE_BRACE
;

bracketed_clause
:
  OPEN_BRACKET word+ CLOSE_BRACKET
;

palo_alto_nested_configuration
:
  statement+ EOF
;

statement
:
  words += word+
  (
    braced_clause
    |
    (
      bracketed_clause terminator
    )
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
