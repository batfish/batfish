parser grammar CumulusFrr_ip_community_list;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_community_list
:
  COMMUNITY_LIST
  (
    icl_expanded
    | icl_standard
  )
;

icl_expanded
:
  EXPANDED name = ip_community_list_name
  action = line_action
  (
    quoted = double_quoted_string
    | regex = REMARK_TEXT
  ) NEWLINE
;

icl_standard
:
  STANDARD name = ip_community_list_name
  action = line_action communities += standard_community+ NEWLINE
;

