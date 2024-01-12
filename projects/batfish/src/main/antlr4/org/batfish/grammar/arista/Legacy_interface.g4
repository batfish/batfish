parser grammar Legacy_interface;

import Legacy_common, Arista_interface, Arista_common;

options {
   tokenVocab = AristaLexer;
}

eos_bandwidth_specifier
:
   FORTYG_FULL
   | ONE_HUNDREDG_FULL
   | ONE_HUNDRED_FULL
   | ONE_THOUSAND_FULL
   | TEN_THOUSAND_FULL
   | TWENTY_FIVEG_FULL
;

eos_vxlan_if_inner
:
   eos_vxif_arp
   | eos_vxif_description
   | eos_vxif_vxlan
;

eos_vxif_arp
:
   ARP REPLY RELAY NEWLINE
;

eos_vxif_description
:
   description_line
;

eos_vxif_vxlan
:
  VXLAN
  (
     eos_vxif_vxlan_flood
     | eos_vxif_vxlan_multicast_group
     | eos_vxif_vxlan_source_interface
     | eos_vxif_vxlan_udp_port
     | eos_vxif_vxlan_virtual_router
     | eos_vxif_vxlan_vlan_vni_range
     | eos_vxif_vxlan_vlan
     | eos_vxif_vxlan_vrf
  )
;

eos_vxif_vxlan_flood
:
   FLOOD VTEP (ADD | REMOVE)? (hosts += IP_ADDRESS)+ NEWLINE
;

eos_vxif_vxlan_multicast_group
:
   MULTICAST_GROUP group = IP_ADDRESS NEWLINE
;

eos_vxif_vxlan_source_interface
:
   SOURCE_INTERFACE iface = interface_name NEWLINE
;

eos_vxif_vxlan_udp_port
:
   UDP_PORT num = dec NEWLINE
;

eos_vxif_vxlan_virtual_router
:
   VIRTUAL_ROUTER
   // TODO: expand to full completions
   (ENCAPSULATION MAC_ADDRESS MLAG_SYSTEM_ID) NEWLINE
;

eos_vxif_vxlan_vlan_vni_range
:
   VLAN vlans = vlan_range VNI vnis = vni_range NEWLINE
;

eos_vxif_vxlan_vlan
:
   VLAN num = dec
   (
      eos_vxif_vxlan_flood
      | eos_vxif_vxlan_vlan_vni
   )
;

eos_vxif_vxlan_vlan_vni
:
   VNI num = dec NEWLINE
;

eos_vxif_vxlan_vrf
:
   VRF vrf = vrf_name VNI vni = dec NEWLINE
;

if_autostate
:
   AUTOSTATE NEWLINE
;

if_bandwidth
:
   BANDWIDTH bw = uint32 KBPS? NEWLINE
;

if_bfd
:
  BFD (IPV4 | IPV6)? (
     if_bfd_authentication
     | if_bfd_echo
     | if_bfd_echo_rx_interval
     | if_bfd_interval
     | if_bfd_neighbor
     | if_bfd_optimize
     | if_bfd_template
  )
;

if_bfd_authentication
:
  AUTHENTICATION KEYED_SHA1 KEYID id = dec (
     HEX_KEY hex_key = variable
     | KEY ascii_key = variable
  )NEWLINE
;

if_bfd_echo
:
  ECHO NEWLINE
;

if_bfd_echo_rx_interval
:
  ECHO_RX_INTERVAL ms = dec NEWLINE
;

if_bfd_interval
:
  INTERVAL tx_ms = dec (MIN_RX | MIN_RX_VAR) tx_ms = dec MULTIPLIER mult = dec NEWLINE
;

if_bfd_neighbor
:
  NEIGHBOR SRC_IP (
     src_ip = IP_ADDRESS DEST_IP dst_ip = IP_ADDRESS
     | src_ip = IPV6_ADDRESS DEST_IP dst_ip = IPV6_ADDRESS
  ) NEWLINE
;

if_bfd_optimize
:
  OPTIMIZE SUBINTERFACE NEWLINE
;

if_bfd_template
:
  TEMPLATE name = variable_permissive NEWLINE
;

if_channel_group_eos
:
  CHANNEL_GROUP
  (
    ifcg_num_eos
    | ifcg_recirculation_eos
  )
;

ifcg_num_eos
:
  num = dec MODE mode_val = (ACTIVE | ON | PASSIVE) NEWLINE
;

ifcg_recirculation_eos
:
  RECIRCULATION num = dec NEWLINE
;

if_crypto
:
  CRYPTO
  if_crypto_map
;

if_crypto_map
:
   CRYPTO MAP name = variable NEWLINE
;

if_default_eos
:
   DEFAULT
   (
     ifd_ip_eos
     | ifd_ipv6_eos
     | ifd_null_eos
     | ifd_switchport_eos
   )
;

ifd_ip_eos
:
  IP
  ifd_ip_null_eos
;

