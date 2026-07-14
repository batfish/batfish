parser grammar CiscoNxosParser;

import
  CiscoNxos_common,
  CiscoNxos_aaa,
  CiscoNxos_bgp,
  CiscoNxos_class_map,
  CiscoNxos_crypto,
  CiscoNxos_dhcp,
  CiscoNxos_eigrp,
  CiscoNxos_evpn,
  CiscoNxos_fex,
  CiscoNxos_flow,
  CiscoNxos_interface,
  CiscoNxos_ip_access_list,
  CiscoNxos_ip_as_path_access_list,
  CiscoNxos_ip_community_list,
  CiscoNxos_ip_prefix_list,
  CiscoNxos_ipv6_access_list,
  CiscoNxos_ipv6_prefix_list,
  CiscoNxos_isis,
  CiscoNxos_lacp,
  CiscoNxos_line,
  CiscoNxos_logging,
  CiscoNxos_mac,
  CiscoNxos_monitor,
  CiscoNxos_ntp,
  CiscoNxos_object_group,
  CiscoNxos_ospf,
  CiscoNxos_ospfv3,
  CiscoNxos_policy_map,
  CiscoNxos_rip,
  CiscoNxos_rmon,
  CiscoNxos_route_map,
  CiscoNxos_snmp,
  CiscoNxos_static,
  CiscoNxos_tacacs_server,
  CiscoNxos_vdc,
  CiscoNxos_vlan,
  CiscoNxos_vrf;

options {
  superClass = 'org.batfish.vendor.cisco_nxos.grammar.CiscoNxosBaseParser';
  tokenVocab = CiscoNxosLexer;
}

cisco_nxos_configuration
:
  NEWLINE?
  statement+ EOF
;

statement
:
  s_aaa
  | s_banner
  | s_boot
  | s_class_map
  | s_cli_null
  | s_clock_null
  | s_control_plane
  | s_crypto
  | s_errdisable_null
  | s_evpn
  | s_fabric
  | s_feature_null
  | s_fex
  | s_flow
  | s_hostname
  | s_interface
  | s_ip
  | s_ipv6
  | s_key
  | s_lacp
  | s_license_null
  | s_line
  | s_logging
  | s_mac
  | s_monitor
  | s_no
  | s_ntp
  | s_nv
  | s_object_group
  | s_policy_map
  | s_rmon
  | s_role
  | s_route_map
  | s_router
  | s_service_null
  | s_snmp_server
  | s_spanning_tree_null
  | s_ssh_null
  | s_system
  | s_tacacs_server
  | s_track
  | s_username_null
  | s_userpassphrase_null
  | s_version
  | s_vdc
  | s_vlan
  | s_vrf_context
  | s_xml
;

s_banner
:
  BANNER
  (
    banner_exec
    | banner_motd
  )
;

banner_exec
:
  EXEC BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

banner_motd
:
  MOTD BANNER_DELIMITER body=BANNER_BODY? BANNER_DELIMITER NEWLINE
;

s_boot
:
  BOOT
  (
    boot_kickstart
    | boot_null
    | boot_nxos
    | boot_system
  )
;

boot_kickstart
:
  KICKSTART image = WORD (SUP_1 | SUP_2)? NEWLINE
;

boot_null
:
  (
    POAP
  ) null_rest_of_line
;

boot_nxos
:
  NXOS image = WORD (SUP_1 | SUP_2)? NEWLINE
;

boot_system
:
  SYSTEM image = WORD (SUP_1 | SUP_2)? NEWLINE
;

s_control_plane
:
  CONTROL_PLANE NEWLINE cp_service_policy*
;

cp_service_policy
:
  SERVICE_POLICY INPUT name = policy_map_cp_name NEWLINE
;

s_fabric: FABRIC (s_fabric_forwarding | s_fabric_null);

