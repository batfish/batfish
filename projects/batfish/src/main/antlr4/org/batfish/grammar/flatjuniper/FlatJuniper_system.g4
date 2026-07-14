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
      | sy_accounting_null
      | sy_allow_v4mapped_packets_null
      | sy_arp_null
      | sy_authentication_order
      | sy_auto_snapshot_null
      | sy_backup_router_null
      | sy_commit_null
      | sy_compress_configuration_files_null
      | sy_ddos_protection_null
      | sy_default_address_selection
      | sy_domain_name
      | sy_domain_search_null
      | sy_dump_on_panic_null
      | sy_extensions_null
      | sy_host_name
      | sy_internet_options_null
      | sy_license_null
      | sy_location_null
      | sy_login_null
      | sy_max_configuration_rollbacks_null
      | sy_max_configurations_on_flash_null
      | sy_name_resolution_null
      | sy_name_server
      | sy_no_ping_record_route_null
      | sy_no_ping_time_stamp_null
      | sy_no_redirects_ipv6_null
      | sy_no_redirects_null
      | sy_ntp
      | sy_ports
      | sy_processes_null
      | sy_radius_options_null
      | sy_radius_server_null
      | sy_root_authentication
      | sy_saved_core_context_null
      | sy_saved_core_files_null
      | sy_scripts_null
      | sy_security_profile
      | sy_services
      | sy_static_host_mapping_null
      | sy_switchover_on_routing_crash_null
      | sy_syslog
      | sy_tacplus_server
      | sy_time_zone_null
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
      syn_boot_server_null
      | syn_broadcast_client_null
      | syn_server
      | syn_source_address
   )
;

syn_boot_server_null
:
   BOOT_SERVER null_filler
;
syn_broadcast_client_null
:
   BROADCAST_CLIENT null_filler
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

sy_accounting_null
:
   ACCOUNTING null_filler
;
sy_allow_v4mapped_packets_null
:
   ALLOW_V4MAPPED_PACKETS null_filler
;
sy_arp_null
:
   ARP null_filler
;
sy_auto_snapshot_null
:
   AUTO_SNAPSHOT null_filler
;
sy_backup_router_null
:
   BACKUP_ROUTER null_filler
;
sy_commit_null
:
   COMMIT null_filler
;
sy_compress_configuration_files_null
:
   COMPRESS_CONFIGURATION_FILES null_filler
;
sy_ddos_protection_null
:
   DDOS_PROTECTION null_filler
;
sy_domain_search_null
:
   DOMAIN_SEARCH null_filler
;
sy_dump_on_panic_null
:
   DUMP_ON_PANIC null_filler
;
sy_extensions_null
:
   EXTENSIONS null_filler
;
sy_internet_options_null
:
   INTERNET_OPTIONS null_filler
;
sy_license_null
:
   LICENSE null_filler
;
sy_location_null
:
   LOCATION null_filler
;
sy_login_null
:
   LOGIN null_filler
;
sy_max_configurations_on_flash_null
:
   MAX_CONFIGURATIONS_ON_FLASH null_filler
;
sy_max_configuration_rollbacks_null
:
   MAX_CONFIGURATION_ROLLBACKS null_filler
;
sy_name_resolution_null
:
   NAME_RESOLUTION null_filler
;
sy_no_ping_record_route_null
:
   NO_PING_RECORD_ROUTE null_filler
;
sy_no_ping_time_stamp_null
:
   NO_PING_TIME_STAMP null_filler
;
sy_no_redirects_null
:
   NO_REDIRECTS null_filler
;
sy_no_redirects_ipv6_null
:
   NO_REDIRECTS_IPV6 null_filler
;
sy_processes_null
:
   PROCESSES null_filler
;
sy_radius_options_null
:
   RADIUS_OPTIONS null_filler
;
sy_radius_server_null
:
   RADIUS_SERVER null_filler
;
sy_saved_core_context_null
:
   SAVED_CORE_CONTEXT null_filler
;
sy_saved_core_files_null
:
   SAVED_CORE_FILES null_filler
;
sy_scripts_null
:
   SCRIPTS null_filler
;
sy_static_host_mapping_null
:
   STATIC_HOST_MAPPING null_filler
;
sy_switchover_on_routing_crash_null
:
   SWITCHOVER_ON_ROUTING_CRASH null_filler
;
sy_time_zone_null
:
   TIME_ZONE null_filler
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
      | syp_insecure_null
      | syp_log_out_on_disconnect_null
      | syp_type_null
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
      sys_allow_duplicates_null
      | sys_alternate_format_null
      | sys_archive_null
      | sys_console_null
      | sys_file
      | sys_grpc_replay_null
      | sys_host
      | sys_log_rotate_frequency_null
      | sys_routing_instance_null
      | sys_server_null
      | sys_source_address
      | sys_time_format_null
      | sys_user_null
   )
