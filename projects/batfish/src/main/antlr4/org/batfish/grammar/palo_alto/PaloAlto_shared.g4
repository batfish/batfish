parser grammar PaloAlto_shared;

import PaloAlto_common, PaloAlto_service, PaloAlto_service_group;

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

// Common syntax between set shared and set vsys
ss_common
:
    s_service
    | s_service_group
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
    BOTNET
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
