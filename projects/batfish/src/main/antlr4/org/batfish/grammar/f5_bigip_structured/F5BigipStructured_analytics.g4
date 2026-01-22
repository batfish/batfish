parser grammar F5BigipStructured_analytics;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_analytics
:
  ANALYTICS a_gui_widget
;

a_gui_widget
:
  GUI_WIDGET name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      agw_drilldown_entities
      | agw_drilldown_values
      | agw_metrics
      | agw_simple_value
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

agw_metrics
:
  METRICS BRACE_LEFT
  (
    value = word
  )*
  BRACE_RIGHT NEWLINE
;

agw_drilldown_entities
:
  DRILLDOWN_ENTITIES BRACE_LEFT
  (
    value = word
  )*
  BRACE_RIGHT NEWLINE
;

agw_drilldown_values
:
  DRILLDOWN_VALUES BRACE_LEFT
  (
    value = word
  )*
  BRACE_RIGHT NEWLINE
;

agw_simple_value
:
  (
    CENT_REPORT_DESTINATION_TYPE
    | CREATION_TIME
    | DESCRIPTION
    | GUI_PAGECODE
    | LAST_MODIFIED_TIME
    | MODULE
    | ORDER_ON_PAGE
    | PERIOD
    | USERNAME
    | VIEW_BY
    | WIDGET_TYPE
  ) ignored
;