ifd_ip_null_eos
:
  (
    DHCP
    | PIM
  ) null_rest_of_line
;

ifd_ipv6_eos
:
  IPV6
  ifd_ipv6_null_eos
;

ifd_ipv6_null_eos
:
  (
    ND
  ) null_rest_of_line
;

ifd_null_eos
:
  (
    ARP
    | ERROR_CORRECTION
    | LACP
    | LOAD_INTERVAL
    | LOGGING
    | NTP
    | PIM
    | QOS
    | SFLOW
    | UNIDIRECTIONAL
  ) null_rest_of_line
;

ifd_switchport_eos
:
  SWITCHPORT
  null_rest_of_line
;

if_default_gw
:
   DEFAULT_GW IP_ADDRESS NEWLINE
;

if_description
:
   description_line
;

if_igmp
:
// 10-31744
  IGMP QUERY_MAX_RESPONSE_TIME decisecs = dec NEWLINE
;

if_ip
:
  IP
  (
    ifip_access_group_eos
    | ifip_arp_eos
    | ifip_attached_routes_eos
    | ifip_address_eos
    | ifip_dhcp_eos
    | if_ip_helper_address
    | if_ip_igmp
    | if_ip_inband_access_group
    | if_ip_local_proxy_arp_eos
    | if_ip_multicast_eos
    | if_ip_nat
    | if_ip_nbar
    | ifip_null_eos
    | ifip_ospf_eos
    | ifip_pim_eos
    | ifip_proxy_arp_eos
    | ifip_verify_eos
    | if_ip_virtual_router
  )
;

ifip_access_group_eos
:
  ACCESS_GROUP name = variable (IN | OUT) NEWLINE
;

ifip_arp_eos
:
  ARP null_rest_of_line
;

ifip_attached_routes_eos
:
  ATTACHED_ROUTES NEWLINE
;

ifip_address_eos
:
  ADDRESS
  (
    ifip_address_address_eos
    | ifip_address_dhcp_eos
    // | ifip_address_unnumbered_eos
    | ifip_address_virtual_eos
  )
;

ifip_address_address_eos
:
  addr = interface_address SECONDARY? NEWLINE
;

ifip_address_dhcp_eos
:
   DHCP NEWLINE
;

ifip_address_virtual_eos
:
  VIRTUAL addr = interface_address SECONDARY? NEWLINE
;

ifip_dhcp_eos
:
   DHCP ifipdhcp_relay_eos
;

ifipdhcp_relay_eos
:
  RELAY
  (
    ifipdhcpr_all_subnets_eos
    | ifipdhcpr_client_eos
    | ifipdhcpr_information_eos
  )
;

ifipdhcpr_all_subnets_eos
:
  ALL_SUBNETS NEWLINE
;

ifipdhcpr_client_eos
:
  CLIENT NEWLINE
;

ifipdhcpr_information_eos
:
  INFORMATION OPTION CIRCUIT_ID id = word NEWLINE
;

if_ip_helper_address
:
   HELPER_ADDRESS address = IP_ADDRESS NEWLINE
;

if_ip_inband_access_group
:
   INBAND ACCESS_GROUP name = variable_permissive NEWLINE
;

if_ip_igmp
:
   IGMP
   (
      NEWLINE
      | ifigmp_access_group
      | ifigmp_host_proxy
      | ifigmp_null
      | ifigmp_static_group
   )
;

if_ip_local_proxy_arp_eos
:
  LOCAL_PROXY_ARP NEWLINE
;

if_ip_multicast_eos
:
  MULTICAST
  (
    ifipm_boundary_eos
    | ifipm_static_eos
  )
;

ifipm_boundary_eos
:
  BOUNDARY (ip_prefix | name = word) OUT? NEWLINE
;

ifipm_static_eos
:
  STATIC NEWLINE
;

if_ip_nat
:
  NAT
  (
    ifipn_destination
    | ifipn_source
  )
;



ifipn_destination: DESTINATION ifipnd_static;

ifipnd_static:
   STATIC
   original_ip = IP_ADDRESS (original_port = port_number)?
   (ACCESS_LIST acl = variable)?
   tx_ip = IP_ADDRESS (tx_port = port_number)?
   (PROTOCOL (TCP | UDP))?
   NEWLINE
;

ifipn_source
:
   SOURCE
   (
     ifipns_dynamic
     | ifipns_static
   )
;

ifipns_dynamic
:
   DYNAMIC ACCESS_LIST acl = variable
   (
     OVERLOAD
     | POOL pool = variable
   ) NEWLINE
;

ifipns_static
:
   STATIC
   original_ip = IP_ADDRESS (original_port = port_number)?
   (ACCESS_LIST acl = variable)?
   tx_ip = IP_ADDRESS (tx_port = port_number)?
   (PROTOCOL (TCP | UDP))?
   NEWLINE
;

if_ip_nbar
:
   NBAR PROTOCOL_DISCOVERY (IPV4 | IPV6)? NEWLINE
;

