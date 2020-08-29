parser grammar PaloAlto_rulebase;

import
    PaloAlto_common,
    PaloAlto_nat;

options {
    tokenVocab = PaloAltoLexer;
}

s_rulebase
:
    RULEBASE rulebase_inner
;

s_post_rulebase
:
    POST_RULEBASE rulebase_inner
;

s_pre_rulebase
:
    PRE_RULEBASE rulebase_inner
;

rulebase_inner
:
    sr_nat
    | sr_security
;

sr_security
:
    SECURITY sr_security_rules?
;

sr_security_rules
:
    RULES srs_definition?
;

srs_definition
:
    name = variable
    (
        srs_action
        | srs_application
        | srs_category
        | srs_description
        | srs_destination
        | srs_disabled
        | srs_from
        | srs_hip_profiles
        | srs_log_end
        | srs_log_setting
        | srs_log_start
        | srs_negate_destination
        | srs_negate_source
        | srs_rule_type
        | srs_service
        | srs_source
        | srs_source_user
        | srs_target
        | srs_to
    )?
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

srs_category
:
    CATEGORY
    null_rest_of_line
;

srs_description
:
    DESCRIPTION description = value
;

srs_destination
:
    DESTINATION src_or_dst_list
;

srs_disabled
:
    DISABLED yn = yes_or_no
;

srs_from
:
    FROM variable_list
;

srs_hip_profiles
:
    HIP_PROFILES ANY // only support any
;

srs_log_end
:
    LOG_END yn = yes_or_no
;

srs_log_setting
:
    LOG_SETTING
    null_rest_of_line
;

srs_log_start
:
    LOG_START yn = yes_or_no
;

srs_negate_destination
:
    NEGATE_DESTINATION yn = yes_or_no
;

srs_negate_source
:
    NEGATE_SOURCE yn = yes_or_no
;

srs_rule_type
:
    RULE_TYPE
    (
      interzone = INTERZONE
      | intrazone = INTRAZONE
      | universal = UNIVERSAL
    )
;

srs_service
:
    SERVICE variable_list
;

srs_source
:
    SOURCE src_or_dst_list
;

srs_source_user
:
    SOURCE_USER ANY // only support user any so far
;

srs_target
:
    TARGET
    null_rest_of_line
;

srs_to
:
    TO variable_list
;
