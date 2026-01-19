parser grammar PaloAlto_service_group;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_service_group
:
    'service-group' s_service_group_definition?
;

s_service_group_definition
:
    name = variable
    (
        sservgrp_members
    )?
;

sservgrp_members
:
    MEMBERS variable_list
;
