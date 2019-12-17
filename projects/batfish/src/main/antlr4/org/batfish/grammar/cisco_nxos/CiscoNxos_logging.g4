parser grammar CiscoNxos_logging;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_logging
:
  LOGGING
  (
    logging_server
    | logging_source_interface
  )
;

logging_server
:
  SERVER host = logging_host level = logging_level? ls_port? ls_secure?
  (
    ls_facility ls_use_vrf?
    | ls_use_vrf ls_facility?
  )?
  NEWLINE
;

logging_host
:
  ip_address
  | ipv6_address
  | WORD
;

logging_level
:
// 0-7
  uint8
;

ls_port
:
  PORT port = tcp_port_number
;

ls_secure
:
  SECURE (TRUSTPOINT CLIENT_IDENTITY name = trustpoint_name)?
;

trustpoint_name
:
// 1-64 characters
  WORD
;

ls_facility
:
  FACILITY
  (
    AUTH
    | AUTHPRIV
    | CRON
    | DAEMON
    | FTP
    | KERNEL
    | LOCAL0
    | LOCAL1
    | LOCAL2
    | LOCAL3
    | LOCAL4
    | LOCAL5
    | LOCAL6
    | LOCAL7
    | LPR
    | MAIL
    | NEWS
    | SYSLOG
    | USER
    | UUCP
  )
;

ls_use_vrf
:
  USE_VRF name = vrf_name
;

logging_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;
