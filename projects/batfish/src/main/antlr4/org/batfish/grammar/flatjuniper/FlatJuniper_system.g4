parser grammar FlatJuniper_system;

import FlatJuniper_common;

options {
   tokenVocab = FlatJuniperLexer;
}

s_system
:
   SYSTEM
   (
      apply
      | sy_authentication_order
      | sy_default_address_selection
      | sy_domain_name
      | sy_host_name
      | sy_name_server
      | sy_ntp
      | sy_null
      | sy_ports
      | sy_root_authentication
      | sy_security_profile
      | sy_services
      | sy_syslog
      | sy_tacplus_server
   )
;

sy_authentication_method
:
   method =
   (
      PASSWORD
      | RADIUS
      | TACPLUS
   )
;

sy_authentication_order
:
   AUTHENTICATION_ORDER sy_authentication_method
;

sy_default_address_selection
:
   DEFAULT_ADDRESS_SELECTION
;

sy_domain_name
:
  // TODO: do better
  DOMAIN_NAME name = junos_name
;

sy_host_name
:
  // TODO: do better
  HOST_NAME name = junos_name
;

sy_name_server
:
  NAME_SERVER server = ip_address (SOURCE_ADDRESS src = ip_address)?
;

sy_ntp
:
   NTP
   (
      syn_null
      | syn_server
      | syn_source_address
   )
;

syn_null
:
   (
      BOOT_SERVER
      | BROADCAST_CLIENT
   ) null_filler
;

syn_server
:
   SERVER hostname = name_or_ip
   (
       syn_server_key
       | syn_server_version
       | syn_server_prefer
       | syn_server_routing_instance
   )*
;

syn_source_address
:
   SOURCE_ADDRESS address = IP_ADDRESS (ROUTING_INSTANCE ri = junos_name)?
;

syn_server_key
:
    KEY secret_string
;

syn_server_prefer
:
    PREFER
;

syn_server_routing_instance
:
    ROUTING_INSTANCE name = junos_name
;

syn_server_version
:
    VERSION VERSION_STRING
;

sy_null
:
   (
      ACCOUNTING
      | ALLOW_V4MAPPED_PACKETS
      | ARP
      | AUTO_SNAPSHOT
      | BACKUP_ROUTER
      | COMMIT
      | COMPRESS_CONFIGURATION_FILES
      | DDOS_PROTECTION
      | DOMAIN_SEARCH
      | DUMP_ON_PANIC
      | EXTENSIONS
      | INTERNET_OPTIONS
      | LICENSE
      | LOCATION
      | LOGIN
      | MAX_CONFIGURATIONS_ON_FLASH
      | MAX_CONFIGURATION_ROLLBACKS
      | NAME_RESOLUTION
      | NO_PING_RECORD_ROUTE
      | NO_PING_TIME_STAMP
      | NO_REDIRECTS
      | NO_REDIRECTS_IPV6
      | PROCESSES
      | RADIUS_OPTIONS
      | RADIUS_SERVER
      | SAVED_CORE_CONTEXT
      | SAVED_CORE_FILES
      | SCRIPTS
      | STATIC_HOST_MAPPING
      | SWITCHOVER_ON_ROUTING_CRASH
      | TIME_ZONE
   ) null_filler
;

sy_porttype
:
   AUXILIARY
   | CONSOLE
;

sy_ports
:
   PORTS porttype = sy_porttype
   (
      apply_groups
      | sy_authentication_order
      | syp_disable
      | syp_null
   )
;

sy_root_authentication
:
   ROOT_AUTHENTICATION
   (
      apply
      | syr_encrypted_password
   )
;

sy_syslog
:
   SYSLOG
   (
      sys_host
      | sys_null
      | sys_source_address
   )
;

sys_host
:
   HOST hostname = junos_name
   (
      sysh_null
      | sysh_routing_instance
   )
;

sys_null
:
   (
      ARCHIVE
      | CONSOLE
      | FILE
      | TIME_FORMAT
      | USER
   ) null_filler
;

sysh_routing_instance: ROUTING_INSTANCE ri = junos_name;

sys_source_address
:
  SOURCE_ADDRESS address = IP_ADDRESS
;

