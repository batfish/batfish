parser grammar PaloAlto_redist_profile;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

redist_profile_priority
:
// 1-255
   uint8
;

vrp_redist_profile
:
    REDIST_PROFILE name = variable
    (
        vrprp_action
        | vrprp_filter
        | vrprp_priority
    )?
;

vrprp_action
:
    ACTION (REDIST | NO_REDIST)
;

vrprp_filter
:
    FILTER
    (
        vrprpf_destination
        | vrprpf_type
    )?
;

vrprp_priority
:
    PRIORITY redist_profile_priority
;

vrprpf_destination
:
    DESTINATION ip_prefix_list
;

vrprpf_type
:
    TYPE variable_list
;



