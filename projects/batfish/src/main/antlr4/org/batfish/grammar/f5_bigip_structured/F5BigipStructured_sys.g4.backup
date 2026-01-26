parser grammar F5BigipStructured_sys;

import F5BigipStructured_common;

options {
  tokenVocab = F5BigipStructuredLexer;
}

sgs_hostname
:
  HOSTNAME hostname = word NEWLINE
;

sgs_null
:
  (
    CONSOLE_INACTIVITY_TIMEOUT
    | GUI_SECURITY_BANNER_TEXT
    | GUI_SETUP
  ) ignored
;

sys_dns
:
  DNS ignored
;

sys_global_settings
:
  GLOBAL_SETTINGS BRACE_LEFT
  (
    NEWLINE
    (
      sgs_hostname
      | sgs_null
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_ha_group
:
  HA_GROUP name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sh_active_bonus
      | sh_pools
      | sh_trunks
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sh_active_bonus
:
  ACTIVE_BONUS bonus = uint16 NEWLINE 
;

sh_pools
:
  POOLS BRACE_LEFT
  (
    NEWLINE shp_pool*
  )? BRACE_RIGHT NEWLINE
;

shp_pool
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      shpp_weight
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

shpp_weight
:
  WEIGHT weight = uint16 NEWLINE
;

sh_trunks
:
  TRUNKS BRACE_LEFT
  (
    NEWLINE sht_trunk*
  )? BRACE_RIGHT NEWLINE
;

sht_trunk
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      shtt_weight
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

shtt_weight
:
  WEIGHT weight = uint16 NEWLINE
;

sys_management_ip
:
  MANAGEMENT_IP ignored
;

sys_management_route
:
  MANAGEMENT_ROUTE ignored
;

sys_ntp
:
  NTP BRACE_LEFT
  (
    NEWLINE
    (
      ntp_null
      | ntp_servers
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ntp_null
:
  TIMEZONE ignored
;

ntp_servers
:
  SERVERS BRACE_LEFT servers += word* BRACE_RIGHT NEWLINE
;

sys_null
:
  (
    DYNAD
    | FEATURE_MODULE
    | FOLDER
    | FPGA
    | HTTPD
    | MANAGEMENT_DHCP
    | OUTBOUND_SMTP
    | PROVISION
    | SFLOW
    | SSHD
    | SYSLOG
    | TURBOFLEX
  ) ignored
;

sys_snmp
:
  SNMP ignored
;

s_sys
:
  SYS
  (
    sys_dns
    | sys_global_settings
    | sys_ha_group
    | sys_management_ip
    | sys_management_route
    | sys_ntp
    | sys_null
    | sys_snmp
    | unrecognized
  )
;

