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
    fe_description_null
    | fe_destination_null
    | fe_source
    | fe_transport_null
    | fe_version_null
  )*
;

fe_name
:
  WORD
;

fe_description_null
:
   DESCRIPTION null_rest_of_line
;
fe_destination_null
:
   DESTINATION null_rest_of_line
;
fe_transport_null
:
   TRANSPORT null_rest_of_line
;
fe_version_null
:
   VERSION null_rest_of_line
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
    ( fr_collect_null | fr_description_null | fr_match_null )
  )*
;

fr_name
:
  WORD
;

fr_collect_null
:
   COLLECT null_rest_of_line
;
fr_description_null
:
   DESCRIPTION null_rest_of_line
;
fr_match_null
:
   MATCH null_rest_of_line
;
