parser grammar CiscoXr_rd_set;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_rd_set
:
   RD_SET name = rd_set_name NEWLINE
   rd_set_elem_list END_SET NEWLINE
;

rd_set_elem_list
:
   // no elements
   | elems += rd_set_elem (COMMA elems += rd_set_elem)*
;

rd_set_elem
:
  NEWLINE*
  (
    rd_set_elem_asdot COLON lo16 = rd_set_elem_lo16
    | asplain_hi16 = uint16 COLON rd_set_elem_32
    | asplain_hi32 = rd_set_elem_32 COLON rd_set_elem_lo16
    | IP_PREFIX COLON rd_set_elem_lo16
    | IP_ADDRESS COLON rd_set_elem_lo16
    | DFA_REGEX COMMUNITY_SET_REGEX
    | IOS_REGEX COMMUNITY_SET_REGEX
  )
  NEWLINE*
;

rd_set_elem_asdot
:
  hi_wild = ASTERISK PERIOD lo_num = uint16
  | hi_num = pint16 PERIOD lo_wild = ASTERISK
  | hi_num = pint16 PERIOD lo_num = uint16
;

pint16
:
  // 1-65535
  uint16
;

rd_set_elem_32
:
  ASTERISK
  | uint32
;

rd_set_elem_lo16
:
 ASTERISK
 | uint16
;
