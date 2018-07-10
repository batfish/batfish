parser grammar PaloAlto_shared;

import PaloAlto_common, PaloAlto_service;

options {
    tokenVocab = PaloAltoLexer;
}

s_shared
:
    SHARED
    (
        ss_common
    )
;

// Common syntax between set shared and set vsys
ss_common
:
    s_service
    | ss_log_settings
;

ss_log_settings
:
    LOG_SETTINGS
    (
        ssl_syslog
    )
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
