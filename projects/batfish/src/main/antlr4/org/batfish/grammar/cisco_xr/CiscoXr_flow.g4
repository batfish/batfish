parser grammar CiscoXr_flow;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_flow: FLOW (flow_exporter_map | flow_monitor_map);

flow_exporter_map: EXPORTER_MAP name = flow_exporter_map_name NEWLINE flow_exporter_map_inner*;

flow_exporter_map_inner
:
  fem_null
  | fem_version
;

fem_null
:
  (
    DESTINATION
    | DSCP
    | PACKET_LENGTH
    | SOURCE
    | TRANSPORT
  ) null_rest_of_line
;

fem_version: VERSION null_rest_of_line fem_version_inner*;

fem_version_inner
:
  femv_null
;

femv_null
:
  (
    OPTIONS
    | TEMPLATE
  ) null_rest_of_line
;

flow_monitor_map: MONITOR_MAP name = flow_monitor_map_name NEWLINE flow_monitor_map_inner*;

flow_monitor_map_inner
:
  fmm_exporter
  | fmm_null
;

fmm_exporter: EXPORTER name = flow_exporter_map_name NEWLINE;

fmm_null
:
  (
    CACHE
    | RECORD
  ) null_rest_of_line
;

s_sampler_map: SAMPLER_MAP name = sampler_map_name NEWLINE sampler_map_inner*;

sampler_map_inner: sm_null;

sm_null
:
  (
    RANDOM
  ) null_rest_of_line
;
