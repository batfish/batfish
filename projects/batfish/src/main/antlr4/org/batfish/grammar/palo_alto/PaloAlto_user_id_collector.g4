parser grammar PaloAlto_user_id_collector;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_user_id_collector
:
    USER_ID_COLLECTOR
    (
        s_user_id_collector_setting
        | s_user_id_collector_syslog_parse_profile
        | s_user_id_collector_server_monitor
    )*
;

s_user_id_collector_setting
:
    SETTING
    (
        suc_enable_mapping_timeout
        | suc_ip_user_mapping_timeout
        | suc_null
    )
;

suc_enable_mapping_timeout
:
    ENABLE_MAPPING_TIMEOUT yes_or_no
;

suc_ip_user_mapping_timeout
:
    IP_USER_MAPPING_TIMEOUT uint32
;

suc_null
:
    null_rest_of_line
;

s_user_id_collector_syslog_parse_profile
:
    SYSLOG_PARSE_PROFILE name = variable
    (
        suc_syslog_regex_identifier
    )*
;

suc_syslog_regex_identifier
:
    REGEX_IDENTIFIER
    (
        suc_syslog_regex_event
        | suc_syslog_regex_username
        | suc_syslog_regex_address
    )
;

suc_syslog_regex_event
:
    EVENT_REGEX variable
;

suc_syslog_regex_username
:
    USERNAME_REGEX variable
;

suc_syslog_regex_address
:
    ADDRESS_REGEX variable
;

s_user_id_collector_server_monitor
:
    SERVER_MONITOR
    (
        suc_server_monitor_interface
    )*
;

suc_server_monitor_interface
:
    MGMT_INTERFACE
    (
        suc_syslog_config
    )
;

suc_syslog_config
:
    SYSLOG
    (
        suc_syslog_profile
        | suc_syslog_address
        | suc_syslog_connection
        | suc_syslog_event
    )*
;

suc_syslog_profile
:
    SYSLOG_PARSE_PROFILE variable
;

suc_syslog_address
:
    ADDRESS variable
;

suc_syslog_connection
:
    CONNECTION_TYPE variable
;

suc_syslog_event
:
    EVENT_TYPE variable
;