ifip_null_eos
:
  (
    MFIB
  ) null_rest_of_line
;

ifip_ospf_eos
:
  OSPF
  (
    ifipo_area_eos
    | ifipo_authentication_eos
    // | ifipo_authentication_key_eos
    | ifipo_cost_eos
    | ifipo_dead_interval_eos
    // | ifipo_disabled_eos
    | ifipo_hello_interval_eos
    | ifipo_message_digest_key_eos
    // | ifipo_mtu_ignore_eos
    // | ifipo_neighbor_eos
    | ifipo_network_eos
    | ifipo_priority_eos
    // | ifipo_retransmit_interval_eos
    // | ifipo_transmit_delay
  )
;

ifipo_area_eos
:
   AREA area = ospf_area NEWLINE
;

ifipo_authentication_eos
:
  AUTHENTICATION
  (
    ifipo_authentication_enc_type
    | ifipo_authentication_message_digest
    | ifipo_authentication_password
  )
;

ifipo_authentication_enc_type
:
  type = dec password = variable NEWLINE
;

ifipo_authentication_message_digest
:
  MESSAGE_DIGEST NEWLINE
;

ifipo_authentication_password
:
  password = variable NEWLINE
;

ifipo_cost_eos
:
   COST cost = dec NEWLINE
;

ifipo_dead_interval_eos
:
   DEAD_INTERVAL seconds = dec NEWLINE
;

ifipo_hello_interval_eos
:
   HELLO_INTERVAL seconds = dec NEWLINE
;

ifipo_message_digest_key_eos
:
  MESSAGE_DIGEST_KEY key = null_rest_of_line
;

ifipo_network_eos
:
   NETWORK POINT_TO_POINT NEWLINE
;

ifipo_priority_eos
:
// 0-255
  PRIORITY pri = dec NEWLINE
;

ifip_pim_eos
:
  PIM
  (
    ifipp_bidirectional_eos
    | ifipp_border_router_eos
    | ifipp_neighbor_filter_eos
    | ifipp_null_eos
    | ifipp_sparse_mode_eos
  )
;

ifipp_bidirectional_eos
:
  BIDIRECTIONAL NEWLINE
;

ifipp_border_router_eos
:
  BORDER_ROUTER NEWLINE
;

ifipp_neighbor_filter_eos
:
  NEIGHBOR_FILTER acl = variable NEWLINE
;

ifipp_null_eos
:
  (
    DR_PRIORITY
    | JOIN_PRUNE_COUNT
    | JOIN_PRUNE_INTERVAL
    | QUERY_COUNT
    | QUERY_INTERVAL
  ) null_rest_of_line
;

ifipp_sparse_mode_eos
:
  SPARSE_MODE NEWLINE
;

ifip_proxy_arp_eos
:
  PROXY_ARP NEWLINE
;

ifip_verify_eos
:
  VERIFY
  (
    ifip_verify_source_eos
    | ifip_verify_unicast_eos
  )
;

ifip_verify_source_eos
:
  SOURCE NEWLINE
;

ifip_verify_unicast_eos
:
  UNICAST SOURCE REACHABLE_VIA
  (
    ANY
    | RX ALLOW_DEFAULT?
  )
  NEWLINE
;

if_ipv6
:
   IPV6
   (
     if_ipv6_enable
     | if_ipv6_null
     | if_ipv6_traffic_filter
   )
;

if_ipv6_enable
:
   ENABLE NEWLINE
;

if_ipv6_null
:
  (
    ADDRESS
    | ATTACHED_ROUTES
    | ND
    | OSPF
  ) null_rest_of_line
;

if_ipv6_traffic_filter
:
   TRAFFIC_FILTER acl = variable_aclname (IN | OUT) NEWLINE
;

if_isis
:
  ISIS
  (
    if_isis_circuit_type
    | if_isis_enable
    | if_isis_hello_interval
    | if_isis_metric
    | if_isis_network
    | if_isis_null
    | if_isis_passive
    | if_isis_tag
  )
;

if_isis_null
:
  (
    AUTHENTICATION
    | CSNP_INTERVAL
    | DS_HELLO_INTERVAL
    | HELLO
    | HELLO_INTERVAL
    | HELLO_MULTIPLIER
    | LSP_INTERVAL
    | POINT_TO_POINT
    | PROTOCOL
    | SMALL_HELLO
    | WIDE_METRIC
  ) null_rest_of_line
;

if_isis_circuit_type
:
   CIRCUIT_TYPE
   (
      LEVEL_1
      | LEVEL_2_ONLY
      | LEVEL_2
   ) NEWLINE
;

if_isis_enable
:
   ENABLE num = dec NEWLINE
;

if_isis_hello_interval
:
   HELLO_INTERVAL dec
   (
      LEVEL_1
      | LEVEL_2
   )? NEWLINE
;

if_isis_metric
:
   METRIC metric = dec
   (
      LEVEL_1
      | LEVEL_2
   )? NEWLINE
;

