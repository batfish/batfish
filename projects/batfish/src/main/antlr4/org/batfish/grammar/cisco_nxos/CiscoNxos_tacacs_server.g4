parser grammar CiscoNxos_tacacs_server;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_tacacs_server
:
  TACACS_SERVER
  (
    ts_host
    | ts_null
  )
;

ts_host
:
  HOST host = tacacs_server_host ts_host_key? ts_host_port? ts_host_timeout? ts_host_single_connection? NEWLINE
;

ts_host_key
:
  KEY key = cisco_nxos_password
;

ts_host_port
:
  PORT port = tcp_port_number
;

ts_host_timeout
:
  TIMEOUT timeout = tacas_server_timeout_s
;

ts_host_single_connection
:
  SINGLE_CONNECTION
;

tacas_server_timeout_s
:
// 1-60 (seconds)
  uint8
;

tacacs_server_host
:
  ip_address
  | ipv6_address
  | WORD
;

ts_null
:
  (
    DEADTIME
    | DIRECTED_REQUEST
    | KEY
    | TEST
    | TIMEOUT
  ) null_rest_of_line
;

ip_tacacs
:
  TACACS ipt_source_interface
;

ipt_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;
