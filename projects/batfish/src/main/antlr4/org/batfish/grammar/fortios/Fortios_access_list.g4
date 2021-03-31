parser grammar Fortios_access_list;

options {
  tokenVocab = FortiosLexer;
}

cr_access_list: ACCESS_LIST newline cral_edit*;

cral_edit: EDIT access_list_name newline crale* NEXT newline;

crale: crale_set | crale_config;

crale_set: SET crale_set_comments;

crale_set_comments: COMMENTS comment = str newline;

crale_config: CONFIG cralec_rule;

cralec_rule: RULE newline cralecr_edit* END newline;

cralecr_edit: EDIT acl_rule_number newline cralecre* NEXT newline;

cralecre: cralecre_set;

cralecre_set
:
    SET (
        cralecre_set_action
        | cralecre_set_exact_match
        | cralecre_set_prefix
        | cralecre_set_wildcard
    )
;

cralecre_set_action: ACTION permit_or_deny newline;

cralecre_set_exact_match: EXACT_MATCH enable_or_disable newline;

cralecre_set_prefix: PREFIX ip_address_with_mask_or_prefix_or_any newline;

cralecre_set_wildcard: WILDCARD ip_wildcard newline;

// 0-4294967295
acl_rule_number: str;
