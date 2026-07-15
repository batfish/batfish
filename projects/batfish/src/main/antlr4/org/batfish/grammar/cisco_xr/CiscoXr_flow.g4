parser grammar CiscoXr_flow;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_flow: FLOW (flow_exporter_map | flow_monitor_map);

flow_exporter_map: EXPORTER_MAP name = flow_exporter_map_name NEWLINE flow_exporter_map_inner*;

flow_exporter_map_inner
:
  fem_destination_null
  | fem_dscp_null
  | fem_packet_length_null
  | fem_source_null
  | fem_transport_null
  | fem_version
;

fem_destination_null
:
   DESTINATION null_rest_of_line
;
fem_dscp_null
:
   DSCP null_rest_of_line
;
fem_packet_length_null
:
   PACKET_LENGTH null_rest_of_line
;
fem_source_null
:
   SOURCE null_rest_of_line
;
fem_transport_null
:
   TRANSPORT null_rest_of_line
;

fem_version: VERSION null_rest_of_line fem_version_inner*;

fem_version_inner
:
  femv_options_null
  | femv_template_null
;

femv_options_null
:
   OPTIONS null_rest_of_line
;
femv_template_null
:
   TEMPLATE null_rest_of_line
;

flow_monitor_map: MONITOR_MAP name = flow_monitor_map_name NEWLINE flow_monitor_map_inner*;

flow_monitor_map_inner
:
  fmm_cache_null
  | fmm_exporter
  | fmm_record_null
;

fmm_exporter: EXPORTER name = flow_exporter_map_name NEWLINE;

fmm_cache_null
:
   CACHE null_rest_of_line
;
fmm_record_null
:
   RECORD null_rest_of_line
;

s_sampler_map: SAMPLER_MAP name = sampler_map_name NEWLINE sampler_map_inner*;

sampler_map_inner: sm_null;

sm_null
:
  (
    RANDOM
  ) null_rest_of_line
;
