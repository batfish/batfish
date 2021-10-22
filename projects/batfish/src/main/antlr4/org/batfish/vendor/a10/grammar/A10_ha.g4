parser grammar A10_ha;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_ha
:
  HA
  (
    ha_group
    | ha_id
    | ha_interface
    | ha_null
    | ha_preemption_enable
  )
;

ha_group: GROUP id = ha_group_id ha_group_option* NEWLINE;

ha_group_option: hago_priority;

ha_id: ID id = ha_id_number SET_ID set_id = ha_set_id_number NEWLINE;

// TODO: check range on ACOSv2 device
ha_id_number: uint8;

// TODO: check range on ACOSv2 device
ha_set_id_number: uint8;

hago_priority: PRIORITY priority = ha_priority_number;

// TODO: check range on ACOSv2 device
ha_priority_number: uint8;

ha_interface: INTERFACE ref = ethernet_or_trunk_reference ha_interface_option* NEWLINE;

ha_interface_option
:
  BOTH
  | ROUTER_INTERFACE
  | NO_HEARTBEAT
  | haio_vlan
;

haio_vlan: VLAN vlan_number;

ha_preemption_enable: PREEMPTION_ENABLE NEWLINE;

sn_ha: HA snha_preemption_enable;

snha_preemption_enable: PREEMPTION_ENABLE NEWLINE;

ha_null
:
  (
    ARP_RETRY
    | CHECK
    | CONN_MIRROR
    | RESTART_TIME
    | TIME_INTERVAL
    | TIMEOUT_RETRY_COUNT
  ) null_rest_of_line
;