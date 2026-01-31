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

sshd_inactivity_timeout
:
  INACTIVITY_TIMEOUT timeout = uint NEWLINE
;

sys_sshd
:
  SSHD BRACE_LEFT
  (
    NEWLINE
    (
      sshd_banner
      | sshd_inactivity_timeout
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sshd_banner
:
  BANNER (ENABLED | DISABLED) NEWLINE
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
      | snmp_sys_contact_null
      | snmp_sys_location_null
      | snmp_traps
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE?
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
  COMMUNITIES BRACE_LEFT NEWLINE
  (
    snmp_community
  )* BRACE_RIGHT NEWLINE
;

snmp_community
:
  name = structure_name BRACE_LEFT NEWLINE
  (
    snmp_community_name
    | snmp_community_source
  )*
  BRACE_RIGHT NEWLINE
;

snmp_community_name
:
  COMMUNITY_NAME name = word_id NEWLINE
;

snmp_community_source
:
  SOURCE source = word NEWLINE
;

snmp_disk_monitors
:
  DISK_MONITORS BRACE_LEFT
  snmp_disk_monitors_content*
  BRACE_RIGHT NEWLINE
;

snmp_disk_monitors_content
:
  NEWLINE snmp_disk_monitor?
;

snmp_disk_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_minspace_null
      | snmp_disk_path_null
      | unrecognized
    )*
  )? BRACE_RIGHT
;

snmp_disk_path_value
:
  (PARTITION | word)+
;

snmp_minspace_null
:
  MINSPACE space = uint NEWLINE
;

snmp_disk_path_null
:
  PATH path = snmp_disk_path_value NEWLINE
;

snmp_process_monitors
:
  PROCESS_MONITORS BRACE_LEFT
  snmp_process_monitors_content*
  BRACE_RIGHT NEWLINE
;

snmp_process_monitors_content
:
  NEWLINE snmp_process_monitor?
;

snmp_process_monitor
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_max_processes_null
      | snmp_process_name_null
      | unrecognized
    )*
  )? BRACE_RIGHT
;

snmp_max_processes_null
:
  MAX_PROCESSES (INFINITY | uint) NEWLINE
;

snmp_process_name_null
:
  PROCESS name = word NEWLINE
;

snmp_sys_contact_null
:
  SYS_CONTACT contact = word NEWLINE
;

snmp_sys_location_null
:
  SYS_LOCATION location = word NEWLINE
;

snmp_traps
:
  TRAPS BRACE_LEFT
  (
    NEWLINE
    (
      snmp_trap
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_trap
:
  name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      snmp_trap_community
      | snmp_trap_host
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

snmp_trap_community
:
  COMMUNITY community = word_id NEWLINE
;

snmp_trap_host
:
  HOST host = ip_address NEWLINE
;

s_sys
:
  SYS
  (
    sys_compatibility_level
    | sys_diags_ihealth
    | sys_ecm
    | sys_log_config
    | sys_management_ovsdb
    | sys_dns
    | sys_global_settings
    | sys_ha_group
    | sys_management_ip
    | sys_management_route
    | sys_ntp
    | sys_null
    | sys_snmp
    | sys_software_update
    | sys_sshd
    | sys_syslog
    | sys_wom_deduplication
    | unrecognized
  )
;

sys_ecm
:
  ECM CLOUD_PROVIDER name = structure_name BRACE_LEFT
  (
    NEWLINE
    (
      sys_ecm_description
      | sys_ecm_property_template
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_ecm_description
:
  DESCRIPTION description_text NEWLINE
;

sys_ecm_property_template
:
  PROPERTY_TEMPLATE BRACE_LEFT
  (
    NEWLINE
    (
      ecm_property_item
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ecm_property_item
:
  word BRACE_LEFT
  (
    NEWLINE
    (
      ecm_property_setting
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

ecm_property_setting
:
  WORD_ID NEWLINE
  | DESCRIPTION description_text NEWLINE
  | VALID_VALUES BRACE_LEFT word+ BRACE_RIGHT NEWLINE
;

sys_log_config
:
  LOG_CONFIG
  (
    PUBLISHER structure_name ignored
    | DESTINATION (MANAGEMENT_PORT | REMOTE_HIGH_SPEED_LOG | REMOTE_SYSLOG) name = structure_name BRACE_LEFT
    (
      NEWLINE
      (
        slc_format
        | slc_protocol
        | slc_remote_high_speed_log
        | ignored
        | unrecognized
      )*
    )? BRACE_RIGHT NEWLINE
  )
;

sys_software_update
:
  SOFTWARE UPDATE BRACE_LEFT
  (
    NEWLINE
    (
      sys_software_update_settings
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_software_update_settings
:
  AUTO_CHECK (ENABLED | DISABLED) NEWLINE
  | AUTO_PHONEHOME (ENABLED | DISABLED) NEWLINE
  | FREQUENCY word_id NEWLINE
  | ignored
;

sys_wom_deduplication
:
  WOM DEDUPLICATION ignored
;

sys_diags_ihealth
:
  DIAGS IHEALTH BRACE_LEFT
  (
    NEWLINE
    (
      ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_management_ovsdb
:
  MANAGEMENT_OVSDB BRACE_LEFT
  (
    NEWLINE
    (
      ignored
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sys_compatibility_level
:
  COMPATIBILITY_LEVEL BRACE_LEFT
  (
    NEWLINE
    (
      sysc_level
      | unrecognized
    )*
  )? BRACE_RIGHT NEWLINE
;

sysc_level
:
  LEVEL uint NEWLINE
;

slc_format
:
  FORMAT format_value = word NEWLINE
;

slc_protocol
:
  PROTOCOL protocol_value = word NEWLINE
;

slc_remote_high_speed_log
:
  REMOTE_HIGH_SPEED_LOG pool_name = structure_name NEWLINE
;