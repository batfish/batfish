parser grammar A10_interface;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

s_rba: RBA sr_role;

sr_role: ROLE name = rba_role_name srr_tail? NEWLINE srr_definition+;

srr_tail: DEFAULT_PRIVILEGE srr_privilege;

srr_privilege: NO_ACCESS | OPER | READ | WRITE;

srr_definition
:
   (
      ACCESS_LIST
      | ACTIVE_PARTITION
      | ADMIN
      | ADMIN_DETAIL
      | ADMIN_LOCKOUT
      | ADMIN_SESSION
      | AUTHENTICATION
      | BACKUP_LOG
      | BACKUP_PERIODIC
      | BACKUP_SYSTEM
      | BANNER
      | BOOTIMAGE
      | CGNV6_RESOURCE_USAGE
      | CONFIGURE_SYNC
      | DELETE_GLM_LICENSE
      | DELETE_PARTITION
      | DELETE_STARTUP_CONFIG
      | DISABLE_MANAGEMENT
      | ENABLE_MANAGEMENT
      | EXPORT
      | FILE_GLM_LICENSE
      | FILE_STARTUP_CONFIG
      | FILE_TECHSUPPORT
      | FILE_TEMPLATE
      | FILE_WEB_SERVICE_CERT_KEY
      | GLM
      | HOSTNAME
      | IMPORT
      | INTERFACE_ETHERNET
      | INTERFACE_MANAGEMENT
      | INTERFACE_VE
      | IPV6_ACCESS_LIST
      | IP_DNS
      | LDAP_SERVER_HOST
      | LINK_STARTUP_CONFIG
      | LOGGING
      | MONITOR
      | MULTI_CONFIG
      | NETWORK_VLAN
      | NTP_AUTH_KEY
      | NTP_SERVER
      | NTP_STATUS
      | NTP_TRUSTED_KEY
      | PARTITION
      | PARTITION_ALL
      | PARTITION_GROUP
      | RADIUS_SERVER
      | RBA
      | REBOOT
      | RELOAD
      | SCM_LICENSE_SRC_INFO
      | SHUTDOWN
      | SLB_RESOURCE_USAGE
      | SMTP
      | SNMP_SERVER
      | SSH_LOGIN_GRACE_TIME
      | SYSTEM_GUI_IMAGE_LIST
      | SYSTEM_RESOURCE_ACCOUNTING
      | SYSTEM_RESOURCE_USAGE
      | SYSTEM_UPGRADE_STATUS
      | TACACS_SERVER
      | TERMINAL
      | TFTP
      | TIMEZONE
      | UPGRADE_CF
      | UPGRADE_HD
      | WEB_SERVICE
      | WRITE_MEMORY
   ) srr_privilege NEWLINE
;
