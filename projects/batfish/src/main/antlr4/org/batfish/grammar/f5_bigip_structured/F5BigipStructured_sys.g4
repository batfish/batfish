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
    | TURBOFLEX
  ) ignored
;

sys_sshd
:
  SSHD BRACE_LEFT
  (
    NEWLINE
    (
      sshd_inactivity_timeout
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sshd_inactivity_timeout
:
  INACTIVITY_TIMEOUT timeout = uint NEWLINE
;

sys_syslog
:
  SYSLOG BRACE_LEFT
  (
    NEWLINE
    (
      syslog_remote_servers
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

syslog_remote_servers
:
  REMOTE_SERVERS BRACE_LEFT
  (
    NEWLINE syslog_remote_server*
  )? BRACE_RIGHT NEWLINE
;

syslog_remote_server
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      syslog_host
      | syslog_local_ip
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

syslog_host
:
  HOST host = word NEWLINE
;

syslog_local_ip
:
  LOCAL_IP ip = word NEWLINE
;

sys_snmp
:
  SNMP BRACE_LEFT
  (
    NEWLINE
    (
      snmp_agent_addresses
      | snmp_allowed_addresses
      | snmp_communities
      | snmp_disk_monitors
      | snmp_process_monitors
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_agent_addresses
:
  AGENT_ADDRESSES BRACE_LEFT addresses += word* BRACE_RIGHT NEWLINE
;

snmp_allowed_addresses
:
  ALLOWED_ADDRESSES BRACE_LEFT addresses += word* BRACE_RIGHT NEWLINE
;

snmp_communities
:
  COMMUNITIES BRACE_LEFT
  (
    NEWLINE snmp_community*
  )? BRACE_RIGHT NEWLINE
;

snmp_community
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_community_name
      | snmp_community_source
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_community_name
:
  COMMUNITY_NAME name = word NEWLINE
;

snmp_community_source
:
  SOURCE source = word NEWLINE
;

snmp_disk_monitors
:
  DISK_MONITORS BRACE_LEFT
  (
    NEWLINE snmp_disk_monitor*
  )? BRACE_RIGHT NEWLINE
;

snmp_disk_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_minspace
      | snmp_disk_path
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_minspace
:
  MINSPACE space = uint NEWLINE
;

snmp_disk_path
:
  PATH path = word NEWLINE
;

snmp_process_monitors
:
  PROCESS_MONITORS BRACE_LEFT
  (
    NEWLINE snmp_process_monitor*
  )? BRACE_RIGHT NEWLINE
;

snmp_process_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_max_processes
      | snmp_process_name
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_max_processes
:
  MAX_PROCESSES (INFINITY | uint) NEWLINE
;

snmp_process_name
:
  PROCESS name = word NEWLINE
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
    | sys_sshd
    | sys_snmp
    | sys_syslog
    | unrecognized
  )
;

