parser grammar CiscoXr_extcommunity_set;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_extcommunity_set
:
  EXTCOMMUNITY_SET extcommunity_set_rt
;

extcommunity_set_rt
:
  RT name = variable NEWLINE
  lines = extcommunity_set_rt_elem_lines
  END_SET NEWLINE
;

// The lines can be:
// * empty (handled in "Comments only")
// * comments, anywhere
// * one or more communities
//
// If there are more than one community, every community but the last one
// has a comma at the end of the line. But comments may be intermixed.
extcommunity_set_rt_elem_lines
:
  // Comments only
  (hash_comment NEWLINE)*
  |
  // One or more communities, with comments mixed in
  (
    // leading comments or not-last elements
    ((hash_comment | elems += extcommunity_set_rt_elem COMMA) NEWLINE)*
    // last element
    elems += extcommunity_set_rt_elem NEWLINE
    // trailing comments
    (hash_comment NEWLINE)*
  )
;

extcommunity_set_rt_elem
:
   extcommunity_set_rt_elem_as_dot_colon
   | extcommunity_set_rt_elem_colon
;

extcommunity_set_rt_elem_as_dot_colon
:
  high = extcommunity_set_rt_elem_16 PERIOD middle =
  extcommunity_set_rt_elem_16 COLON low = extcommunity_set_rt_elem_16
;

extcommunity_set_rt_elem_colon
:
  high = extcommunity_set_rt_elem_32 COLON low = extcommunity_set_rt_elem_16
;

extcommunity_set_rt_elem_16
:
  uint16
;

extcommunity_set_rt_elem_32
:
  uint32
;
