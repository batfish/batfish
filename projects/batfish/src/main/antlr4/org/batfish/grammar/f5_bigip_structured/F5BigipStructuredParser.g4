parser grammar F5BigipStructuredParser;

/* This is only needed if parser grammar is spread across files */
import
  F5BigipStructured_analytics,
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
  | s_apm
  | s_auth
  | s_cm
  | s_ilx
  | s_ltm
  | s_net
  | s_pem
  | s_security
  | s_sys
  | s_wom
;

s_security
:
  SECURITY ignored
;

s_apm
:
  APM ignored
;

s_auth
:
  AUTH ignored
;

s_ilx
:
  ILX ignored
;

s_pem
:
  PEM ignored
;

s_wom
:
  WOM ignored
;
