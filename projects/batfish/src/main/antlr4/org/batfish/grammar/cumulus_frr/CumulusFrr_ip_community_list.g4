parser grammar CumulusFrr_ip_community_list;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_community_list
:
  COMMUNITY_LIST icl_expanded
;

icl_expanded
:
  EXPANDED name = ip_community_list_name
  (
    SEQ seq = ip_community_list_seq
  )? action = line_action
  (
    quoted = double_quoted_string
    | regex = REMARK_TEXT
  ) NEWLINE
;

ip_community_list_seq
:
// 1-4294967294
  uint32
;

double_quoted_string
:
  DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE
;

quoted_text
:
  QUOTED_TEXT
;