if_isis_network
:
   NETWORK POINT_TO_POINT NEWLINE
;

if_isis_passive
:
   PASSIVE NEWLINE
;

if_isis_tag
:
   TAG tag = dec NEWLINE
;

if_l2_protocol
:
  L2_PROTOCOL
  null_rest_of_line
;

if_lacp
:
  LACP
  if_lacp_null
;

if_lacp_null
:
  null_rest_of_line
;

if_lldp
:
  LLDP if_lldp_null
;

if_lldp_null
:
  null_rest_of_line
;

if_load_interval
:
   LOAD_INTERVAL li = dec NEWLINE
;

if_logging
:
  LOGGING null_rest_of_line
;

if_mac
:
  MAC
  (
    ifmac_access_group
    | ifmac_security
    | ifmac_timestamp
  )
;

ifmac_access_group
:
  ACCESS_GROUP name = variable (IN | OUT) NEWLINE
;

ifmac_security
:
  SECURITY PROFILE null_rest_of_line
;

ifmac_timestamp
:
  TIMESTAMP null_rest_of_line
;

if_mac_address
:
  MAC_ADDRESS addr = MAC_ADDRESS_LITERAL NEWLINE
;

if_member_interface
:
  MEMBER_INTERFACE name = interface_name NEWLINE
;

if_eos_mlag
:
   MLAG id = dec NEWLINE
;

if_encapsulation_eos
:
  ENCAPSULATION
  if_encapsulation_dot1q_eos
;

if_encapsulation_dot1q_eos
:
  DOT1Q VLAN id = dec NEWLINE
;

if_evpn_eos
:
  EVPN ETHERNET_SEGMENT NEWLINE
  (
    if_evpn_no_eos
  )*
;

if_evpn_no_eos
:
  NO (
    DESIGNATED_FORWARDER ELECTION HOLD_TIME
    | IDENTIFIER
    | REDUNDANCY
    | ROUTE_TARGET IMPORT
  ) NEWLINE
;

if_mfib
:
  MFIB
  if_mfib_null
;

if_mfib_null
:
  null_rest_of_line
;

if_mld
:
  MLD
  if_mld_null
;

if_mld_null
:
  null_rest_of_line
;

if_mpls
:
  MPLS
  if_mpls_null
;

if_mpls_null
:
  null_rest_of_line
;

if_mtu
:
   MTU mtu_size = dec NEWLINE
;

if_no
:
  NO
  (
    if_no_autostate
    | if_no_bandwidth
    | if_no_bfd
    | if_no_channel_group_eos
    | if_no_description_eos
    | if_no_ip_eos
    | if_no_link_debounce_eos
    | if_no_null_eos
    | if_no_routing_dynamic
    | if_no_shutdown_eos
    | if_no_spanning_tree
    | if_no_speed_eos
    | if_no_switchport_eos
    | if_no_traffic_loopback_eos
    | if_no_vrrp
  )
;

if_no_autostate
:
  AUTOSTATE NEWLINE
;

if_no_bandwidth
:
  BANDWIDTH NEWLINE
;

if_no_bfd
:
  BFD (IPV4 | IPV6)?
  (
     AUTHENTICATION
     | ECHO
     | ECHO_RX_INTERVAL
     | INTERVAL
     | NEIGHBOR SRC_IP src_ip = IP_ADDRESS DEST_IP dst_ip = IP_ADDRESS
     | NEIGHBOR SRC_IP src_ip6 = IPV6_ADDRESS DEST_IP dst_ip6 = IPV6_ADDRESS
     | OPTIMIZE SUBINTERFACE
  ) NEWLINE
;

if_no_channel_group_eos
:
  CHANNEL_GROUP NEWLINE
;

if_no_description_eos
:
  DESCRIPTION NEWLINE
;

if_no_ip_eos
:
  IP
  (
    if_no_ip_address_eos
    | if_no_ip_directed_broadcast_eos
    | if_no_ip_helper_address_eos
    | if_no_ip_local_proxy_arp_eos
    | if_no_ip_null_eos
    | if_no_ip_proxy_arp_eos
  )
;

if_no_ip_address_eos
:
  ADDRESS NEWLINE
;

if_no_ip_directed_broadcast_eos
:
  DIRECTED_BROADCAST NEWLINE
;

if_no_ip_helper_address_eos
:
  HELPER_ADDRESS NEWLINE
;

if_no_ip_local_proxy_arp_eos
:
  LOCAL_PROXY_ARP NEWLINE
;

if_no_ip_null_eos
:
  (
    ARP
    | ATTACHED_HOST
    | ATTACHED_HOSTS
    | IGMP
    | MULTICAST
    | PIM
    | RIP
    | VERIFY
  ) null_rest_of_line
;

if_no_ip_proxy_arp_eos
:
  PROXY_ARP NEWLINE
;

if_no_link_debounce_eos
:
  LINK_DEBOUNCE NEWLINE
;