s_fabric_forwarding
:
  FORWARDING
  (
    ff_admin_distance
    | ff_anycast_gateway_mac
    | ff_dup_host_ip_addr_detection_null
    | ff_dup_host_recovery_timer_null
    | ff_dup_host_unfreeze_timer_null
    | ff_limit_vlan_mac_null
  )
;

ff_admin_distance: ADMIN_DISTANCE dist = protocol_distance NEWLINE;

ff_anycast_gateway_mac: ANYCAST_GATEWAY_MAC mac = mac_address_literal NEWLINE;

ff_dup_host_ip_addr_detection_null
:
   DUP_HOST_IP_ADDR_DETECTION null_rest_of_line
;
ff_dup_host_recovery_timer_null
:
   DUP_HOST_RECOVERY_TIMER null_rest_of_line
;
ff_dup_host_unfreeze_timer_null
:
   DUP_HOST_UNFREEZE_TIMER null_rest_of_line
;
ff_limit_vlan_mac_null
:
   LIMIT_VLAN_MAC null_rest_of_line
;

s_fabric_null: DATABASE null_rest_of_line;

s_hostname
:
  (HOSTNAME | SWITCHNAME) hostname = subdomain_name NEWLINE
;

s_ip
:
  IP
  (
    ip_access_list
    | ip_arp_null
    | ip_as_path_access_list
    | ip_community_list
    | ip_dhcp
    | ip_domain_list_null
    | ip_domain_lookup_null
    | ip_domain_name
    | ip_name_server
    | ip_pim
    | ip_prefix_list
    | ip_route
    | ip_tacacs
    | ip_sla
  )
;

ip_domain_name
:
  DOMAIN_NAME domain = domain_name NEWLINE
;

domain_name
:
// 1-64 characters
  WORD
;

ip_name_server
:
  NAME_SERVER servers += name_server+ (USE_VRF vrf = vrf_name)? NEWLINE
;

name_server
:
  ip_address
  | ipv6_address
;

ip_arp_null
:
   ARP null_rest_of_line
;
ip_domain_list_null
:
   DOMAIN_LIST null_rest_of_line
;
ip_domain_lookup_null
:
   DOMAIN_LOOKUP null_rest_of_line
;

ip_pim
:
  PIM
  (
    ipp_rp_address
    | ipp_rp_candidate
  )
;

ipp_rp_address
:
  RP_ADDRESS ip = ip_address
  (
    GROUP_LIST ip_prefix
    | PREFIX_LIST pl = ip_prefix_list_name
    | ROUTE_MAP map = route_map_name
  )? BIDIR? OVERRIDE? NEWLINE
;

ipp_rp_candidate
:
  RP_CANDIDATE interface_name
  (
    GROUP_LIST ip_prefix
    | PREFIX_LIST pl = ip_prefix_list_name
    | ROUTE_MAP rm = route_map_name
  ) NEWLINE
;

ip_sla
:
  SLA
  (
    ip_sla_block
    | ip_sla_group_null
    | ip_sla_logging_null
    | ip_sla_reaction_configuration_null
    | ip_sla_reaction_trigger_null
    | ip_sla_reset_null
    | ip_sla_responder_null
    | ip_sla_schedule_null
  )
;

ip_sla_block
:
  entry = uint32 NEWLINE
  ip_sla_entry+
;

ip_sla_entry
:
  (
    DNS
    | FREQUENCY
    | HTTP
    | ICMP_ECHO
    | TCP_CONNECT
    | THRESHOLD
    | TIMEOUT
    | UDP_ECHO
    | UDP_JITTER
    | VRF
  ) null_rest_of_line
;

ip_sla_group_null
:
   GROUP null_rest_of_line
;
ip_sla_logging_null
:
   LOGGING null_rest_of_line
;
ip_sla_reaction_configuration_null
:
   REACTION_CONFIGURATION null_rest_of_line
;
ip_sla_reaction_trigger_null
:
   REACTION_TRIGGER null_rest_of_line
