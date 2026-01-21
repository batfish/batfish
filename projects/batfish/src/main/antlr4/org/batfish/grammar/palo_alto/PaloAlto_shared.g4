parser grammar PaloAlto_shared;

import
    PaloAlto_address,
    PaloAlto_address_group,
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
    | ss_service_override
    | s_local_user_database
    | ss_null
;

s_local_user_database
:
    LOCAL_USER_DATABASE variable
;

s_external_list
:
    EXTERNAL_LIST
    (
        name = variable
        (
            sel_type
        )
    )?
;

sel_type
:
    TYPE
    (
      selt_ip
      | selt_url
    )
;

selt_ip
:
    IP
    (
      seltip_auth
      | seltip_certificate_profile
      | seltip_exception_list
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
      FIVE_MINUTE
      | HOURLY
    )
;

seltip_url
:
    URL url = variable
;

seltip_exception_list
:
    EXCEPTION_LIST ip_prefix_list
;

selt_url
:
    URL
    (
      selturl_recurring
      | selturl_url
    )
;

selturl_recurring
:
    RECURRING
    (
      FIVE_MINUTE
      | HOURLY
    )
;

selturl_url
:
    URL url = variable
;

ss_null
:
    (
        ADMIN_ROLE
        | AUTHENTICATION_PROFILE
        | AUTHENTICATION_SEQUENCE
        | BOTNET
        | CERTIFICATE
        | CERTIFICATE_PROFILE
        | CONTENT_PREVIEW
        | PROFILE_GROUP
        | PROFILES
        | SCHEDULE
        | SERVER_PROFILE
        | SSL_TLS_SERVICE_PROFILE
        | USER_ID_COLLECTOR
    )
    null_rest_of_line
;

ss_service_override
 : SERVICE name = variable PROTOCOL (TCP | UDP) OVERRIDE yn = yes_or_no
 ;
