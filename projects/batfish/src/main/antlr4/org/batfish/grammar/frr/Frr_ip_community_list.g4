parser grammar Frr_ip_community_list;

import Frr_common;

options {
  tokenVocab = FrrLexer;
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