if_no_null_eos
:
  (
    ARP
    | BFD
    | DCBX
    | DHCP
    | ENCAPSULATION
    | ERROR_CORRECTION
    | FLOW
    | FLOW_SPEC
    | FLOWCONTROL
    | IPV6
    | L2
    | L2_PROTOCOL
    | LLDP
    | LOGGING
    | MAC
    | MAC_ADDRESS
    | MLD
    | MSRP
    | MULTICAST
    | MVRP
    | PHY
    | PIM
    | PRIORITY_FLOW_CONTROL
    | PTP
    | QOS
    | QUEUE_MONITOR
    | RIP
    | SFLOW
    | SHAPE
    | SNMP
    | STORM_CONTROL
    | TCP
  ) null_rest_of_line
;

if_no_routing_dynamic
:
   ROUTING DYNAMIC NEWLINE
;

if_no_shutdown_eos
:
  SHUTDOWN NEWLINE
;

if_no_spanning_tree
:
   SPANNING_TREE
   (
      if_no_st_null
      | if_no_st_portfast
   )
;

if_no_st_null
:
   (
      BPDUFILTER
      | BPDUGUARD
      | COST
      | GUARD
      | LINK_TYPE
      | MST
      | PORT
      | PORT_PRIORITY
      | PRIORITY
      | PROTECT
      | RSTP
      | VLAN
   ) null_rest_of_line
;

if_no_st_portfast
:
   PORTFAST NEWLINE
;

if_no_speed_eos
:
  SPEED NEWLINE
;

if_no_switchport_eos
:
  SWITCHPORT
  (
    if_no_switchport_switchport_eos
    | if_no_switchport_trunk_eos
  )
;

// "no switchport"
if_no_switchport_switchport_eos
:
  NEWLINE
;

if_no_switchport_trunk_eos
:
  TRUNK
  (
    if_noswpt_allowed_eos
    | if_noswpt_group_eos
  )
;

if_noswpt_allowed_eos
:
  ALLOWED VLAN NEWLINE
;

if_noswpt_group_eos
:
  GROUP (name = VARIABLE)? NEWLINE
;

if_no_traffic_loopback_eos
:
  TRAFFIC_LOOPBACK NEWLINE
;

if_no_vrrp
:
   VRRP groupnum = dec
   (
      if_no_vrrp_preempt
   )
;

if_no_vrrp_preempt
:
   PREEMPT NEWLINE
;



if_null_block
:
   (
      ACTIVE
      | AFFINITY
      | ANTENNA
      | ARP
      | ASYNC
      | ATM
      | AUTHENTICATION
      | AUTO
      | AUTOROUTE
      | BEACON
      | BGP_POLICY
      | BRIDGE_GROUP
      | CABLE
      | CABLELENGTH
      | CARRIER_DELAY
      | CDP
      | CHANNEL
      | CHANNEL_PROTOCOL
      | CLASS
      | CLNS
      | CLOCK
      | COUNTER
      | CRC
      | DAMPENING
      | DCB
      | DCB_POLICY
      | DELAY
      | DESTINATION
      | DIALER
      | DIALER_GROUP
      | DFS
      | DOWNSTREAM
      | DSL
      | DSU BANDWIDTH
      | DUPLEX
      | ENABLE
      | ENCRYPTION
      | ETHERNET
      | EXIT
      | FAIR_QUEUE
      | FAST_REROUTE
      | FLOW
      | FORWARDER
      | FRAME_RELAY
      | FRAMING
      | FULL_DUPLEX
      | GIG_DEFAULT
      | GLBP
      | GROUP_RANGE
      | H323_GATEWAY
      | HALF_DUPLEX
      | HARDWARE
      | HISTORY
      | HOLD_QUEUE
      | IGNORE
      | INGRESS
      | ISDN
      | KEEPALIVE
      | L2_FILTER
      | L2PROTOCOL_TUNNEL
      | L2TRANSPORT
      | LANE
      | LAPB
      | LINK
      | LINK_FAULT_SIGNALING
      | LOAD_BALANCING
      | LOOPBACK
      | LRE
      | MACRO
      | MANAGEMENT
      | MANAGEMENT_ONLY
      | MAP_GROUP
      | MDIX
      | MEDIA_TYPE
      | MEDIUM
      | MEMBER
      | MINIMUM_LINKS
      | MLS
      | MOBILITY
      | MOP
      | NAME
      | NEGOTIATE
      | NEGOTIATION
      | NMSP
      |
      (
         NTP
         (
            BROADCAST
            | DISABLE
            | MULTICAST
         )
      )
      | NV
      | OPENFLOW
      | OPTICAL_MONITOR
      | OSPFV3
      | PACKET
      | PATH_OPTION
      | PEAKDETECT
      | PEER
      | PFC PRIORITY
      | PHYSICAL_LAYER
      | PLATFORM
      | PORT_CHANNEL
      | PORT_CHANNEL_PROTOCOL
      | PORT_NAME
      | PORT_TYPE
      | PORTMODE
      | POS
      | POWER
      | POWER_LEVEL
      | PPP
      | PREEMPT
      | PRIORITY_QUEUE
      | PVC
      | QUEUE_SET
      | RANDOM_DETECT
      | RATE_LIMIT
      | RATE_MODE
      | RCV_QUEUE
      | REDIRECTS
      | REMOTE
      | ROUTE_CACHE
      | ROUTE_ONLY
      | SCRAMBLE
      | SERIAL
      | SERVICE_MODULE
      | SIGNALLED_BANDWIDTH
      | SIGNALLED_NAME
      | SONET
      | SOURCE
      | SPEED_DUPLEX
      | SRR_QUEUE
      | SSID
      | STACK_MIB
      | STATION_ROLE
      | STBC
      | TAG_SWITCHING
      | TAGGED
      | TAP
      | TCAM
      | TRANSCEIVER
      | TRANSPORT_MODE
      | TRUST
      | TUNABLE_OPTIC
      | UC_TX_QUEUE
      | UDLD
      | UNTAGGED
      | VLT_PEER_LAG
      | VMTRACER
      | VPC
      | VTP
      | WEIGHTING
      | WRR_QUEUE
      | X25
      | XCONNECT
   ) ~NEWLINE* NEWLINE if_null_inner*
