parser grammar F5BigipStructured_ltm_rule;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

l_rule
:
  RULE_SPECIAL name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      proc
      | when
    )*
  )? BRACE_RIGHT NEWLINE
;

proc
:
  PROC name = CHARS BRACE_LEFT
  (
    args += CHARS
  )* BRACE_RIGHT BRACE_LEFT
  (
    NEWLINE command_sequence?
  )? BRACE_RIGHT NEWLINE
;

when
:
  WHEN w_event BRACE_LEFT
  (
    NEWLINE command_sequence?
  )? BRACE_RIGHT NEWLINE
;

w_event
:
  EVENT
;

command_sequence
:
  whitespace? command_separator*
  (
    commands += command command_separator+
  )* commands += command whitespace? command_separator*
;

command
:
  words += i_word
  (
    WS words += i_word
  )*
;

command_separator
:
  whitespace?
  (
    SEMICOLON
    | NEWLINE
  ) whitespace?
;

whitespace
:
  (COMMENT | WS)+
;

command_substitution
:
  BRACKET_LEFT command_sequence? BRACKET_RIGHT
;

variable_substitution
:
  DOLLAR
  (
    ivs_braces
    | ivs_name
  )
;

ivs_braces
:
  BRACE_LEFT braced_variable_name BRACE_RIGHT
;

braced_variable_name
:
  CHARS
;

ivs_name
:
  scalar_variable_name index = variable_index?
;

scalar_variable_name
:
  CHARS
;

variable_index
:
  PAREN_LEFT i_word PAREN_RIGHT
;

i_word
:
  segments+=iw_segment+
;

iw_segment
:
  iws_braces
  | iws_chars
  | command_substitution
  | iws_double_quotes
  | backslash_substitution
  | variable_substitution
;

iws_braces
:
  BRACE_LEFT iwsb_segment+ BRACE_RIGHT
;

iwsb_segment
:
  iws_braces
  | iwsbs_chars
  | iwsbs_escape_sequence
;

iwsbs_chars
:
  CHARS
;

iwsbs_escape_sequence
:
  BACKSLASH_NEWLINE_WS
;

iws_chars
:
  CHARS
;

iws_double_quotes
:
  DOUBLE_QUOTE iwsd_segment* DOUBLE_QUOTE
;

iwsd_segment
:
  iwsds_chars
  | command_substitution
  | backslash_substitution
  | variable_substitution
  // may occur when breaking out of nested lexer mode
  | NEWLINE
  | WS
;

iwsds_chars
:
  CHARS
;

backslash_substitution
:
  BACKSLASH_CARRIAGE_RETURN
  | BACKSLASH_CHAR
  | BACKSLASH_NEWLINE
  | BACKSLASH_NEWLINE_WS
;
