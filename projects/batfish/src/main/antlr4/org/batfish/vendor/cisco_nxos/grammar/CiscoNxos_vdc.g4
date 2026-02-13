parser grammar CiscoNxos_vdc;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

vdc_id
:
// 1-4
  uint8
;

vdc_name
:
  WORD
;

s_vdc
:
  VDC name = vdc_name
  (
    ID id = vdc_id NEWLINE
    (
      vdc_allocate
      | vdc_allow
      | vdc_cpu_share
      | vdc_limit_resource_ignored
    )*
    | NEWLINE
  )
;

vdc_allocate
:
  ALLOCATE INTERFACE interface_range NEWLINE
;

vdc_allow
:
  ALLOW FEATURE_SET FEX NEWLINE
;

vdc_cpu_share
:
  CPU_SHARE cores = uint8 NEWLINE
;

vdc_limit_resource_ignored
:
  LIMIT_RESOURCE REMARK_TEXT NEWLINE
;