;
ip_sla_reset_null
:
   RESET null_rest_of_line
;
ip_sla_responder_null
:
   RESPONDER null_rest_of_line
;
ip_sla_schedule_null
:
   SCHEDULE null_rest_of_line
;

s_ipv6
:
  IPV6
  (
    ipv6_access_list
    | ipv6_dhcp
    | ipv6_prefix_list
    | ipv6_route
  )
;

s_key
:
  KEY key_chain
;

key_chain
:
  CHAIN name = key_chain_name NEWLINE kc_key*
;

kc_key
:
  KEY num = uint16 NEWLINE kck_key_string*
;

kck_key_string
:
  KEY_STRING key_text = key_string_text NEWLINE
;

key_string_text
:
// 1-63 characters
  REMARK_TEXT
;

s_cli_null
:
   CLI null_rest_of_line
;
s_clock_null
:
   CLOCK null_rest_of_line
;
s_errdisable_null
:
   ERRDISABLE null_rest_of_line
;
s_feature_null
:
   FEATURE null_rest_of_line
;
s_license_null
:
   LICENSE null_rest_of_line
;
s_service_null
:
   SERVICE null_rest_of_line
;
s_ssh_null
:
   SSH null_rest_of_line
;
s_spanning_tree_null
:
   SPANNING_TREE null_rest_of_line
;
s_username_null
:
   USERNAME null_rest_of_line
;
s_userpassphrase_null
:
   USERPASSPHRASE null_rest_of_line
;

s_no
:
  NO
  (
    no_ip
    | no_system
    // no_null should come last
    | no_null
  )
;

no_ip: IP (
no_ip_adjacency_null
| no_ip_adjmgr_null
| no_ip_amt_null
| no_ip_arp_null
| no_ip_auto_discard_null
| no_ip_dns_null
| no_ip_domain_list_null
| no_ip_domain_lookup_null
| no_ip_dscp_lop_null
| no_ip_extcommunity_list_null
| no_ip_host_null
| no_ip_igmp_null
| no_ip_internal_null
| no_ip_load_sharing_null
| no_ip_mfwd_null
| no_ip_mroute_null
| no_ip_multicast_null
| no_ip_radius_null
| no_ip_route
| no_ip_routing_null
| no_ip_source_route_null
| no_ip_tcp_null
| no_ip_telnet_null
| no_ip_tftp_null
 );

no_ip_adjacency_null
:
   ADJACENCY null_rest_of_line
;
no_ip_adjmgr_null
:
   ADJMGR null_rest_of_line
;
no_ip_amt_null
:
   AMT null_rest_of_line
;
no_ip_arp_null
:
   ARP null_rest_of_line
;
no_ip_auto_discard_null
:
   AUTO_DISCARD null_rest_of_line
;
no_ip_dns_null
:
   DNS null_rest_of_line
;
no_ip_domain_list_null
:
   DOMAIN_LIST null_rest_of_line
;
no_ip_domain_lookup_null
:
   DOMAIN_LOOKUP null_rest_of_line
;
no_ip_dscp_lop_null
:
   DSCP_LOP null_rest_of_line
;
no_ip_extcommunity_list_null
:
   EXTCOMMUNITY_LIST null_rest_of_line
;
no_ip_host_null
:
   HOST null_rest_of_line
;
no_ip_igmp_null
:
   IGMP null_rest_of_line
;
no_ip_internal_null
:
   INTERNAL null_rest_of_line
;
no_ip_load_sharing_null
:
   LOAD_SHARING null_rest_of_line
;
no_ip_mfwd_null
:
   MFWD null_rest_of_line
;
no_ip_mroute_null
:
   MROUTE null_rest_of_line
;
no_ip_multicast_null
:
   MULTICAST null_rest_of_line
;
no_ip_radius_null
:
   RADIUS null_rest_of_line
;
no_ip_routing_null
:
   ROUTING null_rest_of_line
