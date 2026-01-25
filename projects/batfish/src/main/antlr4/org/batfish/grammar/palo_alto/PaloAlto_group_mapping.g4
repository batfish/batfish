parser grammar PaloAlto_group_mapping;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

svg_group_mapping
:
    GROUP_MAPPING name=variable
    (
        svgm_group_object
        | svgm_group_name
        | svgm_group_member
        | svgm_user_object
        | svgm_user_name
        | svgm_server_profile
        | svgm_group_include_list
        | svgm_user_email
        | svgm_group_email
        | svgm_alternate_user_name_1
        | svgm_alternate_user_name_2
        | svgm_alternate_user_name_3
        | svgm_disabled
    )*
;

svgm_group_object
:
    GROUP_OBJECT variable
;

svgm_group_name
:
    GROUP_NAME variable
;

svgm_group_member
:
    GROUP_MEMBER variable
;

svgm_user_object
:
    USER_OBJECT variable
;

svgm_user_name
:
    USER_NAME variable
;

svgm_server_profile
:
    SERVER_PROFILE variable
;

svgm_group_include_list
:
    GROUP_INCLUDE_LIST variable
;

svgm_user_email
:
    USER_EMAIL variable?
;

svgm_group_email
:
    GROUP_EMAIL variable?
;

svgm_alternate_user_name_1
:
    ALTERNATE_USER_NAME_1 variable?
;

svgm_alternate_user_name_2
:
    ALTERNATE_USER_NAME_2 variable?
;

svgm_alternate_user_name_3
:
    ALTERNATE_USER_NAME_3 variable?
;

svgm_disabled
:
    DISABLED variable
;
