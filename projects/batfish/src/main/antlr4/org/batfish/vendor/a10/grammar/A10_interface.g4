parser grammar A10_interface;

import A10_common;

options {
    tokenVocab = A10Lexer;
}

s_interface: INTERFACE si_definition;

si_definition: sid_ethernet | sid_loopback;

sid_ethernet: ETHERNET num = ethernet_number NEWLINE sid*;

sid_loopback: LOOPBACK num = loopback_number NEWLINE sid*;

sid: (sid_disable | sid_enable | sid_ip | sid_mtu | sid_name);

sid_enable: ENABLE NEWLINE;

sid_disable: DISABLE NEWLINE;

sid_ip: IP sidi_address;

sidi_address: ADDRESS ip_prefix NEWLINE;

sid_mtu: MTU interface_mtu NEWLINE;

sid_name: NAME interface_name_str NEWLINE;

// 1-40? assuming 40 is max for now
ethernet_number: uint8;

// 0-10
loopback_number: uint8;

// 434-1500
interface_mtu: uint16;
