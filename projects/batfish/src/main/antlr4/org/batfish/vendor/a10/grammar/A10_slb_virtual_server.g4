parser grammar A10_slb_virtual_server;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

ss_virtual_server: VIRTUAL_SERVER virtual_server_name virtual_server_target? NEWLINE ssvs_definition*;

virtual_server_target: ip_address | ipv6_address;

ssvs_definition
:
   ssvs_disable
   | ssvs_enable
   | ssvs_ha_group
   | ssvs_redistribution_flagged
   | ssvs_port
   | ssvs_stats_data_disable
   | ssvs_stats_data_enable
   | ssvs_template
   | ssvs_vrid
;

ssvs_disable: DISABLE NEWLINE;

ssvs_enable: ENABLE NEWLINE;

ssvs_ha_group: HA_GROUP id = ha_group_id NEWLINE;

ssvs_redistribution_flagged: REDISTRIBUTION_FLAGGED NEWLINE;

ssvs_stats_data_disable: STATS_DATA_DISABLE NEWLINE;

ssvs_stats_data_enable: STATS_DATA_ENABLE NEWLINE;

ssvs_template: TEMPLATE null_rest_of_line;

ssvs_vrid: VRID non_default_vrid NEWLINE;

ssvs_port: PORT port_number virtual_server_port_type (RANGE port_range_value)? NEWLINE ssvsp_definition*;

ssvsp_definition
:
   ssvspd_access_list
   | ssvspd_aflex
   | ssvspd_bucket_count
   | ssvspd_conn_limit
   | ssvspd_disable
   | ssvspd_enable
   | ssvspd_def_selection_if_pref_failed
   | ssvspd_name
   | ssvspd_no_auto_up_on_aflex
   | ssvspd_optimization_level
   | ssvspd_service_group
   | ssvspd_source_nat
   | ssvspd_stats_data_disable
   | ssvspd_stats_data_enable
   | ssvspd_template
   | ssvspd_use_rcv_hop_for_resp
;

ssvspd_access_list: ACCESS_LIST NAME access_list_name NEWLINE;

ssvspd_aflex: AFLEX aflex_name NEWLINE;

ssvspd_bucket_count: BUCKET_COUNT traffic_bucket_count NEWLINE;

ssvspd_conn_limit: CONN_LIMIT connection_limit NEWLINE;

ssvspd_disable: DISABLE NEWLINE;

ssvspd_enable: ENABLE NEWLINE;

ssvspd_def_selection_if_pref_failed: DEF_SELECTION_IF_PREF_FAILED NEWLINE;

ssvspd_name: NAME virtual_service_name NEWLINE;

ssvspd_no_auto_up_on_aflex: NO_AUTO_UP_ON_AFLEX NEWLINE;

ssvspd_optimization_level: OPTIMIZATION_LEVEL null_rest_of_line;

ssvspd_service_group: SERVICE_GROUP service_group_name NEWLINE;

ssvspd_source_nat: SOURCE_NAT POOL nat_pool_name NEWLINE;

ssvspd_stats_data_disable: STATS_DATA_DISABLE NEWLINE;

ssvspd_stats_data_enable: STATS_DATA_ENABLE NEWLINE;

ssvspd_template: TEMPLATE null_rest_of_line;

ssvspd_use_rcv_hop_for_resp: USE_RCV_HOP_FOR_RESP NEWLINE;

// 1-256
traffic_bucket_count: uint16;

// TODO add many more types
virtual_server_port_type
:
   DIAMETER
   | HTTP
   | HTTPS
   | RADIUS
   | SIP
   | SMTP
   | SSL_PROXY
   | TCP
   | TCP_PROXY
   | UDP
;
