parser grammar A10_slb_service_group;

import A10_common;

options {
   tokenVocab = A10Lexer;
}

ss_service_group: SERVICE_GROUP service_group_name tcp_or_udp? NEWLINE sssg_definition*;

sssg_definition
:
   sssgd_health_check
   | sssgd_health_check_disable
   | sssgd_member
   | sssgd_method
   | sssgd_min_active_member
   | sssgd_stats_data_disable
   | sssgd_stats_data_enable
;

sssgd_health_check: HEALTH_CHECK health_check_name NEWLINE;

sssgd_health_check_disable: HEALTH_CHECK_DISABLE NEWLINE;

// TODO support declaring a new server in this context, i.e. when IP address is provided as well
sssgd_member: MEMBER slb_server_name sssgd_member_tail sssgdm_definition*;

sssgd_member_tail: sssgd_member_port_number | sssgd_member_acos2_tail;

// Syntax for ACOS v4+
sssgd_member_port_number: port_number NEWLINE;

// Alternate syntax for ACOS v2
sssgd_member_acos2_tail: (sssgd_member_disable | sssgd_member_priority)* NEWLINE;

sssgd_member_disable: DISABLE;

sssgd_member_priority: PRIORITY service_group_member_priority;

sssgd_method: METHOD service_group_method NEWLINE;

sssgd_min_active_member: MIN_ACTIVE_MEMBER minimum_active_member NEWLINE;

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
service_group_method
:
   LEAST_CONNECTION
   | LEAST_REQUEST
   | ROUND_ROBIN
   | SERVICE_LEAST_CONNECTION
;

// 1-16
service_group_member_priority: uint8;

// 1-1024
minimum_active_member: uint16;
