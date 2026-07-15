parser grammar CiscoNxos_interface;

import CiscoNxos_common;

options {
  tokenVocab = CiscoNxosLexer;
}

s_interface
:
  INTERFACE
  (
    s_interface_breakout
    | s_interface_nve
    | s_interface_regular
  )
;

s_interface_breakout
:
  BREAKOUT MODULE module = uint8 PORT ranges = uint8_range_set MAP TENG_4X NEWLINE
;

s_interface_nve
:
  nverange = nve_interface_range NEWLINE
  (
    nve_global
    | nve_host_reachability
    | nve_member
    | nve_no
    | nve_source_interface
  )*
;

s_interface_regular
:
  irange = interface_range NEWLINE
  (
    i_autostate
    | i_bandwidth
    | i_bfd_null
    | i_carrier_delay_null
    | i_cdp_null
    | i_channel_group
    | i_delay
    | i_description
    | i_duplex_null
    | i_encapsulation
    | i_fabric
    | i_fex_null
    | i_flowcontrol_null
    | i_hardware_null
    | i_hsrp
    | i_ip
    | i_ipv6
    | i_isis
    | i_lacp
    | i_link_null
    | i_lldp_null
    | i_load_interval_null
    | i_logging_null
    | i_mac_address
    | i_mdix_null
    | i_medium_null
    | i_mtu
    | i_negotiate_null
    | i_no
    | i_ospfv3
    | i_packet_null
    | i_priority_flow_control_null
    | i_private_vlan
    | i_service_policy
    | i_shutdown
    | i_snmp_null
    | i_spanning_tree_null
    | i_speed
    | i_storm_control_null
    | i_switchport
    | i_udld_null
    | i_vpc_null
    | i_vrf_member
    | i_vtp_null
  )*
;

i_autostate
:
  AUTOSTATE NEWLINE
;

i_bandwidth
:
  BANDWIDTH
  (
    inherit = INHERIT bw = interface_bandwidth_kbps?
    | bw = interface_bandwidth_kbps
  ) NEWLINE
;

interface_bandwidth_eigrp_kbps
:
// 1-2560000000, units are kbps (for effective range of 1kbps-2.56Tbps)
  uint32
;

interface_bandwidth_kbps
:
// 1-100000000, units are kbps (for effective range of 1kbps-100Gbps) for physical interfaces.
// 1-3200000000, units are kbps (for effective range of 1kbps-3.2Tbps) for port-channels,
//               which can bundle 32 physical interfaces
  uint32
;

i_channel_group
:
  CHANNEL_GROUP id = channel_id force = FORCE? (MODE (ACTIVE|ON|PASSIVE))? NEWLINE
;

channel_id
:
// 1-4096
  UINT8
  | UINT16
;

i_delay
:
  DELAY delay = interface_delay_10us NEWLINE
;

interface_delay_10us
:
// 1-16777215 tens of microseconds
  uint32
;

i_description
:
  DESCRIPTION desc = interface_description NEWLINE
;

interface_description
:
  REMARK_TEXT
;

i_encapsulation
:
  ENCAPSULATION DOT1Q vlan = unreserved_vlan_id NEWLINE
;

i_fabric: FABRIC (i_fabric_forwarding | i_fabric_null);

i_fabric_forwarding: FORWARDING MODE ANYCAST_GATEWAY NEWLINE;

i_fabric_null: DATABASE null_rest_of_line;

i_hsrp
:
  HSRP
  (
    | ih_bfd
    | ih_delay
    | ih_group
    | ih_version
  )
;

ih_bfd
:
  BFD NEWLINE
;

ih_delay
:
  DELAY
  (
    ihd_minimum ihd_reload?
    | ihd_reload ihd_minimum?
  ) NEWLINE
;

ihd_minimum
:
  MINIMUM delay_s = hsrp_delay_minimum
;

hsrp_delay_minimum
:
// 0-10000 (s)
  uint16
;

ihd_reload
:
  RELOAD delay_s = hsrp_delay_reload
;

hsrp_delay_reload
:
// 0-10000 (s)
  uint16
;

ih_group
:
  group = hsrp_group_number
  (
    ihg_ipv4
    | ihg_ipv6
  )
;

ihg_ipv4
:
  NEWLINE
  (
    ihg_common
    | ihg4_ip
  )*
;

ihg_ipv6
:
  IPV6 NEWLINE
  (
    ihg_common
    | ihg6_ip
  )*
;

ihg_common
:
  ihg_authentication
  | ihg_bfd
  | ihg_mac_address
  | ihg_name
  | ihg_no
  | ihg_preempt
  | ihg_priority
  | ihg_timers
  | ihg_track
;

hsrp_group_number
:
// 0-4095
  uint16
;

