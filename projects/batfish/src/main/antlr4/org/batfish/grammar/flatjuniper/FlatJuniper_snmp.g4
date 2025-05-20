parser grammar FlatJuniper_snmp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_snmp
:
   SNMP
   (
      snmp_client_list
      | snmp_community
      | snmp_filter_interfaces
      | snmp_name
      | snmp_null
      | snmp_trap_group
   )
;

snmp_client_list
:
    CLIENT_LIST name = junos_name
    (
      apply
      | snmpcl_apply_path
      | snmpcl_network
    )
;

snmpcl_apply_path: APPLY_PATH path = DOUBLE_QUOTED_STRING;
snmpcl_network: prefix = ip_prefix_default_32;

snmp_community
:
   COMMUNITY comm = junos_name
   (
      apply
      | snmpc_authorization
      | snmpc_logical_system
      | snmpcls_common
      | snmpc_null
   )
;

snmp_filter_interfaces
:
   FILTER_INTERFACES
   (
      snmpfi_all_internal_interfaces_null
      | snmpfi_interfaces_null
   )
;

snmpfi_all_internal_interfaces_null
:
   ALL_INTERNAL_INTERFACES
;

snmpfi_interfaces_null
:
   INTERFACES
   (
      name = interface_id
      | wc = interface_wildcard
   )
;

snmp_name
:
    NAME_LITERALLY name = junos_name
;

snmp_null
:
   (
      CONTACT
      | DESCRIPTION
      | ENGINE_ID
      | FILTER_DUPLICATES
      | INTERFACE
      | LOCATION
      | ROUTING_INSTANCE_ACCESS
      | STATS_CACHE_LIFETIME
      | TRACEOPTIONS
      | TRAP_OPTIONS
      | VIEW
      | V3
   ) null_filler
;

snmp_trap_group
:
   TRAP_GROUP name = junos_name
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
   CLIENT_LIST_NAME name = junos_name
;

snmpc_logical_system
:
   LOGICAL_SYSTEM name = junos_name
   snmpcls_common
;

snmpcls_common
:
   snmpcls_routing_instance
   | snmpclsri_common
;

snmpcls_routing_instance
:
   ROUTING_INSTANCE name = junos_name
   snmpclsri_common
;

snmpclsri_common
:
   snmpc_client_list_name
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
      | ROUTING_INSTANCE
      | VERSION
   ) null_filler
;

snmptg_targets
:
   TARGETS target = ip_address
;
