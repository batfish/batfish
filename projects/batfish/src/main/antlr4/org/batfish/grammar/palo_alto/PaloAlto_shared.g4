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
    | s_admin_role_null
    | s_application
    | s_application_filter
    | s_application_group
    | s_authentication_profile_null
    | s_botnet_null
    | s_certificate_null
    | s_certificate_profile_null
    | s_content_preview_null
    | s_external_list
    | s_log_settings
    | s_profile_group_null
    | s_profiles
    | s_post_rulebase
    | s_pre_rulebase
    | s_profiles_null
    | s_server_profile_null
    | s_service
    | s_service_group
    | s_tag
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

s_admin_role_null
:
   ADMIN_ROLE null_rest_of_line
;
s_authentication_profile_null
:
   AUTHENTICATION_PROFILE null_rest_of_line
;
s_botnet_null
:
   BOTNET null_rest_of_line
;
s_certificate_null
:
   CERTIFICATE null_rest_of_line
;
s_certificate_profile_null
:
   CERTIFICATE_PROFILE null_rest_of_line
;
s_content_preview_null
:
   CONTENT_PREVIEW null_rest_of_line
;
s_profile_group_null
:
   PROFILE_GROUP null_rest_of_line
;
s_profiles_null
:
   PROFILES null_rest_of_line
;
s_server_profile_null
:
   SERVER_PROFILE null_rest_of_line
;
