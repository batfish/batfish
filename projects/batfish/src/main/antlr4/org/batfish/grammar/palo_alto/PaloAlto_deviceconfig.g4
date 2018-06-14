parser grammar PaloAlto_deviceconfig;

import PaloAlto_common;

options {
    tokenVocab = PaloAltoLexer;
}

s_deviceconfig
:
    DEVICECONFIG
    (
        sd_system
    )
;

sd_system
:
    SYSTEM
    (
        sds_dns_setting
        | sds_hostname
        | sds_ntp_servers
    )
;

sds_dns_setting
:
    DNS_SETTING
    (
        sdsd_servers
    )
;

sdsd_servers
:
    SERVERS
    (
        PRIMARY primary_name = IP_ADDRESS
        | SECONDARY secondary_name = IP_ADDRESS
    )
;

sds_hostname
:
    HOSTNAME name = variable
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

sdsn_ntp_server_address
:
    NTP_SERVER_ADDRESS address = variable
;
