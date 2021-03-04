parser grammar Fortios_policy;

options {
  tokenVocab = FortiosLexer;
}

cf_policy: POLICY NEWLINE cfp_edit*;

cfp_edit
:
    EDIT policy_number NEWLINE (
        SET (cfp_set_singletons | cfp_set_lists)
        | APPEND cfp_append
        | SELECT cfp_set_lists
    )* NEXT NEWLINE
;

cfp_set_singletons
:
    cfp_set_action
    | cfp_set_comments
    | cfp_set_name
    | cfp_set_status
;

cfp_set_action: ACTION action = policy_action NEWLINE;

cfp_set_comments: COMMENTS comments = str NEWLINE;

cfp_set_name: NAME name = str NEWLINE;

cfp_set_status: STATUS status = enable_or_disable NEWLINE;

cfp_set_lists
:
    cfp_set_dstaddr
    | cfp_set_dstintf
    | cfp_set_service
    | cfp_set_srcaddr
    | cfp_set_srcintf
;

// TODO handle this and use cfp_append_... rules
cfp_append
:
    cfp_set_dstaddr
    | cfp_set_dstintf
    | cfp_set_service
    | cfp_set_srcaddr
    | cfp_set_srcintf
;

cfp_set_dstaddr: DSTADDR addresses = address_names NEWLINE;

cfp_set_srcaddr: SRCADDR addresses = address_names NEWLINE;

cfp_set_service: SERVICE services = service_names NEWLINE;

cfp_set_dstintf: DSTINTF interfaces = interface_names NEWLINE;

cfp_set_srcintf: SRCINTF interfaces = interface_names NEWLINE;

// 0-4294967294
policy_number: str;

policy_action: ALLOW | DENY;

address_names: address_name+;

// 1-79 characters
address_name: str;

service_names: service_name+;

interface_names: interface_name+;