sysh_null
:
   (
      ALLOW_DUPLICATES
      | AUTHORIZATION
      | ANY
      | CHANGE_LOG
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

sy_security_profile
:
  SECURITY_PROFILE name = junos_name
  (
    apply
    | sysp_logical_system
    | sysp_null
  )
;

sy_services
:
   SERVICES
   (
      syserv_ftp
      | syserv_ssh
      | syserv_telnet
      | syserv_null
   )
;

syserv_ftp
:
   FTP
   (
      apply_groups
      | sy_authentication_order
      | syserv_common_null
   )?
;

syserv_ssh
:
   SSH
   (
      apply_groups
      | sy_authentication_order
      | syserv_common_null
      | syservs_access_disable_external_null
      | syservs_allow_tcp_forwarding_null
      | syservs_no_challenge_response_null
      | syservs_no_password_authentication_null
      | syservs_no_passwords_null
      | syservs_no_public_keys_null
      | syservs_no_tcp_forwarding_null
      | syservs_root_login_null
      | syservs_tcp_forwarding_null
      | syservs_null
   )?
;

syserv_telnet
:
   TELNET
   (
      apply_groups
      | sy_authentication_order
      | syserv_common_null
   )?
;

syserv_null
:
   (
      DATABASE_REPLICATION
      | DHCP
      | DHCP_LOCAL_SERVER
      | DNS
      | DTCP_ONLY
      | DYNAMIC_DNS
      | EXTENSIBLE_SUBSCRIBER
      | EXTENSION_SERVICE
      | FINGER
      | NETCONF
      | OUTBOUND_SSH
      | REST
      | REVERSE
      | SERVICE_DEPLOYMENT
      | SUBSCRIBER_MANAGEMENT
      | TFTP_SERVER
      | WEB_MANAGEMENT
      | WEBAPI
      | XNM_CLEAR_TEXT
      | XNM_SSL
   ) null_filler
;

sy_tacplus_server
:
   TACPLUS_SERVER
   (
      tacplus_server_host
      | wildcard
   )
   (
      apply
      | syt_secret
      | syt_source_address
      | syt_null
      | syt_routing_instance
   )
;

tacplus_server_host
:
  ip_address
  | ipv6_address
;

syp_disable
:
   DISABLE
;

syp_null
:
   (
      INSECURE
      | TYPE
      | LOG_OUT_ON_DISCONNECT
   ) null_filler
;

syr_encrypted_password
:
   ENCRYPTED_PASSWORD password = secret_string
;

syserv_common_null
:
   // Options shared by SSH, FTP, and TELNET
   (
      CONNECTION_LIMIT
      | RATE_LIMIT
   ) null_filler
;

syservs_access_disable_external_null
:
   ACCESS_DISABLE_EXTERNAL
;

syservs_allow_tcp_forwarding_null
:
   ALLOW_TCP_FORWARDING
;

syservs_no_challenge_response_null
:
   NO_CHALLENGE_RESPONSE
;

syservs_no_password_authentication_null
:
   NO_PASSWORD_AUTHENTICATION
;

syservs_no_public_keys_null
:
   NO_PUBLIC_KEYS
;

syservs_null
:
   // Other SSH-only options (not yet extracted)
   (
      AUTHORIZED_KEYS_COMMAND
      | AUTHORIZED_KEYS_COMMAND_USER
      | CIPHERS
      | CLIENT_ALIVE_COUNT_MAX
      | CLIENT_ALIVE_INTERVAL
      | FINGERPRINT_HASH
      | HOSTKEY_ALGORITHM
      | KEY_EXCHANGE
      | MACS
      | MAX_PRE_AUTHENTICATION_PACKETS
      | MAX_SESSIONS_PER_CONNECTION
      | PROTOCOL_VERSION
      | REKEY
   ) null_filler
;

syservs_no_passwords_null
:
   NO_PASSWORDS
;

syservs_no_tcp_forwarding_null
:
   NO_TCP_FORWARDING
;

syservs_root_login_null
:
   ROOT_LOGIN null_filler
;

syservs_tcp_forwarding_null
:
   TCP_FORWARDING
;

sysp_logical_system
:
  LOGICAL_SYSTEM name = junos_name
;

sysp_null
:
  (
    FLOW_GATE
    | FLOW_SESSION
    | POLICY
    | SCHEDULER
    | ZONE
  ) null_filler
;

syt_secret
:
  SECRET secret_string
;

syt_routing_instance: ROUTING_INSTANCE name = junos_name;

syt_source_address
:
   SOURCE_ADDRESS address = ip_address
;

syt_null
:
   (
      PORT
      | SINGLE_CONNECTION
      | TIMEOUT
   ) null_filler
;