;

if_null_inner
:
   NO?
   (
      ADDRESS
      | BACKUP
      | BRIDGE_DOMAIN
      | DIALER
      | ENCAPSULATION
      | L2PROTOCOL
      | MODE
      | PRIORITY
      | PROPAGATE
      | PROTOCOL
      | RECEIVE
      | REMOTE_PORTS
      | REWRITE
      | SATELLITE_FABRIC_LINK
      | TRANSMIT
      | VIRTUAL_ADDRESS
   ) ~NEWLINE* NEWLINE  // do not change to null_rest_of_line
;

if_null_single
:
  (
    BCMC_OPTIMIZATION
    | DOT1X
    | JUMBO
    | LINKDEBOUNCE
    | MAB
    | REDUNDANCY
    | SUPPRESS_ARP
    | TRIMODE
    | TRUSTED
  ) ~NEWLINE* NEWLINE // do not change to null_rest_of_line

;

if_phy
:
  PHY MEDIA null_rest_of_line
;

if_pim
:
  PIM
  if_pim_null
;

if_pim_null
:
  null_rest_of_line
;

if_priority_flow_control
:
  PRIORITY_FLOW_CONTROL
  if_priority_flow_control_null
;

if_priority_flow_control_null
:
  null_rest_of_line
;

if_private_vlan
:
   PRIVATE_VLAN MAPPING (ADD | REMOVE)? null_rest_of_line
;

if_ptp_eos
:
  PTP
  if_ptp_null_eos
;

if_ptp_null_eos
:
  null_rest_of_line
;

if_qos
:
  QOS
  if_qos_null
;

if_qos_null
:
  null_rest_of_line
;

if_queue_monitor_eos
:
  QUEUE_MONITOR
  if_queue_monitor_null_eos
;

if_queue_monitor_null_eos
:
  null_rest_of_line
;

if_routing_dynamic
:
   ROUTING DYNAMIC NEWLINE
;

if_service_instance
:
   SERVICE INSTANCE id = dec ETHERNET NEWLINE
   if_si_inner*
;

if_si_inner
:
    if_si_bridge_domain
    | if_si_encapsulation
    | if_si_l2protocol
    | if_si_no
    | if_si_rewrite
    | if_si_service_policy
;

if_si_bridge_domain
:
    BRIDGE_DOMAIN id = dec SPLIT_HORIZON? NEWLINE
;

if_si_encapsulation
:
    ENCAPSULATION null_rest_of_line
;

if_si_l2protocol
:
    L2PROTOCOL TUNNEL? (DROP | FORWARD | PEER)? (CDP | DOT1X | DTP | LACP | PAGP | STP | VTP)? NEWLINE
;

if_si_no
:
  NO
  (
    if_si_no_bridge_domain
    | if_si_no_encapsulation
  )
;

if_si_no_bridge_domain
:
    BRIDGE_DOMAIN id = dec NEWLINE
;

if_si_no_encapsulation
:
    ENCAPSULATION null_rest_of_line
;

if_si_rewrite
:
    REWRITE null_rest_of_line
;

if_si_service_policy
:
    SERVICE_POLICY (INPUT | OUTPUT) policy_map = variable NEWLINE
;

if_snmp
:
  SNMP
  if_snmp_null
;

if_snmp_null
:
  null_rest_of_line
;

if_spanning_tree
:
   SPANNING_TREE
   (
      if_st_null
      | if_st_portfast
      | NEWLINE
   )
;

if_speed_eos
:
   SPEED
   (
     if_speed_auto_eos
     | if_speed_bw_eos
     | if_speed_forced_eos
   )
;

if_speed_auto_eos
:
  AUTO eos_bandwidth_specifier? NEWLINE
;

