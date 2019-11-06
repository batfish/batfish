parser grammar cisco_zone;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_zone
:
   ZONE SECURITY? name = variable_permissive NEWLINE
;

s_zone_pair
:
   ZONE_PAIR SECURITY name = variable SOURCE source = variable DESTINATION
   destination = variable NEWLINE
   (
      zp_service_policy_inspect
   )*
;

zp_service_policy_inspect
:
   SERVICE_POLICY TYPE INSPECT name = variable_permissive NEWLINE
;
