parser grammar Ftd_common;

options {
   tokenVocab = FtdLexer;
}

access_list_action
:
   PERMIT
   | DENY
   | TRUST
;

dec
:
   UINT8
   | UINT16
   | UINT32
   | DEC
;

description_line
:
   DESCRIPTION text = RAW_TEXT? NEWLINE
;

ip_address
:
   IP_ADDRESS
;

ip_prefix
:
   IP_PREFIX
;

null_rest_of_line
:
   ~NEWLINE* NEWLINE
;

port_specifier
:
   EQ port = port_value_null
   | GT port = port_value_null
   | LT port = port_value_null
   | NEQ port = port_value_null
   | RANGE port_low = dec port_high = dec
;

port_value_null
:
   dec
   | port_name_null
;

port_name_null
:
   NAME
   | WORD
   | DOMAIN
   | DNS
   | HTTPS
;

protocol
:
   DEC
   | ICMP
   | IP
   | TCP
   | UDP
;

uint8
:
   UINT8
   | DEC
;

uint16
:
   UINT16
   | DEC
;

uint32
:
   UINT32
   | DEC
;
