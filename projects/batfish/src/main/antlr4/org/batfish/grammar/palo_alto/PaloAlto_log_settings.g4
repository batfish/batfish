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
        | slss_format
    )*
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
        | slss_format
    )*
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
        | slss_certificate_profile
        | slss_community
        | slss_facility
        | slss_format
        | slss_from
        | slss_gateway
        | slss_manager
        | slss_port
        | slss_server
        | slss_tls_version
        | slss_to
        | slss_transport
        | slss_version
    )*
;



slss_address
:
    ADDRESS address = variable
;

slss_and_also_to
:
    AND_ALSO_TO email = variable
;

slss_certificate_profile
:
    CERTIFICATE_PROFILE certificate_profile = variable
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
    // Special case: format escaping escaped-characters <value> | format escaping escape-character <value>
    FORMAT ESCAPING (ESCAPED_CHARACTERS | ESCAPE_CHARACTER) esc_value = variable
    // CEF format fields: format config "...", format system "...", etc.
    | FORMAT field_name = variable field_value = variable
    // Flat format: FORMAT (BSD | IETF) (ESCAPING ESCAPED_CHARACTERS variable)?
    | FORMAT format = (BSD | IETF)
    (
        ESCAPING ESCAPED_CHARACTERS escaped_chars = variable
        | ESCAPING ESCAPE_CHARACTER escape_char = variable
    )?
    // Nested format: FORMAT { slss_format_block* }
    | FORMAT OPEN_BRACE slss_format_block* CLOSE_BRACE
;

slss_format_block
:
    slss_format_escaping
    | slss_format_quoted_field
;

slss_format_escaping
:
    ESCAPING OPEN_BRACE
    (
        slss_format_escaped_characters
        | slss_format_escape_character
    )+
    CLOSE_BRACE
;

slss_format_escaped_characters
:
    ESCAPED_CHARACTERS escaped_val = variable SEMICOLON
;

slss_format_escape_character
:
    ESCAPE_CHARACTER escape_val = variable SEMICOLON
;

slss_format_quoted_field
:
    field_name = variable field_value = variable SEMICOLON
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

slss_tls_version
:
    TLS_VERSION version = variable
;

slsnmp_version
:
    VERSION version = variable
    (
        sls_server
    )?
;
