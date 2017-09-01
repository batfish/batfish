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
      NEWLINE
      | ss_community
      | ss_enable_mib_null
      | ss_enable_traps
      | ss_file_transfer
      | ss_host
      | ss_null
      | ss_removed
      | ss_source_interface
      | ss_tftp_server_list
      | ss_trap_source
   )
;

ss_community
:
   COMMUNITY name = variable_community_name
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

ss_host
:
   HOST
   (
      ip4 = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | host = variable
   )
   (
      ss_host_use_vrf
      |
      (
         ss_host_informs
         | ss_host_traps
         | ss_host_version
      )* comm_or_username = variable_snmp_host
      (
         traps += variable_snmp_host
      )*
   ) NEWLINE
;

ss_host_informs
:
   INFORMS
;

ss_host_traps
:
   TRAPS
;

ss_host_use_vrf
:
   USE_VRF vrf = variable
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

ss_null
:
   (
      AAA
      | AAA_USER
      | CHASSIS_ID
      | COMMUNITY_MAP
      | CONTACT
      | CONTEXT
      | ENGINEID
      | GLOBALENFORCEPRIV
      | GROUP
      | IFINDEX
      | IFMIB
      | LOCATION
      | LOGGING
      | MANAGER
      | MAX_IFINDEX_PER_MODULE
      | OVERLOAD_CONTROL
      | PRIORITY
      | PROTOCOL
      | QUEUE_LENGTH
      | TCP_SESSION
      | TRAP
      | TRAPS
      | USER
      | VIEW
      | VRF
   ) ~NEWLINE* NEWLINE
;

ss_removed
:
   ~NEWLINE* REMOVED ~NEWLINE* NEWLINE
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
         ) DEC?
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
   ~( NEWLINE | INFORMS | TRAPS | VERSION | USE_VRF | VRF )
;
