parser grammar PaloAlto_rulebase;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_rulebase
:
    RULEBASE
    (
        sr_security
    )
;

sr_security
:
    SECURITY RULES name = variable
    (
        srs_action
        | srs_application
        | srs_description
        | srs_destination
        | srs_disabled
        | srs_from
        | srs_service
        | srs_source
        | srs_to
    )
;

srs_action
:
    ACTION
    (
        ALLOW
        | DENY
        | DROP
        | RESET_BOTH
        | RESET_CLIENT
        | RESET_SERVER
    )
;

srs_application
:
    APPLICATION variable_list
;

srs_description
:
    DESCRIPTION description = variable
;

srs_destination
:
    DESTINATION src_or_dst_list
;

srs_disabled
:
    DISABLED
    (
        NO
        | YES
    )
;

srs_from
:
    FROM variable_list
;

srs_service
:
    SERVICE variable_list
;

srs_source
:
    SOURCE src_or_dst_list
;

srs_to
:
    TO variable_list
;
