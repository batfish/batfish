parser grammar Cisco_snmp;

import Cisco_common;

options {
   tokenVocab = CiscoLexer;
}

s_snmp_server
:
   SNMP_SERVER
   (
   	  NEWLINE
      | ss_community
      | ss_enable_mib_null
      | ss_enable_traps
      | ss_host
      | ss_null
      | ss_trap_source
   )
;

ss_community
:
   COMMUNITY name = variable
   (
      ssc_access_control
      | ssc_group
      | ssc_use_ipv4_acl
      | ssc_use_ipv6_acl
   )
;

ssc_access_control
:
   (
      RO
      | RW
   )? name = variable? NEWLINE
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

ss_host
:
   HOST
   (
      ip4 = IP_ADDRESS
      | ip6 = IPV6_ADDRESS
      | host = variable
   )
   (
      ss_host_null
      | ss_host_use_vrf
   )
;

ss_host_null
:
   (
      TRAPS
      | VERSION
   ) ~NEWLINE* NEWLINE
;

ss_host_use_vrf
:
   USE_VRF vrf = variable NEWLINE
;

ss_host_version
:
   VERSION
;

ss_null
:
   (
      CHASSIS_ID
      | CONTACT
      | ENGINEID
      | GROUP
      | IFINDEX
      | LOCATION
      | LOGGING
      | MANAGER
      | OVERLOAD_CONTROL
      | SOURCE_INTERFACE
      | TRAPS
      | USER
      | VIEW
   ) ~NEWLINE* NEWLINE
;

ss_trap_source
:
   TRAP_SOURCE interface_name NEWLINE
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

