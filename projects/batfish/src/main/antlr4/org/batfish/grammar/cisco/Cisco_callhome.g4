parser grammar Cisco_callhome;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

call_home_alert_group_null: NO? ALERT_GROUP null_rest_of_line;
call_home_contact_null: NO? CONTACT null_rest_of_line;
call_home_contact_email_addr_null: NO? CONTACT_EMAIL_ADDR null_rest_of_line;
call_home_contact_name_null: NO? CONTACT_NAME null_rest_of_line;
call_home_contract_id_null: NO? CONTRACT_ID null_rest_of_line;
call_home_customer_id_null: NO? CUSTOMER_ID null_rest_of_line;
call_home_mail_server_null: NO? MAIL_SERVER null_rest_of_line;
call_home_phone_number_null: NO? PHONE_NUMBER null_rest_of_line;
call_home_sender_null: NO? SENDER null_rest_of_line;
call_home_service_null: NO? SERVICE null_rest_of_line;
call_home_site_id_null: NO? SITE_ID null_rest_of_line;
call_home_source_interface_null: NO? SOURCE_INTERFACE null_rest_of_line;
call_home_source_ip_address_null: NO? SOURCE_IP_ADDRESS null_rest_of_line;
call_home_street_address_null: NO? STREET_ADDRESS null_rest_of_line;
call_home_vrf_null: NO? VRF null_rest_of_line;

call_home_profile
:
   PROFILE null_rest_of_line
   (
      call_home_profile_active_null
      | call_home_profile_destination_null
      | call_home_profile_subscribe_to_alert_group_null
   )*
;

call_home_profile_active_null: NO? ACTIVE null_rest_of_line;
call_home_profile_destination_null: NO? DESTINATION null_rest_of_line;
call_home_profile_subscribe_to_alert_group_null: NO? SUBSCRIBE_TO_ALERT_GROUP null_rest_of_line;

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
   MESSAGE_LEVEL dec NEWLINE
;

callhome_destination_profile_message_size
:
   MESSAGE_SIZE dec NEWLINE
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
      callhome_diagnostic_signature_active_null
      | callhome_diagnostic_signature_profile_null
   )*
;

callhome_diagnostic_signature_active_null: NO? ACTIVE null_rest_of_line;
callhome_diagnostic_signature_profile_null: NO? PROFILE null_rest_of_line;

callhome_email_contact
:
   EMAIL_CONTACT variable NEWLINE
;

callhome_enable
:
   ENABLE NEWLINE
;

callhome_data_privacy_null: NO? DATA_PRIVACY null_rest_of_line;
callhome_duplicate_message_null: NO? DUPLICATE_MESSAGE null_rest_of_line;
callhome_no_null: NO ( DESTINATION_PROFILE | ENABLE | TRANSPORT ) null_rest_of_line;
callhome_periodic_inventory_null: NO? PERIODIC_INVENTORY null_rest_of_line;

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
   SWITCH_PRIORITY dec NEWLINE
;

callhome_transport
:
   TRANSPORT
   (
      callhome_transport_email
      | callhome_transport_http_null
   )
;

callhome_transport_http_null: HTTP null_rest_of_line;

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
         PORT p = dec
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
      call_home_alert_group_null
      | call_home_contact_email_addr_null
      | call_home_contact_name_null
      | call_home_contact_null
      | call_home_contract_id_null
      | call_home_customer_id_null
      | call_home_mail_server_null
      | call_home_phone_number_null
      | call_home_profile
      | call_home_sender_null
      | call_home_service_null
      | call_home_site_id_null
      | call_home_source_interface_null
      | call_home_source_ip_address_null
      | call_home_street_address_null
      | call_home_vrf_null
   )*
;

s_callhome
:
   CALLHOME NEWLINE
   (
      callhome_data_privacy_null
      | callhome_duplicate_message_null
      | callhome_email_contact
      | callhome_destination_profile
      | callhome_diagnostic_signature
      | callhome_enable
      | callhome_no_null
      | callhome_periodic_inventory_null
      | callhome_phone_contact
      | callhome_streetaddress
      | callhome_switch_priority
      | callhome_transport
   )*
;
