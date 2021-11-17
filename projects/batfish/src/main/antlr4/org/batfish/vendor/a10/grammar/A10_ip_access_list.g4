parser grammar A10_ip_access_list;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

si_access_list: ACCESS_LIST access_list_name NEWLINE sial_rule_definition*;

sial_rule_definition
:
   sialr_action sialr_protocol
   source = access_list_address
   destination = access_list_address (dest_range = access_list_port_range)?
   NEWLINE
;

sialr_action: PERMIT | DENY;

sialr_protocol: ICMP | IP | TCP | UDP;

access_list_address: access_list_address_any | access_list_address_host;

access_list_address_any: ANY;

access_list_address_host: HOST address = ip_address;

access_list_port_range: RANGE from = acl_port_number to = acl_port_number;
