parser grammar PaloAlto_log_settings;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_log_settings
:
    LOG_SETTINGS
    (
        sl_profiles
        | sl_syslog
    )
;

sl_profiles
:
    PROFILES null_rest_of_line
;

sl_syslog
:
    SYSLOG name = variable
    (
        sls_server
    )
;

sls_server
:
    SERVER name = variable
    (
        slss_facility
        | slss_format
        | slss_port
        | slss_server
        | slss_transport
    )
;

slss_facility
:
    FACILITY facility = variable
;

slss_format
:
    FORMAT format = (BSD | IETF)
;

slss_port
:
    PORT pn = port_number
;

slss_server
:
    SERVER address = variable
;

slss_transport
:
    TRANSPORT protocol = (SSL | TCP | UDP)
;