ihg_authentication
:
  AUTHENTICATION
  (
    ihga_md5
    | ihga_text
  )
;

ihga_md5
:
  MD5 ihgam_key_chain
;

ihga_text
:
  TEXT text = hsrp_authentication_string NEWLINE
;

hsrp_authentication_string
:
// 1-8 characters
  WORD
;

ihgam_key_chain
:
  KEY_CHAIN name = key_chain_name NEWLINE
;

ihg_bfd: BFD NEWLINE;

ihg_mac_address
:
  MAC_ADDRESS mac = mac_address_literal NEWLINE
;

ihg_name
:
  NAME name = hsrp_name NEWLINE
;

ihg_no
:
  NO (
    ihg_no_bfd
    | ihg_no_preempt
  )
;

ihg_no_bfd: BFD NEWLINE;

ihg_no_preempt: PREEMPT NEWLINE;

ihg_preempt
:
  PREEMPT
  (
    DELAY
    (
      MINIMUM minimum_s = hsrp_preempt_delay
      | RELOAD reload_s = hsrp_preempt_delay
      | SYNC sync_s = hsrp_preempt_delay
    )*
  )? NEWLINE
;

hsrp_preempt_delay
:
// 0-3600 (s)
  uint16
;

ihg_priority
:
  PRIORITY priority = uint8
  (
    FORWARDING_THRESHOLD LOWER lower = uint8 UPPER upper = uint8
  )? NEWLINE
;

ihg_timers
:
  TIMERS
  (
    hello_interval_s = hsrp_timers_hello_interval_s
    | MSEC hello_interval_ms = hsrp_timers_hello_interval_ms
  )
  (
    hold_time_s = hsrp_timers_hold_time_s
    | MSEC hold_time_ms = hsrp_timers_hold_time_ms
  ) NEWLINE
;

hsrp_timers_hello_interval_s
:
// 1-254 (s)
  uint8
;

hsrp_timers_hello_interval_ms
:
// 250-999 (ms)
  uint16
;

hsrp_timers_hold_time_s
:
// 3-255 (s)
  uint8
;

hsrp_timers_hold_time_ms
:
// 750-3000 (ms)
  uint16
;

ihg_track
:
  TRACK num = track_object_id (DECREMENT decrement = hsrp_track_decrement)? NEWLINE
;

hsrp_track_decrement
:
// 1-255
  uint8
;

ihg4_ip
:
  IP
  (
    ip = ip_address
    | prefix = ip_prefix
  ) SECONDARY? NEWLINE
;

ihg6_ip
:
  IP
  (
    ihg6_ip_address
    | ihg6_ip_autoconfig
  )
;

ihg6_ip_address
:
  (
    ip6 = ipv6_address
    | prefix6 = ipv6_prefix
  ) SECONDARY? NEWLINE
;

ihg6_ip_autoconfig
:
  AUTOCONFIG NEWLINE
;

ih_version
:
  VERSION version = hsrp_version NEWLINE
;

hsrp_version
:
  HSRP_VERSION_1
  | HSRP_VERSION_2  
;

i_ip
:
  IP
  (
    i_ip_access_group
    | i_ip_address
    | i_ip_arp_null
    | i_ip_authentication
    | i_ip_bandwidth
    | i_ip_delay
    | i_ip_directed_broadcast_null
    | i_ip_distribute_list
    | i_ip_dhcp
    | i_ip_eigrp
    | i_ip_flow_null
    | i_ip_forward
    | i_ip_hello_interval
    | i_ip_hold_time
    | i_ip_igmp
    | i_ip_ospf
    | i_ip_passive_interface
    | i_ip_pim
    | i_ip_policy
    | i_ip_port
    | i_ip_port_unreachable
    | i_ip_proxy_arp
    | i_ip_redirects_null
    | i_ip_rip
    | i_ip_router
    | i_ip_sticky_arp
    | i_ip_unreachables_null
    | i_ip_verify_null
  )
;

i_ip_access_group
:
  ACCESS_GROUP name = ip_access_list_name
  (
    IN
    | OUT
  ) NEWLINE
;

i_ip_address
:
  ADDRESS
  (
    i_ip_address_concrete
    | i_ip_address_dhcp
  )
;

i_ip_authentication
:
  AUTHENTICATION
  (
    iipa_key_chain
    | iipa_mode
  )
;

iipa_key_chain
:
  KEY_CHAIN EIGRP tag = router_eigrp_process_tag keychain = key_chain_name NEWLINE
;

iipa_mode
:
  MODE EIGRP tag = router_eigrp_process_tag MD5 NEWLINE
;

i_ip_address_concrete
:
  addr = interface_address SECONDARY?
  (
    ROUTE_PREFERENCE rp = uint8
    | TAG tag = uint32
  )* NEWLINE
;

