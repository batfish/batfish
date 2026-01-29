parser grammar F5BigipStructured_asm;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

s_asm
:
  (
    asm_policy
    | asm_predefined_policy
  )+
;

asm_policy
:
  ASM POLICY name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      asm_policy_active
      | asm_policy_blocking_mode
      | asm_policy_description
      | asm_policy_encoding
      | asm_policy_policy_builder
      | asm_policy_policy_template
      | asm_policy_policy_type
      | asm_policy_parent_policy
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

asm_predefined_policy
:
  ASM PREDEFINED_POLICY name = word BRACE_LEFT BRACE_RIGHT NEWLINE
;

// ASM policy properties

asm_policy_active
:
  ACTIVE NEWLINE
;

asm_policy_blocking_mode
:
  BLOCKING_MODE (
    ENABLED
    | DISABLED
  ) NEWLINE
;

asm_policy_description
:
  DESCRIPTION ignored
;

asm_policy_encoding
:
  ENCODING ignored
;

asm_policy_policy_builder
:
  POLICY_BUILDER (
    ENABLED
    | DISABLED
  ) NEWLINE
;

asm_policy_policy_template
:
  POLICY_TEMPLATE ignored
;

asm_policy_policy_type
:
  POLICY_TYPE ignored
;

asm_policy_parent_policy
:
  (
    PARENT_POLICY
    | PARENT
  ) ignored
;
