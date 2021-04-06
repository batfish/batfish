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

s_call_home
:
   NO? CALL_HOME null_rest_of_line
   (
      call_home_null
      | call_home_profile
   )*
;
