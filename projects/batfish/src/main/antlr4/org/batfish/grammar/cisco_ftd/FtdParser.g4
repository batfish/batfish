parser grammar FtdParser;

import
Ftd_common,
Ftd_interface,
Ftd_acl,
Ftd_object,
Ftd_route,
Ftd_nat,
Ftd_failover,
Ftd_crypto,
Ftd_tunnel_group,
Ftd_ospf,
Ftd_bgp;

options {
   superClass = 'org.batfish.grammar.cisco_ftd.parsing.FtdBaseParser';
   tokenVocab = FtdLexer;
}

ftd_configuration
:
   NEWLINE?
   (
      stanza
      | NEWLINE
   )* EOF
;

stanza
:
   enable_stanza
   | hostname_stanza
   | interface_stanza
   | access_list_stanza
   | access_group_stanza
   | names_stanza
   | object_stanza
   | object_group_stanza
   | object_group_search_stanza
   | route_stanza
   | nat_stanza
   | dns_stanza
   | ftp_stanza
   | ngips_stanza
   | service_module_stanza
   | time_range_stanza
   | failover_stanza
   | crypto_stanza
   | tunnel_group_stanza
   | router_ospf_stanza
   | router_bgp_stanza
   | logging_stanza
   | mtu_stanza
   | timeout_stanza
   | ssh_stanza
   | telnet_stanza
   | icmp_stanza
   | snmp_server_stanza
   | arp_stanza
   | version_stanza
   | enable_password_stanza
   | names_stanza
   | mac_address_stanza
   | cts_stanza
   | snort_stanza
   | flow_offload_stanza
   | cryptochecksum_stanza
   | class_map_stanza
   | policy_map_stanza
   | service_policy_stanza
   | threat_detection_stanza
   | monitor_interface_stanza
   | pager_stanza
   | aaa_stanza
   | mac_address_stanza
   | unrecognized_stanza
;

enable_stanza
:
   ENABLE PASSWORD password = ~NEWLINE+ NEWLINE
;

hostname_stanza
:
   HOSTNAME (name_parts += ~NEWLINE)+ NEWLINE
;

names_stanza
:
   NO? NAMES NEWLINE
;

dns_stanza
:
   DNS
   (
      dns_domain_lookup
      | dns_server_group
      | dns_group
   )
;

dns_domain_lookup
:
   DOMAIN_LOOKUP iface_name = ~NEWLINE+ NEWLINE
;

dns_server_group
:
   SERVER_GROUP name = ~NEWLINE+ NEWLINE
   dns_server_group_tail*
;

dns_server_group_tail
:
   (
      NAME_SERVER ip = IP_ADDRESS NEWLINE
      | TIMEOUT timeout = dec NEWLINE
      | stanza_unrecognized_line
   )
;

dns_group
:
   DNS_GROUP name = ~NEWLINE+ NEWLINE
;

ftp_stanza
:
   FTP MODE mode_value = ~NEWLINE+ NEWLINE
;


ngips_stanza
:
   NGIPS null_rest_of_line
;

service_module_stanza
:
   SERVICE_MODULE null_rest_of_line
;

time_range_stanza
:
   TIME_RANGE name = ~NEWLINE+ NEWLINE
   time_range_tail*
;

time_range_tail
:
   (
      ABSOLUTE null_rest_of_line
      | stanza_unrecognized_line
   )
;

logging_stanza
:
   LOGGING null_rest_of_line
;

mtu_stanza
:
   MTU iface_name = ~NEWLINE+ mtu_value = dec NEWLINE
;

timeout_stanza
:
   TIMEOUT null_rest_of_line
;

ssh_stanza
:
   SSH null_rest_of_line
;

telnet_stanza
:
   TELNET null_rest_of_line
;

icmp_stanza
:
   ICMP null_rest_of_line
;

snmp_server_stanza
:
   SNMP_SERVER null_rest_of_line
;

arp_stanza
:
   ARP null_rest_of_line
;

class_map_stanza
:
   CLASS_MAP class_map_type? name = class_map_name NEWLINE
   class_map_tail*
;

class_map_type
:
   TYPE type = class_map_type_value
;

class_map_type_value
:
   NAME
   | WORD
;

class_map_name
:
   name_parts += ~NEWLINE+
;

class_map_tail
:
   (
      MATCH null_rest_of_line
      | description_line
      | stanza_unrecognized_line
   )
;

policy_map_stanza
:
   POLICY_MAP policy_map_type? name = policy_map_name NEWLINE
   policy_map_tail*
;

policy_map_type
:
   TYPE type = policy_map_type_value
;

policy_map_type_value
:
   NAME
   | WORD
;

policy_map_name
:
   name_parts += ~NEWLINE+
;

policy_map_tail
:
   (
      CLASS null_rest_of_line
      | PARAMETERS null_rest_of_line
      | description_line
      | stanza_unrecognized_line
   )
;

service_policy_stanza
:
   SERVICE_POLICY policy_name = service_policy_name service_policy_scope? NEWLINE
;

service_policy_name
:
   name_parts += ~(GLOBAL | INTERFACE | NEWLINE)+
;

service_policy_scope
:
   GLOBAL
   | INTERFACE interface_name_value = service_policy_interface_name
;

service_policy_interface_name
:
   name_parts += ~NEWLINE+
;
threat_detection_stanza
:
   THREAT_DETECTION null_rest_of_line
;

monitor_interface_stanza
:
   MONITOR_INTERFACE null_rest_of_line
;

access_group_stanza
:
   ACCESS_GROUP name = access_group_name access_group_tail? NEWLINE
;

access_group_name
:
   name_parts += ~(GLOBAL | IN | OUT | INTERFACE | NEWLINE)+
;

access_group_tail
:
   GLOBAL
   | access_group_direction (INTERFACE interface_name_value = access_group_interface_name)?
;

access_group_direction
:
   IN
   | OUT
;

access_group_interface_name
:
   name_parts += ~NEWLINE+
;

pager_stanza
:
   NO? PAGER null_rest_of_line
;

aaa_stanza
:
   AAA null_rest_of_line
;

mac_address_stanza
:
   NO? MAC_ADDRESS AUTO NEWLINE
;

version_stanza
:
   NGFW VERSION version = ~NEWLINE* NEWLINE
;

enable_password_stanza
:
   ENABLE PASSWORD ~NEWLINE* NEWLINE
;

cts_stanza
:
   CTS null_rest_of_line
;

snort_stanza
:
   SNORT null_rest_of_line
;

flow_offload_stanza
:
   NO? FLOW_OFFLOAD null_rest_of_line
;

cryptochecksum_stanza
:
   CRYPTOCHECKSUM ~NEWLINE* NEWLINE
;

// Generic unrecognized line - matches any line
unrecognized_line
:
   ~NEWLINE* NEWLINE
;

// Restricted unrecognized line that doesn't match lines starting with stanza keywords
// Used in stanza tail rules to prevent consuming subsequent stanzas
stanza_unrecognized_line
:
   ~(NEWLINE | INTERFACE | ACCESS_LIST | OBJECT | OBJECT_GROUP | HOSTNAME | ROUTE | NAT | FAILOVER | CRYPTO | ACCESS_GROUP | DNS | FTP | TIME_RANGE | CLASS_MAP | POLICY_MAP | SERVICE_POLICY | TUNNEL_GROUP)
   ~NEWLINE* NEWLINE
;

unrecognized_stanza
:
   unrecognized_line
;
