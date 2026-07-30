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
   | extcommunity_set_rt_elem_colon_la32
;

extcommunity_set_rt_elem_as_dot_colon
:
  high = extcommunity_set_rt_elem_16 PERIOD middle =
  extcommunity_set_rt_elem_16 COLON low = extcommunity_set_rt_elem_16
;

// RFC 4360 type 2: 4-byte global administrator, 2-byte local administrator.
extcommunity_set_rt_elem_colon
:
  high = extcommunity_set_rt_elem_32 COLON low = extcommunity_set_rt_elem_16
;

// RFC 4360 type 0: 2-byte global administrator, 4-byte local administrator.
// Ambiguous with extcommunity_set_rt_elem_colon when both halves fit in 16
// bits; ANTLR resolves to that rule, which extracts to the same value. Only
// one administrator may be 4 bytes, so neither rule accepts two 32-bit halves.
extcommunity_set_rt_elem_colon_la32
:
  high = extcommunity_set_rt_elem_16 COLON low = extcommunity_set_rt_elem_32
;

extcommunity_set_rt_elem_16
:
  uint16
;

extcommunity_set_rt_elem_32
:
  uint32
;
