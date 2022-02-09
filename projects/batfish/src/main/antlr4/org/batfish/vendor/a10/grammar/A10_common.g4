parser grammar A10_common;

options {
    tokenVocab = A10Lexer;
}

quoted_text: QUOTED_TEXT;
double_quoted_string: DOUBLE_QUOTE text = quoted_text? DOUBLE_QUOTE;
single_quoted_string: SINGLE_QUOTE text = quoted_text? SINGLE_QUOTE;
word_content: (double_quoted_string | single_quoted_string | WORD)+;
word: WORD_SEPARATOR word_content;

access_list_name: word;

// 1-199 (for unnamed access lists)
access_list_number: uint8;

access_list_action: PERMIT | DENY;

access_list_address_any: ANY;

access_list_address_host: HOST address = ip_address;

access_list_protocol: ICMP | IP | TCP | UDP;

aflex_name: word;

health_check_name: word;

hostname: word;

ethernet_or_trunk_reference
:
  ETHERNET ethnum = ethernet_number
  | TRUNK trunknum = trunk_number
;

interface_name_str: word;

nat_pool_name: word;

partition_name: word;

route_description: word;

service_group_name: word;

slb_server_name: word;

template_name: word;

vlan_name: word;

user_tag: word;

// 1-127 chars
virtual_server_name: word;

// 1-127 chars
virtual_service_name: word;

ip_prefix: ip_address ip_netmask;

ip_netmask: subnet_mask | ip_slash_prefix;

// Wildcard formatted as 1.1.1.0 /24 or 1.0.1.0 0.255.0.255 (1s are ignore bits)
ip_wildcard: ip_address ip_wildcard_mask;

ip_wildcard_mask: ip_slash_prefix | ip_address;

ip_address: IP_ADDRESS | SUBNET_MASK;

ip_slash_prefix: IP_SLASH_PREFIX;

ipv6_address: IPV6_ADDRESS;

subnet_mask: SUBNET_MASK;

null_rest_of_line: ~NEWLINE* NEWLINE;

// 1-4294967295
bgp_asn: uint32;

// 2-4094
vlan_number: uint16;

// 1-40? assuming 40 is max for now
ethernet_number: uint8;

// 0-10
loopback_number: uint8;

// 1-31
ha_group_id: uint8;

// 2-8
ports_threshold: uint8;

// 1-300
ports_threshold_timer: uint16;

// 1-16
scaleout_device_id: uint8;

// 1-4096
trunk_number: uint16;

// 0-31
vrid: uint8;

// 1-31
non_default_vrid: uint8;

// 1-64000000
connection_limit: uint32;

// 1-1000
connection_weight: uint16;

// 0-65535; 0 in this context seems to mean non-port-based protocol
port_number: uint16;

// 1-65535
acl_port_number: uint16;

// 0-254
port_range_value: uint8;

uint8: UINT8;

uint16: UINT8 | UINT16;

uint32: UINT8 | UINT16 | UINT32;

tcp_or_udp: TCP | UDP;
