parser grammar F5BigipStructured_ltm_data_group;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

l_data_group
:
  DATA_GROUP
  (
    ldg_external
    | ldg_internal
  )
;

ldg_external
:
  EXTERNAL name = structure_name BRACE_LEFT
  (
    NEWLINE ldg_common*
  )? BRACE_RIGHT NEWLINE
;

ldg_internal
:
  INTERNAL name = structure_name BRACE_LEFT
  (
    NEWLINE ldg_common*
  )? BRACE_RIGHT NEWLINE
;

ldg_common
:
  ignored
;
