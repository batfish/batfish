parser grammar F5BigipStructured_prefix_list;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

nr_prefix_list
:
  PREFIX_LIST name = word BRACE_LEFT
  (
    NEWLINE
    (
      nrp_entries
      | nrp_route_domain
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrp_entries
:
  ENTRIES BRACE_LEFT
  (
    NEWLINE nrpe_entry*
  )? BRACE_RIGHT NEWLINE
;

nrp_route_domain
:
  ROUTE_DOMAIN name = word NEWLINE
;

nrpe_entry
:
  num = word BRACE_LEFT
  (
    NEWLINE
    (
      nrpee_action
      | nrpee_prefix
      | nrpee_prefix_len_range
    )*
  )? BRACE_RIGHT NEWLINE
;

nrpee_action
:
  ACTION action = prefix_list_action NEWLINE
;

nrpee_prefix
:
  PREFIX prefix = word NEWLINE
;

nrpee_prefix_len_range
:
  PREFIX_LEN_RANGE range = word NEWLINE
;

prefix_list_action
:
  PERMIT
  | DENY
;
