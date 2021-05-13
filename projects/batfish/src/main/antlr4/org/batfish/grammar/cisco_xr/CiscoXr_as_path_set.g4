parser grammar CiscoXr_as_path_set;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_as_path_set
:
  AS_PATH_SET name = as_path_set_name NEWLINE
  as_path_set_elem_list END_SET NEWLINE
;

as_path_set_elem_list
:
   // no elements
   | elems += as_path_set_elem (COMMA elems += as_path_set_elem)*
;

as_path_set_elem
:
  NEWLINE*
  (
    aspse_dfa_regex
    | aspse_ios_regex
    | aspse_length
    | aspse_neighbor_is
    | aspse_originates_from
    | aspse_passes_through
    | aspse_unique_length
  )
  NEWLINE*
;

aspse_dfa_regex: DFA_REGEX as_path_regex;

aspse_ios_regex: IOS_REGEX as_path_regex;

aspse_length: LENGTH comparator as_path_length ALL?;

aspse_neighbor_is: NEIGHBOR_IS as_range_list EXACT?;

aspse_originates_from: ORIGINATES_FROM as_range_list EXACT?;

aspse_passes_through: PASSES_THROUGH as_range_list EXACT?;

aspse_unique_length: UNIQUE_LENGTH comparator as_path_length ALL?;

as_range_list: SINGLE_QUOTE as_range+ SINGLE_QUOTE;

as_range
:
  lo = as_number
  | BRACKET_LEFT lo = as_number DOTDOT hi = as_number BRACKET_RIGHT
;