i_ip_bandwidth
:
  BANDWIDTH EIGRP router_eigrp_process_tag bw = interface_bandwidth_eigrp_kbps NEWLINE
;

i_ip_delay
:
  // delay 1 - 16,777,215 (2^24) tens of microseconds
  DELAY EIGRP router_eigrp_process_tag delay = interface_delay_10us NEWLINE
;

i_ip_address_dhcp
:
  DHCP NEWLINE
;

i_ip_dhcp
:
  DHCP i_ip_dhcp_relay
;

i_ip_dhcp_relay
:
  RELAY ADDRESS ip_address NEWLINE
;

i_ip_distribute_list
:
  DISTRIBUTE_LIST EIGRP router_eigrp_process_tag
  (
    iipdl_prefix_list
    | iipdl_route_map
  )
;

iipdl_prefix_list
:
  PREFIX_LIST prefixlist = ip_prefix_list_name (IN | OUT) NEWLINE
;

iipdl_route_map
:
  ROUTE_MAP routemap = route_map_name (IN | OUT) NEWLINE
;

i_ip_eigrp
:
  EIGRP tag = router_eigrp_process_tag
  i_ip_eigrp_bfd
;

i_ip_eigrp_bfd
:
  BFD DISABLE? NEWLINE
;

i_ip_forward
:
  FORWARD NEWLINE
;

i_ip_igmp
:
  IGMP
  (
    iipi_access_group
    | iipi_query_interval_null
    | iipi_query_max_response_time_null
    | iipi_version_null
  )
;

iipi_access_group
:
  ACCESS_GROUP acl = ip_access_list_name NEWLINE
;

iipi_query_interval_null
:
   QUERY_INTERVAL null_rest_of_line
;
iipi_query_max_response_time_null
:
   QUERY_MAX_RESPONSE_TIME null_rest_of_line
;
iipi_version_null
:
   VERSION null_rest_of_line
;

i_ip_hello_interval
:
  HELLO_INTERVAL EIGRP tag = router_eigrp_process_tag time = uint16 NEWLINE
;

i_ip_hold_time
:
  HOLD_TIME EIGRP tag = router_eigrp_process_tag time = uint16 NEWLINE
;

i_ip_arp_null
:
   ARP null_rest_of_line
;
i_ip_directed_broadcast_null
:
   DIRECTED_BROADCAST null_rest_of_line
;
i_ip_flow_null
:
   FLOW null_rest_of_line
;
i_ip_redirects_null
:
   REDIRECTS null_rest_of_line
;
i_ip_unreachables_null
:
   UNREACHABLES null_rest_of_line
;
i_ip_verify_null
:
   VERIFY null_rest_of_line
;

i_ip_ospf
:
  OSPF
  (
    iipo_authentication
    | iipo_authentication_key
    | iipo_bfd
    | iipo_cost
    | iipo_dead_interval
    | iipo_hello_interval
    | iipo_message_digest_key
    | iipo_network
    | iipo_passive_interface
    | iipo_priority
  )
;

i_ip_passive_interface
:
  PASSIVE_INTERFACE EIGRP router_eigrp_process_tag NEWLINE
;

i_ip_pim
:
  PIM
  (
    iipp_bfd_instance_null
    | iipp_border_null
    | iipp_dr_delay_null
    | iipp_dr_priority_null
    | iipp_hello_authentication
    | iipp_hello_interval_null
    | iipp_jp_policy
    | iipp_neighbor_policy
    | iipp_passive_null
    | iipp_sparse_mode_null
    | iipp_strict_rfc_compliant_null
  )
;

iipp_hello_authentication
:
  HELLO_AUTHENTICATION AH_MD5 cisco_nxos_password NEWLINE
;

iipp_jp_policy
:
  JP_POLICY
  (
    iipp_jp_policy_prefix_list
    | iipp_jp_policy_route_map
  )
;

iipp_jp_policy_prefix_list
:
  PREFIX_LIST list = ip_prefix_list_name (IN | OUT)? NEWLINE
;

iipp_jp_policy_route_map
:
  map = route_map_name (IN | OUT)? NEWLINE
;

iipp_neighbor_policy
:
  NEIGHBOR_POLICY
  (
    iipp_neighbor_policy_prefix_list
    | iipp_neighbor_policy_route_map
  )
;

iipp_neighbor_policy_prefix_list
:
  PREFIX_LIST list = ip_prefix_list_name (IN | OUT)? NEWLINE
;

iipp_neighbor_policy_route_map
:
  map = route_map_name (IN | OUT)? NEWLINE
;

iipp_bfd_instance_null
:
   BFD_INSTANCE null_rest_of_line
;
iipp_border_null
:
   BORDER null_rest_of_line
;
iipp_dr_delay_null
:
   DR_DELAY null_rest_of_line
