parser grammar AosCxParser;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = AosCxLexer;
}

aoscx_configuration
:
  (statement | NEWLINE)* EOF
;

statement
:
  s_hostname
  | null_statement
;

s_hostname
:
  HOSTNAME WORD NEWLINE
;

null_statement
:
  WORD+ NEWLINE
;
