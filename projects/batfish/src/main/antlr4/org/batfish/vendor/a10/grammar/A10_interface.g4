parser grammar A10_interface;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_interface: INTERFACE si_definition;

si_definition: sid_ethernet | sid_loopback | sid_trunk | sid_ve;

sid_ethernet: ETHERNET num = ethernet_number NEWLINE sid*;

sid_loopback: LOOPBACK num = loopback_number NEWLINE sid*;

sid_trunk: TRUNK num = trunk_number NEWLINE sid*;

sid_ve: VE num = vlan_number NEWLINE sid*;

sid
:
   sid_disable
   | sid_duplexity
   | sid_enable
   | sid_ip
   | sid_ipg_bit_time
   | sid_lacp
   | sid_lldp
   | sid_load_interval
   | sid_mtu
   | sid_name
   | sid_ports_threshold
   | sid_speed
   | sid_trunk_group
;

sid_enable: ENABLE NEWLINE;

sid_disable: DISABLE NEWLINE;

sid_duplexity: DUPLEXITY null_rest_of_line;

sid_ipg_bit_time: IPG_BIT_TIME null_rest_of_line;

sid_load_interval: LOAD_INTERVAL null_rest_of_line;

sid_ip: IP sidi_address;

sidi_address: ADDRESS ip_prefix NEWLINE;

sid_mtu: MTU interface_mtu NEWLINE;

sid_name: NAME interface_name_str NEWLINE;

// TODO support non-default speed
sid_speed: SPEED AUTO NEWLINE;

sid_ports_threshold: PORTS_THRESHOLD ports_threshold sidpt_options;

sidpt_options: (TIMER ports_threshold_timer)? DO_AUTO_RECOVERY? NEWLINE;

sid_trunk_group: TRUNK_GROUP trunk_number (trunk_type)? NEWLINE sidtg*;

sidtg: sidtg_mode | sidtg_timeout | sidtg_user_tag;

sidtg_mode: MODE trunk_mode NEWLINE;

sidtg_timeout: TIMEOUT trunk_timeout NEWLINE;

sidtg_user_tag: USER_TAG tag = user_tag NEWLINE;

// 434-1500
interface_mtu: uint16;

trunk_type: STATIC | LACP | LACP_UDLD;

trunk_mode: ACTIVE | PASSIVE;

trunk_timeout: SHORT | LONG;

sid_lldp: LLDP sidll;

sidll: sidll_enable | sidll_notification;

sidll_enable: ENABLE sidlle+ NEWLINE;

sidlle: RX | TX;

sidll_notification: NOTIFICATION ENABLE NEWLINE;

// ACOS 2.X syntax
sid_lacp: LACP sidl;

sidl: sidl_trunk | sidl_timeout;

sidl_trunk: TRUNK num = trunk_number sidlt_mode;

sidlt_mode: MODE trunk_mode NEWLINE;

sidl_timeout: TIMEOUT trunk_timeout NEWLINE;
