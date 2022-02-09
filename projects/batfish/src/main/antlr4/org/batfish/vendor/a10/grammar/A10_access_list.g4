parser grammar A10_access_list;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

s_access_list: ACCESS_LIST access_list_number sal_rule_definition NEWLINE;

// This only matches extended access lists (numbered 100-199).
// TODO: Support standard access lists.
sal_rule_definition
:
   access_list_action access_list_protocol
   source = sal_address
   destination = sal_address
;

sal_address
:
   access_list_address_any
   | access_list_address_host
   | ip_wildcard
;
