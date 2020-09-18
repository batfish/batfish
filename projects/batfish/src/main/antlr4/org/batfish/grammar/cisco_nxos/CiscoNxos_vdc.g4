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
  VDC name = vdc_name ID id = vdc_id NEWLINE
  (
    vdc_allow
    | vdc_limit_resource
  )*
;

vdc_allow
:
  ALLOW FEATURE_SET FEX NEWLINE
;

vdc_limit_resource
:
  LIMIT_RESOURCE null_rest_of_line
;