;

sys_file
:
   FILE filename = junos_name
   (
      sysf_allow_duplicates_null
      | sysf_any_null
      | sysf_archive
      | sysf_archive_sites_null
      | sysf_authorization_null
      | sysf_change_log_null
      | sysf_conflict_log_null
      | sysf_daemon_null
      | sysf_dfc_null
      | sysf_explicit_priority_null
      | sysf_external_null
      | sysf_firewall_null
      | sysf_ftp_null
      | sysf_interactive_commands_null
      | sysf_kernel_null
      | sysf_match_null
      | sysf_match_strings_null
      | sysf_ntp_null
      | sysf_pfe_null
      | sysf_structured_data_null
      | sysf_user_null
   )
;

sysf_archive
:
   ARCHIVE
   (
      sysfa_binary_data_null
      | sysfa_file
      | sysfa_no_binary_data_null
      | sysfa_no_world_readable_null
      | sysfa_size
      | sysfa_start_time_null
      | sysfa_transfer_interval_null
      | sysfa_world_readable_null
   )?
;

sysfa_file
:
   FILES count = dec
;

sysfa_size
:
   SIZE size = dec
   (
      unit = K
      | unit = M
      | unit = G
   )?
;

sysfa_binary_data_null
:
   BINARY_DATA null_filler
;
sysfa_no_binary_data_null
:
   NO_BINARY_DATA null_filler
;
sysfa_no_world_readable_null
:
   NO_WORLD_READABLE null_filler
;
sysfa_start_time_null
:
   START_TIME null_filler
;
sysfa_transfer_interval_null
:
   TRANSFER_INTERVAL null_filler
;
sysfa_world_readable_null
:
   WORLD_READABLE null_filler
;

sysf_allow_duplicates_null
:
   ALLOW_DUPLICATES null_filler
;
sysf_any_null
:
   ANY null_filler
;
sysf_archive_sites_null
:
   ARCHIVE_SITES null_filler
;
sysf_authorization_null
:
   AUTHORIZATION null_filler
;
sysf_change_log_null
:
   CHANGE_LOG null_filler
;
sysf_conflict_log_null
:
   CONFLICT_LOG null_filler
;
sysf_daemon_null
:
   DAEMON null_filler
;
sysf_dfc_null
:
   DFC null_filler
;
sysf_explicit_priority_null
:
   EXPLICIT_PRIORITY null_filler
;
sysf_external_null
:
   EXTERNAL null_filler
;
sysf_firewall_null
:
   FIREWALL null_filler
;
sysf_ftp_null
:
   FTP null_filler
;
sysf_interactive_commands_null
:
   INTERACTIVE_COMMANDS null_filler
;
sysf_kernel_null
:
   KERNEL null_filler
;
sysf_match_null
:
   MATCH null_filler
;
sysf_match_strings_null
:
   MATCH_STRINGS null_filler
;
sysf_ntp_null
:
   NTP null_filler
;
sysf_pfe_null
:
   PFE null_filler
;
sysf_structured_data_null
:
   STRUCTURED_DATA null_filler
;
sysf_user_null
:
   USER null_filler
;

sys_host
:
   HOST hostname = junos_name
   (
      sysh_allow_duplicates_null
      | sysh_exclude_hostname_null
      | sysh_explicit_priority_null
      | sysh_facility
      | sysh_facility_override_null
      | sysh_log_prefix_null
      | sysh_match_null
      | sysh_match_strings_null
      | sysh_port
      | sysh_routing_instance
      | sysh_source_address_null
      | sysh_structured_data_null
      | sysh_tlsdetails_null
      | sysh_transport
   )
;

sysh_facility
:
   facility = syslog_facility severity = syslog_severity
;

syslog_facility
:
   ANY
   | AUTHORIZATION
   | CHANGE_LOG
   | CONFLICT_LOG
   | DAEMON
   | DFC
   | EXTERNAL
   | FIREWALL
   | FTP
   | INTERACTIVE_COMMANDS
   | KERNEL
   | NTP
   | PFE
   | USER
;

syslog_severity
:
   ANY
   | NONE
   | EMERGENCY
   | ALERT
   | CRITICAL
   | ERROR
   | WARNING
   | NOTICE
   | INFO
;

sysh_port
:
   PORT num = port_number
;

sysh_transport
:
   TRANSPORT protocol = syslog_transport_protocol
;

syslog_transport_protocol
:
   TCP
   | TLS
   | UDP
;

sys_allow_duplicates_null
:
   ALLOW_DUPLICATES null_filler
;
sys_alternate_format_null
:
   ALTERNATE_FORMAT null_filler
