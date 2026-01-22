parser grammar F5BigipStructuredParser;

/* This is only needed if parser grammar is spread across files */
import
  F5BigipStructured_common,
  F5BigipStructured_cm,
  F5BigipStructured_ltm,
  F5BigipStructured_net,
  F5BigipStructured_sys;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = F5BigipStructuredLexer;
}

// goal rule

f5_bigip_structured_configuration
:
  NEWLINE?
  (
    statement
  )+ imish_chunk? EOF
;

// other rules

imish_chunk
:
  IMISH_CHUNK
;

statement
:
  s_analytics
  | s_cm
  | s_ltm
  | s_net
  | s_security
  | s_sys
;

s_analytics
:
  ANALYTICS ignored
;

s_security
:
  SECURITY ignored
;