;
iipp_dr_priority_null
:
   DR_PRIORITY null_rest_of_line
;
iipp_hello_interval_null
:
   HELLO_INTERVAL null_rest_of_line
;
iipp_passive_null
:
   PASSIVE null_rest_of_line
;
iipp_sparse_mode_null
:
   SPARSE_MODE null_rest_of_line
;
iipp_strict_rfc_compliant_null
:
   STRICT_RFC_COMPLIANT null_rest_of_line
;

i_ip_policy
:
  POLICY ROUTE_MAP name = route_map_name NEWLINE
;

i_ip_port
:
  PORT iip_port_access_group
;

iip_port_access_group
:
  ACCESS_GROUP acl = ip_access_list_name IN NEWLINE
;

i_ip_port_unreachable
:
  PORT_UNREACHABLE NEWLINE
;

i_ip_proxy_arp
:
  PROXY_ARP NEWLINE
;

i_ip_sticky_arp
:
  STICKY_ARP IGNORE NEWLINE
;

iipo_authentication
:
  AUTHENTICATION
  (
    iipoa_authentication
    | iipoa_key_chain
    | iipoa_message_digest
    | iipoa_null
  )
;

iipoa_authentication
:
  NEWLINE
;

iipoa_key_chain
:
  KEY_CHAIN name = key_chain_name NEWLINE
;

iipoa_message_digest
:
  MESSAGE_DIGEST NEWLINE
;

iipoa_null
:
  NULL NEWLINE
;

iipo_authentication_key
:
  AUTHENTICATION_KEY key = cisco_nxos_password NEWLINE
;

iipo_bfd
:
  BFD NEWLINE
;

iipo_cost
:
  COST cost = interface_ospf_cost NEWLINE
;

interface_ospf_cost
:
// 1-65535
  uint16
;

iipo_dead_interval
:
  DEAD_INTERVAL interval_s = ospf_dead_interval NEWLINE
;

ospf_dead_interval
:
// 1-65535
  uint16
;

iipo_hello_interval
:
  HELLO_INTERVAL interval_s = ospf_hello_interval NEWLINE
;

ospf_hello_interval
:
// 1-65535
  uint16
;

iipo_message_digest_key
:
  MESSAGE_DIGEST_KEY key_id = uint8 MD5 cisco_nxos_password NEWLINE
;

iipo_network
:
  NETWORK
  (
    BROADCAST
    | POINT_TO_POINT
  ) NEWLINE
;

iipo_passive_interface
:
  PASSIVE_INTERFACE NEWLINE
;

iipo_priority
:
  PRIORITY priority = ospf_priority NEWLINE
;

i_ip_rip
:
  RIP
  (
    iiprip_authentication
    | iiprip_route_filter
  )
;

iiprip_authentication
:
  AUTHENTICATION
  (
    iiprip_a_mode
    | iiprip_a_key_chain
  )
;

iiprip_a_mode
:
  MODE MD5 NEWLINE
;

iiprip_a_key_chain
:
  KEY_CHAIN name = key_chain_name NEWLINE
;

iiprip_route_filter
:
  ROUTE_FILTER
  (
    iiprip_rf_prefix_list
    | iiprip_rf_route_map
  )
;

iiprip_rf_prefix_list
:
  PREFIX_LIST name = ip_prefix_list_name (IN | OUT) NEWLINE
;

iiprip_rf_route_map
:
  ROUTE_MAP name = route_map_name (IN | OUT) NEWLINE
;

i_ip_router
:
  ROUTER
  (
    iipr_eigrp
    | iipr_isis
    | iipr_ospf
    | iipr_rip
  )
;

iipr_eigrp
:
  eigrp_instance NEWLINE
;

iipr_isis
:
  ISIS tag = router_isis_process_tag NEWLINE
;

iipr_ospf
:
  ospf_instance AREA area = ospf_area_id NEWLINE
;

iipr_rip
:
  rip_instance NEWLINE
;

i_ipv6
:
  IPV6
  (
    iip6_address
    | iip6_dhcp_null
    | iip6_mld_null
    | iip6_nd_null
    | iip6_pim_null
    | iip6_redirects_null
    | iip6_router
    | iip6_traffic_filter
    | iip6_verify_null
  )
;

iip6_address
:
  ADDRESS
  (
    i_ipv6_address_concrete
    | i_ipv6_address_dhcp
  )
;

i_ipv6_address_concrete
:
  addr = interface_ipv6_address SECONDARY?
  (
    TAG tag = uint32
  )? NEWLINE
;

i_ipv6_address_dhcp
:
  DHCP NEWLINE
;

iip6_dhcp_null
:
   DHCP null_rest_of_line
;
iip6_mld_null
:
   MLD null_rest_of_line
