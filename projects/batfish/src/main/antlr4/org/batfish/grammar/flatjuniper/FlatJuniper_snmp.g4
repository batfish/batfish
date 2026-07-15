parser grammar FlatJuniper_snmp;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_snmp
:
   SNMP
   (
      apply
      | snmp_client_list
      | snmp_community
      | snmp_contact_null
      | snmp_description_null
      | snmp_engine_id_null
      | snmp_filter_duplicates_null
      | snmp_filter_interfaces
      | snmp_interface_null
      | snmp_location_null
      | snmp_name
      | snmp_routing_instance_access_null
      | snmp_stats_cache_lifetime_null
      | snmp_traceoptions_null
      | snmp_trap_group
      | snmp_trap_options_null
      | snmp_v3_null
      | snmp_view_null
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
      | snmpc_clients_null
      | snmpc_logical_system
      | snmpc_view_null
      | snmpcls_common
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

snmp_contact_null
:
   CONTACT null_filler
;
snmp_description_null
:
   DESCRIPTION null_filler
;
snmp_engine_id_null
:
   ENGINE_ID null_filler
;
snmp_filter_duplicates_null
:
   FILTER_DUPLICATES null_filler
;
snmp_interface_null
:
   INTERFACE null_filler
;
snmp_location_null
:
   LOCATION null_filler
;
snmp_routing_instance_access_null
:
   ROUTING_INSTANCE_ACCESS null_filler
;
snmp_stats_cache_lifetime_null
:
   STATS_CACHE_LIFETIME null_filler
;
snmp_traceoptions_null
:
   TRACEOPTIONS null_filler
;
snmp_trap_options_null
:
   TRAP_OPTIONS null_filler
;
snmp_view_null
:
   VIEW null_filler
;
snmp_v3_null
:
   V3 null_filler
;

snmp_trap_group
:
   TRAP_GROUP name = junos_name
   (
      snmptg_categories_null
      | snmptg_routing_instance_null
      | snmptg_targets
      | snmptg_version_null
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

snmpc_clients_null
:
   CLIENTS null_filler
;
snmpc_view_null
:
   VIEW null_filler
;

snmptg_categories_null
:
   CATEGORIES null_filler
;
snmptg_routing_instance_null
:
   ROUTING_INSTANCE null_filler
;
snmptg_version_null
:
   VERSION null_filler
;

snmptg_targets
:
   TARGETS target = ip_address
;
