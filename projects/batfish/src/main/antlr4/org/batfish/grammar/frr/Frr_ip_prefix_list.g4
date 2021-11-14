parser grammar Frr_ip_prefix_list;

import Frr_common;

options {
  tokenVocab = FrrLexer;
}

ip_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name pl_line
;

pl_line
:
  pl_line_action
  | pl_line_description
;

pl_line_action
:
  (SEQ num = uint32)?
  action = line_action
  (
    (ANY NEWLINE)
    |
    (
      ip_prefix = prefix
      (GE ge = ip_prefix_length)?
      (LE le = ip_prefix_length)?
      NEWLINE
    )
  )
;

pl_line_description
:
  DESCRIPTION REMARK_TEXT NEWLINE
;