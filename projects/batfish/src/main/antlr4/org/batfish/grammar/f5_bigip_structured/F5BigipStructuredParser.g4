parser grammar F5BigipStructuredParser;

/* This is only needed if parser grammar is spread across files */
import F5BigipStructured_common, F5BigipStructured_net, F5BigipStructured_sys;

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
  )+ EOF
;

// other rules

statement
:
  s_net
  | s_sys
  | u
;

