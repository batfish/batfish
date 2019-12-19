parser grammar PaloAlto_address_group;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_address_group
:
    ADDRESS_GROUP s_address_group_definition?
;

s_address_group_definition
:
    name = variable
    (
        sag_description
        | sag_dynamic
        | sag_static
        | sag_tag
    )?
;

sag_description
:
    DESCRIPTION description = value
;

sag_dynamic
:
    DYNAMIC sagd_filter?
;

sagd_filter
:
    FILTER filter = variable
;

sag_static
:
    STATIC
    (
        variable
        |
        (
            OPEN_BRACKET
            (
                variable
            )*
            CLOSE_BRACKET
        )
    )
;

sag_tag
:
    TAG tags = variable_list
;