;
iip6_nd_null
:
   ND null_rest_of_line
;
iip6_pim_null
:
   PIM null_rest_of_line
;
iip6_redirects_null
:
   REDIRECTS null_rest_of_line
;
iip6_verify_null
:
   VERIFY null_rest_of_line
;

iip6_router
:
  ROUTER
  (
    iip6r_isis
    | iip6r_ospfv3
  )
;

iip6r_isis
:
  ISIS tag = router_isis_process_tag NEWLINE
;

iip6_traffic_filter
:
  TRAFFIC_FILTER name = ip_access_list_name
  (
    IN
    | OUT
  ) NEWLINE
;

iip6r_ospfv3
:
  ospfv3_instance AREA area = ospf_area_id NEWLINE
;

i_isis
:
  ISIS
  (
    iisis_authentication_null
    | iisis_authentication_check_null
    | iisis_authentication_type_null
    | iisis_bfd_null
    | iisis_circuit_type
    | iisis_csnp_interval_null
    | iisis_hello_interval_null
    | iisis_hello_multiplier_null
    | iisis_hello_padding_null
    | iisis_ipv6_null
    | iisis_lsp_interval_null
    | iisis_mesh_group_null
    | iisis_metric_null
    | iisis_mtu_check_null
    | iisis_network
    | iisis_no
    | iisis_passive_interface_null
    | iisis_prefix_attributes_null
    | iisis_priority_null
    | iisis_retransmit_interval_null
    | iisis_retransmit_throttle_interval_null
    | iisis_shutdown_null
    | iisis_suppressed_null
  )
;

iisis_authentication_null
:
  AUTHENTICATION null_rest_of_line
;

iisis_authentication_check_null
:
  AUTHENTICATION_CHECK null_rest_of_line
;

iisis_authentication_type_null
:
  AUTHENTICATION_TYPE null_rest_of_line
;

iisis_bfd_null
:
  BFD null_rest_of_line
;

iisis_circuit_type
:
  CIRCUIT_TYPE level = isis_level NEWLINE
;

iisis_csnp_interval_null
:
  CSNP_INTERVAL null_rest_of_line
;

iisis_hello_interval_null
:
  HELLO_INTERVAL null_rest_of_line
;

iisis_hello_multiplier_null
:
  HELLO_MULTIPLIER null_rest_of_line
;

iisis_hello_padding_null
:
  HELLO_PADDING null_rest_of_line
;

iisis_ipv6_null
:
  IPV6 null_rest_of_line
;

iisis_lsp_interval_null
:
  LSP_INTERVAL null_rest_of_line
;

iisis_mesh_group_null
:
  MESH_GROUP null_rest_of_line
;

iisis_metric_null
:
  METRIC null_rest_of_line
;

iisis_mtu_check_null
:
  MTU_CHECK null_rest_of_line
;

iisis_network
:
  NETWORK POINT_TO_POINT null_rest_of_line
;

iisis_passive_interface_null
:
  PASSIVE_INTERFACE null_rest_of_line
;

iisis_prefix_attributes_null
:
  PREFIX_ATTRIBUTES null_rest_of_line
;

iisis_priority_null
:
  PRIORITY null_rest_of_line
;

iisis_retransmit_interval_null
:
  RETRANSMIT_INTERVAL null_rest_of_line
;

iisis_retransmit_throttle_interval_null
:
  RETRANSMIT_THROTTLE_INTERVAL null_rest_of_line
;

iisis_shutdown_null
:
  SHUTDOWN null_rest_of_line
;

iisis_suppressed_null
:
  SUPPRESSED null_rest_of_line
;

iisis_no
:
  NO
  (
    iisisno_authentication_null
    | iisisno_authentication_check_null
    | iisisno_authentication_type_null
    | iisisno_bfd_null
    | iisisno_csnp_interval_null
    | iisisno_hello_interval_null
    | iisisno_hello_multiplier_null
    | iisisno_hello_padding_null
    | iisisno_ipv6_null
    | iisisno_lsp_interval_null
    | iisisno_mesh_group_null
    | iisisno_metric_null
    | iisisno_mtu_check_null
    | iisisno_network_null
    | iisisno_passive_interface_null
    | iisisno_prefix_attributes_null
    | iisisno_priority_null
    | iisisno_retransmit_interval_null
    | iisisno_retransmit_throttle_interval_null
    | iisisno_shutdown_null
    | iisisno_suppressed_null
  )
;

iisisno_authentication_null
:
  AUTHENTICATION null_rest_of_line
;

iisisno_authentication_check_null
:
  AUTHENTICATION_CHECK null_rest_of_line
;

iisisno_authentication_type_null
:
  AUTHENTICATION_TYPE null_rest_of_line
;

iisisno_bfd_null
:
  BFD null_rest_of_line
;

