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
        | sd_null
        | sd_system
    )
;

sd_high_availability
:
    HIGH_AVAILABILITY
    (
      sdha_group
    )
;

sdha_group
:
    GROUP
    (
      sdhag_group_id
      | sdhag_mode
    )
;

sdhag_group_id: GROUP_ID id = ha_group_id;

sdhag_mode
:
    MODE
    (
      sdhagm_active_active
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

sd_null
:
    SETTING
    null_rest_of_line
;

sd_system
:
    SYSTEM
    (
        sds_default_gateway
        | sds_dns_setting
        | sds_domain
        | sds_hostname
        | sds_ip_address
        | sds_netmask
        | sds_ntp_servers
        | sds_panorama_server_null
        | sds_service_null
        | sds_timezone_null
        | sds_type_null
        | sds_update_schedule_null
        | sds_update_server_null
    )
;

sds_default_gateway
:
    DEFAULT_GATEWAY ip_address
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
    HOSTNAME name = variable
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
        sdsn_ntp_server_address
    )
;

sds_panorama_server_null
:
   PANORAMA_SERVER null_rest_of_line
;
sds_service_null
:
   SERVICE null_rest_of_line
;
sds_timezone_null
:
   TIMEZONE null_rest_of_line
;
sds_type_null
:
   TYPE null_rest_of_line
;
sds_update_schedule_null
:
   UPDATE_SCHEDULE null_rest_of_line
;
sds_update_server_null
:
   UPDATE_SERVER null_rest_of_line
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

