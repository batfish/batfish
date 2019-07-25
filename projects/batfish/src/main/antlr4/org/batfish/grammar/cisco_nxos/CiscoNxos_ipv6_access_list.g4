parser grammar CiscoNxos_ipv6_access_list;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

ipv6_access_list
:
  ACCESS_LIST name = ip_access_list_name NEWLINE acl6_null*
;

acl6_null
:
  (
    uint32
    | DENY
    | FRAGMENTS
    | PERMIT
    | REMARK
    | STATISTICS
  ) null_rest_of_line
;