iisisno_csnp_interval_null
:
  CSNP_INTERVAL null_rest_of_line
;

iisisno_hello_interval_null
:
  HELLO_INTERVAL null_rest_of_line
;

iisisno_hello_multiplier_null
:
  HELLO_MULTIPLIER null_rest_of_line
;

iisisno_hello_padding_null
:
  HELLO_PADDING null_rest_of_line
;

iisisno_ipv6_null
:
  IPV6 null_rest_of_line
;

iisisno_lsp_interval_null
:
  LSP_INTERVAL null_rest_of_line
;

iisisno_mesh_group_null
:
  MESH_GROUP null_rest_of_line
;

iisisno_metric_null
:
  METRIC null_rest_of_line
;

iisisno_mtu_check_null
:
  MTU_CHECK null_rest_of_line
;

iisisno_network_null
:
  NETWORK null_rest_of_line
;

iisisno_passive_interface_null
:
  PASSIVE_INTERFACE null_rest_of_line
;

iisisno_prefix_attributes_null
:
  PREFIX_ATTRIBUTES null_rest_of_line
;

iisisno_priority_null
:
  PRIORITY null_rest_of_line
;

iisisno_retransmit_interval_null
:
  RETRANSMIT_INTERVAL null_rest_of_line
;

iisisno_retransmit_throttle_interval_null
:
  RETRANSMIT_THROTTLE_INTERVAL null_rest_of_line
;

iisisno_shutdown_null
:
  SHUTDOWN null_rest_of_line
;

iisisno_suppressed_null
:
  SUPPRESSED null_rest_of_line
;

i_lacp
:
  LACP
  (
    il_fast_select_hot_standby_null
    | il_min_links
    | il_port_priority_null
    | il_rate_null
    | il_suspend_individual_null
  )
;

il_min_links
:
  MIN_LINKS num = min_links_number NEWLINE
;

min_links_number
:
// 1-32
  uint8
;

il_fast_select_hot_standby_null
:
   FAST_SELECT_HOT_STANDBY null_rest_of_line
;
il_port_priority_null
:
   PORT_PRIORITY null_rest_of_line
;
il_rate_null
:
   RATE null_rest_of_line
;
il_suspend_individual_null
:
   SUSPEND_INDIVIDUAL null_rest_of_line
;

i_mac_address
:
  MAC_ADDRESS mac = mac_address_literal NEWLINE
;

i_mtu
:
  MTU interface_mtu NEWLINE
;

interface_mtu
:
// range depends on interface type
  mtu = uint16
;

i_no
:
  NO
  (
    i_no_autostate
    | i_no_bandwidth
    | i_no_beacon_null
    | i_no_bfd
    | i_no_cdp_null
    | i_no_description
    | i_no_hardware_null
    | i_no_hsrp_null
    | i_no_ip
    | i_no_ipv6
    | i_no_lacp_null
    | i_no_link_null
    | i_no_lldp_null
    | i_no_load_interval_null
    | i_no_logging_null
    | i_no_mac_address
    | i_no_management_null
    | i_no_negotiate_null
    | i_no_ospfv3_null
    | i_no_port_channel_null
    | i_no_shutdown
    | i_no_snmp_null
    | i_no_spanning_tree_null
    | i_no_storm_control_null
    | i_no_switchport
    | i_no_udld_null
    | i_no_vrf_member
    | i_no_vtp_null
  )
;

i_no_autostate
:
  AUTOSTATE NEWLINE
;

i_no_bandwidth
:
  BANDWIDTH i_no_bandwidth_inherit
;

i_no_bandwidth_inherit
:
  INHERIT NEWLINE
;

i_no_bfd
:
  BFD null_rest_of_line
;

i_no_description
:
  DESCRIPTION NEWLINE
;

i_no_ip
:
  IP
  (
    inoip_arp_null
    | inoip_dhcp_null
    | inoip_forward
    | inoip_ospf
    | inoip_port_unreachable_null
    | inoip_proxy_arp
    | inoip_redirects_null
    | inoip_verify_null
  )
;

inoip_arp_null
:
   ARP null_rest_of_line
;
inoip_dhcp_null
:
   DHCP null_rest_of_line
;
inoip_port_unreachable_null
:
   PORT_UNREACHABLE null_rest_of_line
;
inoip_redirects_null
:
   REDIRECTS null_rest_of_line
;
inoip_verify_null
:
   VERIFY null_rest_of_line
;

inoip_forward
:
  FORWARD NEWLINE
;

inoip_ospf
:
  OSPF inoipo_passive_interface
;

inoip_proxy_arp
:
  PROXY_ARP NEWLINE
;

inoipo_passive_interface
:
  PASSIVE_INTERFACE NEWLINE
;

i_no_ipv6
:
  IPV6 inoip6_null
