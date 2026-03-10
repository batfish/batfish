parser grammar CiscoNxos_ip_community_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
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
  (
    SEQ seq = ip_community_list_seq
  )? action = line_action
  (
    quoted = double_quoted_string
    | regex = REMARK_TEXT
  ) NEWLINE
;

icl_standard
:
  STANDARD name = ip_community_list_name
  (
    SEQ seq = ip_community_list_seq
  )? action = line_action communities += standard_community+ NEWLINE
;

ip_community_list_seq
:
// 1-4294967294
  uint32
;
