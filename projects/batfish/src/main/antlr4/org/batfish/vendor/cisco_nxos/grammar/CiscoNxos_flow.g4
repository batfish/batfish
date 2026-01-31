parser grammar CiscoNxos_flow;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_flow
:
  FLOW
  (
    flow_exporter
    | flow_monitor
    | flow_record
  )
;

flow_exporter
:
  EXPORTER name = fe_name NEWLINE 
  (
    fe_null
    | fe_source
  )*
;

fe_name
:
  WORD
;

fe_null
:
  (
    DESCRIPTION
    | DESTINATION
    | TRANSPORT
    | VERSION
  ) null_rest_of_line
;

fe_source
:
  SOURCE iface = interface_name NEWLINE
;

flow_monitor
:
  MONITOR name = fm_name NEWLINE 
  (
    fm_exporter
    | fm_null
    | fm_record
  )*
;

fm_exporter
:
  EXPORTER exporter = fe_name NEWLINE
;

fm_name
:
  WORD
;

fm_null
:
  (
    DESCRIPTION
  ) null_rest_of_line
;

fm_record
:
  RECORD record = fr_name NEWLINE
;

flow_record
:
  RECORD name = fr_name NEWLINE 
  (
    fr_null
  )*
;

fr_name
:
  WORD
;

fr_null
:
  (
    COLLECT 
    | DESCRIPTION
    | MATCH
  ) null_rest_of_line
;