;

inoip6_null
:
  (
    REDIRECTS
  ) null_rest_of_line
;

i_no_mac_address
:
  MAC_ADDRESS NEWLINE
;

i_no_beacon_null
:
   BEACON null_rest_of_line
;
i_no_cdp_null
:
   CDP null_rest_of_line
;
i_no_hardware_null
:
   HARDWARE null_rest_of_line
;
i_no_hsrp_null
:
   HSRP null_rest_of_line
;
i_no_lacp_null
:
   LACP null_rest_of_line
;
i_no_link_null
:
   LINK null_rest_of_line
;
i_no_lldp_null
:
   LLDP null_rest_of_line
;
i_no_load_interval_null
:
   LOAD_INTERVAL null_rest_of_line
;
i_no_logging_null
:
   LOGGING null_rest_of_line
;
i_no_management_null
:
   MANAGEMENT null_rest_of_line
;
i_no_negotiate_null
:
   NEGOTIATE null_rest_of_line
;
i_no_ospfv3_null
:
   OSPFV3 null_rest_of_line
;
i_no_port_channel_null
:
   PORT_CHANNEL null_rest_of_line
;
i_no_snmp_null
:
   SNMP null_rest_of_line
;
i_no_spanning_tree_null
:
   SPANNING_TREE null_rest_of_line
;
i_no_storm_control_null
:
   STORM_CONTROL null_rest_of_line
;
i_no_udld_null
:
   UDLD null_rest_of_line
;
i_no_vtp_null
:
   VTP null_rest_of_line
;

i_no_shutdown
:
  SHUTDOWN
  (
    inoshut
    | inoshut_lan
  )
;

inoshut
:
  // just "no shutdown"
  NEWLINE
;

inoshut_lan
:
  LAN NEWLINE
;

i_no_switchport
:
  SWITCHPORT
  (
    inos_access
    | inos_block
    | inos_dot1q
    | inos_host
    | inos_monitor
    | inos_priority
    | inos_switchport
  )
;

inos_access
:
  ACCESS VLAN (vlan = unreserved_vlan_id)? NEWLINE
;

inos_block
:
  BLOCK (MULTICAST | UNICAST) NEWLINE
;

inos_dot1q
:
  DOT1Q ETHERTYPE NEWLINE
;

inos_host
:
  HOST NEWLINE
;

inos_monitor
:
  MONITOR NEWLINE
;

inos_priority
:
  PRIORITY EXTEND NEWLINE
;

inos_switchport
:
  // just newline, for `no switchport`
  NEWLINE
;

i_no_vrf_member
:
  VRF MEMBER (name = vrf_name)? NEWLINE
;

i_bfd_null
:
   BFD null_rest_of_line
;
i_carrier_delay_null
:
   CARRIER_DELAY null_rest_of_line
;
i_cdp_null
:
   CDP null_rest_of_line
;
i_duplex_null
:
   DUPLEX null_rest_of_line
;
i_fex_null
:
   FEX null_rest_of_line
;
i_flowcontrol_null
:
   FLOWCONTROL null_rest_of_line
;
i_hardware_null
:
   HARDWARE null_rest_of_line
;
i_link_null
:
   LINK null_rest_of_line
;
i_lldp_null
:
   LLDP null_rest_of_line
;
i_load_interval_null
:
   LOAD_INTERVAL null_rest_of_line
;
i_logging_null
:
   LOGGING null_rest_of_line
;
i_mdix_null
:
   MDIX null_rest_of_line
;
i_medium_null
:
   MEDIUM null_rest_of_line
;
i_negotiate_null
:
   NEGOTIATE null_rest_of_line
;
i_packet_null
:
   PACKET null_rest_of_line
;
i_priority_flow_control_null
:
   PRIORITY_FLOW_CONTROL null_rest_of_line
;
i_snmp_null
:
   SNMP null_rest_of_line
;
i_spanning_tree_null
:
   SPANNING_TREE null_rest_of_line
;
i_storm_control_null
:
   STORM_CONTROL null_rest_of_line
;
i_udld_null
:
   UDLD null_rest_of_line
;
i_vpc_null
:
   VPC null_rest_of_line
;
i_vtp_null
:
   VTP null_rest_of_line
;

i_ospfv3
:
  OSPFV3
  (
    io3_bfd
    | io3_cost
    | io3_dead_interval
    | io3_hello_interval
    | io3_network
  )
;

io3_bfd
:
  BFD NEWLINE
;

io3_cost
:
  COST cost = interface_ospf_cost NEWLINE
;

io3_dead_interval
:
  DEAD_INTERVAL interval_s = ospf_dead_interval NEWLINE
;

io3_hello_interval
:
  HELLO_INTERVAL interval_s = ospf_hello_interval NEWLINE
