parser grammar PaloAlto_rulebase;

import
    PaloAlto_application_override,
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
    sr_application_override
    | sr_default_security
    | sr_nat
    | sr_pbf
    | sr_security
;

sr_default_security
:
    DEFAULT_SECURITY_RULES sr_security_rules?
;

sr_security
:
    SECURITY sr_security_rules?
;

sr_pbf
:
    PBF srp_rules?
;

srp_rules
:
    RULES srp_definition?
;

srp_definition
:
    name = variable
    srp_uuid_null?
    (
        srp_action
        | srp_application
        | srp_destination
        | srp_disabled
        | srp_enforce_symmetric_return
        | srp_from
        | srp_group_tag
        | srp_log_end
        | srp_log_start
        | srp_negate_destination
        | srp_negate_source
        | srp_option
        | srp_service
        | srp_source
        | srp_source_user
        | srp_tag
        | srp_null
    )?
;

srp_uuid_null
:
    UUID
    | ~(
        ACTION
        | FROM
        | SOURCE
        | DESTINATION
        | APPLICATION
        | SERVICE
        | ENFORCE_SYMMETRIC_RETURN
        | LOG_END
        | LOG_START
        | NEGATE_DESTINATION
        | NEGATE_SOURCE
        | OPTION
        | NEWLINE
    )
;

srp_action
:
    ACTION FORWARD
    (
        srp_action_monitor
        | srp_action_nexthop
        | srp_action_egress_interface
    )?
;

srp_action_monitor
:
    MONITOR
    (
        PROFILE variable
        | DISABLE_IF_UNREACHABLE yes_or_no
        | IP_ADDRESS_LITERAL ip_address
    )
;

srp_action_nexthop
:
    NEXTHOP IP_ADDRESS_LITERAL ip_address
;

srp_action_egress_interface
:
    EGRESS_INTERFACE variable
;

srp_from
:
    FROM ZONE variable_list
;

srp_source
:
    SOURCE src_or_dst_list
;

srp_destination
:
    DESTINATION src_or_dst_list
;

srp_application
:
    APPLICATION variable_list
;

srp_service
:
    SERVICE variable_list
;

srp_enforce_symmetric_return
:
    ENFORCE_SYMMETRIC_RETURN ENABLED yes_or_no
;

srp_null
:
    ~NEWLINE
;

srp_source_user
:
    SOURCE_USER
    (
        any = ANY
        | names = variable_list
    )
;

srp_tag
:
    TAG tags = variable_list
;

srp_disabled
:
    DISABLED yn = yes_or_no
;

srp_group_tag
:
    GROUP_TAG variable
;



srp_option
:
    OPTION
    (
        DISABLE_SERVER_RESPONSE_INSPECTION yn=yes_or_no
    )
;

srp_log_end
:
    LOG_END yn=yes_or_no
;

srp_log_start
:
    LOG_START yn=yes_or_no
;

srp_negate_destination
:
    NEGATE_DESTINATION yn=yes_or_no
;

srp_negate_source
:
    NEGATE_SOURCE yn=yes_or_no
;

sr_security_rules
:
    RULES srs_definition?
;

srs_definition
:
    name = variable
    // Optional UUID/identifier
    srs_uuid_null?
    (
        srs_action
        | srs_application
        | srs_category
        | srs_description
        | srs_destination
        | srs_destination_hip
        | srs_disabled
        | srs_from
        | srs_from
        | srs_group_tag
        | srs_hip_profiles
        | srs_log_end
        | srs_log_setting
        | srs_log_start
        | srs_negate_destination
        | srs_negate_source
        | srp_option
        | srs_profile_setting
        | srs_rule_type
        | srs_schedule
        | srs_service
        | srs_source
        | srs_source_hip
        | srs_source_user
        | srs_target_null
        | srs_to
        | srs_tag
    ) ?
;

srs_uuid_null
:
    UUID
    | ~(
        ACTION
        | APPLICATION
        | CATEGORY
        | DESCRIPTION
        | DESTINATION
        | DESTINATION_HIP
        | DISABLED
        | FROM
        | HIP_PROFILES
        | LOG_END
        | LOG_SETTING
        | LOG_START
        | NEGATE_DESTINATION
        | NEGATE_SOURCE
        | OPTION
        | NEWLINE
        | RULE_TYPE
        | SERVICE
        | SOURCE
        | SOURCE_HIP
        | SOURCE_USER
        | TAG
        | TARGET
        | TO
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

srs_category
:
    CATEGORY variable_list
;

srs_description
:
    DESCRIPTION description=null_rest_of_line
;

srs_destination
:
    DESTINATION src_or_dst_list
;

srs_destination_hip
:
    DESTINATION_HIP
    (
        any = ANY
        | names = variable_list
    )
;

srs_disabled
:
    DISABLED yn = yes_or_no
;

srs_from
:
    FROM variable_list
;

srs_group_tag
:
    GROUP_TAG variable
;

srs_hip_profiles
:
    HIP_PROFILES
    (
        any = ANY
        | names = variable_list
    )
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

srs_profile_setting
:
    PROFILE_SETTING null_rest_of_line
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

srs_schedule
:
    SCHEDULE variable
;

srs_service
:
    SERVICE variable_list
;

srs_source
:
    SOURCE src_or_dst_list
;

srs_source_hip
:
    SOURCE_HIP
    (
        any = ANY
        | names = variable_list
    )
;

srs_source_user
:
    SOURCE_USER
    (
        any = ANY
        | names = variable_list
    )
;

srs_tag
:
    TAG tags = variable_list
;

srs_target_null
:
    TARGET
    null_rest_of_line
;

srs_to
:
    TO variable_list
;
