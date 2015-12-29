parser grammar FlatJuniper_applications;

import FlatJuniper_common, FlatJuniper_protocols;

options {
   tokenVocab = FlatJuniperLexer;
}

application_protocol
:
   DNS
   | FTP
   | HTTP
   | HTTPS
   | IGNORE
   | IKE_ESP_NAT
   | IMAP
   | MGCP_CA
   | MGCP_UA
   | MS_RPC
   | Q931
   | RAS
   | REALAUDIO
   | RTSP
   | SCCP
   | SIP
   | SMTP
   | SQLNET_V2
   | SSH
   | SUN_RPC
   | TALK
   | TELNET
   | TFTP
;

appst_application
:
   APPLICATION name = variable appst_application_tail
;

appst_application_tail
:
   appst_term
   | appst_term_tail
;

appst_term
:
   TERM name = variable appst_term_tail
;

appst_term_tail
:
   apptt_alg
   | apptt_application_protocol
   | apptt_destination_port
   | apptt_icmp_code
   | apptt_icmp_type
   | apptt_icmp6_code
   | apptt_icmp6_type
   | apptt_inactivity_timeout
   | apptt_protocol
   | apptt_rpc_program_number
   | apptt_source_port
   | apptt_uuid
;

apptt_alg
:
   ALG application_protocol
;

apptt_application_protocol
:
   APPLICATION_PROTOCOL application_protocol
;

apptt_destination_port
:
   DESTINATION_PORT subrange
;

apptt_icmp_code
:
   ICMP_CODE icmp_code
;

apptt_icmp_type
:
   ICMP_TYPE icmp_type
;

apptt_icmp6_code
:
   ICMP6_CODE code = DEC
;

apptt_icmp6_type
:
   ICMP6_TYPE type = DEC
;

apptt_inactivity_timeout
:
   INACTIVITY_TIMEOUT
   (
      DEC
      | NEVER
   )
;

apptt_protocol
:
   PROTOCOL ip_protocol
;

apptt_rpc_program_number
:
   RPC_PROGRAM_NUMBER DEC
;

apptt_source_port
:
   SOURCE_PORT subrange
;

apptt_uuid
:
   UUID s_null_filler
;

s_applications
:
   APPLICATIONS s_applications_tail
;

s_applications_tail
:
   appst_application
;