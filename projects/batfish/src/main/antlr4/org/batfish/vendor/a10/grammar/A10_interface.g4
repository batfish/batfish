parser grammar A10_interface;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_interface: INTERFACE si_definition;

si_definition: sid_ethernet | sid_loopback | sid_trunk | sid_ve;

sid_ethernet: ETHERNET num = ethernet_number newline sid*;

sid_loopback: LOOPBACK num = loopback_number newline sid*;

sid_trunk: TRUNK num = trunk_number newline sid*;

sid_ve: VE num = vlan_number newline sid*;

sid: sid_disable | sid_enable | sid_ip | sid_lacp | sid_mtu | sid_name | sid_trunk_group;

sid_enable: ENABLE newline;

sid_disable: DISABLE newline;

sid_ip: IP sidi_address;

sidi_address: ADDRESS ip_prefix newline;

sid_mtu: MTU interface_mtu newline;

sid_name: NAME interface_name_str newline;

sid_trunk_group: TRUNK_GROUP trunk_number (trunk_type)? newline sidtg*;

sidtg: sidtg_mode | sidtg_timeout | sidtg_user_tag;

sidtg_mode: MODE trunk_mode newline;

sidtg_timeout: TIMEOUT trunk_timeout newline;

sidtg_user_tag: USER_TAG tag = user_tag newline;

// 434-1500
interface_mtu: uint16;

trunk_type: STATIC | LACP | LACP_UDLD;

trunk_mode: ACTIVE | PASSIVE;

trunk_timeout: SHORT | LONG;

// ACOS 2.X syntax
sid_lacp: LACP sidl;

sidl: sidl_trunk | sidl_timeout;

sidl_trunk: TRUNK num = trunk_number sidlt_mode;

sidlt_mode: MODE trunk_mode newline;

sidl_timeout: TIMEOUT trunk_timeout newline;
