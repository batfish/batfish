parser grammar FastpathParser;

import Fastpath_common;

options {
  superClass = 'org.batfish.grammar.BatfishParser';
  tokenVocab = FastpathLexer;
}

fastpath_configuration
:
  NEWLINE?
  statement+ EOF
;

statement
:
  s_aaa
  | s_hostname
  | s_ip
  | s_logging
  | s_no
  | s_set
  | s_sntp
  | s_tacacs_server
;

s_hostname
:
  HOSTNAME hostname NEWLINE
;

s_set
:
  SET set_prompt
;

set_prompt
:
  PROMPT hostname NEWLINE
;

s_ip
:
  IP
  (
    ip_domain
    | ip_host_null
    | ip_name
  )
;

ip_host_null
:
  HOST null_rest_of_line
;

ip_domain
:
  DOMAIN
  (
    ipd_list_null
    | ipd_lookup
    | ipd_name
    | ipd_retry_null
    | ipd_timeout_null
  )
;

ipd_list_null
:
  LIST null_rest_of_line
;

ipd_lookup
:
  LOOKUP NEWLINE
;

ipd_name
:
  NAME domain_name NEWLINE
;

ipd_retry_null
:
  RETRY null_rest_of_line
;

ipd_timeout_null
:
  TIMEOUT null_rest_of_line
;

ip_name
:
  NAME
  (
    ipn_server
    | ipn_source_interface
  )
;

ipn_server
:
  SERVER ip_address+ NEWLINE
;

ipn_source_interface
:
  SOURCE_INTERFACE iface = interface_name NEWLINE
;

domain_name
:
  word
;

s_sntp
:
  SNTP
  (
    sntp_client
    | sntp_server
    | sntp_source_interface
    | sntp_unicast_null
  )
;

sntp_client
:
  CLIENT
  (
    sntpc_mode
    | sntpc_port
  )
;

sntpc_mode
:
  MODE sntp_client_mode? NEWLINE
;

sntp_client_mode
:
  BROADCAST
  | UNICAST
;

sntpc_port
:
  PORT port = uint16 NEWLINE
;

// Recognized operational tuning; parsed but intentionally not modeled. The tail (`client
// poll-interval|poll-retry <n>`) lexes into keywords, so null_rest_of_line consumes it.
sntp_unicast_null
:
  UNICAST null_rest_of_line
;

sntp_server
:
  SERVER
  (
    ss_host
    | ss_status_null
  )
;

// Fastpath allows an optional `[priority [version [portid]]]` after the host (up to three uint16
// values), the trailing values are tolerated via `uint16*` and only the server host is extracted
ss_host
:
  host_value uint16* NEWLINE
;

// Some software versions leak operational `sntp server status is ...` show-output into the config
// between real server lines; tolerate it silently
ss_status_null
:
  STATUS null_rest_of_line
;

sntp_source_interface
:
  SOURCE_INTERFACE iface = interface_name NEWLINE
;

s_logging
:
  LOGGING
  (
    logging_buffered
    | logging_cli_command
    | logging_console
    | logging_email_null
    | logging_host
    | logging_persistent
    | logging_port_null
    | logging_syslog
    | logging_traps_null
  )
;

logging_buffered
:
  BUFFERED
  (
    lb_enable
    | lb_wrap
  )
;

lb_enable
:
  severity = logging_severity? NEWLINE
;

lb_wrap
:
  WRAP NEWLINE
;

logging_cli_command
:
  CLI_COMMAND NEWLINE
;

logging_console
:
  CONSOLE severity = logging_severity? NEWLINE
;

logging_email_null
:
  EMAIL null_rest_of_line
;

logging_host
:
  HOST
  (
    lh_reconfigure_null
    | lh_remove_null
    | lh_server
  )
;

lh_reconfigure_null
:
  RECONFIGURE null_rest_of_line
;

lh_remove_null
:
  REMOVE null_rest_of_line
;

// In practice `port` and `severity` are supplied together or not at all, but the grammar keeps them
// independently optional for tolerance. Because `port` precedes `severity`, a lone trailing number
// binds to `port`.
lh_server
:
  host_value logging_addr_type? port = uint16? severity = logging_severity? NEWLINE
;

logging_persistent
:
  PERSISTENT severity = logging_severity NEWLINE
;

logging_port_null
:
  PORT null_rest_of_line
;

logging_syslog
:
  SYSLOG
  (
    ls_port_null
    | ls_source_interface
    | ls_enable
  )
;

ls_enable
:
  NEWLINE
;

ls_port_null
:
  PORT null_rest_of_line
;

ls_source_interface
:
  SOURCE_INTERFACE iface = interface_name NEWLINE
