parser grammar PaloAlto_profiles;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_profiles: PROFILES sp;

sp
:
    sp_custom_url_category
    | sp_spyware
    | sp_vulnerability
;

sp_spyware: SPYWARE null_rest_of_line;

sp_vulnerability: VULNERABILITY null_rest_of_line;

sp_custom_url_category: CUSTOM_URL_CATEGORY custom_url_category_name spc_definition;

spc_definition: spc_description | spc_list | spc_type;

spc_description: DESCRIPTION description = value;

spc_list: LIST list = variable_list;

spc_type: TYPE type = variable;

// Up to 31 characters
custom_url_category_name: variable;
