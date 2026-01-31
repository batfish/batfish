parser grammar CiscoNxos_ip_as_path_access_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ip_as_path_access_list
:
  AS_PATH ACCESS_LIST name = ip_as_path_access_list_name
  (
    SEQ seq = ip_as_path_access_list_seq
  )? action = line_action regex = as_path_regex NEWLINE
;

ip_as_path_access_list_seq
:
// 1-4294967294
  uint32
;

as_path_regex
:
// 1-63 chars
  dqs = double_quoted_string
;
