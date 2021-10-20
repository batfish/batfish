parser grammar A10_health_monitor;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_health_monitor: HEALTH MONITOR health_check_name null_rest_of_line shm_definition*;

shm_definition
:
   shmd_interval
   | shmd_method
   | shmd_override_port
   | shmd_retry
   | shmd_ssl_ciphers
   | shmd_up_retry
;

shmd_interval: INTERVAL null_rest_of_line;

shmd_override_port: OVERRIDE_PORT null_rest_of_line;

shmd_retry: RETRY null_rest_of_line;

shmd_ssl_ciphers: SSL_CIPHERS null_rest_of_line;

shmd_up_retry: UP_RETRY null_rest_of_line;

shmd_method: METHOD health_check_method null_rest_of_line;

health_check_method
:
   COMPOUND
   | DATABASE
   | DNS
   | EXTERNAL
   | FTP
   | HTTP
   | HTTPS
   | ICMP
   | IMAP
   | KERBEROS_KDC
   | LDAP
   | NTP
   | POP3
   | RADIUS
   | RTSP
   | SIP
   | SMTP
   | SNMP
   | TACPLUS
   | TCP
   | UDP
;