if_speed_bw_eos
:
  eos_bandwidth_specifier NEWLINE
;

if_speed_forced_eos
:
  FORCED eos_bandwidth_specifier NEWLINE
;

if_st_null
:
   (
      BPDUFILTER
      | BPDUGUARD
      | COST
      | GUARD
      | LINK_TYPE
      | MST
      | PORT
      | PORT_PRIORITY
      | PRIORITY
      | PROTECT
      | RSTP
      | VLAN
   ) null_rest_of_line
;

if_st_portfast
:
   PORTFAST
   (
      auto = AUTO
      | disable = DISABLE
      | edge = EDGE
      | network = NETWORK
      | trunk = TRUNK
   )* NEWLINE
;

if_service_policy
:
   SERVICE_POLICY
   (
      TYPE (
         CONTROL SUBSCRIBER
         | PBR
         | PDP
         | QOS
         | QUEUING
      )
   )?
   (INPUT | OUTPUT)?
   policy_map = variable NEWLINE
;

if_sflow
:
  SFLOW
  if_sflow_enable
;

if_sflow_enable
:
  ENABLE NEWLINE
;

if_shape
:
  SHAPE
  if_shape_null
;

if_shape_null
:
  null_rest_of_line
;

if_shutdown_eos
:
  SHUTDOWN NEWLINE
;

if_switchport
:
  SWITCHPORT
  (
    if_switchport_switchport
    | if_switchport_access
    // | if_switchport_backup_eos
    // | if_switchport_backup_link_eos (not on 4.21, on 4.23)
    | if_switchport_mode
    // | if_switchport_port_security_eos
    | if_switchport_trunk_eos
  )
;

// "switchport"
if_switchport_switchport
:
  NEWLINE
;

if_switchport_access
:
   ACCESS VLAN
   (
      vlan = dec
      | DYNAMIC
   ) NEWLINE
;

if_switchport_mode
:
   MODE
   (
      ACCESS
      | DOT1Q_TUNNEL
      |
      (
         DYNAMIC
         (
            AUTO
            | DESIRABLE
         )
      )
      | FEX_FABRIC
      | if_switchport_mode_monitor
      | PRIVATE_VLAN
      | TAP
      | TOOL
      | TRUNK
   ) NEWLINE
;

if_switchport_mode_monitor
:
   MONITOR BUFFER_LIMIT limit=dec (BYTES | KBYTES | MBYTES | PACKETS)
;

if_switchport_trunk_eos
:
  TRUNK (
    if_switchport_trunk_allowed_eos
    | if_switchport_trunk_group_eos
    | if_switchport_trunk_native_eos
  )
;

if_switchport_trunk_allowed_eos
:
   ALLOWED VLAN
   (
      ALL
      | NONE
      | (ADD | REMOVE | EXCEPT)? r = range
   ) NEWLINE
;

if_switchport_trunk_group_eos
:
   GROUP name = variable NEWLINE
;

if_switchport_trunk_native_eos
:
   NATIVE VLAN vlan = dec NEWLINE
;

if_traffic_loopback_eos
:
  TRAFFIC_LOOPBACK
  if_traffic_loopback_null_eos
;

if_traffic_loopback_null_eos
:
  null_rest_of_line
;

if_tunnel
:
   TUNNEL
   (
       iftunnel_bandwidth
       | iftunnel_destination
       | iftunnel_key
       | iftunnel_mode
       | iftunnel_path_mtu_discovery
       | iftunnel_protection
       | iftunnel_source
   )
;

if_tx_queue
:
  TX_QUEUE num = dec NEWLINE
  (
    if_txq_no
    | if_txq_null
  )*
;

if_txq_no
:
  NO if_txq_no_null
;

if_txq_no_null
:
  (
    BANDWIDTH
    | PRIORITY
    | RANDOM_DETECT
    | SHAPE
  ) null_rest_of_line
;

if_txq_null
:
  (
    BANDWIDTH
    | PRIORITY
    | RANDOM_DETECT
    | SHAPE
  ) null_rest_of_line
;

if_vrf
:
   VRF
   (
     if_vrf_forwarding
     | if_vrf_name
   )
;

if_vrf_forwarding
:
  // EOS < 4.23 - this command is deprecated by 'vrf [VRF_ID]'
  FORWARDING if_vrf_name
;

if_vrf_name
:
  // EOS >= 4.23
  name = vrf_name NEWLINE
;

if_vrrp
:
   VRRP groupnum = dec
   (
      ifvrrp_advertisement
      | ifvrrp_authentication
      | ifvrrp_description
      | ifvrrp_ip
      | ifvrrp_ipv4
      | ifvrrp_ipv6
      | ifvrrp_preempt
      | ifvrrp_priority
      | ifvrrp_priority_level
   )
;

ifigmp_access_group
:
   ACCESS_GROUP name = variable NEWLINE
;

ifigmp_host_proxy
:
   HOST_PROXY (
       ifigmphp_access_list
       | ifigmphp_null
   )
