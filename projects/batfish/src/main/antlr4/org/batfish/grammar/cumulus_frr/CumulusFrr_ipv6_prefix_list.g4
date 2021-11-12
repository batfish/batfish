parser grammar CumulusFrr_ipv6_prefix_list;

import CumulusFrr_common;

options {
  tokenVocab = CumulusFrrLexer;
}

ipv6_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name pl6_line
;

pl6_line
:
  pl6_line_action
  | pl6_line_description
;

pl6_line_action
:
  (SEQ num = uint32)?
  action = line_action
  (
    (ANY NEWLINE)
    |
    (
      ip_prefix = prefix6
      (GE ge = ip_prefix_length)?
      (LE le = ip_prefix_length)?
      NEWLINE
    )
  )
;

pl6_line_description
:
  DESCRIPTION REMARK_TEXT NEWLINE
;
