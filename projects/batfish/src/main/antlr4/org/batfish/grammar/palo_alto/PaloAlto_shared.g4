parser grammar PaloAlto_shared;

import PaloAlto_application, PaloAlto_common, PaloAlto_service, PaloAlto_service_group, PaloAlto_tag;

options {
    tokenVocab = PaloAltoLexer;
}

s_shared
:
    SHARED
    (
        ss_common
        | ss_null
    )
;

// Common syntax between vsys, panorama, and other shared contexts
ss_common
:
    s_address
    | s_address_group
    | s_application
    | s_application_filter
    | s_application_group
    | s_external_filter
    | s_service
    | s_service_group
    | s_tag
    | ss_log_settings
;

s_external_filter
:
    EXTERNAL_LIST name = variable
    (
      sef_type
    )
;

sef_type
:
    TYPE
    (
      seft_ip
    )
;

seft_ip
:
    IP
    (
      seftip_auth
      | seftip_certificate_profile
      | seftip_recurring
      | seftip_url
    )
;

seftip_auth
:
    AUTH
    (
      seftipa_password
      | seftipa_username
    )
;

seftipa_username
:
    USERNAME null_rest_of_line
;

seftipa_password
:
    PASSWORD null_rest_of_line
;

seftip_certificate_profile
:
    CERTIFICATE_PROFILE name = variable
;

seftip_recurring
:
    RECURRING
    (
      HOURLY
    )
;

seftip_url
:
    URL url = null_rest_of_line
;

ss_log_settings
:
    LOG_SETTINGS
    (
        ssl_syslog
    )
;

ss_null
:
    (
        AUTHENTICATION_PROFILE
        | BOTNET
        | CERTIFICATE
        | CERTIFICATE_PROFILE
        | CONTENT_PREVIEW
        | SERVER_PROFILE
    )
    null_rest_of_line
;

ssl_syslog
:
    SYSLOG name = variable
    (
        ssls_server
    )
;

ssls_server
:
    SERVER name = variable
    (
        sslss_server
    )
;

sslss_server
:
    SERVER address = variable
;
