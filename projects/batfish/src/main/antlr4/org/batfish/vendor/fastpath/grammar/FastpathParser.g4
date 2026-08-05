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
  s_hostname
  | s_ip
  | s_logging
  | s_no
  | s_set_prompt
  | s_sntp
;

s_hostname
:
  HOSTNAME hostname NEWLINE
;

s_set_prompt
:
  SET PROMPT hostname NEWLINE
;

s_ip
:
  IP
  (
    ip_name_server
    | ip_domain_name
  )
;

ip_name_server
:
  NAME SERVER ip_address+ NEWLINE
;

ip_domain_name
:
  DOMAIN NAME domain_name NEWLINE
;

domain_name
:
  double_quoted_string
;

s_sntp
:
  SNTP sntp_server
;

sntp_server
:
  SERVER host_value uint16* NEWLINE
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
  SOURCE_INTERFACE iface = source_interface NEWLINE
;

logging_traps_null
:
  TRAPS null_rest_of_line
;

s_no
:
  NO no_logging
;

no_logging
:
  LOGGING nl_console
;

nl_console
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
  | UINT8
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
