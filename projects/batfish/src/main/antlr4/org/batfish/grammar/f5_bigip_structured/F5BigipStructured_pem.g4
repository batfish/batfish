parser grammar F5BigipStructured_pem;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

pem_global_settings
:
  GLOBAL_SETTINGS name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

s_pem
:
  PEM
  (
    pem_global_settings
    | unrecognized
  )
;
