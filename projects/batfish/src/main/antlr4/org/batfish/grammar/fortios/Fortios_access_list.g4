parser grammar Fortios_access_list;

options {
  tokenVocab = FortiosLexer;
}

cr_access_list: ACCESS_LIST newline cral_edit*;

cr_prefix_list: PREFIX_LIST newline crpl_edit*;

crpl_edit: EDIT prefix_list_name newline crple* NEXT newline;

crple: crple_set | crple_config;

crple_set: SET crple_set_comments;

crple_set_comments: COMMENTS comment = str newline;

crple_config: CONFIG crplec_rule;

crplec_rule: RULE newline crplecr_edit* END newline;

crplecr_edit: EDIT prefix_list_rule_number newline crplecre* NEXT newline;

crplecre: crplecre_set | crplecre_unset;

crplecre_set
:
    SET (
        crplecre_set_prefix
        | crplecre_set_ge
        | crplecre_set_le
    )
;

crplecre_unset: UNSET (crplecre_unset_ge | crplecre_unset_le);

crplecre_set_prefix: PREFIX ip_address_with_mask_or_prefix newline;

crplecre_set_ge: GE ge = uint8 newline;

crplecre_set_le: LE le = uint8 newline;

crplecre_unset_ge: GE newline;

crplecre_unset_le: LE newline;

// 1-63 characters
prefix_list_name: str;

// 0-4294967295
prefix_list_rule_number: str;

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
