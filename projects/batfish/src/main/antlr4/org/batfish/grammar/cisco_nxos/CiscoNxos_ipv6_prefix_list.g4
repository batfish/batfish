parser grammar CiscoNxos_ipv6_prefix_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ipv6_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name
  (
    pl6_action
    | pl6_description
  )
;

pl6_action
:
  (
    SEQ num = ip_prefix_list_line_number
  )? action = line_action prefix = ipv6_prefix
  (
    EQ eq = ipv6_prefix_list_line_prefix_length
    | (GE ge = ipv6_prefix_list_line_prefix_length)? (LE le = ipv6_prefix_list_line_prefix_length)?
  )
  (
    MASK mask = ipv6_address
  )? NEWLINE
;

ipv6_prefix_list_line_prefix_length
:
// 1-128
  UINT8
;

pl6_description
:
  DESCRIPTION text = ip_prefix_list_description NEWLINE
;
