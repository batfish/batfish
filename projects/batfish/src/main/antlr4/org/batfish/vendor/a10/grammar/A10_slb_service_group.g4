parser grammar A10_slb_service_group;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

ss_service_group: SERVICE_GROUP service_group_name tcp_or_udp? NEWLINE sssg_definition*;

sssg_definition
:
   sssgd_health_check
   | sssgd_member
   | sssgd_method
   | sssgd_stats_data_disable
   | sssgd_stats_data_enable
;

sssgd_health_check: HEALTH_CHECK health_check_name NEWLINE;

// TODO support declaring a new server in this context, i.e. when IP address is provided as well
sssgd_member: MEMBER slb_server_name port_number NEWLINE sssgdm_definition*;

sssgd_method: METHOD service_group_method NEWLINE;

sssgd_stats_data_disable: STATS_DATA_DISABLE NEWLINE;

sssgd_stats_data_enable: STATS_DATA_ENABLE NEWLINE;

sssgdm_definition
:
   sssgdmd_disable
   | sssgdmd_enable
   | sssgdmd_priority
;

sssgdmd_disable: DISABLE NEWLINE;

sssgdmd_enable: ENABLE NEWLINE;

sssgdmd_priority: PRIORITY service_group_member_priority NEWLINE;

// TODO support lots of other methods
service_group_method: LEAST_REQUEST | ROUND_ROBIN;

// 1-16
service_group_member_priority: uint8;
