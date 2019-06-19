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
      | snmp_filter_interfaces
      | snmp_name
      | snmp_null
      | snmp_trap_group
   )
;

snmp_community
:
   COMMUNITY comm = variable
   (
      apply
      | snmpc_authorization
      | snmpc_client_list_name
      | snmpc_null
   )
;

snmp_filter_interfaces
:
    FILTER_INTERFACES INTERFACES DOUBLE_QUOTED_STRING
;

snmp_name
:
    NAME name = variable
;

snmp_null
:
   (
      CLIENT_LIST
      | CONTACT
      | DESCRIPTION
      | FILTER_DUPLICATES
      | INTERFACE
      | LOCATION
      | STATS_CACHE_LIFETIME
      | TRACEOPTIONS
      | TRAP_OPTIONS
      | VIEW
   ) null_filler
;

snmp_trap_group
:
   TRAP_GROUP name = variable
   (
      snmptg_null
      | snmptg_targets
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
      | VIEW
   ) null_filler
;

snmptg_null
:
   (
      CATEGORIES
      | VERSION
   ) null_filler
;

snmptg_targets
:
   TARGETS target = IP_ADDRESS
;
