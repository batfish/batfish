parser grammar CiscoNxos_ip_community_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ip_community_list
:
  COMMUNITY_LIST icl_standard
;

icl_standard
:
  STANDARD name = ip_community_list_name
  (
    SEQ seq = ip_community_list_seq
  )? action = line_action communities += standard_community+ NEWLINE
;

ip_community_list_name
:
// 1-63 characters
  WORD
;

ip_community_list_seq
:
// 1-4294967294
  uint32
;
