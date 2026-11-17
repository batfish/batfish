parser grammar CiscoNxos_ipv6_access_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ipv6_access_list
:
  ACCESS_LIST name = ip_access_list_name NEWLINE ipv6_access_list_inner*
;

ipv6_access_list_inner
:
  acl6_deny_null
  | acl6_fragments_null
  | acl6_num_null
  | acl6_permit_null
  | acl6_remark_null
  | acl6_statistics_null
;

acl6_deny_null: DENY null_rest_of_line;
acl6_fragments_null: FRAGMENTS null_rest_of_line;
acl6_num_null: uint32 null_rest_of_line;
acl6_permit_null: PERMIT null_rest_of_line;
acl6_remark_null: REMARK null_rest_of_line;
acl6_statistics_null: STATISTICS null_rest_of_line;