;

ifigmphp_access_list
:
   ACCESS_LIST name = variable NEWLINE
;

ifigmphp_null
:
   (
      EXCLUDE
      | INCLUDE
      | IP_ADDRESS
      | REPORT_INTERVAL
      | VERSION
   ) null_rest_of_line

;

ifigmp_null
:
   (
      GROUP_TIMEOUT
      | JOIN_GROUP
      | LAST_MEMBER_QUERY_COUNT
      | LAST_MEMBER_QUERY_INTERVAL
      | LAST_MEMBER_QUERY_RESPONSE_TIME
      | MULTICAST_STATIC_ONLY
      | QUERY_INTERVAL
      | QUERY_MAX_RESPONSE_TIME
      | QUERY_TIMEOUT
      | ROBUSTNESS_VARIABLE
      | ROUTER_ALERT
      | SNOOPING
      | STARTUP_QUERY_COUNT
      | STARTUP_QUERY_INTERVAL
      | VERSION
   ) null_rest_of_line
;

ifigmp_static_group
:
   STATIC_GROUP
   (
      ifigmpsg_acl
      | ifigmpsg_null
   )
;

ifigmpsg_acl
:
   ACL name = variable NEWLINE
;

ifigmpsg_null
:
   (
      IP_ADDRESS
      | RANGE
   ) null_rest_of_line
;

iftunnel_bandwidth
:
   BANDWIDTH 
   (
      RECEIVE
      | TRANSMIT
   ) dec NEWLINE
;


iftunnel_destination
:
   DESTINATION IP_ADDRESS NEWLINE
;

iftunnel_key
:
   KEY keynum = dec NEWLINE
;

iftunnel_mode
:
   MODE
   (
     | gre_id = GRE id = dec // aruba, tunnel ID
     | gre_ipv4 = GRE IPV4 // ios-xr
     | gre_multipoint = GRE MULTIPOINT
     | ipsec_ipv4 = IPSEC IPV4
     | ipv6ip = IPV6IP
   )
   NEWLINE
;

iftunnel_path_mtu_discovery
:
   PATH_MTU_DISCOVERY NEWLINE
;

iftunnel_protection
:
   PROTECTION IPSEC PROFILE name = variable NEWLINE
;

iftunnel_source
:
   SOURCE 
   (
     DYNAMIC
     | IP_ADDRESS
     | iname = interface_name_unstructured
   ) NEWLINE
;

ifvrrp_advertisement
:
   ADVERTISEMENT INTERVAL secs = dec NEWLINE
;

ifvrrp_authentication
:
   AUTHENTICATION TEXT text = variable_permissive NEWLINE
;

ifvrrp_description
:
   description_line
;

ifvrrp_ip
:
   IP ip = IP_ADDRESS SECONDARY? NEWLINE
;

ifvrrp_ipv4
:
   IPV4 ip = IP_ADDRESS NEWLINE
;

ifvrrp_ipv6
:
   IPV6 ip = IPV6_ADDRESS NEWLINE
;

ifvrrp_preempt
:
   PREEMPT DELAY
   (
      MINIMUM
      | RELOAD
   ) dec NEWLINE
;

ifvrrp_priority
:
   PRIORITY priority = dec NEWLINE
;

ifvrrp_priority_level
:
   PRIORITY_LEVEL priority = dec NEWLINE
;

s_eos_vxlan_interface
:
   INTERFACE iname = eos_vxlan_interface_name NEWLINE
   eos_vxlan_if_inner*
;

s_interface
:
   INTERFACE PRECONFIGURE? iname = interface_name
   (
      L2TRANSPORT
      | MULTIPOINT
      | POINT_TO_POINT
   )?
   NEWLINE
   if_inner*
;

if_inner
:
   if_autostate
   | if_bandwidth
   | if_bfd
   | if_channel_group_eos
   | if_crypto
   | if_default_eos
   | if_default_gw
   | if_description
   | if_eos_mlag
   | if_encapsulation_eos
   | if_evpn_eos
   | if_igmp
   | if_ip
   | if_ipv6
   | if_isis
   | if_l2_protocol
   | if_lacp
   | if_lldp
   | if_load_interval
   | if_logging
   | if_mac
   | if_mac_address
   | if_member_interface
   | if_mfib
   | if_mld
   | if_mpls
   | if_mtu
   | if_no
   | if_phy
   | if_pim
   | if_priority_flow_control
   | if_private_vlan
   | if_ptp_eos
   | if_qos
   | if_queue_monitor_eos
   | if_routing_dynamic
   | if_service_instance
   | if_service_policy
   | if_sflow
   | if_shape
   | if_shutdown_eos
   | if_snmp
   | if_spanning_tree
   | if_speed_eos
   | if_switchport
   | if_traffic_loopback_eos
   | if_tunnel
   | if_tx_queue
   | if_vrf
   | if_vrrp
   // do not rearrange items below
 
   | if_null_single
   | if_null_block
;