parser grammar F5BigipStructured_prefix_list;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

nr_prefix_list
:
  PREFIX_LIST name = structure_name BRACE_LEFT
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

nrpe_entry
:
  num = uint32 BRACE_LEFT
  (
    NEWLINE
    (
      nrpee_action
      | nrpee_prefix
      | nrpee_prefix6
      | nrpee_prefix_len_range
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrpee_action
:
  ACTION action = prefix_list_action NEWLINE
;

prefix_list_action
:
  PERMIT
  | DENY
;

nrpee_prefix
:
  PREFIX prefix = ip_prefix NEWLINE
;

nrpee_prefix6
:
  PREFIX prefix6 = ipv6_prefix NEWLINE
;

nrpee_prefix_len_range
:
  PREFIX_LEN_RANGE range = prefix_len_range NEWLINE
;

prefix_len_range
:
// need to validate when bounds known during extraction
  STANDARD_COMMUNITY
;

nrp_route_domain
:
  ROUTE_DOMAIN name = structure_name NEWLINE
;
