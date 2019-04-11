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

sys_ntp
:
  NTP BRACE_LEFT
  (
    NEWLINE
    (
      ntp_servers
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ntp_servers
:
  SERVERS BRACE_LEFT servers += word* BRACE_RIGHT NEWLINE
;

s_sys
:
  SYS
  (
    sys_global_settings
    | sys_ntp
    | unrecognized
  )
;

