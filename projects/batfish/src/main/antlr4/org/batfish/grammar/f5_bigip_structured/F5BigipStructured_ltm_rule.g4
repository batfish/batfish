parser grammar F5BigipStructured_ltm_rule;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

l_rule
:
  RULE name = structure_name BRACE_LEFT
  (
    NEWLINE lr_when*
  )? BRACE_RIGHT NEWLINE
;

lr_when
:
  WHEN event = word args += word* BRACE_LEFT
  (
    NEWLINE
    (
      ignored
    )*
  )? BRACE_RIGHT NEWLINE
;