;

io3_network
:
  NETWORK POINT_TO_POINT NEWLINE
;

i_private_vlan
:
  PRIVATE_VLAN MAPPING (ADD | REMOVE)? vlan_id_range NEWLINE
;

i_service_policy
:
  SERVICE_POLICY isp_type
;

isp_type
:
  TYPE
  (
    ispt_qos
    | ispt_queuing
  )
;

ispt_qos
:
  QOS (INPUT | OUTPUT) name = policy_map_qos_name NEWLINE
;

ispt_queuing
:
  QUEUING (INPUT | OUTPUT) name = policy_map_queuing_name NEWLINE
;

i_shutdown
:
  SHUTDOWN FORCE? NEWLINE
;

i_speed
:
  SPEED
  (
    i_speed_auto
    | i_speed_number
  )
;

i_speed_auto
:
  AUTO NEWLINE
;

i_speed_number
:
  speed = interface_speed NEWLINE
;

interface_speed
:
// 100; 1,000; 10,000; 25,000; 40,000; 100,000
  uint32
;

i_switchport
:
  SWITCHPORT
  (
    i_switchport_access
    | i_switchport_mode
    | i_switchport_monitor
    | i_switchport_switchport
    | i_switchport_trunk_allowed
    | i_switchport_trunk
  )
;

i_switchport_access
:
  ACCESS VLAN vlan = unreserved_vlan_id NEWLINE
;

i_switchport_mode
:
  MODE
  (
    i_switchport_mode_access
    | i_switchport_mode_dot1q_tunnel
    | i_switchport_mode_fex_fabric
    | i_switchport_mode_monitor
    | i_switchport_mode_trunk
  )
;

i_switchport_mode_access
:
  ACCESS NEWLINE
;

i_switchport_mode_dot1q_tunnel
:
  DOT1Q_TUNNEL NEWLINE
;

i_switchport_mode_fex_fabric
:
  FEX_FABRIC NEWLINE
;

i_switchport_mode_monitor
:
  MONITOR null_rest_of_line
;

i_switchport_mode_trunk
:
  TRUNK NEWLINE
;

i_switchport_monitor
:
  MONITOR NEWLINE
;

i_switchport_switchport
:
  NEWLINE
;

i_switchport_trunk
:
  TRUNK
  (
    i_switchport_trunk_allowed
    | i_switchport_trunk_native
  )
;

i_switchport_trunk_allowed
:
  ALLOWED VLAN
  (
    (
      ADD
      | REMOVE
    )? vlans = vlan_id_range
    | EXCEPT except = vlan_id
    | NONE
  ) NEWLINE
;

i_switchport_trunk_native
:
  NATIVE VLAN vlan = unreserved_vlan_id NEWLINE
;

i_vrf_member
:
  VRF MEMBER name = vrf_name NEWLINE
;

interface_range
:
  iname = interface_name
  (
    DASH last = uint16
  )?
;

nve_interface_range
:
  iname = nve_interface_name
  (
    DASH last = uint8
  )?
;

nve_global
:
  GLOBAL
  (
    nvg_ingress_replication
    | nvg_mcast_group
    | nvg_suppress_arp
  )
;

nvg_ingress_replication
:
  INGRESS_REPLICATION PROTOCOL BGP NEWLINE
;

nvg_mcast_group
:
  MCAST_GROUP ip_address (L2 | L3) NEWLINE
;

nvg_suppress_arp
:
  SUPPRESS_ARP NEWLINE
;

nve_host_reachability
:
  HOST_REACHABILITY PROTOCOL BGP NEWLINE
;

nve_member
:
// 1-16777214
  MEMBER VNI vni = vni_number ASSOCIATE_VRF? NEWLINE
  (
     nvm_ingress_replication
     | nvm_mcast_group
     | nvm_peer_ip
     | nvm_peer_vtep
     | nvm_spine_anycast_gateway
     | nvm_suppress_arp
  )*
;

nvm_ingress_replication
:
   INGRESS_REPLICATION PROTOCOL (BGP | STATIC) NEWLINE
;

nvm_mcast_group
:
   MCAST_GROUP first = ip_address second = ip_address? NEWLINE
;

nvm_peer_ip
:
   PEER_IP ip_address NEWLINE
;

nvm_peer_vtep
:
   PEER_VTEP ip_address NEWLINE
;

nvm_spine_anycast_gateway
:
   SPINE_ANYCAST_GATEWAY NEWLINE
;

nvm_suppress_arp
:
   SUPPRESS_ARP DISABLE? NEWLINE
;

nve_no
:
   NO nve_no_shutdown
;

nve_no_shutdown
:
   SHUTDOWN NEWLINE
;

nve_source_interface
:
   SOURCE_INTERFACE name = interface_name NEWLINE
;
