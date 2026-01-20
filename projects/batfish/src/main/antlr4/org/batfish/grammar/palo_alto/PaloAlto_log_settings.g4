parser grammar PaloAlto_log_settings;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_log_settings
:
    LOG_SETTINGS
    (
        sl_and_also_to_null
        | sl_config
        | sl_email
        | sl_profiles
        | sl_snmptrap
        | sl_syslog
        | sl_system
        | sl_userid
    )?
;

sl_and_also_to_null
:
    AND_ALSO_TO null_rest_of_line
;

sl_config
:
    CONFIG null_rest_of_line
;

sl_email
:
    EMAIL name = variable
    (
        sls_server
    )?
;

sl_profiles
:
    PROFILES null_rest_of_line
;

sl_snmptrap
:
    SNMPTRAP name = variable
    (
        slsnmp_version
    )?
;

sl_syslog
:
    SYSLOG name = variable
    (
        sls_server
    )?
;

sl_system
:
    SYSTEM null_rest_of_line
;

sl_userid
:
    USERID null_rest_of_line
;

sls_server
:
    SERVER name = variable
    (
        slss_address
        | slss_and_also_to
        | slss_community
        | slss_facility
        | slss_format
        | slss_from
        | slss_gateway
        | slss_manager
        | slss_port
        | slss_server
        | slss_to
        | slss_transport
        | slss_version
    )
;



slss_address
:
    ADDRESS address = variable
;

slss_and_also_to
:
    AND_ALSO_TO email = variable
;

slss_community
:
    COMMUNITY community = variable
;

slss_facility
:
    FACILITY facility = variable
;

slss_format
:
    FORMAT format = (BSD | IETF)
;

slss_from
:
    FROM email = variable
;

slss_gateway
:
    GATEWAY address = variable
;

slss_manager
:
    MANAGER address = variable
;

slss_port
:
    PORT pn = port_number
;

slss_server
:
    SERVER address = variable
;

slss_to
:
    TO email = variable
;

slss_transport
:
    (TRANSPORT | PROTOCOL) protocol = (SSL | TCP | UDP | SMTP)
;

slss_version
:
    VERSION version = variable
;

slsnmp_version
:
    VERSION version = variable
    (
        sls_server
    )?
;