;
sys_archive_null
:
   ARCHIVE null_filler
;
sys_console_null
:
   CONSOLE null_filler
;
sys_grpc_replay_null
:
   GRPC_REPLAY null_filler
;
sys_log_rotate_frequency_null
:
   LOG_ROTATE_FREQUENCY null_filler
;
sys_routing_instance_null
:
   ROUTING_INSTANCE null_filler
;
sys_server_null
:
   SERVER null_filler
;
sys_time_format_null
:
   TIME_FORMAT null_filler
;
sys_user_null
:
   USER null_filler
;

sysh_routing_instance: ROUTING_INSTANCE ri = junos_name;

sys_source_address
:
  SOURCE_ADDRESS address = IP_ADDRESS
;

sysh_allow_duplicates_null
:
   ALLOW_DUPLICATES null_filler
;
sysh_exclude_hostname_null
:
   EXCLUDE_HOSTNAME null_filler
;
sysh_explicit_priority_null
:
   EXPLICIT_PRIORITY null_filler
;
sysh_facility_override_null
:
   FACILITY_OVERRIDE null_filler
;
sysh_log_prefix_null
:
   LOG_PREFIX null_filler
;
sysh_match_null
:
   MATCH null_filler
;
sysh_match_strings_null
:
   MATCH_STRINGS null_filler
;
sysh_source_address_null
:
   SOURCE_ADDRESS null_filler
;
sysh_structured_data_null
:
   STRUCTURED_DATA null_filler
;
sysh_tlsdetails_null
:
   TLSDETAILS null_filler
;

sy_security_profile
:
  SECURITY_PROFILE name = junos_name
  (
    apply
    | sysp_flow_gate_null
    | sysp_flow_session_null
    | sysp_logical_system
    | sysp_policy_null
    | sysp_scheduler_null
    | sysp_zone_null
  )
;

sy_services
:
   SERVICES
   (
      syserv_database_replication_null
      | syserv_dhcp_local_server_null
      | syserv_dhcp_null
      | syserv_dns
      | syserv_dtcp_only_null
      | syserv_dynamic_dns_null
      | syserv_extensible_subscriber_null
      | syserv_extension_service_null
      | syserv_finger_null
      | syserv_ftp
      | syserv_netconf_null
      | syserv_outbound_ssh_null
      | syserv_rest_null
      | syserv_reverse_null
      | syserv_service_deployment_null
      | syserv_ssh
      | syserv_subscriber_management_null
      | syserv_telnet
      | syserv_tftp_server_null
      | syserv_web_management_null
      | syserv_webapi_null
      | syserv_xnm_clear_text_null
      | syserv_xnm_ssl_null
   )
;

syserv_dns
:
   DNS
   (
      syservd_dns_proxy
      | syservd_dnssec_null
      | syservd_forwarders
      | syservd_max_cache_ttl_null
      | syservd_max_ncache_ttl_null
      | syservd_traceoptions_null
   )
;

syservd_forwarders
:
   FORWARDERS name = ip_address
;

syservd_dns_proxy
:
   DNS_PROXY
   (
      syservddp_cache_null
      | syservddp_default_domain_null
      | syservddp_interface
      | syservddp_propogate_setting_null
      | syservddp_view_null
   )
;

syservd_dnssec_null
:
   DNSSEC null_filler
;
syservd_max_cache_ttl_null
:
   MAX_CACHE_TTL null_filler
;
syservd_max_ncache_ttl_null
:
   MAX_NCACHE_TTL null_filler
;
syservd_traceoptions_null
:
   TRACEOPTIONS null_filler
;

syservddp_interface
:
   INTERFACE iface = interface_id
;

syservddp_cache_null
:
   CACHE null_filler
;
syservddp_default_domain_null
:
   DEFAULT_DOMAIN null_filler
;
syservddp_propogate_setting_null
:
   PROPOGATE_SETTING null_filler
;
syservddp_view_null
:
   VIEW null_filler
;

syserv_ftp
:
   FTP
   (
      apply_groups
      | sy_authentication_order
      | syserv_common_connection_limit_null
      | syserv_common_rate_limit_null
   )?
;

syserv_ssh
:
   SSH
   (
      apply_groups
      | sy_authentication_order
      // Options shared by SSH, FTP, and TELNET
      | syserv_common_connection_limit_null
      | syserv_common_rate_limit_null
      // Other SSH-only options (not yet extracted)
      | syservs_access_disable_external_null
      | syservs_allow_tcp_forwarding_null
      | syservs_authorized_keys_command_null
      | syservs_authorized_keys_command_user_null
      | syservs_ciphers_null
      | syservs_client_alive_count_max_null
      | syservs_client_alive_interval_null
      | syservs_fingerprint_hash_null
      | syservs_hostkey_algorithm_null
      | syservs_key_exchange_null
      | syservs_macs_null
      | syservs_max_pre_authentication_packets_null
      | syservs_max_sessions_per_connection_null
      | syservs_no_challenge_response_null
      | syservs_no_password_authentication_null
      | syservs_no_passwords_null
      | syservs_no_public_keys_null
      | syservs_no_tcp_forwarding_null
      | syservs_protocol_version_null
      | syservs_rekey_null
      | syservs_root_login_null
      | syservs_tcp_forwarding_null
   )?
