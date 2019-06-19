parser grammar F5BigipStructured_route_map;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

nr_route_map
:
  ROUTE_MAP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      nrr_entries
      | nrr_route_domain
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrr_entries
:
  ENTRIES BRACE_LEFT
  (
    NEWLINE nrre_entry*
  )? BRACE_RIGHT NEWLINE
;

nrre_entry
:
  num = uint32 BRACE_LEFT
  (
    NEWLINE
    (
      nrree_action
      | nrree_match
      | nrree_set
    )*
  )? BRACE_RIGHT NEWLINE
;

nrree_action
:
  ACTION action = route_map_action NEWLINE
;

nrree_match
:
  MATCH BRACE_LEFT
  (
    NEWLINE
    (
      nreem_ipv4
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nreem_ipv4
:
  IPV4 BRACE_LEFT
  (
    NEWLINE
    (
      nreem4_address
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nreem4_address
:
  ADDRESS BRACE_LEFT
  (
    NEWLINE
    (
      nreem4a_prefix_list
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nreem4a_prefix_list
:
  PREFIX_LIST name = structure_name NEWLINE
;

nrree_set
:
  SET BRACE_LEFT
  (
    NEWLINE
    (
      nrrees_community
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nrrees_community
:
  COMMUNITY BRACE_LEFT
  (
    NEWLINE
    (
      nreesc_value
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

nreesc_value
:
  VALUE BRACE_LEFT communities += standard_community+ BRACE_RIGHT NEWLINE
;

nrr_route_domain
:
  ROUTE_DOMAIN name = structure_name NEWLINE
;

route_map_action
:
  PERMIT
  | DENY
;

standard_community
:
  STANDARD_COMMUNITY
;
