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
  LOGGING logging_host
;

logging_host
:
  HOST host_value logging_addr_type? uint16? logging_severity? NEWLINE
;

logging_addr_type
:
  IPV4
  | IPV6
;

logging_severity
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
