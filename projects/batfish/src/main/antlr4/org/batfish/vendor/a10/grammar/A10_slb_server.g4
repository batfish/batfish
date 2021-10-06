parser grammar A10_slb_server;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

ss_server: SERVER slb_server_name slb_server_target? NEWLINE sss_definition*;

// TODO support other target types, like hostname or interface
slb_server_target: ip_address;

sss_definition
:
   sssd_conn_limit
   | sssd_disable
   | sssd_enable
   | sssd_port
   | sssd_stats_data_disable
   | sssd_stats_data_enable
   | sssd_template
   | sssd_weight
;

sssd_conn_limit: CONN_LIMIT connection_limit NEWLINE;

sssd_disable: DISABLE NEWLINE;

sssd_enable: ENABLE NEWLINE;

sssd_stats_data_disable: STATS_DATA_DISABLE NEWLINE;

sssd_stats_data_enable: STATS_DATA_ENABLE NEWLINE;

sssd_template: TEMPLATE sssdt_server;

sssdt_server: SERVER template_name NEWLINE;

sssd_weight: WEIGHT connection_weight NEWLINE;

sssd_port: PORT port_number tcp_or_udp (RANGE port_range_value)? NEWLINE sssdp_definition*;

sssdp_definition
:
   sssdpd_conn_limit
   | sssdpd_disable
   | sssdpd_enable
   | sssdpd_stats_data_disable
   | sssdpd_stats_data_enable
   | sssdpd_template
   | sssdpd_weight
;

sssdpd_conn_limit: CONN_LIMIT connection_limit NEWLINE;

sssdpd_disable: DISABLE NEWLINE;

sssdpd_enable: ENABLE NEWLINE;

sssdpd_stats_data_disable: STATS_DATA_DISABLE NEWLINE;

sssdpd_stats_data_enable: STATS_DATA_ENABLE NEWLINE;

sssdpd_template: TEMPLATE sssdpdt_port;

sssdpdt_port: PORT template_name NEWLINE;

sssdpd_weight: WEIGHT connection_weight NEWLINE;