;

logging_traps_null
:
  TRAPS null_rest_of_line
;

s_no
:
  NO
  (
    no_ip
    | no_logging
  )
;

no_ip
:
  IP noip_domain
;

noip_domain
:
  DOMAIN noipd_lookup
;

noipd_lookup
:
  LOOKUP NEWLINE
;

no_logging
:
  LOGGING nol_console
;

nol_console
:
  CONSOLE NEWLINE
;

logging_addr_type
:
  DNS
  | IPV4
  | IPV6
;

logging_severity
:
  logging_severity_keyword
  | uint8
;

logging_severity_keyword
:
  ALERT
  | CRITICAL
  | DEBUG
  | EMERGENCY
  | ERROR
  | INFO
  | NOTICE
  | WARNING
;

s_tacacs_server
:
  TACACS_SERVER
  (
    ts_host
    | ts_key
    | ts_keystring_null
    | ts_source_interface
    | ts_timeout
  )
;

ts_host
:
  HOST host_value NEWLINE tsh_block
;

tsh_block
:
  (
    tsh_key
    | tsh_keystring_null
    | tsh_port
    | tsh_priority
    | tsh_timeout
  )* EXIT NEWLINE
;

tsh_key
:
  KEY
  (
    ENCRYPTED? word
  )? NEWLINE
;

tsh_keystring_null
:
  KEYSTRING null_rest_of_line
;

tsh_port
:
  PORT port = uint16 NEWLINE
;

tsh_priority
:
  PRIORITY priority = uint16 NEWLINE
;

tsh_timeout
:
  TIMEOUT timeout = uint8 NEWLINE
;

ts_source_interface
:
  SOURCE_INTERFACE iface = interface_name NEWLINE
;

ts_key
:
  KEY
  (
    ENCRYPTED? word
  )? NEWLINE
;

ts_keystring_null
:
  KEYSTRING null_rest_of_line
;

ts_timeout
:
  TIMEOUT timeout = uint8 NEWLINE
;

s_aaa
:
  AAA
  (
    aaa_accounting
    | aaa_authentication
    | aaa_authorization
    | aaa_ias_user
    | aaa_session_id_null
  )
;

aaa_authentication
:
  AUTHENTICATION
  (
    aaa_authentication_enable
    | aaa_authentication_login
  )
;

aaa_authentication_login
:
  LOGIN name = aaa_list_name aaa_authentication_method+ NEWLINE
;

aaa_authentication_enable
:
  ENABLE name = aaa_list_name aaa_authentication_method+ NEWLINE
;

aaa_authentication_method
:
  DENY
  | ENABLE
  | LINE
  | LOCAL
  | NONE
  | RADIUS
  | TACACS
;

aaa_authorization
:
  AUTHORIZATION
  (
    aaa_authorization_commands
    | aaa_authorization_exec
  )
;

aaa_authorization_commands
:
  COMMANDS name = aaa_list_name aaa_authorization_method+ NEWLINE
;

aaa_authorization_exec
:
  EXEC name = aaa_list_name aaa_authorization_method+ NEWLINE
;

// Both authorization types share one documented method set, so `local` is accepted here even though
// the device rejects it for `commands` authorization specifically
aaa_authorization_method
:
  LOCAL
  | NONE
  | RADIUS
  | TACACS
;

aaa_accounting
:
  ACCOUNTING
  (
    aaa_accounting_commands
    | aaa_accounting_dot1x
    | aaa_accounting_exec
  )
;

aaa_accounting_exec
:
  EXEC name = aaa_list_name record = aaa_accounting_record aaa_accounting_method* NEWLINE
;

aaa_accounting_commands
:
  COMMANDS name = aaa_list_name record = aaa_accounting_record aaa_accounting_method* NEWLINE
;

aaa_accounting_dot1x
:
  DOT1X DEFAULT record = aaa_dot1x_accounting_record RADIUS? NEWLINE
;

aaa_list_name
:
  DEFAULT
  | word
;

aaa_accounting_record
:
  NONE
  | START_STOP
  | STOP_ONLY
;

aaa_dot1x_accounting_record
:
  NONE
  | START_STOP
;

aaa_accounting_method
:
  RADIUS
  | TACACS
;

// `aaa ias-user username <user>` enters AAA IAS User Config mode, whose body is optional
// `password` statements then `exit`
aaa_ias_user
:
  IAS_USER USERNAME user = word NEWLINE aaaiu_block
;

aaaiu_block
:
  aaaiu_password_null* EXIT NEWLINE
;

aaaiu_password_null
:
  PASSWORD null_rest_of_line
;

aaa_session_id_null
:
  SESSION_ID null_rest_of_line
;
