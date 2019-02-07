parser grammar F5BigipStructured_sys;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

sgs_hostname
:
  HOSTNAME hostname = word NEWLINE
;

sys_global_settings
:
  GLOBAL_SETTINGS BRACE_LEFT
  (
    NEWLINE
    (
      sgs_hostname
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

s_sys
:
  SYS
  (
    sys_global_settings
    | unrecognized
  )
;