;
no_ip_source_route_null
:
   SOURCE_ROUTE null_rest_of_line
;
no_ip_tcp_null
:
   TCP null_rest_of_line
;
no_ip_telnet_null
:
   TELNET null_rest_of_line
;
no_ip_tftp_null
:
   TFTP null_rest_of_line
;

no_null
:
  (
    FEATURE
    | IP
    | NTP
  ) null_rest_of_line
;

no_system
:
  SYSTEM
  (
    no_sys_default
    | no_sys_interface_null
    | no_sys_mode_null
  )
;

no_sys_default
:
  DEFAULT no_sysd_switchport
;

no_sys_interface_null
:
   INTERFACE null_rest_of_line
;
no_sys_mode_null
:
   MODE null_rest_of_line
;

no_sysd_switchport
:
  SWITCHPORT
  (
    no_sysds_shutdown
    | no_sysds_switchport
  )
;

no_sysds_shutdown
:
  SHUTDOWN NEWLINE
;

no_sysds_switchport
:
  NEWLINE
;

s_nv
:
  NV OVERLAY EVPN NEWLINE
;

s_role
:
  ROLE NAME name = role_name NEWLINE ( role_description_null | role_rule_null )*
;

role_name
:
// 1-16 characters
  WORD
;

role_description_null
:
   DESCRIPTION null_rest_of_line
;
role_rule_null
:
   RULE null_rest_of_line
;

s_router
:
  ROUTER
  (
    router_bgp
    | router_eigrp
    | router_isis
    | router_ospf
    | router_ospfv3
    | router_rip
  )
;

s_system
:
  SYSTEM
  (
    sys_default
    | sys_qos
    | sys_vlan
  )
;

sys_default
:
  DEFAULT sysd_switchport
;

sysd_switchport
:
  SWITCHPORT
  (
    sysds_shutdown
    | sysds_switchport
  )
;

sysds_shutdown
:
  SHUTDOWN NEWLINE
;

sysds_switchport
:
  NEWLINE
;

sys_qos
:
  QOS NEWLINE
  (
    sysqos_null
    | sysqos_service_policy
  )*
;

sysqos_null
:
  (
    FEX
  ) null_rest_of_line
;

sysqos_service_policy
:
  SERVICE_POLICY TYPE 
  (
    sysqosspt_network_qos
    | sysqosspt_qos
    | sysqosspt_queueing
  )
;

sysqosspt_network_qos
:
  NETWORK_QOS name = policy_map_network_qos_name NEWLINE
;

sysqosspt_qos
:
  QOS INPUT name = policy_map_qos_name NEWLINE
;

sysqosspt_queueing
:
  QUEUING (INPUT | OUTPUT) name = policy_map_queuing_name NEWLINE
;

sys_vlan
:
  VLAN sysvlan_reserve
;

sysvlan_reserve
:
  // 2-3968
  first = vlan_id RESERVE NEWLINE
;

s_track: TRACK num = track_object_id track_definition;

track_definition
:
  track_interface
  | track_ip
;

track_interface: INTERFACE interface_name track_interface_mode NEWLINE;

track_interface_mode
:
  LINE_PROTOCOL
  | IP ROUTING
  | IPV6 ROUTING
;

// Currently unsupported tracking structures
track_ip
:
  IP
  (
    track_ip_route
    | track_ip_sla
  )
;

track_ip_route
:
  ROUTE prefix = ip_prefix REACHABILITY HMM? NEWLINE
  track_ip_route_inner*
;

track_ip_route_inner
:
  tir_vrf
  | tir_null
;

tir_vrf: VRF MEMBER name = vrf_name NEWLINE;

tir_null: DELAY null_rest_of_line;

track_ip_sla
:
  SLA null_rest_of_line
;

s_version
:
// arbitray string, not actual command
  VERSION version = REMARK_TEXT NEWLINE
;

s_xml: XML SERVER VALIDATE ALL NEWLINE;