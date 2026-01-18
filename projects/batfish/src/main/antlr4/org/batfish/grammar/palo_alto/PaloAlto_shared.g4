parser grammar PaloAlto_shared;

import
    PaloAlto_application,
    PaloAlto_common,
    PaloAlto_log_settings,
    PaloAlto_profiles,
    PaloAlto_service,
    PaloAlto_service_group,
    PaloAlto_tag;

options {
    tokenVocab = PaloAltoLexer;
}

s_shared
:
    SHARED ss_common?
;

// Common syntax between vsys, panorama, and other shared contexts
ss_common
:
    s_address
    | s_address_group
    | s_application
    | s_application_filter
    | s_application_group
    | s_external_list
    | s_log_settings
    | s_profiles
    | s_post_rulebase
    | s_pre_rulebase
    | s_service
    | s_service_group
    | s_tag
    | ss_null
;

s_external_list
:
    EXTERNAL_LIST name = variable
    (
      sel_type
    )
;

sel_type
:
    TYPE
    (
      selt_ip
    )
;

selt_ip
:
    IP
    (
      seltip_auth
      | seltip_certificate_profile
      | seltip_recurring
      | seltip_url
    )
;

seltip_auth
:
    AUTH
    (
      seltipa_password
      | seltipa_username
    )
;

seltipa_username
:
    USERNAME null_rest_of_line
;

seltipa_password
:
    PASSWORD null_rest_of_line
;

seltip_certificate_profile
:
    CERTIFICATE_PROFILE name = variable
;

seltip_recurring
:
    RECURRING
    (
      HOURLY
    )
;

seltip_url
:
    URL url = variable
;

ss_null
:
    (
        ADMIN_ROLE
        | AUTHENTICATION_PROFILE

        | BOTNET
        | CERTIFICATE
        | CERTIFICATE_PROFILE
        | CONTENT_PREVIEW
        | PROFILE_GROUP
        | PROFILES
        | SERVER_PROFILE
    )
    null_rest_of_line
;
