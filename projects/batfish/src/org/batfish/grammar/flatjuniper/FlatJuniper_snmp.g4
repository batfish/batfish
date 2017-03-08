parser grammar FlatJuniper_snmp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_snmp
:
   SNMP
   (
      snmp_community
      | snmp_null
      | snmp_trap_group
   )
;

snmp_community
:
   COMMUNITY comm = variable
   (
      snmpc_authorization
      | snmpc_client_list_name
      | snmpc_null
   )
;

snmp_null
:
   (
      CONTACT
      | LOCATION
      | TRACEOPTIONS
      | TRAP_OPTIONS
   ) null_filler
;

snmp_trap_group
:
   TRAP_GROUP
   (
      snmptg_trap_destinations
   )
;

snmpc_authorization
:
   AUTHORIZATION
   (
      READ_ONLY
      | READ_WRITE
   )
;

snmpc_client_list_name
:
   CLIENT_LIST_NAME name = variable
;

snmpc_null
:
   (
      CLIENTS
   ) null_filler
;

snmptg_trap_destinations
:
   TRAP_DESTINATIONS
   (
      snmptgtd_null
      | snmptgtd_targets
   )
;

snmptgtd_null
:
   (
      CATEGORIES
   ) null_filler
;

snmptgtd_targets
:
   TARGETS target = IP_ADDRESS
;
