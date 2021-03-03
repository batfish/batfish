parser grammar PaloAlto_deviceconfig;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_deviceconfig
:
    DEVICECONFIG
    (
        sd_null
        | sd_system
    )
;

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
        | sds_null
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

sds_null
:
    (
        PANORAMA_SERVER
        | SERVICE
        | TIMEZONE
        | TYPE
        | UPDATE_SCHEDULE
        | UPDATE_SERVER
    )
    null_rest_of_line
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

