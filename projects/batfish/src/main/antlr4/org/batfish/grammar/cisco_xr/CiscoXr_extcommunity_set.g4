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
  RT name = variable NEWLINE extcommunity_set_rt_elem_list END_SET NEWLINE
;

extcommunity_set_rt_elem_list
:
// no elements

   |
   (
      (
         (
            elems += extcommunity_set_rt_elem COMMA
         )
         | hash_comment
      ) NEWLINE
   )*
   (
      elems += extcommunity_set_rt_elem
      | hash_comment
   ) NEWLINE
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
