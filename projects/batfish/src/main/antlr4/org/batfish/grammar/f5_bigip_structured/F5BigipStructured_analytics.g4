parser grammar F5BigipStructured_analytics;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_analytics
:
  ANALYTICS
  (
    a_gui_widget
  )
;

a_gui_widget
:
  GUI_WIDGET name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      agw_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

agw_null
:
  (
    CENT_REPORT_DESTINATION_TYPE
    | CREATION_TIME
    | DESCRIPTION
    | DRILLDOWN_ENTITIES
    | DRILLDOWN_VALUES
    | GUI_PAGECODE
    | LAST_MODIFIED_TIME
    | METRICS
    | MODULE
    | ORDER_ON_PAGE
    | PERIOD
    | USERNAME
    | VIEW_BY
    | WIDGET_TYPE
  ) ignored
;
