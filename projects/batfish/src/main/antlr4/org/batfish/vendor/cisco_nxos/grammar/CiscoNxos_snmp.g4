parser grammar CiscoNxos_snmp;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_snmp_server
:
  SNMP_SERVER
  (
    snmps_community
    | snmps_contact
    | snmps_location
    | snmps_enable
    | snmps_host
    | snmps_source_interface
    | snmps_user
  )
;

snmps_community
:
  COMMUNITY community = snmp_community
  (
    snmps_community_group
    | snmps_community_use_acl
    | snmps_community_use_ipv4acl
    | snmps_community_use_ipv6acl
  )
;

snmp_community
:
// 1-32 characters
  WORD
;

snmps_community_group
:
  GROUP name = group_name NEWLINE
;

snmps_community_use_acl
:
  USE_ACL name = ip_access_list_name NEWLINE
;

snmps_community_use_ipv4acl
:
  USE_IPV4ACL name = ip_access_list_name NEWLINE
;

snmps_community_use_ipv6acl
:
  USE_IPV6ACL name = ip_access_list_name NEWLINE
;

snmps_contact
:
  CONTACT text = REMARK_TEXT NEWLINE
;

snmps_location
:
  LOCATION text = REMARK_TEXT NEWLINE
;

snmps_enable
:
  ENABLE TRAPS null_rest_of_line
;

snmps_host
:
  HOST host = snmp_server_host
  (
    snmpsh_source_interface
    | snmpsh_traps
    | snmpsh_use_vrf
  )
;

snmp_server_host
:
  dns = WORD
  | ip = ip_address
  | ip6 = ipv6_address
;

snmpsh_source_interface
:
  SOURCE_INTERFACE name = interface_name NEWLINE
;

snmpsh_traps
:
  TRAPS (VERSION version = snmp_version)? community = snmp_community NEWLINE
;

snmp_version
:
  SNMP_VERSION_1
  | SNMP_VERSION_2
  | SNMP_VERSION_2C
;

snmpsh_use_vrf
:
  USE_VRF name = vrf_name NEWLINE
;

snmps_source_interface
:
  SOURCE_INTERFACE
  (
    snmpssi_informs
    | snmpssi_traps
  )
;

snmpssi_informs
:
  (INFORM | INFORMS) name = interface_name NEWLINE
;

snmpssi_traps
:
// abbreviation of 'traps' to 'trap' seems to survive on at least one NX-OS v6 config
  (TRAP | TRAPS) name = interface_name NEWLINE
;

snmps_user
:
  USER user = user_name group = group_name?
  (
    snmpsu_auth
    | snmpsu_use_ipv4acl
    | snmpsu_use_ipv6acl
  )
;

snmpsu_auth
:
  AUTH (MD5 | SHA) user_pass = user_password (PRIV AES_128? priv_pass = privacy_password)? LOCALIZEDKEY? NEWLINE
;

snmpsu_use_ipv4acl
:
  USE_IPV4ACL name = ip_access_list_name NEWLINE
;

snmpsu_use_ipv6acl
:
  USE_IPV6ACL name = ip_access_list_name NEWLINE
;

privacy_password
:
// 1-130 characters
  WORD
;
