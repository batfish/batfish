parser grammar PaloAlto_deviceconfig;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

active_active_device_id
:
  // 0 or 1
  uint8
;

ha_group_id
:
  // 1-63
  uint8
;

s_deviceconfig
:
    DEVICECONFIG
    (
        sd_high_availability
        | sd_setting
        | sd_system
    )
;

sd_high_availability
:
    HIGH_AVAILABILITY
    (
      sdha_enabled
      | sdha_group
      | sdha_interface
    )
;

sdha_enabled
:
    ENABLED yes_or_no
;

sdha_interface
:
    INTERFACE variable null_rest_of_line
;

sdha_group
:
    GROUP
    (
      sdhag_group_id
      | sdhag_mode
      | sdhag_peer_ip
      | sdhag_peer_ip_backup
      | sdhag_election_option
      | sdhag_state_synchronization
      | sdhag_monitoring
    )
;

sdhag_group_id: GROUP_ID id = ha_group_id;

sdhag_mode
:
    MODE
    (
      sdhagm_active_active
      | sdhagm_active_passive
    )
;

sdhagm_active_active
:
    ACTIVE_ACTIVE
    (
      sdhagmaa_device_id
    )
;

sdhagmaa_device_id: DEVICE_ID id = active_active_device_id;

sd_setting
:
    SETTING
    (
        sds_config
        | sds_management
        | sds_auto_mac_detect
        | sd_setting_null
    )
;

sds_config
:
    CONFIG REMATCH yes_or_no
;

sds_management
:
    MANAGEMENT
    (
        sds_management_hostname_type
        | sds_management_disable_predefined_reports
        | sds_management_null
    )
;

sds_management_hostname_type
:
    HOSTNAME_TYPE_IN_SYSLOG variable
;

sds_management_disable_predefined_reports
:
    DISABLE_PREDEFINED_REPORTS
    (
        OPEN_BRACKET
        (
            variable
        )*
        CLOSE_BRACKET
        | variable
    )
;

sds_management_null
:
    null_rest_of_line
;

sds_auto_mac_detect
:
    AUTO_MAC_DETECT yes_or_no
;

sd_setting_null
:
    null_rest_of_line
;

sd_system
:
    SYSTEM
    (
        sds_default_gateway
        | sds_device_telemetry
        | sds_dns_setting
        | sds_domain
        | sds_hostname
        | sds_ip_address
        | sds_netmask
        | sds_ntp_servers
        | sds_permitted_ip
        | sds_route
        | sds_service
        | sds_snmp_setting
        | sds_timezone
        | sds_type
        | sds_update_schedule
        | sds_update_server
        | sds_null
    )
;

sds_default_gateway
:
    DEFAULT_GATEWAY ip_address
;

sds_device_telemetry
:
    DEVICE_TELEMETRY null_rest_of_line
;

sds_dns_setting
:
    DNS_SETTING
    (
        sdsd_servers
    )
;

sds_domain
:
    DOMAIN name = variable
;

sds_hostname
:
    HOSTNAME (name=variable | name_token=HIGH_AVAILABILITY | name_token=TYPE)
;

sds_ip_address
:
    IP_ADDRESS_LITERAL ip_address
;

sds_netmask
:
    NETMASK ip_address
;

sds_ntp_servers
:
    NTP_SERVERS
    (
        PRIMARY_NTP_SERVER
        | SECONDARY_NTP_SERVER
    )
    (
        sdsn_authentication_type
        | sdsn_ntp_server_address
    )
;

sdsn_authentication_type
:
    AUTHENTICATION_TYPE
    (
        NONE
        | variable
    )
;

sds_service
:
    SERVICE ( DISABLE_TELNET | DISABLE_HTTP | DISABLE_SNMP ) yes_or_no
;

sds_timezone
:
    TIMEZONE variable
;

sds_type
:
    TYPE variable
;

sds_update_schedule
:
    UPDATE_SCHEDULE null_rest_of_line
;

sds_update_server
:
    UPDATE_SERVER variable
;

sds_null
:
    PANORAMA_SERVER
    null_rest_of_line
;

sds_snmp_setting
:
    SNMP_SETTING
    (
        sdss_access_setting
        | sdss_snmp_system
    )*
;

sdss_access_setting
:
    ACCESS_SETTING
    sdssa_definition*
;

sdssa_definition
:
    VERSION variable
    | VIEWS variable sdssav_definition*
    | USERS variable sdssau_definition*
    | SNMP_COMMUNITY_STRING variable
;

sdssav_definition
:
    VIEW variable
    (
        OID variable
        | OPTION variable
        | MASK variable
    )
;

sdssau_definition
:
    AUTHPRIV variable sdssaua_definition*
;

sdssaua_definition
:
    AUTHPWD variable
    | PRIVPWD variable
    | VIEW variable
;

sdss_snmp_system
:
    SNMP_SYSTEM
    (
        LOCATION variable
        | CONTACT variable
        | SEND_EVENT_SPECIFIC_TRAPS yes_or_no
    )
;

sdsd_servers
:
    SERVERS
    (
        PRIMARY primary_name = ip_address
        | SECONDARY secondary_name = ip_address
    )
;

sdsn_ntp_server_address
:
    NTP_SERVER_ADDRESS address = variable
;

sds_permitted_ip
:
    PERMITTED_IP ip_prefix
;

sds_route
:
    ROUTE null_rest_of_line
;

sdhag_peer_ip
:
    PEER_IP ip_address
;

sdhag_peer_ip_backup
:
    PEER_IP_BACKUP ip_address
;

sdhag_election_option
:
    ELECTION_OPTION null_rest_of_line
;

sdhag_state_synchronization
:
    STATE_SYNCHRONIZATION null_rest_of_line
;

sdhag_monitoring
:
    MONITORING null_rest_of_line
;

sdhagm_active_passive
:
   ACTIVE_PASSIVE null_rest_of_line
;
