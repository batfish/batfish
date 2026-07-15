parser grammar Cisco_snmp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

snmp_file_transfer_protocol
:
   FTP
   | RCP
   | SCP
   | SFTP
   | TFTP
;

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
      | ss_enable_mib_null
      | ss_enable_trap
      | ss_enable_traps
      | ss_engineid_null
      | ss_file_transfer
      | ss_globalenforcepriv_null
      | ss_group
      | ss_host
      | ss_ifindex_null
      | ss_ifmib_null
      | ss_location_null
      | ss_logging_null
      | ss_manager_null
      | ss_max_ifindex_per_module_null
      | ss_mib
      | ss_notify_filter_null
      | ss_overload_control_null
      | ss_packetsize_null
      | ss_priority_null
      | ss_protocol_null
      | ss_queue_length_null
      | ss_source_interface
      | ss_system_shutdown_null
      | ss_tcp_session_null
      | ss_tftp_server_list
      | ss_trap_null
      | ss_trap_source
      | ss_trap_timeout_null
      | ss_traps_null
      | ss_user_null
      | ss_view_null
      | ss_vrf_null
      | NEWLINE
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

ss_enable_mib_null
:
   ENABLE MIB variable NEWLINE
;

ss_enable_trap
:
   ENABLE TRAP NEWLINE
;

ss_enable_traps
:
   ENABLE TRAPS
   (
      snmp_trap_type = variable
      (
         subfeature += variable
      )*
   )? NEWLINE
;

ss_file_transfer
:
   FILE_TRANSFER ACCESS_GROUP acl = variable
   (
      PROTOCOL snmp_file_transfer_protocol
   )? NEWLINE
;

ss_group
:
  GROUP name = variable
  (
    ss_group_v1_null
    | ss_group_v2c_null
    | ss_group_v3
  )
;

ss_group_v1_null
:
   V1 null_rest_of_line
;
ss_group_v2c_null
:
   V2C null_rest_of_line
;

ss_view_name
:
   name = variable
   | quoted_name = double_quoted_string
;

ss_group_v3
:
  V3
  level = (AUTH | NOAUTH | PRIV)
  (CONTEXT cname = variable)?
  (MATCH mtype = (EXACT | PREFIX))?
  (READ rview = ss_view_name)?
  (WRITE wview = ss_view_name)?
  (NOTIFY nview = ss_view_name)?
  (ACCESS (v4acl = variable | IPV6 v6acl = variable))?
  NEWLINE
;

ss_host
:
   HOST ss_host_generic
;

ss_host_generic
:
   (
      ip4 = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | host = variable
   )
   (
      ss_host_use_vrf
      |
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
   ) NEWLINE
;

ss_host_informs
:
   INFORMS
;

ss_host_name
:
   NAME name = variable
;

ss_host_traps
:
   TRAPS
;

ss_host_use_vrf
:
   (
      USE_VRF
      | VRF
   ) vrf = variable
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
ss_vrf_null
:
   VRF null_rest_of_line
;

ss_source_interface
:
   SOURCE_INTERFACE
   (
      TRAP
      | TRAPS
      | INFORM
      | INFORMS
   )? iname = interface_name NEWLINE
;

ss_tftp_server_list
:
   TFTP_SERVER_LIST name = variable NEWLINE
;

ss_trap_source
:
   TRAP_SOURCE IPV4? iname = interface_name NEWLINE
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
         ) dec?
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
