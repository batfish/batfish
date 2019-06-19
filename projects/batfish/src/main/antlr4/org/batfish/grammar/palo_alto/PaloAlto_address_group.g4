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
        | sag_null
        | sag_static
    )?
;

sag_description
:
    DESCRIPTION description = variable
;

sag_dynamic
:
    DYNAMIC
    null_rest_of_line
;

sag_null
:
    TAG
    null_rest_of_line
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
