parser grammar Frr_bgp_community_list;

import Frr_common;

options {
  tokenVocab = FrrLexer;
}

b_community_list
:
  COMMUNITY_LIST
  (
    bcl_expanded
    | bcl_standard
  )
;

bcl_expanded
:
  EXPANDED name = ip_community_list_name
  (SEQ seq = uint32)?
  action = line_action
  (
    quoted = double_quoted_string
    | regex = REMARK_TEXT
  ) NEWLINE
;

bcl_standard
:
  STANDARD name = ip_community_list_name
  (SEQ seq = uint32)?
  action = line_action communities += standard_community+ NEWLINE
;

