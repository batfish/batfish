parser grammar CiscoXr_community_set;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

community_set_stanza
:
   COMMUNITY_SET name = community_set_name NEWLINE
   community_set_elem_list END_SET NEWLINE
;

community_set_elem_list
:
   // no elements
   | elems += community_set_elem (COMMA elems += community_set_elem)*
;

community_set_elem
:
  ASTERISK
  | literal_community
  | hi = community_set_elem_half COLON lo = community_set_elem_half
  | DFA_REGEX COMMUNITY_SET_REGEX
  | IOS_REGEX COMMUNITY_SET_REGEX
;

community_set_elem_half
:
  ASTERISK
  | value = uint16
  | BRACKET_LEFT first = uint16 DOTDOT last = uint16 BRACKET_RIGHT
  | PRIVATE_AS
;

