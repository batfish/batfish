parser grammar CiscoXr_callhome;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

call_home_null
:
   NO?
   (
      ALERT_GROUP
      | CONTACT
      | CONTACT_EMAIL_ADDR
      | CONTACT_NAME
      | CONTRACT_ID
      | CUSTOMER_ID
      | MAIL_SERVER
      | PHONE_NUMBER
      | SENDER
      | SERVICE
      | SITE_ID
      | SOURCE_INTERFACE
      | SOURCE_IP_ADDRESS
      | STREET_ADDRESS
      | VRF
   ) null_rest_of_line
;

call_home_profile
:
   PROFILE null_rest_of_line
   (
      call_home_profile_null
   )*
;

call_home_profile_null
:
   NO?
   (
      ACTIVE
      | DESTINATION
      | SUBSCRIBE_TO_ALERT_GROUP
   ) null_rest_of_line
;

callhome_destination_profile
:
   DESTINATION_PROFILE name = variable
   (
      callhome_destination_profile_alert_group
      | callhome_destination_profile_email_addr
      | callhome_destination_profile_format
      | callhome_destination_profile_message_level
      | callhome_destination_profile_message_size
      | callhome_destination_profile_transport_method
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

callhome_destination_profile_message_size
:
   MESSAGE_SIZE DEC NEWLINE
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

callhome_destination_profile_transport_method
:
   TRANSPORT_METHOD variable NEWLINE
;

callhome_diagnostic_signature
:
   DIAGNOSTIC_SIGNATURE NEWLINE
   (
      callhome_diagnostic_signature_null
   )*
;

callhome_diagnostic_signature_null
:
   NO?
   (
      ACTIVE
      | PROFILE
   ) null_rest_of_line
;

callhome_email_contact
:
   EMAIL_CONTACT variable NEWLINE
;

callhome_enable
:
   ENABLE NEWLINE
;

callhome_null
:
   NO?
   (
      DATA_PRIVACY
      | DUPLICATE_MESSAGE
      |
      (
         NO
         (
            DESTINATION_PROFILE
            | ENABLE
            | TRANSPORT
         )
      )
      | PERIODIC_INVENTORY
      |
      (
         TRANSPORT
         (
            HTTP
         )
      )
   ) null_rest_of_line
;

callhome_phone_contact
:
   PHONE_CONTACT variable NEWLINE
;

callhome_streetaddress
:
   STREETADDRESS variable NEWLINE
;

callhome_switch_priority
:
   SWITCH_PRIORITY DEC NEWLINE
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
   )
   (
      (
         PORT p = DEC
      )
      |
      (
         USE_VRF vrf = variable
      )
   )* NEWLINE
;

s_call_home
:
   NO? CALL_HOME null_rest_of_line
   (
      call_home_null
      | call_home_profile
   )*
;

s_callhome
:
   CALLHOME NEWLINE
   (
      callhome_email_contact
      | callhome_destination_profile
      | callhome_diagnostic_signature
      | callhome_enable
      | callhome_null
      | callhome_phone_contact
      | callhome_streetaddress
      | callhome_switch_priority
      | callhome_transport
   )*
;
