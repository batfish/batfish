parser grammar F5BigipStructured_security;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

// Security DOS configuration
sec_dos_device_config
:
  DOS DEVICE_CONFIG name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_ip_uncommon_protolist
:
  DOS IP_UNCOMMON_PROTOLIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_ipv6_ext_hdr
:
  DOS IPV6_EXT_HDR name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sec_dos_udp_portlist
:
  DOS UDP_PORTLIST name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Security Firewall configuration
sec_firewall_config_change_log
:
  FIREWALL CONFIG_CHANGE_LOG BRACE_LEFT
  (
    NEWLINE
    (
      unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

// Other security configurations that are not yet implemented
sec_dos_other
:
  DOS unrecognized
;

sec_firewall_other
:
  FIREWALL unrecognized
;

sec_null
:
  (
    DOS
    | FIREWALL
  ) ignored
;

s_security
:
  SECURITY
  (
    sec_dos_device_config
    | sec_dos_ip_uncommon_protolist
    | sec_dos_ipv6_ext_hdr
    | sec_dos_udp_portlist
    | sec_dos_other
    | sec_firewall_config_change_log
    | sec_firewall_other
    | sec_null
    | unrecognized
  )
;
