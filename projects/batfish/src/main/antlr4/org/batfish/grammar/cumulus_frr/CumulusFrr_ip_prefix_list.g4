parser grammar CumulusFrr_ip_prefix_list;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ip_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name pl_line
;

pl_line
:
  SEQ num = ip_prefix_list_line_number
  action = line_action ip_prefix = prefix
  (GE ge = ip_prefix_list_line_prefix_length)? (LE le = ip_prefix_list_line_prefix_length)?
  NEWLINE
;

ip_prefix_list_line_number
:
// 1-4294967294
  UINT8
  | UINT16
  | UINT32
;

ip_prefix_list_line_prefix_length
:
// 1-32
  UINT8
;