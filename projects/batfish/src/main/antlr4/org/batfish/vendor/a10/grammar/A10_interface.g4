parser grammar A10_interface;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_interface: INTERFACE si_definition;

si_definition: sid_ethernet | sid_loopback | sid_ve;

sid_ethernet: ETHERNET num = ethernet_number newline sid*;

sid_loopback: LOOPBACK num = loopback_number newline sid*;

sid_ve: VE num = vlan_number newline sid*;

sid: (sid_disable | sid_enable | sid_ip | sid_mtu | sid_name);

sid_enable: ENABLE newline;

sid_disable: DISABLE newline;

sid_ip: IP sidi_address;

sidi_address: ADDRESS ip_prefix newline;

sid_mtu: MTU interface_mtu newline;

sid_name: NAME interface_name_str newline;

// 434-1500
interface_mtu: uint16;
