parser grammar FlatJuniper_system;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_system
:
   SYSTEM
   (
      sy_default_address_selection
      | sy_domain_name
      | sy_host_name
      | sy_name_server
      | sy_ntp
      | sy_null
      | sy_root_authentication
      | sy_syslog
      | sy_tacplus_server
   )
;

sy_default_address_selection
:
   DEFAULT_ADDRESS_SELECTION
;

sy_domain_name
:
   DOMAIN_NAME variable
;

sy_host_name
:
   HOST_NAME variable
;

sy_name_server
:
   NAME_SERVER hostname = variable
;

sy_ntp
:
   NTP
   (
      syn_null
      | syn_server
   )
;

sy_null
:
   (
      ACCOUNTING
      | ALLOW_V4MAPPED_PACKETS
      | ARP
      | AUTHENTICATION_ORDER
      | AUTO_SNAPSHOT
      | BACKUP_ROUTER
      | COMMIT
      | COMPRESS_CONFIGURATION_FILES
      | DDOS_PROTECTION
      | DOMAIN_SEARCH
      | EXTENSIONS
      | INTERNET_OPTIONS
      | LICENSE
      | LOCATION
      | LOGIN
      | MAX_CONFIGURATIONS_ON_FLASH
      | MAX_CONFIGURATION_ROLLBACKS
      | NAME_RESOLUTION
      | NO_REDIRECTS
      | PORTS
      | PROCESSES
      | RADIUS_OPTIONS
      | RADIUS_SERVER
      | SAVED_CORE_CONTEXT
      | SAVED_CORE_FILES
      | SCRIPTS
      | SERVICES
      | STATIC_HOST_MAPPING
      | SWITCHOVER_ON_ROUTING_CRASH
      | TIME_ZONE
   ) null_filler
;

sy_root_authentication
:
   ROOT_AUTHENTICATION
   (
      syr_encrypted_password
   )
;

sy_syslog
:
   SYSLOG
   (
      sys_host
      | sys_null
   )
;

sy_tacplus_server
:
   TACPLUS_SERVER hostname =
   (
      IP_ADDRESS
      | IPV6_ADDRESS
   )
   (
      apply
      | syt_secret
      | syt_source_address
      | syt_null
   )
;

syn_null
:
   (
      BOOT_SERVER
      | BROADCAST_CLIENT
      | SOURCE_ADDRESS
   ) null_filler
;

syn_server
:
   SERVER hostname = variable PREFER?
;

syr_encrypted_password
:
   ENCRYPTED_PASSWORD password = variable
;

sys_host
:
   HOST hostname = variable
   (
      sysh_null
   )
;

sys_null
:
   (
      ARCHIVE
      | CONSOLE
      | FILE
      | SOURCE_ADDRESS
      | TIME_FORMAT
      | USER
   ) null_filler
;

sysh_null
:
   (
      ANY
      | DAEMON
      | EXPLICIT_PRIORITY
      | FACILITY_OVERRIDE
      | FIREWALL
      | INTERACTIVE_COMMANDS
      | KERNEL
      | LOG_PREFIX
      | MATCH
      | PORT
      | SOURCE_ADDRESS
      | STRUCTURED_DATA
      | USER
   ) null_filler
;

syt_secret
:
   SECRET secret = DOUBLE_QUOTED_STRING
;

syt_source_address
:
   SOURCE_ADDRESS address = IP_ADDRESS
;

syt_null
:
   (
      PORT
      | SINGLE_CONNECTION
      | TIMEOUT
   ) null_filler
;
