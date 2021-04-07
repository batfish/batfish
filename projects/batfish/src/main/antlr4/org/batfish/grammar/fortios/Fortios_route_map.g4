parser grammar Fortios_route_map;

options {
  tokenVocab = FortiosLexer;
}

cr_route_map: ROUTE_MAP newline crrm_edit*;

crrm_edit: EDIT route_map_name newline crrme* NEXT newline;

crrme: crrme_set | crrme_config;

crrme_set: SET crrme_set_comments;

crrme_set_comments: COMMENTS comment = str newline;

crrme_config: CONFIG crrmec_rule;

crrmec_rule: RULE newline crrmecr_edit* END newline;

crrmecr_edit: EDIT route_map_rule_number newline crrmecre* NEXT newline;

crrmecre: crrmecre_set | crrme_unset;

crrme_unset: UNSET unimplemented;

crrmecre_set: SET (crrmecre_set_action | crrmecre_set_match_ip_address);

crrmecre_set_action: ACTION route_map_action newline;

crrmecre_set_match_ip_address: MATCH_IP_ADDRESS access_list_or_prefix_list_name newline;

// 0-4294967295
route_map_rule_number: str;

route_map_action: permit_or_deny;
