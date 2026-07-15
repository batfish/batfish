parser grammar CiscoXr_snmp;

import CiscoXr_common;

options {
   tokenVocab = CiscoXrLexer;
}

s_snmp_server
:
   SNMP_SERVER
   (
      ss_aaa_null
      | ss_aaa_user_null
      | ss_card_trap_inh_null
      | ss_chassis_id_null
      | ss_community
      | ss_community_map_null
      | ss_contact_null
      | ss_context_null
      | ss_engineid_null
      | ss_globalenforcepriv_null
      | ss_group_null
      | ss_host
      | ss_ifindex_null
      | ss_ifmib_null
      | ss_location_null
      | ss_logging_null
      | ss_manager_null
      | ss_max_ifindex_per_module_null
      | ss_mib
      | ss_mibs_null
      | ss_notify_filter_null
      | ss_overload_control_null
      | ss_packetsize_null
      | ss_priority_null
      | ss_protocol_null
      | ss_queue_length_null
      | ss_system_shutdown_null
      | ss_tcp_session_null
      | ss_trap_null
      | ss_trap_source
      | ss_trap_timeout_null
      | ss_traps_null
      | ss_user_null
      | ss_view_null
      | ss_vrf
   )
;

ss_community
:
   COMMUNITY
   (
      name = variable_community_name
      |
      (
         DOUBLE_QUOTE name = variable_community_name DOUBLE_QUOTE
      )
   )
   (
      ssc_group
      | ssc_use_ipv4_acl
      | ssc_use_ipv6_acl
      | ssc_access_control
   )
;

ss_host
:
  HOST
  (
    ip4 = IP_ADDRESS
    | ip6 = IPV6_ADDRESS
    | host = variable
  )
  (
     (
        ss_host_informs
        | ss_host_traps
        | ss_host_version
     )* comm_or_username = variable_snmp_host
     (
        traps += variable_snmp_host
     )*
  )
  NEWLINE
;

ss_host_informs
:
   INFORMS
;

ss_host_traps
:
   TRAPS
;

ss_host_version
:
   VERSION version = variable_snmp_host
   (
      AUTH
      | NOAUTH
      | PRIV
   )?
;

ss_mib
:
   MIB COMMUNITY_MAP (community_string = variable) CONTEXT (context_name = variable) NEWLINE
;

ss_aaa_null
:
   AAA null_rest_of_line
;
ss_aaa_user_null
:
   AAA_USER null_rest_of_line
;
ss_card_trap_inh_null
:
   CARD_TRAP_INH null_rest_of_line
;
ss_chassis_id_null
:
   CHASSIS_ID null_rest_of_line
;
ss_community_map_null
:
   COMMUNITY_MAP null_rest_of_line
;
ss_contact_null
:
   CONTACT null_rest_of_line
;
ss_context_null
:
   CONTEXT null_rest_of_line
;
ss_engineid_null
:
   ENGINEID null_rest_of_line
;
ss_globalenforcepriv_null
:
   GLOBALENFORCEPRIV null_rest_of_line
;
ss_group_null
:
   GROUP null_rest_of_line
;
ss_ifindex_null
:
   IFINDEX null_rest_of_line
;
ss_ifmib_null
:
   IFMIB null_rest_of_line
;
ss_location_null
:
   LOCATION null_rest_of_line
;
ss_logging_null
:
   LOGGING null_rest_of_line
;
ss_manager_null
:
   MANAGER null_rest_of_line
;
ss_max_ifindex_per_module_null
:
   MAX_IFINDEX_PER_MODULE null_rest_of_line
;
ss_mibs_null
:
   MIBS null_rest_of_line
;
ss_notify_filter_null
:
   NOTIFY_FILTER null_rest_of_line
;
ss_overload_control_null
:
   OVERLOAD_CONTROL null_rest_of_line
;
ss_packetsize_null
:
   PACKETSIZE null_rest_of_line
;
ss_priority_null
:
   PRIORITY null_rest_of_line
;
ss_protocol_null
:
   PROTOCOL null_rest_of_line
;
ss_queue_length_null
:
   QUEUE_LENGTH null_rest_of_line
;
ss_system_shutdown_null
:
   SYSTEM_SHUTDOWN null_rest_of_line
;
ss_tcp_session_null
:
   TCP_SESSION null_rest_of_line
;
ss_trap_null
:
   TRAP null_rest_of_line
;
ss_trap_timeout_null
:
   TRAP_TIMEOUT null_rest_of_line
;
ss_traps_null
:
   TRAPS null_rest_of_line
;
ss_user_null
:
   USER null_rest_of_line
;
ss_view_null
:
   VIEW null_rest_of_line
;

ss_trap_source
:
   TRAP_SOURCE IPV4? iname = interface_name NEWLINE
;

ss_vrf: VRF name = vrf_name NEWLINE ss_vrf_inner*;

ss_vrf_inner
:
  ss_host
  | ssv_null
;

ssv_null
:
  (
    CONTEXT
  ) null_rest_of_line
;

ssc_access_control
:
   (
      readonly = RO
      | readwrite = RW
      | SDROWNER
      | SYSTEMOWNER
      |
      (
         VIEW view = variable_snmp
      )
   )*
   (
      (
         (
            IPV4 acl4 = variable_snmp
         )
         |
         (
            IPV6 acl6 = variable_snmp
         ) uint_legacy?
      )
      | acl4 = variable_snmp
   )* NEWLINE
;

ssc_group
:
   GROUP name = variable NEWLINE
;

ssc_use_ipv4_acl
:
   (
      USE_ACL
      | USE_IPV4_ACL
   ) name = variable NEWLINE
;

ssc_use_ipv6_acl
:
   USE_IPV6_ACL name = variable NEWLINE
;

variable_snmp
:
   ~( IPV4 | IPV6 | GROUP | NEWLINE | RO | RW | SDROWNER | SYSTEMOWNER |
   USE_ACL | USE_IPV4_ACL | USE_IPV6_ACL | VIEW )
;

variable_snmp_host
:
   ~( NEWLINE | INFORMS | NAME | TRAPS | VERSION | USE_VRF | VRF )
;
