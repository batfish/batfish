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
    | s_service
    | s_service_group
    | s_tag
    | ss_log_settings
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
