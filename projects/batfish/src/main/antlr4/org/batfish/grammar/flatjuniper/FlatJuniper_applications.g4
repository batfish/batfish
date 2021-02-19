parser grammar FlatJuniper_applications;

import FlatJuniper_common, FlatJuniper_protocols;

options {
   tokenVocab = FlatJuniperLexer;
}

a_application
:
   APPLICATION name = variable
   (
      aa_common
      | aa_description
      | aa_term
   )
;

a_application_set
:
   APPLICATION_SET name = variable
   (
      aas_application
      | aas_application_set
   )
;

aa_common
:
   aat_alg
   | aat_application_protocol
   | aat_destination_port
   | aat_icmp_code
   | aat_icmp_type
   | aat_icmp6_code
   | aat_icmp6_type
   | aat_inactivity_timeout
   | aat_protocol
   | aat_rpc_program_number
   | aat_source_port
   | aat_uuid
;

aa_description
:
   DESCRIPTION null_filler
;

aa_term
:
   TERM name = junos_name aa_common+
;

aas_application
:
   APPLICATION
   (
      junos_application
      | name = variable
   )
;

aas_application_set
:
   APPLICATION_SET
   (
      junos_application_set
      | name = variable
   )
;

aat_alg
:
   ALG application_protocol
;

aat_application_protocol
:
   APPLICATION_PROTOCOL application_protocol
;

aat_destination_port
:
   DESTINATION_PORT (port | subrange)
;

aat_icmp_code
:
   ICMP_CODE icmp_code
;

aat_icmp_type
:
   ICMP_TYPE icmp_type
;

aat_icmp6_code
:
   ICMP6_CODE code = dec
;

aat_icmp6_type
:
   ICMP6_TYPE type = dec
;

aat_inactivity_timeout
:
   INACTIVITY_TIMEOUT
   (
      dec
      | NEVER
   )
;

aat_protocol
:
   PROTOCOL ip_protocol
;

aat_rpc_program_number
:
   RPC_PROGRAM_NUMBER dec
;

aat_source_port
:
   SOURCE_PORT (port | subrange)
;

aat_uuid
:
   UUID null_filler
;

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

s_applications
:
   APPLICATIONS
   (
      apply
      | a_application
      | a_application_set
   )
;
