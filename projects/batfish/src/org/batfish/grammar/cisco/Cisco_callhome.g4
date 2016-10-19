parser grammar Cisco_callhome;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_callhome
:
   CALLHOME NEWLINE
   (
      callhome_email_contact
      | callhome_destination_profile
      | callhome_enable
      | callhome_phone_contact
      | callhome_streetaddress
      | callhome_transport
   )*
;

callhome_destination_profile
:
   DESTINATION_PROFILE name = variable
   (
      callhome_destination_profile_alert_group
      | callhome_destination_profile_email_addr
      | callhome_destination_profile_message_level
      | callhome_destination_profile_format
      | NEWLINE
   )
;

callhome_destination_profile_alert_group
:
   ALERT_GROUP variable NEWLINE
;

callhome_destination_profile_email_addr
:
   EMAIL_ADDR variable NEWLINE
;

callhome_destination_profile_message_level
:
   MESSAGE_LEVEL DEC NEWLINE
;

callhome_destination_profile_format
:
   FORMAT
   (
      XML
      | FULL_TXT
      | SHORT_TXT
   ) NEWLINE
;

callhome_email_contact
:
   EMAIL_CONTACT variable NEWLINE
;

callhome_enable
:
   ENABLE NEWLINE
;

callhome_phone_contact
:
   PHONE_CONTACT variable NEWLINE
;

callhome_streetaddress
:
   STREETADDRESS variable NEWLINE
;

callhome_transport
:
   TRANSPORT
   (
      callhome_transport_email
   )
;

callhome_transport_email
:
   EMAIL
   (
      callhome_transport_email_from
      | callhome_transport_email_reply_to
      | callhome_transport_email_smtp_server
   )
;

callhome_transport_email_from
:
   FROM variable NEWLINE
;

callhome_transport_email_reply_to
:
   REPLY_TO variable NEWLINE
;

callhome_transport_email_smtp_server
:
   SMTP_SERVER
   (
      IP_ADDRESS
      | IPV6_ADDRESS
      | variable
   ) NEWLINE
;