;

syserv_telnet
:
   TELNET
   (
      apply_groups
      | sy_authentication_order
      | syserv_common_connection_limit_null
      | syserv_common_rate_limit_null
   )?
;

syserv_database_replication_null
:
   DATABASE_REPLICATION null_filler
;
syserv_dhcp_null
:
   DHCP null_filler
;
syserv_dhcp_local_server_null
:
   DHCP_LOCAL_SERVER null_filler
;
syserv_dtcp_only_null
:
   DTCP_ONLY null_filler
;
syserv_dynamic_dns_null
:
   DYNAMIC_DNS null_filler
;
syserv_extensible_subscriber_null
:
   EXTENSIBLE_SUBSCRIBER null_filler
;
syserv_extension_service_null
:
   EXTENSION_SERVICE null_filler
;
syserv_finger_null
:
   FINGER null_filler
;
syserv_netconf_null
:
   NETCONF null_filler
;
syserv_outbound_ssh_null
:
   OUTBOUND_SSH null_filler
;
syserv_rest_null
:
   REST null_filler
;
syserv_reverse_null
:
   REVERSE null_filler
;
syserv_service_deployment_null
:
   SERVICE_DEPLOYMENT null_filler
;
syserv_subscriber_management_null
:
   SUBSCRIBER_MANAGEMENT null_filler
;
syserv_tftp_server_null
:
   TFTP_SERVER null_filler
;
syserv_web_management_null
:
   WEB_MANAGEMENT null_filler
;
syserv_webapi_null
:
   WEBAPI null_filler
;
syserv_xnm_clear_text_null
:
   XNM_CLEAR_TEXT null_filler
;
syserv_xnm_ssl_null
:
   XNM_SSL null_filler
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
      | syt_port_null
      | syt_secret
      | syt_single_connection_null
      | syt_source_address
      | syt_routing_instance
      | syt_timeout_null
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

syp_insecure_null
:
   INSECURE null_filler
;
syp_type_null
:
   TYPE null_filler
;
syp_log_out_on_disconnect_null
:
   LOG_OUT_ON_DISCONNECT null_filler
;

syr_encrypted_password
:
   ENCRYPTED_PASSWORD password = secret_string
;

syserv_common_connection_limit_null
:
   CONNECTION_LIMIT null_filler
;
syserv_common_rate_limit_null
:
   RATE_LIMIT null_filler
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

syservs_authorized_keys_command_null
:
   AUTHORIZED_KEYS_COMMAND null_filler
;
syservs_authorized_keys_command_user_null
:
   AUTHORIZED_KEYS_COMMAND_USER null_filler
;
syservs_ciphers_null
:
   CIPHERS null_filler
;
syservs_client_alive_count_max_null
:
   CLIENT_ALIVE_COUNT_MAX null_filler
;
syservs_client_alive_interval_null
:
   CLIENT_ALIVE_INTERVAL null_filler
;
syservs_fingerprint_hash_null
:
   FINGERPRINT_HASH null_filler
;
syservs_hostkey_algorithm_null
:
   HOSTKEY_ALGORITHM null_filler
;
syservs_key_exchange_null
:
   KEY_EXCHANGE null_filler
;
syservs_macs_null
:
   MACS null_filler
;
syservs_max_pre_authentication_packets_null
:
   MAX_PRE_AUTHENTICATION_PACKETS null_filler
;
syservs_max_sessions_per_connection_null
:
   MAX_SESSIONS_PER_CONNECTION null_filler
;
syservs_protocol_version_null
:
   PROTOCOL_VERSION null_filler
;
syservs_rekey_null
:
   REKEY null_filler
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

sysp_flow_gate_null
:
   FLOW_GATE null_filler
;
sysp_flow_session_null
:
   FLOW_SESSION null_filler
;
sysp_policy_null
:
   POLICY null_filler
;
sysp_scheduler_null
:
   SCHEDULER null_filler
;
sysp_zone_null
:
   ZONE null_filler
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

syt_port_null
:
   PORT null_filler
;
syt_single_connection_null
:
   SINGLE_CONNECTION null_filler
;
syt_timeout_null
:
   TIMEOUT null_filler
;

