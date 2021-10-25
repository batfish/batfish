parser grammar A10_ha;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_ha
:
  HA
  (
    ha_arp_retry
    | ha_check
    | ha_conn_mirror
    | ha_group
    | ha_id
    | ha_interface
    | ha_preemption_enable
    | ha_restart_time
    | ha_time_interval
    | ha_timeout_retry_count
  )
;

ha_arp_retry: ARP_RETRY null_rest_of_line;

ha_check: CHECK null_rest_of_line;

ha_conn_mirror: CONN_MIRROR IP ip = ip_address NEWLINE;

ha_group: GROUP id = ha_group_id PRIORITY priority = ha_priority_number NEWLINE;

ha_id: ID id = ha_id_number SET_ID set_id = ha_set_id_number NEWLINE;

// 1-2
ha_id_number: uint8;

// 1-7
ha_set_id_number: uint8;

// 1-255
ha_priority_number: uint8;

ha_interface
:
  INTERFACE ref = ethernet_or_trunk_reference
  (
    BOTH
    | ROUTER_INTERFACE
    | SERVER_INTERFACE
  )?
  (
    NO_HEARTBEAT
    | VLAN vlan_number
  )? NEWLINE
;

ha_preemption_enable: PREEMPTION_ENABLE NEWLINE;

sn_ha: HA snha_preemption_enable;

snha_preemption_enable: PREEMPTION_ENABLE NEWLINE;

ha_restart_time: RESTART_TIME null_rest_of_line;

ha_time_interval: TIME_INTERVAL null_rest_of_line;

ha_timeout_retry_count: TIMEOUT_RETRY_COUNT null_rest_of_line;
