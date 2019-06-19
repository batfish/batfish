parser grammar PaloAlto_application;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_application
:
    APPLICATION s_application_definition?
;

s_application_definition
:
    name = variable
    (
        sapp_description
    )?
;

sapp_description
:
    DESCRIPTION description = variable
;

s_application_group
:
    APPLICATION_GROUP sappg_definition?
;

sappg_definition
:
    name = variable
    (
       sappg_members
    )?
;

sappg_members
:
    MEMBERS variable_list?
;